package com.commonsware.empub;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;

public class DownloadCheckTask extends AsyncTask<Void, Void, Boolean> {
  private static final String UPDATE_URL=
      "http://misc.commonsware.com/android-update.json";
  static final String UPDATE_FILENAME="book.apk";
  private PackageManager pkgMgr=null;
  private DownloadManager downMgr=null;
  private String packageName=null;
  private String title=null;
  private String desc=null;
  private ComponentName cn=null;
  private Exception e=null;
  private OnCompletionListener listener=null;

  DownloadCheckTask(Context ctxt, OnCompletionListener listener) {
    pkgMgr=ctxt.getPackageManager();
    downMgr=
        (DownloadManager)ctxt.getSystemService(Context.DOWNLOAD_SERVICE);
    packageName=ctxt.getPackageName();
    title=ctxt.getString(R.string.empub_update_title);
    desc=ctxt.getString(R.string.empub_update_description);
    cn=new ComponentName(ctxt, DownloadCompleteReceiver.class);
    this.listener=listener;
  }

  @Override
  protected Boolean doInBackground(Void... unused) {
    BufferedReader reader=null;
    boolean isDownloading=false;

    try {
      URL url=new URL(UPDATE_URL);
      HttpURLConnection c=(HttpURLConnection)url.openConnection();

      c.setRequestMethod("GET");
      c.setReadTimeout(15000);
      c.connect();

      reader=
          new BufferedReader(new InputStreamReader(c.getInputStream()));

      StringBuilder buf=new StringBuilder();
      String line=null;

      while ((line=reader.readLine()) != null) {
        buf.append(line + "\n");
      }

      isDownloading=checkDownloadInfo(buf.toString());
    }
    catch (Exception e) {
      this.e=e;
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          this.e=e;
        }
      }
    }

    return(isDownloading);
  }

  @TargetApi(9)
  private boolean checkDownloadInfo(String raw) throws JSONException {
    JSONObject json=new JSONObject(raw);
    int versionCode=json.getInt("versionCode");

    try {
      if (versionCode > pkgMgr.getPackageInfo(packageName, 0).versionCode) {
        String url=json.getString("url");
        DownloadManager.Request req=
            new DownloadManager.Request(Uri.parse(url));

        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                   .mkdirs();

        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                                       | DownloadManager.Request.NETWORK_MOBILE)
           .setAllowedOverRoaming(false)
           .setTitle(title)
           .setDescription(desc)
           .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                              UPDATE_FILENAME);

        pkgMgr.setComponentEnabledSetting(cn,
                                          PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                          PackageManager.DONT_KILL_APP);

        downMgr.enqueue(req);
        
        return(true);
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Exception in checking for update", e);
    }
    
    return(false);
  }

  @Override
  public void onPostExecute(Boolean isDownloading) {
    if (e == null) {
      if (listener!=null) {
        listener.onSuccess(isDownloading);
      }
    }
    else {
      Log.e(getClass().getSimpleName(),
            "Exception retrieving update info", e);
      
      if (listener!=null) {
        listener.onError(e);
      }
    }
  }
  
  interface OnCompletionListener {
    void onError(Exception e);
    void onSuccess(boolean isDownloading);
  }
}

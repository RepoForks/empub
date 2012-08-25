package com.commonsware.empub;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import java.io.File;

public class DownloadCompleteReceiver extends BroadcastReceiver {
  private static int NOTIFY_ID=1337;

  @TargetApi(11)
  @Override
  public void onReceive(final Context ctxt, Intent unused) {
    File update=
        new File(
                 Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                 DownloadCheckTask.UPDATE_FILENAME);

    if (update.exists()) {
      Intent i;

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        i=new Intent(Intent.ACTION_INSTALL_PACKAGE);
        i.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
      }
      else {
        i=new Intent(Intent.ACTION_VIEW);
      }

      i.setDataAndType(Uri.fromFile(update),
                       "application/vnd.android.package-archive");
      i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      NotificationCompat.Builder b=new NotificationCompat.Builder(ctxt);

      b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
       .setWhen(System.currentTimeMillis());

      b.setContentTitle(ctxt.getString(R.string.empub_download_complete))
       .setContentText(ctxt.getString(R.string.empub_download_install))
       .setSmallIcon(android.R.drawable.stat_sys_download_done)
       .setTicker(ctxt.getString(R.string.empub_download_complete));

      b.setContentIntent(PendingIntent.getActivity(ctxt, 0, i, 0));
      NotificationManager mgr=
          (NotificationManager)ctxt.getSystemService(Context.NOTIFICATION_SERVICE);

      mgr.notify(NOTIFY_ID, b.getNotification());

      if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
        final PendingResult pr=goAsync();
        
        new Thread() {
          public void run() {
            makeMeGoByeBye(ctxt);
            pr.finish();
          }
        }.start();
      }
      else {
        makeMeGoByeBye(ctxt);
      }
    }
  }

  public void makeMeGoByeBye(Context ctxt) {
    ctxt.getPackageManager()
        .setComponentEnabledSetting(new ComponentName(ctxt, getClass()),
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP);

  }
}

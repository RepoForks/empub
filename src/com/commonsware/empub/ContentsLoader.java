package com.commonsware.empub;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import org.json.JSONObject;

public class ContentsLoader extends AsyncTaskLoader<BookContents> {
  private BookContents contents=null;

  public ContentsLoader(Context context) {
    super(context);
  }

  @Override
  public BookContents loadInBackground() {
    try {
      StringBuilder buf=new StringBuilder();
      InputStream json=
          getContext().getResources().getAssets().open("contents.json");
      BufferedReader in=new BufferedReader(new InputStreamReader(json));
      String str;

      while ((str=in.readLine()) != null) {
        buf.append(str);
      }

      in.close();

      return(new BookContents(new JSONObject(buf.toString())));
    }
    catch (Exception e) {
      String msg=getContext().getString(R.string.empub_exception_json);

      throw new RuntimeException(msg, e);
    }
  }

  @Override
  protected void onStartLoading() {
    if (contents != null) {
      deliverResult(contents);
    }

    if (takeContentChanged() || contents == null) {
      forceLoad();
    }
  }
}

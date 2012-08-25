/***
  Copyright (c) 2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
 */

package com.commonsware.empub;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Stack;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class EmPubActivity extends SherlockFragmentActivity implements
    NavListener, SharedPreferences.OnSharedPreferenceChangeListener,
    DownloadCheckTask.OnCompletionListener {
  public static final String TAG="EmPub";
  public static final String CONTENT_PREFIX="file:///android_asset/";
  public static final String EXTRA_FILE="file";
  private static final String PREF_SAVE_LAST_POSITION=
      "saveLastPosition";
  private static final String PREF_LAST_POSITION="lastPosition";
  private static final String PREF_KEEP_SCREEN_ON="keepScreenOn";
  private static final String PREF_INTERNAL_BACK_STACK=
      "internalBackStack";
  static final String PREF_ZOOM="zoom";
  private static final String MODEL="model";
  private ViewPager pager=null;
  private ContentsAdapter adapter=null;
  private HashMap<String, String> anchors=new HashMap<String, String>();
  private ModelFragment model=null;
  private Stack<Integer> backStack=new Stack<Integer>();
  private boolean internalBackStack=false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
        && BuildConfig.DEBUG) {
      enableStrictMode();
    }

    if (savedInstanceState == null) {
      model=new ModelFragment();
      getSupportFragmentManager().beginTransaction().add(model, MODEL)
                                 .commit();
    }
    else {
      model=
          (ModelFragment)getSupportFragmentManager().findFragmentByTag(MODEL);
    }

    setContentView(R.layout.empub_main);
    pager=(ViewPager)findViewById(R.id.pager);
    pager.setOnPageChangeListener(pageListener);
  }

  @TargetApi(9)
  @Override
  public void onPause() {
    if (getPrefs() != null) {
      int position=pager.getCurrentItem();
      final SharedPreferences.Editor editor=
          getPrefs().edit().putInt(PREF_LAST_POSITION, position);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
        editor.apply();
      }
      else {
        new Thread() {
          public void run() {
            editor.commit();
          }
        }.start();
      }
    }

    super.onPause();
  }

  @Override
  public void onDestroy() {
    if (getPrefs() != null) {
      getPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    // if the user has been forward-navigating, our current
    // page will be on top of the stack, so we need to move
    // past that to whatever they were on before that

    int position=pager.getCurrentItem();

    while (position == pager.getCurrentItem() && backStack.size() > 0) {
      position=backStack.pop();
    }

    if (position != pager.getCurrentItem()) {
      pager.setOnPageChangeListener(null);
      pager.setCurrentItem(position);
      pager.setOnPageChangeListener(pageListener);
    }
    else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    new MenuInflater(this).inflate(R.menu.empub_options, menu);

    // if (Build.VERSION.SDK_INT >=
    // Build.VERSION_CODES.GINGERBREAD) {
    // menu.findItem(R.id.empub_update).setVisible(true);
    // }

    return(super.onCreateOptionsMenu(menu));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      pager.setCurrentItem(1, false);

      return(true);
    }
    // else if (item.getItemId() == R.id.empub_update) {
    // new DownloadCheckTask(this, this).execute();
    //
    // return(true);
    // }
    else if (item.getItemId() == R.id.empub_settings) {
      startActivity(new Intent(this, Preferences.class));

      return(true);
    }
    else if (item.getItemId() == R.id.empub_about) {
      Intent i=new Intent(this, SimpleContentActivity.class);

      i.putExtra(SimpleContentActivity.EXTRA_FILE,
                 "file:///android_asset/misc/about.html");

      startActivity(i);

      return(true);
    }
    else if (item.getItemId() == R.id.empub_help) {
      Intent i=new Intent(this, SimpleContentActivity.class);

      i.putExtra(SimpleContentActivity.EXTRA_FILE,
                 "file:///android_asset/misc/help.html");

      startActivity(i);

      return(true);
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  public void onInternalLinkClicked(String url) {
    String[] pieces=url.substring(CONTENT_PREFIX.length()).split("#");
    int position=getContents().getPositionForFile(pieces[0]);

    if (pieces.length == 2) {
      anchors.put(pieces[0].substring(5), pieces[1]);

      ChapterFragment frag=
          (ChapterFragment)adapter.getActiveFragment(pager,
                                                     position + 2);

      if (frag != null) {
        frag.jumpTo(pieces[1]);
      }
    }

    pager.setCurrentItem(position + 2, false);
  }

  @Override
  public void onExternalLinkClicked(String url) {
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    if (pager != null && PREF_KEEP_SCREEN_ON.equals(key)) {
      pager.setKeepScreenOn(getPrefs().getBoolean(key, false));
    }
    else if (PREF_INTERNAL_BACK_STACK.equals(key)) {
      internalBackStack=getPrefs().getBoolean(key, false);
    }
  }

  @Override
  public void onError(Exception e) {
    Toast.makeText(this,
                   getString(R.string.error_trying_update)
                       + e.getMessage(), Toast.LENGTH_LONG).show();
  }

  @Override
  public void onSuccess(boolean isDownloading) {
    if (!isDownloading) {
      Toast.makeText(this, R.string.no_update_available,
                     Toast.LENGTH_LONG).show();
    }
  }

  String popAnchor(String path) {
    return(anchors.remove(path));
  }

  int getZoom() {
    if (getPrefs() == null) {
      return(100);
    }

    return(getPrefs().getInt(EmPubActivity.PREF_ZOOM, 100));
  }

  void setupPager(SharedPreferences prefs, BookContents contents) {
    prefs.registerOnSharedPreferenceChangeListener(this);
    adapter=new ContentsAdapter(this, contents);
    pager.setAdapter(adapter);
    pager.setKeepScreenOn(prefs.getBoolean(PREF_KEEP_SCREEN_ON, false));

    if (prefs.getBoolean(PREF_SAVE_LAST_POSITION, true)) {
      pager.setCurrentItem(prefs.getInt(PREF_LAST_POSITION, 0));
    }

    findViewById(R.id.progressBar).setVisibility(View.GONE);
    findViewById(R.id.pager).setVisibility(View.VISIBLE);

    internalBackStack=
        getPrefs().getBoolean(PREF_INTERNAL_BACK_STACK, false);
  }

  private SharedPreferences getPrefs() {
    return(model.getPrefs());
  }

  private BookContents getContents() {
    return(model.getContents());
  }

  @TargetApi(9)
  private void enableStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
                                                                    .penaltyLog()
                                                                    .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                                                            .penaltyLog()
                                                            .build());
  }

  private ViewPager.SimpleOnPageChangeListener pageListener=
      new ViewPager.SimpleOnPageChangeListener() {
        public void onPageSelected(int position) {
          if (internalBackStack && position >= 2) {
            backStack.push(position);
          }
        }
      };
}
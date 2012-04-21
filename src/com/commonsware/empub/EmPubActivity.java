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

import java.util.HashMap;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.loaderex.acl.SharedPreferencesLoader;

public class EmPubActivity extends SherlockFragmentActivity implements
    NavListener {
  public static final String TAG="EmPub";
  public static final String CONTENT_PREFIX="file:///android_asset/";
  public static final String EXTRA_FILE="file";
  private static final String PREF_SAVE_LAST_POSITION=
      "saveLastPosition";
  private static final String PREF_LAST_POSITION="lastPosition";
  static final String PREF_ZOOM="zoom";
  private static final String MODEL="model";
  private ViewPager pager=null;
  private BookContents contents=null;
  private ContentsAdapter adapter=null;
  private SharedPreferences prefs=null;
  private HashMap<String, String> anchors=new HashMap<String, String>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
        && BuildConfig.DEBUG) {
      enableStrictMode();
    }

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
                                 .add(new ModelFragment(), MODEL)
                                 .commit();
    }

    setContentView(R.layout.empub_main);
    pager=(ViewPager)findViewById(R.id.pager);
  }

  @Override
  public void onPause() {
    int position=pager.getCurrentItem();

    SharedPreferencesLoader.persist(prefs.edit()
                                         .putInt(PREF_LAST_POSITION,
                                                 position));

    super.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    new MenuInflater(this).inflate(R.menu.empub_options, menu);

    return(super.onCreateOptionsMenu(menu));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      pager.setCurrentItem(1, false);
    }
    else if (item.getItemId() == R.id.empub_settings) {
      startActivity(new Intent(this, Preferences.class));
    }
    else if (item.getItemId() == R.id.empub_about) {
      Intent i=new Intent(this, SimpleContentActivity.class);

      i.putExtra(SimpleContentActivity.KEY_FILE,
                 "file:///android_asset/misc/about.html");

      startActivity(i);
    }
    else if (item.getItemId() == R.id.empub_help) {
      Intent i=new Intent(this, SimpleContentActivity.class);

      i.putExtra(SimpleContentActivity.KEY_FILE,
                 "file:///android_asset/misc/help.html");

      startActivity(i);
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  public void onInternalLinkClicked(String url) {
    String[] pieces=url.substring(CONTENT_PREFIX.length()).split("#");
    int position=contents.getPositionForFile(pieces[0]);

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

  String popAnchor(String path) {
    return(anchors.remove(path));
  }

  int getZoom() {
    return(prefs.getInt(EmPubActivity.PREF_ZOOM, 100));
  }

  void setupPager(SharedPreferences prefs, BookContents contents) {
    this.prefs=prefs;
    this.contents=contents;

    adapter=new ContentsAdapter(this, contents);
    pager.setAdapter(adapter);

    if (prefs.getBoolean(PREF_SAVE_LAST_POSITION, true)) {
      pager.setCurrentItem(prefs.getInt(PREF_LAST_POSITION, 0));
    }

    findViewById(R.id.progressBar).setVisibility(View.GONE);
    findViewById(R.id.pager).setVisibility(View.VISIBLE);
  }

  private void enableStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
                                                                    .penaltyLog()
                                                                    .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                                                            .penaltyLog()
                                                            .build());
  }
}
package com.commonsware.empub;

import java.util.HashMap;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.commonsware.cwac.loaderex.acl.SharedPreferencesLoader;

public class EmPubActivity extends SherlockFragmentActivity implements
    LoaderManager.LoaderCallbacks<BookContents>, NavListener {
  public static final String TAG="EmPub";
  public static final String CONTENT_PREFIX="file:///android_asset/";
  public static final String EXTRA_FILE="file";
  private static final int LOADER_CONTENTS=0;
  private static final int LOADER_PREFS=1;
  private ViewPager pager=null;
  private BookContents contents=null;
  private ContentsAdapter adapter=null;
  private SharedPreferences prefs=null;
  private HashMap<String, String> anchors=new HashMap<String, String>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
      ApplicationInfo appInfo=getApplicationInfo();
      int appFlags=appInfo.flags;

      if ((appFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
        enableStrictMode();
      }
    }

    setContentView(R.layout.empub_main);
    pager=(ViewPager)findViewById(R.id.pager);

    getSupportLoaderManager().initLoader(LOADER_PREFS, null,
                                         new PrefsLoaderCallbacks());
    getSupportLoaderManager().initLoader(LOADER_CONTENTS, null, this);
  }

  @Override
  public Loader<BookContents> onCreateLoader(int id, Bundle args) {
    return(new ContentsLoader(this));
  }

  @Override
  public void onLoadFinished(Loader<BookContents> loader,
                             BookContents contents) {
    if (this.contents == null) {
      this.contents=contents;
      setupPager();
    }
  }

  @Override
  public void onLoaderReset(Loader<BookContents> loader) {
    // unused
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      pager.setCurrentItem(0, false);
    }

    return(super.onOptionsItemSelected(item));
  }

  @Override
  public void onInternalLinkClicked(String url) {
    String[] pieces=url.substring(CONTENT_PREFIX.length()).split("#");
    int position=contents.getPositionForFile(pieces[0]);

    if (pieces.length == 2) {
      anchors.put(pieces[0], pieces[1]);

      ChapterFragment frag=(ChapterFragment)adapter.getActiveFragment(pager, position+1);
      
      if (frag!=null) {
        frag.jumpTo(pieces[1]);
      }
    }

    pager.setCurrentItem(position + 1, false);
  }

  @Override
  public void onExternalLinkClicked(String url) {
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
  }

  String popAnchor(String path) {
    return(anchors.remove(path));
  }
  
  SharedPreferences getPreferences() {
    return(prefs);
  }

  private void setupPager() {
    if (contents != null && prefs != null) {
      adapter=new ContentsAdapter(this, contents, this);
      pager.setAdapter(adapter);
      findViewById(R.id.progressBar).setVisibility(View.GONE);
      findViewById(R.id.pager).setVisibility(View.VISIBLE);
    }
  }

  private void enableStrictMode() {
    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
                                                                    .penaltyLog()
                                                                    .build());
    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                                                            .penaltyLog()
                                                            .build());
  }

  private static class ContentsAdapter extends FragmentPagerAdapter {
    BookContents contents=null;
    NavListener nav=null;
    FragmentActivity ctxt=null;

    public ContentsAdapter(FragmentActivity ctxt,
                           BookContents contents, NavListener nav) {
      super(ctxt.getSupportFragmentManager());
      this.ctxt=ctxt;
      this.contents=contents;
      this.nav=nav;
    }

    @Override
    public int getCount() {
      return(contents.getChapterCount() + 1);
    }

    @Override
    public Fragment getItem(int position) {
      ChapterFragment frag=null;

      if (position == 0) {
        frag=ChapterFragment.newInstance(contents.getTOCFile());
      }
      else {
        String path=contents.getChapterFile(position - 1);

        frag=ChapterFragment.newInstance(path);
      }

      frag.setNavListener(nav);

      return(frag);
    }

    @Override
    public String getPageTitle(int position) {
      if (position == 0) {
        return(ctxt.getString(R.string.empub_toc));
      }

      return(contents.getChapterTitle(position - 1));
    }
    
    // nasty nasty hack
    // http://stackoverflow.com/a/9293207/115145

    Fragment getActiveFragment(ViewPager container, int position) {
      String name=makeFragmentName(container.getId(), position);
      return ctxt.getSupportFragmentManager().findFragmentByTag(name);
    }

    private static String makeFragmentName(int viewId, int index) {
      return "android:switcher:" + viewId + ":" + index;
    }
  }

  private class PrefsLoaderCallbacks implements
      LoaderManager.LoaderCallbacks<SharedPreferences> {

    @Override
    public Loader<SharedPreferences> onCreateLoader(int id, Bundle arg1) {
      return(new SharedPreferencesLoader(EmPubActivity.this));
    }

    @Override
    public void onLoadFinished(Loader<SharedPreferences> loader,
                               SharedPreferences prefs) {
      EmPubActivity.this.prefs=prefs;
      setupPager();
    }

    @Override
    public void onLoaderReset(Loader<SharedPreferences> loader) {
      // unused
    }
  }
}
package com.commonsware.empub;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.commonsware.cwac.loaderex.acl.SharedPreferencesLoader;

public class ChapterFragment extends WebViewFragment implements
    SeekBar.OnSeekBarChangeListener,
    SharedPreferences.OnSharedPreferenceChangeListener {
  private static int cacheZoomLevel=100;
  private static final String KEY_FILE="file";
  private static final String PREF_ZOOM="zoom";
  private ChapterClient client=null;
  private NavListener nav=null;
  private SeekBar zoom=null;
  private SharedPreferences prefs=null;

  static ChapterFragment newInstance(String file) {
    ChapterFragment f=new ChapterFragment();

    Bundle args=new Bundle();

    args.putString(KEY_FILE, file);
    f.setArguments(args);

    return(f);
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRetainInstance(true);
    setHasOptionsMenu(true);
    prefs=((EmPubActivity)getActivity()).getPreferences();
    this.client=new ChapterClient();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View result=
        super.onCreateView(inflater, container, savedInstanceState);

    setZoom();
    getWebView().setWebViewClient(client);
    
    // following required due to bug
    // http://code.google.com/p/android/issues/detail?id=3440
    getWebView().getSettings().setBuiltInZoomControls(false);
    getWebView().getSettings().setJavaScriptEnabled(true);

    prefs.registerOnSharedPreferenceChangeListener(this);
    cacheZoomLevel=prefs.getInt(PREF_ZOOM, 100);

    if (zoom != null) {
      updateZoom();
    }

    String page=
        "file:///android_asset/" + getArguments().getString(KEY_FILE);

    getWebView().loadUrl(page);
    
    final String anchor=((EmPubActivity)getActivity()).popAnchor(getArguments().getString(KEY_FILE));

    // would use onPageFinished(), but it is hopelessly buggy

    if (anchor!=null) {
      result.postDelayed(new Runnable() {
        public void run() {
          jumpTo(anchor);
        }
      }, 1000);
    }
    
    return(result);
  }

  @Override
  public void onResume() {
    super.onResume();

    setZoom();
  }

  @Override
  public void onDestroy() {
    prefs.unregisterOnSharedPreferenceChangeListener(this);

    super.onDestroy();
  }
  
  // needed due to Android Support bug
  // see http://stackoverflow.com/questions/8748064/starting-activity-from-fragment-causes-nullpointerexception
  
  @Override
  public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      setUserVisibleHint(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.empub_actions, menu);

    View action=menu.findItem(R.id.empub_zoom).getActionView();

    zoom=(SeekBar)action.findViewById(R.id.zoomBar);
    zoom.setOnSeekBarChangeListener(this);

    if (prefs != null && getWebView() != null) {
      updateZoom();
    }
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress,
                                boolean fromUser) {
    if (fromUser) {
      cacheZoomLevel=progress + 50;
      SharedPreferencesLoader.persist(prefs.edit()
                                           .putInt(PREF_ZOOM,
                                                   progress + 50));
      setZoom();
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {
    // unused
  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {
    // unused
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    if (getWebView() != null) {
      setZoom();
    }
  }

  void setNavListener(NavListener nav) {
    this.nav=nav;
  }
  
  void jumpTo(String anchor) {
    if (getWebView()!=null) {
      StringBuilder buf=new StringBuilder("javascript:location.href=\"#");
      
      buf.append(anchor);
      buf.append("\";");
      
      getWebView().loadUrl(buf.toString());
    }
  }

  private void updateZoom() {
    setZoom();
    zoom.setProgress(cacheZoomLevel - 50);
  }

  private void setZoom() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      getWebView().getSettings().setTextZoom(cacheZoomLevel);
    }
    else {
      if (cacheZoomLevel <= 25) {
        getWebView().getSettings()
                    .setTextSize(WebSettings.TextSize.SMALLEST);
      }
      else if (cacheZoomLevel <= 75) {
        getWebView().getSettings()
                    .setTextSize(WebSettings.TextSize.SMALLER);
      }
      else if (cacheZoomLevel <= 125) {
        getWebView().getSettings()
                    .setTextSize(WebSettings.TextSize.NORMAL);
      }
      else if (cacheZoomLevel <= 175) {
        getWebView().getSettings()
                    .setTextSize(WebSettings.TextSize.LARGER);
      }
      else {
        getWebView().getSettings()
                    .setTextSize(WebSettings.TextSize.LARGEST);
      }
    }
  }

  class ChapterClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (url.startsWith(EmPubActivity.CONTENT_PREFIX)) {
        nav.onInternalLinkClicked(url);
      }
      else {
        nav.onExternalLinkClicked(url);
      }

      return(true);
    }
  }
}

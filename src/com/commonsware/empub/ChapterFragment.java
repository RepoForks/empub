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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;

public class ChapterFragment extends AbstractContentFragment implements
    OnLongClickListener {
  private static final String KEY_FILE="file";
  private int zoomLevel=100;

  protected static ChapterFragment newInstance(String file) {
    ChapterFragment f=new ChapterFragment();

    Bundle args=new Bundle();

    args.putString(KEY_FILE, file);
    f.setArguments(args);

    return(f);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // http://code.google.com/p/android/issues/detail?id=3440
    getWebView().getSettings().setBuiltInZoomControls(false);
    getWebView().setWebViewClient(new ChapterClient());
    getWebView().setOnLongClickListener(this);
    updateZoom();

    final String anchor=
        ((EmPubActivity)getActivity()).popAnchor(getArguments().getString(KEY_FILE));

    // would use onPageFinished(), but it is hopelessly
    // buggy

    if (anchor != null) {
      getWebView().postDelayed(new Runnable() {
        public void run() {
          jumpTo(anchor);
        }
      }, 1000);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    setZoom(((EmPubActivity)getActivity()).getZoom());
  }

  @Override
  public boolean onLongClick(View v) {
    HitTestResult hit=getWebView().getHitTestResult();

    if (hit.getType() == HitTestResult.IMAGE_TYPE
        || hit.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
      Intent i=new Intent(getActivity(), ImageActivity.class);
      
      i.putExtra(ImageActivity.EXTRA_FILE, Uri.parse(hit.getExtra()).getLastPathSegment());
      startActivity(i);
    }

    return false;
  }

  @Override
  String getPage() {
    return("file:///android_asset/book/" + getArguments().getString(KEY_FILE));
  }

  protected void jumpTo(String anchor) {
    if (getWebView() != null) {
      StringBuilder buf=
          new StringBuilder("javascript:location.href=\"#");

      buf.append(anchor);
      buf.append("\";");

      getWebView().loadUrl(buf.toString());
    }
  }

  void setZoom(int zoomLevel) {
    this.zoomLevel=zoomLevel;

    if (getWebView() != null) {
      updateZoom();
    }
  }

  @TargetApi(14)
  private void updateZoom() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      getWebView().getSettings().setTextZoom(zoomLevel);
    }
    else {
      if (zoomLevel <= 25) {
        getWebView().getSettings()
                    .setTextSize(WebSettings.TextSize.SMALLEST);
      }
      else if (zoomLevel <= 75) {
        getWebView().getSettings()
                    .setTextSize(WebSettings.TextSize.SMALLER);
      }
      else if (zoomLevel <= 125) {
        getWebView().getSettings()
                    .setTextSize(WebSettings.TextSize.NORMAL);
      }
      else if (zoomLevel <= 175) {
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
        ((NavListener)getActivity()).onInternalLinkClicked(url);
      }
      else {
        ((NavListener)getActivity()).onExternalLinkClicked(url);
      }

      return(true);
    }
  }
}

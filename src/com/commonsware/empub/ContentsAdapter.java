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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

class ContentsAdapter extends FragmentStatePagerAdapter {
  BookContents contents=null;
  FragmentActivity ctxt=null;

  public ContentsAdapter(FragmentActivity ctxt, BookContents contents) {
    super(ctxt.getSupportFragmentManager());
    this.ctxt=ctxt;
    this.contents=contents;
  }

  @Override
  public int getCount() {
    return(contents.getChapterCount() + 2);
  }

  @Override
  public Fragment getItem(int position) {
    Fragment frag=null;

    if (position == 0) {
      frag=
          ImageFragment.newInstance(contents.getCoverImage(),
                                    contents.getTitle());
    }
    else if (position == 1) {
      frag=ChapterFragment.newInstance(contents.getTOCFile());
    }
    else {
      String path=contents.getChapterFile(position - 2);

      frag=ChapterFragment.newInstance(path);
    }

    return(frag);
  }

  @Override
  public String getPageTitle(int position) {
    if (position == 0) {
      return(ctxt.getString(R.string.empub_cover));
    }
    else if (position == 1) {
      return(ctxt.getString(R.string.empub_toc));
    }

    return(contents.getChapterTitle(position - 2));
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
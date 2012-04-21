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

import java.io.IOException;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.actionbarsherlock.app.SherlockFragment;

public class ImageFragment extends SherlockFragment {
  private static final String KEY_IMAGE="image";
  private static final String KEY_DESCRIPTION="desc";

  protected static ImageFragment newInstance(String file, String desc) {
    ImageFragment f=new ImageFragment();

    Bundle args=new Bundle();

    args.putString(KEY_IMAGE, file);
    args.putString(KEY_DESCRIPTION, desc);
    f.setArguments(args);

    return(f);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    View contents=
        inflater.inflate(R.layout.empub_image, container, false);
    ImageView image=(ImageView)contents.findViewById(R.id.image);

    try {
      image.setImageBitmap(getImageBitmap());
    }
    catch (IOException e) {
      throw new RuntimeException("Exception loading bitmap", e);
    }

    image.setContentDescription(getDescription());

    return(contents);
  }

  private Bitmap getImageBitmap() throws IOException {
    Bitmap bitmap=null;
    InputStream is=null;

    try {
      is=
          getActivity().getAssets()
                       .open("book/"
                                 + getArguments().getString(KEY_IMAGE));
      bitmap=BitmapFactory.decodeStream(is);
    }
    finally {
      if (is != null) {
        is.close();
      }
    }

    return(bitmap);
  }

  private String getDescription() {
    return(getArguments().getString(KEY_DESCRIPTION));
  }
}

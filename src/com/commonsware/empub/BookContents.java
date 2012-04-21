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
import org.json.JSONArray;
import org.json.JSONObject;

public class BookContents {
  JSONObject raw=null;
  HashMap<String, Integer> files=new HashMap<String, Integer>();
  JSONArray chapters;
  
  BookContents(JSONObject raw) {
    this.raw=raw;
    chapters=raw.optJSONArray("chapters");
    
    for (int i=0;i<getChapterCount();i++) {
      files.put(getChapterFile(i), i);
    }
  }
  
  int getChapterCount() {
    return(chapters.length());
  }
  
  String getChapterFile(int position) {
    JSONObject chapter=chapters.optJSONObject(position);
    
    return(chapter.optString("file"));
  }
  
  String getChapterTitle(int position) {
    JSONObject chapter=chapters.optJSONObject(position);
    String result=chapter.optString("title");
    
    if (result==null) {
      result=String.valueOf(position+1);
    }
    
    return(result);
  }
  
  int getPositionForFile(String file) {
    return(files.get(file.substring(5)));
  }
  
  String getTOCFile() {
    return(raw.optString("toc"));
  }
  
  String getCoverImage() {
    return(raw.optString("cover"));
  }
  
  String getTitle() {
    return(raw.optString("title"));
  }
}

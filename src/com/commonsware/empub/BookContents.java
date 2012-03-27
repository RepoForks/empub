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
    return(files.get(file));
  }
  
  String getTOCFile() {
    return(raw.optString("toc"));
  }
}

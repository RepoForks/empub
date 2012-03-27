package com.commonsware.empub;

public interface NavListener {
  void onInternalLinkClicked(String url);
  void onExternalLinkClicked(String url);
}

package com.example.rednotedemo.presentation.view.layout;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class SafeStaggeredGridLayoutManager extends StaggeredGridLayoutManager {

  private Context context;
  public SafeStaggeredGridLayoutManager(Context context, int spanCount, int orientation) {
    super(spanCount, orientation);
    this.context = context;
  }

  @Override
  public void onScrollStateChanged(int state) {
    try {
      super.onScrollStateChanged(state);
    } catch (IndexOutOfBoundsException e) {
      Toast.makeText(context, "请勿滑动得太频繁", Toast.LENGTH_SHORT).show();
      Log.e("SafeStaggeredGridLayout", "IndexOutOfBoundsException handled", e);
      // 防止崩溃，不向上抛出异常
    } catch (Exception e) {
      Log.e("SafeStaggeredGridLayout", "Exception handled", e);
    }
  }
}
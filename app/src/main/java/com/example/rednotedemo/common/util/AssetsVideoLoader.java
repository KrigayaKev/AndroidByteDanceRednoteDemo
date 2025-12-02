package com.example.rednotedemo.common.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.rednotedemo.config.AppConfig;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.AssetDataSource;
import com.google.android.exoplayer2.upstream.DataSource;

public class AssetsVideoLoader {
  private static final String TAG = "AssetsVideoLoader";

  /**
   * 创建assets视频的MediaSource
   */
  public static MediaSource createMediaSource(Context context) {
    try {
      // 构建assets视频路径
      String videoPath = AppConfig.LOCAL_VIDEO_PATH + AppConfig.DEFAULT_VIDEO_FILE;
      Log.d(TAG, "加载assets视频: " + videoPath);

      // 创建AssetDataSource
      DataSource.Factory dataSourceFactory = new DataSource.Factory() {
        @Override
        public DataSource createDataSource() {
          return new AssetDataSource(context);
        }
      };

      // 创建MediaItem - 使用assets路径格式
      MediaItem mediaItem = MediaItem.fromUri("asset:///" + videoPath);

      // 创建并返回MediaSource
      return new ProgressiveMediaSource.Factory(dataSourceFactory)
         .createMediaSource(mediaItem);

    } catch (Exception e) {
      Log.e(TAG, "创建assets视频MediaSource失败", e);
      throw new RuntimeException("无法加载assets视频", e);
    }
  }

  /**
   * 检查是否为assets路径
   */
  public static boolean isAssetsPath(String path) {
    return path != null && (path.startsWith("asset://") ||
       path.startsWith("file:///android_asset/") ||
       !path.contains("://")); // 假设没有协议前缀的是assets文件
  }
}
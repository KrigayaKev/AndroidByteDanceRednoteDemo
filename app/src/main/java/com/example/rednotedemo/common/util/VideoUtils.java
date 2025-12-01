package com.example.rednotedemo.common.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 视频处理工具类
 */
public class VideoUtils {

  private static final String TAG = "VideoUtils";

  /**
   * 提取视频指定时间的帧
   */
  public static interface VideoFrameCallback {
    void onFrameExtracted(Bitmap frameBitmap, long duration);
    void onFrameExtractFailed(String error);
  }

  /**
   * 异步提取视频帧
   */
  public static void extractVideoFrame(Context context, Uri videoUri, long timeMs, VideoFrameCallback callback) {
    new AsyncTask<Void, Void, Result>() {
      @Override
      protected Result doInBackground(Void... voids) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
          retriever.setDataSource(context, videoUri);

          // 获取视频时长
          String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
          long duration = durationStr != null ? Long.parseLong(durationStr) : 0;

          // 提取指定时间点的帧
          Bitmap frame = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

          return new Result(frame, duration, null);
        } catch (Exception e) {
          Log.e(TAG, "提取视频帧失败", e);
          return new Result(null, 0, e.getMessage());
        } finally {
          try {
            retriever.release();
          } catch (IOException e) {
            Log.e(TAG, "释放MediaMetadataRetriever失败", e);
          }
        }
      }

      @Override
      protected void onPostExecute(Result result) {
        if (result.bitmap != null) {
          callback.onFrameExtracted(result.bitmap, result.duration);
        } else {
          callback.onFrameExtractFailed(result.error != null ? result.error : "提取视频帧失败");
        }
      }
    }.execute();
  }

  /**
   * 提取视频第一帧（简化方法）
   */
  public static void extractVideoFirstFrame(Context context, Uri videoUri, VideoFrameCallback callback) {
    extractVideoFrame(context, videoUri, 0, callback);
  }

  /**
   * 打开图库选择封面图片
   */
  public static void pickCoverImage(Context context) {
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");
    ((android.app.Activity) context).startActivityForResult(intent, 102);
  }

  /**
   * 将Bitmap保存为临时文件
   */
  public static File saveBitmapToTempFile(Context context, Bitmap bitmap, String fileName) {
    try {
      File cacheDir = context.getCacheDir();
      File tempFile = new File(cacheDir, fileName);

      FileOutputStream fos = new FileOutputStream(tempFile);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
      fos.flush();
      fos.close();

      return tempFile;
    } catch (IOException e) {
      Log.e(TAG, "保存Bitmap失败", e);
      return null;
    }
  }

  private static class Result {
    Bitmap bitmap;
    long duration;
    String error;

    Result(Bitmap bitmap, long duration, String error) {
      this.bitmap = bitmap;
      this.duration = duration;
      this.error = error;
    }
  }
}
// GiteeUploader.java
package com.example.rednotedemo.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.example.rednotedemo.config.AppConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GiteeUploader {
  private static final String TAG = "GiteeUploader";
  private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB限制

  private OkHttpClient client;
  private Context context;

  public interface UploadCallback {
    void onSuccess(String imageUrl);

    void onFailure(String error);
  }

  public GiteeUploader(Context context) {
    this.context = context;
    this.client = new OkHttpClient();
  }

  /**
   * 上传图片到图片文件夹
   */
  public void uploadImage(Uri imageUri, String fileName, UploadCallback callback) {
    uploadFile(imageUri, fileName, AppConfig.GITEE_IMAGE_FOLDER, AppConfig.MAX_IMAGE_SIZE, callback);
  }

  /**
   * 上传视频到视频文件夹
   */
  public void uploadVideo(Uri videoUri, String fileName, UploadCallback callback) {
    uploadFile(videoUri, fileName, AppConfig.GITEE_VIDEO_FOLDER, AppConfig.MAX_VIDEO_SIZE, callback);
  }

  /**
   * 上传封面图片到封面文件夹
   */
  public void uploadCover(Bitmap bitmap, String fileName, UploadCallback callback) {
    try {
      // 将Bitmap保存为临时文件
      File tempFile = VideoUtils.saveBitmapToTempFile(context, bitmap, fileName);
      if (tempFile == null) {
        callback.onFailure("无法保存封面图片");
        return;
      }

      // 读取文件内容为Base64
      String base64Content = fileToBase64(tempFile);
      if (base64Content == null) {
        callback.onFailure("封面图片编码失败");
        return;
      }

      // 上传到封面文件夹
      uploadToGitee(fileName, base64Content, AppConfig.GITEE_COVER_FOLDER, callback);

      // 删除临时文件
      tempFile.delete();

    } catch (Exception e) {
      Log.e(TAG, "上传封面失败", e);
      callback.onFailure("上传封面失败: " + e.getMessage());
    }
  }

  /**
   * 上传图片Uri到封面文件夹
   */
  public void uploadCover(Uri imageUri, String fileName, UploadCallback callback) {
    uploadFile(imageUri, fileName, AppConfig.GITEE_COVER_FOLDER, AppConfig.MAX_IMAGE_SIZE, callback);
  }

  /**
   * 通用文件上传方法
   */
  private void uploadFile(Uri uri, String fileName, String folder, long maxSize, UploadCallback callback) {
    try {
      // 1. 将Uri转换为File
      File file = uriToFile(uri, fileName);
      if (file == null) {
        callback.onFailure("无法读取文件");
        return;
      }

      // 2. 检查文件大小
      if (file.length() > maxSize) {
        String fileType = folder.equals(AppConfig.GITEE_VIDEO_FOLDER) ? "视频" : "图片";
        long maxSizeMB = maxSize / (1024 * 1024);
        callback.onFailure(String.format("%s文件过大，请压缩后上传（最大%dMB）", fileType, maxSizeMB));
        return;
      }

      // 3. 读取文件内容为Base64
      String base64Content = fileToBase64(file);
      if (base64Content == null) {
        callback.onFailure("文件编码失败");
        return;
      }

      // 4. 调用Gitee API上传到指定文件夹
      uploadToGitee(fileName, base64Content, folder, callback);

    } catch (Exception e) {
      Log.e(TAG, "上传文件失败", e);
      callback.onFailure("上传失败: " + e.getMessage());
    }
  }

  /**
   * 将 Uri 转换为 File
   */
  private File uriToFile(Uri uri, String fileName) {
    try {
      if ("file".equals(uri.getScheme())) {
        return new File(uri.getPath());
      } else {
        // 处理 content:// Uri
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = new File(context.getCacheDir(), fileName);

        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
          outputStream.write(buffer, 0, length);
        }

        outputStream.close();
        inputStream.close();
        return tempFile;
      }
    } catch (Exception e) {
      Log.e(TAG, "Uri 转 File 失败", e);
      return null;
    }
  }

  /**
   * 将文件转换为 Base64
   */
  private String fileToBase64(File file) {
    try {
      FileInputStream inputStream = new FileInputStream(file);
      byte[] bytes = new byte[(int) file.length()];
      inputStream.read(bytes);
      inputStream.close();
      return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    } catch (IOException e) {
      Log.e(TAG, "文件转 Base64 失败", e);
      return null;
    }
  }

  /**
   * 调用Gitee API上传文件到指定文件夹
   */
  private void uploadToGitee(String fileName, String base64Content, String folder, UploadCallback callback) {
    String fullPath = folder + fileName;
    String url = String.format("https://gitee.com/api/v5/repos/%s/%s/contents/%s",
       AppConfig.GITEE_USERNAME, AppConfig.GITEE_REPO, fullPath);

    RequestBody requestBody = new MultipartBody.Builder()
       .setType(MultipartBody.FORM)
       .addFormDataPart("access_token", AppConfig.GITEE_ACCESS_TOKEN)
       .addFormDataPart("message", "Upload from RedNote app")
       .addFormDataPart("content", base64Content)
       .addFormDataPart("branch", AppConfig.GITEE_BRANCH)
       .build();

    Request request = new Request.Builder()
       .url(url)
       .post(requestBody)
       .build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        Log.e(TAG, "Gitee API调用失败", e);
        callback.onFailure("网络请求失败: " + e.getMessage());
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
          // 生成访问URL
          String rawUrl = String.format("https://gitee.com/%s/%s/raw/%s/%s%s",
             AppConfig.GITEE_USERNAME,
             AppConfig.GITEE_REPO,
             AppConfig.GITEE_BRANCH,
             folder,
             fileName);
          Log.d(TAG, "文件上传成功，URL: " + rawUrl);
          callback.onSuccess(rawUrl);
        } else {
          String errorBody = response.body() != null ? response.body().string() : "Unknown error";
          Log.e(TAG, "Gitee API响应失败: " + response.code() + " - " + errorBody);

          String errorMessage;
          switch (response.code()) {
            case 400: errorMessage = "请求参数错误"; break;
            case 401: errorMessage = "认证失败，请检查Access Token"; break;
            case 403: errorMessage = "权限不足"; break;
            case 404: errorMessage = "仓库不存在"; break;
            case 422: errorMessage = "文件已存在或路径无效"; break;
            default: errorMessage = "上传失败，状态码: " + response.code();
          }
          callback.onFailure(errorMessage);
        }
      }
    });
  }

  /**
   * 生成图片文件名
   */
  public static String generateImageFileName(long timestamp, int imageIndex) {
    return "image_" + timestamp + "_" + imageIndex + ".jpg";
  }

  /**
   * 生成视频文件名
   */
  public static String generateVideoFileName(long timestamp) {
    return "video_" + timestamp + ".mp4";
  }

  /**
   * 生成封面文件名
   */
  public static String generateCoverFileName(long timestamp) {
    return "cover_" + timestamp + ".jpg";
  }

  /**
   * 检查配置是否完整
   */
  public static boolean isConfigValid() {
    return AppConfig.GITEE_ACCESS_TOKEN != null && !AppConfig.GITEE_ACCESS_TOKEN.isEmpty() &&
       AppConfig.GITEE_USERNAME != null && !AppConfig.GITEE_USERNAME.isEmpty() &&
       AppConfig.GITEE_REPO != null && !AppConfig.GITEE_REPO.isEmpty();
  }

}
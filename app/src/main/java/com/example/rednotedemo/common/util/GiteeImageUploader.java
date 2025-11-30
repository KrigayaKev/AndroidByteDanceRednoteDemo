// GiteeImageUploader.java
package com.example.rednotedemo.common.util;

import android.content.Context;
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

public class GiteeImageUploader {
    private static final String TAG = "GiteeImageUploader";

    private OkHttpClient client;
    private Context context;

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    public GiteeImageUploader(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    /**
     * 上传图片到 Gitee 仓库
     */
    public void uploadImage(Uri imageUri, String fileName, UploadCallback callback) {
        try {
            // 1. 将 Uri 转换为 File
            File imageFile = uriToFile(imageUri, fileName);
            if (imageFile == null) {
                callback.onFailure("无法读取图片文件");
                return;
            }

            // 2. 读取文件内容为 Base64
            String base64Content = fileToBase64(imageFile);
            if (base64Content == null) {
                callback.onFailure("图片编码失败");
                return;
            }

            // 3. 调用 Gitee API 上传
            uploadToGitee(fileName, base64Content, callback);

        } catch (Exception e) {
            Log.e(TAG, "上传图片失败", e);
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
     * 调用 Gitee API 上传文件
     */
    private void uploadToGitee(String fileName, String base64Content, UploadCallback callback) {
        // 使用 AppConfig 中的配置
        String fullPath = AppConfig.GITEE_IMAGE_FOLDER + fileName;
        String url = String.format("https://gitee.com/api/v5/repos/%s/%s/contents/%s",
                AppConfig.GITEE_USERNAME, AppConfig.GITEE_REPO, fullPath);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("access_token", AppConfig.GITEE_ACCESS_TOKEN)
                .addFormDataPart("message", "Upload image from RedNote app")
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
                Log.e(TAG, "Gitee API 调用失败", e);
                callback.onFailure("网络请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 使用 AppConfig 生成图片访问 URL
                    String rawUrl = String.format("https://gitee.com/%s/%s/raw/%s/%s%s",
                            AppConfig.GITEE_USERNAME,
                            AppConfig.GITEE_REPO,
                            AppConfig.GITEE_BRANCH,
                            AppConfig.GITEE_IMAGE_FOLDER,
                            fileName);
                    Log.d(TAG, "图片上传成功，URL: " + rawUrl);
                    callback.onSuccess(rawUrl);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Gitee API 响应失败: " + response.code() + " - " + errorBody);

                    String errorMessage;
                    switch (response.code()) {
                        case 400:
                            errorMessage = "请求参数错误";
                            break;
                        case 401:
                            errorMessage = "认证失败，请检查 Access Token";
                            break;
                        case 403:
                            errorMessage = "权限不足";
                            break;
                        case 404:
                            errorMessage = "仓库不存在";
                            break;
                        case 422:
                            errorMessage = "文件已存在或路径无效";
                            break;
                        default:
                            errorMessage = "上传失败，状态码: " + response.code();
                    }
                    callback.onFailure(errorMessage);
                }
            }
        });
    }

    /**
     * 生成唯一的文件名
     */
    public static String generateImageFileName(long timestamp, int imageIndex) {
        return "post_" + timestamp + "_image_" + imageIndex + ".jpg";
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
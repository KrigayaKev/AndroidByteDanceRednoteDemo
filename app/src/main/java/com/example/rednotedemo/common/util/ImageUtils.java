// ImageUtils.java
package com.example.rednotedemo.common.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * 图片处理工具类
 */
public class ImageUtils {

  /**
   * 将 Uri 对应的图片保存到应用内部存储，并返回文件路径
   */
  public static String saveImageToInternalStorage(Context context, Uri imageUri, String fileName) {
    try {
      // 创建 images 目录
      File imagesDir = new File(context.getFilesDir(), "images");
      if (!imagesDir.exists()) {
        imagesDir.mkdirs();
      }

      // 创建目标文件
      File outputFile = new File(imagesDir, fileName);

      // 复制文件
      InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
      OutputStream outputStream = new FileOutputStream(outputFile);

      byte[] buffer = new byte[1024];
      int length;
      while ((length = inputStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, length);
      }

      outputStream.close();
      inputStream.close();

      // 返回文件路径（用于 Room 存储）
      return outputFile.getAbsolutePath();

    } catch (Exception e) {
      Log.e("ImageUtils", "保存图片失败", e);
      return null;
    }
  }

  /**
   * 生成唯一的文件名
   */
  public static String generateImageFileName(int postId, int imageIndex) {
    return "post_" + postId + "_image_" + imageIndex + "_" + System.currentTimeMillis() + ".jpg";
  }

  /**
   * 从文件路径获取 Uri（用于 Glide 加载）
   */
  public static Uri getImageUriFromPath(String filePath) {
    return Uri.fromFile(new File(filePath));
  }
}
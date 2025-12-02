package com.example.rednotedemo.common.util;

import com.example.rednotedemo.config.AppConfig;

public class GiteeVideoUrlBuilder {

  /**
   * 构建本地assets视频URL
   */
  public static String buildLocalVideoUrl(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return "";
    }

    // 构建本地assets视频路径
    return "file:///android_asset/" + AppConfig.LOCAL_VIDEO_PATH + fileName;
  }

  /**
   * 构建 Gitee 视频的 raw URL
   */
  public static String buildVideoUrl(String fileName) {
    return String.format("https://gitee.com/%s/%s/raw/%s/%s%s",
       AppConfig.GITEE_USERNAME,
       AppConfig.GITEE_REPO,
       AppConfig.GITEE_BRANCH,
       AppConfig.GITEE_VIDEO_FOLDER,
       fileName);
  }

  /**
   * 构建 Gitee 封面的 raw URL
   */
  public static String buildCoverUrl(String fileName) {
    return String.format("https://gitee.com/%s/%s/raw/%s/%s%s",
       AppConfig.GITEE_USERNAME,
       AppConfig.GITEE_REPO,
       AppConfig.GITEE_BRANCH,
       AppConfig.GITEE_COVER_FOLDER,
       fileName);
  }

  /**
   * 构建 Gitee 图片的 raw URL
   */
  public static String buildImageUrl(String fileName) {
    return String.format("https://gitee.com/%s/%s/raw/%s/%s%s",
       AppConfig.GITEE_USERNAME,
       AppConfig.GITEE_REPO,
       AppConfig.GITEE_BRANCH,
       AppConfig.GITEE_IMAGE_FOLDER,
       fileName);
  }

  /**
   * 从完整 URL 中提取文件名
   */
  public static String extractFileNameFromUrl(String url) {
    if (url == null || url.isEmpty()) return "";

    try {
      // 如果是本地assets路径，提取文件名
      if (url.contains("android_asset/")) {
        String[] parts = url.split("/");
        if (parts.length > 0) {
          return parts[parts.length - 1];
        }
      }

      // 如果是 Gitee raw URL，提取文件名
      if (url.contains("/raw/")) {
        String[] parts = url.split("/");
        if (parts.length > 0) {
          String fileName = parts[parts.length - 1];
          // 移除可能的查询参数
          int queryIndex = fileName.indexOf("?");
          if (queryIndex != -1) {
            fileName = fileName.substring(0, queryIndex);
          }
          return fileName;
        }
      }

      // 尝试从 URL 路径中提取
      int lastSlash = url.lastIndexOf('/');
      if (lastSlash != -1 && lastSlash < url.length() - 1) {
        String fileName = url.substring(lastSlash + 1);
        // 移除可能的查询参数
        int queryIndex = fileName.indexOf("?");
        if (queryIndex != -1) {
          fileName = fileName.substring(0, queryIndex);
        }
        return fileName;
      }

      return url;
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * 检查 URL 是否是本地assets路径
   */
  public static boolean isLocalAssetsUrl(String url) {
    return url != null && url.startsWith("file:///android_asset/");
  }

  /**
   * 检查 URL 是否是有效的 Gitee raw URL
   */
  public static boolean isValidGiteeUrl(String url) {
    return url != null && url.startsWith("https://gitee.com/") && url.contains("/raw/");
  }

  /**
   * 处理视频 URL，根据实际情况返回本地或远程URL
   */
  public static String ensureVideoUrl(String videoUrl, boolean isVideo) {
    if (videoUrl == null || videoUrl.isEmpty()) {
      return "";
    }

    // 如果已经是完整的 URL，直接返回
    if (videoUrl.startsWith("http") || videoUrl.startsWith("file://")) {
      return videoUrl;
    }

    // 对于视频文件，优先使用本地assets路径
    // 这里可以根据实际情况判断，这里假设所有视频都在本地assets中
    if (isVideo) {
      // 检查是否为已知的视频文件
      if (videoUrl.equals("VID_20250713_133634.mp4")) {
        return buildLocalVideoUrl(videoUrl);
      }
      // 其他视频也尝试使用本地路径
      return buildLocalVideoUrl(videoUrl);
    } else {
      // 封面图片暂时使用默认封面
      return "";
    }
  }

  /**
   * 构建带时间戳的 URL（防止缓存）
   */
  public static String buildUrlWithTimestamp(String url) {
    if (url == null || url.isEmpty()) return "";

    // 本地assets文件不需要时间戳
    if (isLocalAssetsUrl(url)) {
      return url;
    }

    if (url.contains("?")) {
      return url + "&_t=" + System.currentTimeMillis();
    } else {
      return url + "?_t=" + System.currentTimeMillis();
    }
  }
}
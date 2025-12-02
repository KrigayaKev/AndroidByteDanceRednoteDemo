package com.example.rednotedemo.config;

public class AppConfig {
  // 这些信息应该从安全的地方获取，不要硬编码在代码中
  public static final String GITEE_ACCESS_TOKEN = "eb78674115dd613b814bdd83b7673928";
  public static final String GITEE_USERNAME = "Haluzzz";
  public static final String GITEE_REPO = "my-images";
  public static final String GITEE_BRANCH = "master";

  // 图片和视频存储路径
  public static final String GITEE_IMAGE_FOLDER = "bytedance/";      // 图片文件夹
  public static final String GITEE_VIDEO_FOLDER = "videos/";      // 视频文件夹
  public static final String GITEE_COVER_FOLDER = "covers/";      // 封面文件夹

  // 最大文件限制
  public static final long MAX_IMAGE_SIZE = 50 * 1024 * 1024;     // 50MB
  public static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;     // 50MB

  // 本地assets路径
  public static final String LOCAL_VIDEO_PATH = "video/";
  // 默认视频文件名
  public static final String DEFAULT_VIDEO_FILE = "VID_20250713_133634.mp4";

  // Gitee API URL模板
  public static final String GITEE_RAW_URL_TEMPLATE = "https://gitee.com/api/v5/repos/%s/%s/raw/%s/%s";

  /**
   * 构建带Token的Gitee文件URL
   * 使用Gitee API的raw接口，支持Token认证
   */
  public static String buildGiteeFileUrl(String folder, String fileName) {
    String filePath = folder + fileName;
    return String.format(GITEE_RAW_URL_TEMPLATE,
       GITEE_USERNAME,
       GITEE_REPO,
       GITEE_BRANCH,
       filePath) + "?access_token=" + GITEE_ACCESS_TOKEN;
  }
}
package com.example.rednotedemo.common.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;

public class VideoPlayerHelper {
  private static final String TAG = "VideoPlayerHelper";

  // 单例实例
  private static volatile VideoPlayerHelper instance;

  private ExoPlayer exoPlayer;
  private Context context;
  private PlayerView playerView;
  private boolean isPlaying = false;
  private OnVideoStateChangeListener stateListener;
  private Handler mainHandler;

  // 视频播放状态监听器
  public interface OnVideoStateChangeListener {
    void onVideoLoading();
    void onVideoReady();
    void onVideoPlaying();
    void onVideoPaused();
    void onVideoEnded();
    void onVideoError(String errorMessage);
  }

  // 私有构造函数
  private VideoPlayerHelper(Context context) {
    this.context = context.getApplicationContext();
    this.mainHandler = new Handler(Looper.getMainLooper());
  }

  // 获取单例
  public static VideoPlayerHelper getInstance(Context context) {
    if (instance == null) {
      synchronized (VideoPlayerHelper.class) {
        if (instance == null) {
          instance = new VideoPlayerHelper(context);
        }
      }
    }
    return instance;
  }

  /**
   * 初始化播放器
   */
  public void initialize() {
    if (exoPlayer == null) {
      exoPlayer = new ExoPlayer.Builder(context).build();
      setupPlayerListeners();
    }
  }

  /**
   * 绑定 PlayerView
   */
  public void bindPlayerView(PlayerView playerView) {
    this.playerView = playerView;
    if (exoPlayer != null && playerView != null) {
      playerView.setPlayer(exoPlayer);
    }
  }

  /**
   * 准备播放本地assets视频（完全忽略传入参数）
   */
  public void prepareVideo() {
    if (exoPlayer == null) {
      Log.e(TAG, "播放器未初始化");
      return;
    }

    Log.d(TAG, "准备播放本地assets视频");

    try {
      // 使用AssetsVideoLoader创建MediaSource
      MediaSource mediaSource = AssetsVideoLoader.createMediaSource(context);

      // 设置媒体源
      exoPlayer.setMediaSource(mediaSource);
      exoPlayer.prepare();
      exoPlayer.setPlayWhenReady(false);

      notifyLoading();

    } catch (Exception e) {
      Log.e(TAG, "准备本地视频失败", e);
      notifyError("加载本地视频失败: " + e.getMessage());
    }
  }

  /**
   * 播放视频
   */
  public void play() {
    if (exoPlayer != null) {
      exoPlayer.setPlayWhenReady(true);
      isPlaying = true;
      notifyPlaying();
    }
  }

  /**
   * 暂停视频
   */
  public void pause() {
    if (exoPlayer != null) {
      exoPlayer.setPlayWhenReady(false);
      isPlaying = false;
      notifyPaused();
    }
  }

  /**
   * 停止视频播放并释放资源
   */
  public void stop() {
    if (exoPlayer != null) {
      exoPlayer.stop();
      exoPlayer.setPlayWhenReady(false);
      isPlaying = false;
    }
  }

  /**
   * 释放播放器资源
   */
  public void release() {
    if (exoPlayer != null) {
      stop();
      exoPlayer.release();
      exoPlayer = null;
    }
    playerView = null;
    stateListener = null;
  }

  /**
   * 设置视频播放状态监听器
   */
  public void setOnVideoStateChangeListener(OnVideoStateChangeListener listener) {
    this.stateListener = listener;
  }

  /**
   * 获取播放器实例
   */
  @Nullable
  public ExoPlayer getPlayer() {
    return exoPlayer;
  }

  /**
   * 检查是否正在播放
   */
  public boolean isPlaying() {
    return isPlaying;
  }

  /**
   * 设置播放器音量（0.0 - 1.0）
   */
  public void setVolume(float volume) {
    if (exoPlayer != null) {
      exoPlayer.setVolume(volume);
    }
  }

  /**
   * 设置播放器监听器
   */
  private void setupPlayerListeners() {
    if (exoPlayer == null) return;

    exoPlayer.addListener(new Player.Listener() {
      @Override
      public void onPlaybackStateChanged(int playbackState) {
        switch (playbackState) {
          case Player.STATE_BUFFERING:
            notifyLoading();
            break;
          case Player.STATE_READY:
            notifyReady();
            break;
          case Player.STATE_ENDED:
            notifyEnded();
            break;
          case Player.STATE_IDLE:
            // 空闲状态
            break;
        }
      }

      @Override
      public void onPlayerError(PlaybackException error) {
        String errorMessage = getErrorMessage(error);
        Log.e(TAG, "播放错误: " + errorMessage, error);
        notifyError(errorMessage);
      }

      @Override
      public void onIsPlayingChanged(boolean isPlaying) {
        VideoPlayerHelper.this.isPlaying = isPlaying;
        if (isPlaying) {
          notifyPlaying();
        } else {
          notifyPaused();
        }
      }
    });
  }

  /**
   * 获取友好的错误信息
   */
  private String getErrorMessage(PlaybackException error) {
    if (error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND) {
      return "视频文件不存在，请检查assets中是否有视频文件";
    } else if (error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED) {
      return "视频格式不支持或文件损坏";
    } else if (error.getMessage() != null && error.getMessage().contains("asset")) {
      return "无法从assets加载视频";
    } else {
      return error.getMessage() != null ? error.getMessage() : "视频播放错误";
    }
  }

  // 通知方法
  private void notifyLoading() {
    mainHandler.post(() -> {
      if (stateListener != null) {
        stateListener.onVideoLoading();
      }
    });
  }

  private void notifyReady() {
    mainHandler.post(() -> {
      if (stateListener != null) {
        stateListener.onVideoReady();
      }
    });
  }

  private void notifyPlaying() {
    mainHandler.post(() -> {
      if (stateListener != null) {
        stateListener.onVideoPlaying();
      }
    });
  }

  private void notifyPaused() {
    mainHandler.post(() -> {
      if (stateListener != null) {
        stateListener.onVideoPaused();
      }
    });
  }

  private void notifyEnded() {
    mainHandler.post(() -> {
      if (stateListener != null) {
        stateListener.onVideoEnded();
      }
    });
  }

  private void notifyError(String errorMessage) {
    mainHandler.post(() -> {
      if (stateListener != null) {
        stateListener.onVideoError(errorMessage);
      }
    });
  }
}
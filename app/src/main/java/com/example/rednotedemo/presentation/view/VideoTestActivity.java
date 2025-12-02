package com.example.rednotedemo.presentation.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rednotedemo.R;
import com.example.rednotedemo.common.util.VideoPlayerHelper;
import com.google.android.exoplayer2.ui.PlayerView;

public class VideoTestActivity extends AppCompatActivity implements VideoPlayerHelper.OnVideoStateChangeListener {

  private VideoPlayerHelper videoPlayerHelper;
  private PlayerView playerView;
  private EditText etVideoUrl;
  private Button btnPlay;
  private Button btnPause;
  private Button btnStop;
  private TextView tvState;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video_test);

    initViews();
    initVideoPlayer();
  }

  private void initViews() {
    playerView = findViewById(R.id.player_view_test);
    etVideoUrl = findViewById(R.id.et_video_url);
    btnPlay = findViewById(R.id.btn_play);
    btnPause = findViewById(R.id.btn_pause);
    btnStop = findViewById(R.id.btn_stop);
    tvState = findViewById(R.id.tv_status);

    // 设置一些测试URL
    LinearLayout llTestUrls = findViewById(R.id.ll_test_urls);

    String[] testUrls = {
       "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
       "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
       "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
       // 这里可以添加你的Gitee视频URL
       "https://gitee.com/Haluzzz/my-images/raw/master/videos/test.mp4"
    };

    for (String url : testUrls) {
      Button btn = new Button(this);
      btn.setText(url.substring(url.lastIndexOf('/') + 1));
      btn.setOnClickListener(v -> {
        etVideoUrl.setText(url);
      });
      llTestUrls.addView(btn);
    }

    btnPlay.setOnClickListener(v -> {
      String url = etVideoUrl.getText().toString().trim();
      if (url.isEmpty()) {
        Toast.makeText(this, "请输入视频URL", Toast.LENGTH_SHORT).show();
        return;
      }
      videoPlayerHelper.prepareVideo();
      videoPlayerHelper.play();
    });

    btnPause.setOnClickListener(v -> {
      videoPlayerHelper.pause();
    });

    btnStop.setOnClickListener(v -> {
      videoPlayerHelper.stop();
    });
  }

  private void initVideoPlayer() {
    videoPlayerHelper = VideoPlayerHelper.getInstance(this);
    videoPlayerHelper.initialize();
    videoPlayerHelper.bindPlayerView(playerView);
    videoPlayerHelper.setOnVideoStateChangeListener(this);
  }

  @Override
  public void onVideoLoading() {
    tvState.setText("状态：加载中...");
  }

  @Override
  public void onVideoReady() {
    tvState.setText("状态：准备就绪");
  }

  @Override
  public void onVideoPlaying() {
    tvState.setText("状态：播放中");
  }

  @Override
  public void onVideoPaused() {
    tvState.setText("状态：已暂停");
  }

  @Override
  public void onVideoEnded() {
    tvState.setText("状态：播放结束");
  }

  @Override
  public void onVideoError(String errorMessage) {
    tvState.setText("状态：错误 - " + errorMessage);
    Toast.makeText(this, "播放错误: " + errorMessage, Toast.LENGTH_LONG).show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (videoPlayerHelper != null) {
      videoPlayerHelper.release();
    }
  }
}
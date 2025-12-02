package com.example.rednotedemo.presentation.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.rednotedemo.R;
import com.example.rednotedemo.common.util.VideoPlayerHelper;
import com.example.rednotedemo.entity.Comment;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;
import com.example.rednotedemo.entity.User;
import com.example.rednotedemo.presentation.view.adapter.CommentAdapter;
import com.example.rednotedemo.presentation.view.adapter.PostImagePagerAdapter;
import com.example.rednotedemo.presentation.viewmodel.PostDetailViewModel;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity
   implements VideoPlayerHelper.OnVideoStateChangeListener {

  private static final String TAG = "PostDetailActivity";

  // 视图组件
  private Toolbar toolbar;
  private ImageView ivAuthorAvatar;
  private TextView tvAuthorName;
  private TextView tvPostTime;
  private ImageButton btnMore;

  // 图片容器
  private LinearLayout llImageContainer;
  private ViewPager2 vpImages;
  private TextView tvImageIndicator;
  private LinearLayout llIndicator;

  // 视频容器
  private FrameLayout flVideoContainer;
  private PlayerView playerView;
  private ImageView ivVideoCover;
  private ImageView ivPlayButton;
  private TextView tvVideoDuration;

  // 内容区域
  private TextView tvTitle;
  private TextView tvContent;
  private TextView tvLikeCount;
  private TextView tvCommentCount;
  private TextView tvCommentTotal;
  private RecyclerView rvComments;
  private TextView tvNoComments;
  private EditText etComment;
  private Button btnSend;

  private PostDetailViewModel viewModel;
  private CommentAdapter commentAdapter;
  private PostImagePagerAdapter imagePagerAdapter;

  // 视频播放器助手
  private VideoPlayerHelper videoPlayerHelper;

  // 当前帖子ID
  private int currentPostId = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post_detail);

    initViews();
    initVideoPlayer();
    initViewModel();
    setupClickListeners();
    setupImagePager();
  }

  private void initViews() {
    // 初始化所有视图组件
    toolbar = findViewById(R.id.toolbar);
    ivAuthorAvatar = findViewById(R.id.iv_author_avatar);
    tvAuthorName = findViewById(R.id.tv_author_name);
    tvPostTime = findViewById(R.id.tv_post_time);
    btnMore = findViewById(R.id.btn_more);

    // 图片容器
    llImageContainer = findViewById(R.id.ll_image_container);
    vpImages = findViewById(R.id.vp_images);
    tvImageIndicator = findViewById(R.id.tv_image_indicator);
    llIndicator = findViewById(R.id.ll_indicator);

    // 视频容器
    flVideoContainer = findViewById(R.id.fl_video_container);
    playerView = findViewById(R.id.player_view);
    ivVideoCover = findViewById(R.id.iv_video_cover);
    ivPlayButton = findViewById(R.id.iv_play_button);
    tvVideoDuration = findViewById(R.id.tv_video_duration);

    // 内容区域
    tvTitle = findViewById(R.id.tv_title);
    tvContent = findViewById(R.id.tv_content);
    tvLikeCount = findViewById(R.id.tv_like_count);
    tvCommentCount = findViewById(R.id.tv_comment_count);
    tvCommentTotal = findViewById(R.id.tv_comment_total);
    rvComments = findViewById(R.id.rv_comments);
    tvNoComments = findViewById(R.id.tv_no_comments);
    etComment = findViewById(R.id.et_comment);
    btnSend = findViewById(R.id.btn_send);

    // 设置工具栏
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle("帖子详情");
    }

    // 初始化评论适配器
    commentAdapter = new CommentAdapter();
    rvComments.setAdapter(commentAdapter);
    rvComments.setLayoutManager(new LinearLayoutManager(this));
  }

  private void initVideoPlayer() {
    // 初始化视频播放器助手
    videoPlayerHelper = VideoPlayerHelper.getInstance(this);
    videoPlayerHelper.initialize();
    videoPlayerHelper.bindPlayerView(playerView);
    videoPlayerHelper.setOnVideoStateChangeListener(this);

    // 设置播放按钮点击事件
    ivPlayButton.setOnClickListener(v -> playVideo());

    // 设置封面点击事件
    ivVideoCover.setOnClickListener(v -> playVideo());
  }

  private void setupImagePager() {
    // 设置 ViewPager2 方向为水平
    vpImages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

    // 初始化图片适配器
    imagePagerAdapter = new PostImagePagerAdapter(null);
    vpImages.setAdapter(imagePagerAdapter);

    // 设置页面切换监听
    vpImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
      @Override
      public void onPageSelected(int position) {
        super.onPageSelected(position);
        updateImageIndicator(position);
      }
    });
  }

  private void initViewModel() {
    viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

    // 获取传递的帖子ID
    currentPostId = getIntent().getIntExtra("POST_ID", -1);
    if (currentPostId == -1) {
      Toast.makeText(this, "帖子ID无效", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // 设置帖子ID，触发数据加载
    viewModel.setPostId(currentPostId);

    // 观察帖子数据
    viewModel.getPost().observe(this, this::bindPostData);

    // 观察图片数据
    viewModel.getPostImages().observe(this, this::bindImageData);

    // 观察评论数据
    viewModel.getComments().observe(this, this::bindCommentData);
  }

  private void bindPostData(Post post) {
    if (post == null) {
      Toast.makeText(this, "帖子数据加载失败", Toast.LENGTH_SHORT).show();
      return;
    }

    // 更新帖子基本信息
    tvTitle.setText(post.getTitle());
    tvContent.setText(post.getContent());
    tvPostTime.setText(formatTime(post.getCreateTime()));

    // 获取作者信息
    viewModel.getAuthor(post.getUserId()).observe(this, this::bindAuthorData);

    // 根据帖子类型显示不同的内容
    if (post.isVideo()) {
      // 视频帖子
      llImageContainer.setVisibility(View.GONE);
      flVideoContainer.setVisibility(View.VISIBLE);

      // 设置视频封面 - 使用默认封面
      ivVideoCover.setImageResource(R.drawable.ic_default_video_cover);

      // 设置视频时长
      if (post.getVideoDuration() > 0) {
        tvVideoDuration.setVisibility(View.VISIBLE);
        tvVideoDuration.setText(formatDuration(post.getVideoDuration()));
      } else {
        // 设置默认时长
        tvVideoDuration.setVisibility(View.VISIBLE);
        tvVideoDuration.setText("00:30"); // 假设30秒
      }

      // 直接准备播放本地assets视频
      Log.d(TAG, "检测到视频帖子，准备播放本地assets视频");
      videoPlayerHelper.prepareVideo();

    } else {
      // 图片帖子
      flVideoContainer.setVisibility(View.GONE);
      llImageContainer.setVisibility(View.VISIBLE);
    }
  }

  private void bindImageData(List<PostImage> images) {
    if (images != null && !images.isEmpty()) {
      // 设置图片到 ViewPager2
      imagePagerAdapter.setImageList(images);
      vpImages.setVisibility(View.VISIBLE);

      // 设置指示器
      setupIndicators(images.size());
      updateImageIndicator(0);

      // 如果有多个图片，显示数字指示器
      if (images.size() > 1) {
        tvImageIndicator.setVisibility(View.VISIBLE);
      } else {
        tvImageIndicator.setVisibility(View.GONE);
      }
    } else {
      vpImages.setVisibility(View.GONE);
      tvImageIndicator.setVisibility(View.GONE);

      // 如果是图片帖子但没有图片，显示提示
      viewModel.getPost().observe(this, post -> {
        if (post != null && !post.isVideo()) {
          Toast.makeText(this, "该帖子没有图片", Toast.LENGTH_SHORT).show();
        }
      });
    }
  }

  /**
   * 设置图片指示器（小圆点）
   */
  private void setupIndicators(int count) {
    llIndicator.removeAllViews();

    if (count <= 1) {
      llIndicator.setVisibility(View.GONE);
      return;
    }

    llIndicator.setVisibility(View.VISIBLE);

    for (int i = 0; i < count; i++) {
      ImageView dot = new ImageView(this);
      dot.setImageResource(R.drawable.dot_indicator);

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
         dpToPx(8), dpToPx(8)
      );
      params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
      dot.setLayoutParams(params);

      // 默认第一个选中
      dot.setSelected(i == 0);

      llIndicator.addView(dot);
    }
  }

  /**
   * 更新图片指示器
   */
  private void updateImageIndicator(int position) {
    int total = imagePagerAdapter.getItemCount();

    // 更新数字指示器
    if (total > 0) {
      tvImageIndicator.setText((position + 1) + "/" + total);
    }

    // 更新小圆点指示器
    for (int i = 0; i < llIndicator.getChildCount(); i++) {
      ImageView dot = (ImageView) llIndicator.getChildAt(i);
      dot.setSelected(i == position);
    }
  }

  /**
   * dp转px
   */
  private int dpToPx(int dp) {
    float density = getResources().getDisplayMetrics().density;
    return Math.round(dp * density);
  }

  private void bindAuthorData(User author) {
    if (author != null) {
      tvAuthorName.setText(author.getUsername());
      // 加载头像
      if (author.getAvatarUrl() != null && !author.getAvatarUrl().isEmpty()) {
        Glide.with(this)
           .load(author.getAvatarUrl())
           .placeholder(R.drawable.ic_default_avatar)
           .into(ivAuthorAvatar);
      }
    } else {
      tvAuthorName.setText("未知用户");
      ivAuthorAvatar.setImageResource(R.drawable.ic_default_avatar);
    }
  }

  private void bindCommentData(List<Comment> comments) {
    if (comments != null) {
      commentAdapter.submitList(comments);
      tvCommentCount.setText(comments.size() + " 评论");
      tvCommentTotal.setText("(" + comments.size() + ")");

      if (comments.isEmpty()) {
        rvComments.setVisibility(View.GONE);
        tvNoComments.setVisibility(View.VISIBLE);
      } else {
        rvComments.setVisibility(View.VISIBLE);
        tvNoComments.setVisibility(View.GONE);
      }
    } else {
      rvComments.setVisibility(View.GONE);
      tvNoComments.setVisibility(View.VISIBLE);
    }
  }

  private String formatTime(long timeStamp) {
    return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
       .format(new java.util.Date(timeStamp));
  }

  private String formatDuration(long durationMillis) {
    long totalSeconds = durationMillis / 1000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
  }

  private void playVideo() {
    if (videoPlayerHelper != null && videoPlayerHelper.getPlayer() != null) {
      videoPlayerHelper.play();
      ivVideoCover.setVisibility(View.GONE);
      ivPlayButton.setVisibility(View.GONE);
      playerView.setVisibility(View.VISIBLE);
    }
  }

  private void pauseVideo() {
    if (videoPlayerHelper != null) {
      videoPlayerHelper.pause();
    }
  }

  @Override
  public void onVideoLoading() {
    Log.d(TAG, "视频加载中...");
  }

  @Override
  public void onVideoReady() {
    Log.d(TAG, "视频准备就绪");
  }

  @Override
  public void onVideoPlaying() {
    Log.d(TAG, "视频播放中");
  }

  @Override
  public void onVideoPaused() {
    Log.d(TAG, "视频已暂停");
  }

  @Override
  public void onVideoEnded() {
    Log.d(TAG, "视频播放结束");
    runOnUiThread(() -> {
      ivVideoCover.setVisibility(View.VISIBLE);
      ivPlayButton.setVisibility(View.VISIBLE);
      playerView.setVisibility(View.GONE);
    });
  }

  @Override
  public void onVideoError(String errorMessage) {
    Log.e(TAG, "视频播放错误: " + errorMessage);
    runOnUiThread(() -> {
      Toast.makeText(this, "视频播放错误: " + errorMessage, Toast.LENGTH_LONG).show();
      ivVideoCover.setVisibility(View.VISIBLE);
      ivPlayButton.setVisibility(View.VISIBLE);
      playerView.setVisibility(View.GONE);
    });
  }

  private void setupClickListeners() {
    // 返回按钮
    toolbar.setNavigationOnClickListener(v -> finish());

    // 发送评论
    btnSend.setOnClickListener(v -> {
      String commentText = etComment.getText().toString().trim();
      if (TextUtils.isEmpty(commentText)) {
        Toast.makeText(this, "请输入评论内容", Toast.LENGTH_SHORT).show();
        return;
      }

      // 这里userId应该从登录信息获取，暂时写死为1
      viewModel.addComment(commentText, 1);
      etComment.setText("");

      // 隐藏软键盘
      hideKeyboard();
    });

    // 更多操作
    btnMore.setOnClickListener(v -> showMoreOptions());
  }

  private void hideKeyboard() {
    View view = this.getCurrentFocus();
    if (view != null) {
      android.view.inputmethod.InputMethodManager imm =
         (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  private void showMoreOptions() {
    String[] options = {"分享", "收藏", "举报"};
    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
    builder.setTitle("选择操作")
       .setItems(options, (dialog, which) -> {
         switch (which) {
           case 0:
             sharePost();
             break;
           case 1:
             collectPost();
             break;
           case 2:
             reportPost();
             break;
         }
       });
    builder.show();
  }

  private void sharePost() {
    Toast.makeText(this, "分享功能", Toast.LENGTH_SHORT).show();
  }

  private void collectPost() {
    Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
  }

  private void reportPost() {
    Toast.makeText(this, "举报成功", Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onPause() {
    super.onPause();
    pauseVideo();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // 这里不需要重新播放，用户需要点击才能播放
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // 释放视频播放器资源
    if (videoPlayerHelper != null) {
      videoPlayerHelper.release();
    }
  }

  @Override
  public void onBackPressed() {
    if (videoPlayerHelper != null && videoPlayerHelper.isPlaying()) {
      // 如果正在播放，暂停播放
      videoPlayerHelper.pause();

      // 显示播放按钮和封面
      ivVideoCover.setVisibility(View.VISIBLE);
      ivPlayButton.setVisibility(View.VISIBLE);
      playerView.setVisibility(View.GONE);
    } else {
      // 使用新的后退处理方式
      super.onBackPressed();
    }
  }
}
package com.example.rednotedemo.presentation.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.rednotedemo.entity.Comment;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;
import com.example.rednotedemo.entity.User;
import com.example.rednotedemo.presentation.view.adapter.CommentAdapter;
import com.example.rednotedemo.presentation.view.adapter.PostImagePagerAdapter;
import com.example.rednotedemo.presentation.viewmodel.PostDetailViewModel;

import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

  private static final String TAG = "PostDetailActivity";

  // 视图组件
  private Toolbar toolbar;
  private ImageView ivAuthorAvatar;
  private TextView tvAuthorName;
  private TextView tvPostTime;
  private ImageButton btnMore;
  private ViewPager2 vpImages;
  private TextView tvImageIndicator;
  private TextView tvTitle;
  private TextView tvContent;
  private TextView tvLikeCount;
  private TextView tvCommentCount;
  private TextView tvCommentTotal;
  private RecyclerView rvComments;
  private TextView tvNoComments;
  private EditText etComment;
  private Button btnSend;
  private LinearLayout llIndicator;

  private PostDetailViewModel viewModel;
  private CommentAdapter commentAdapter;
  private PostImagePagerAdapter imagePagerAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post_detail);

    initViews();
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
    vpImages = findViewById(R.id.vp_images);
    tvImageIndicator = findViewById(R.id.tv_image_indicator);
    llIndicator = findViewById(R.id.ll_indicator);
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

    // 设置帖子ID（这里暂时写死为1，实际应该从Intent获取）
    int postId = getIntent().getIntExtra("POST_ID", 1);
    viewModel.setPostId(postId);

    // 观察帖子数据 - 当帖子数据变化时，同时获取作者信息
    viewModel.getPost().observe(this, post -> {
      if (post != null) {
        bindPostData(post);
        // 获取作者信息
        viewModel.getAuthor(post.getUserId()).observe(this, this::bindAuthorData);
      }
    });

    // 观察图片数据
    viewModel.getPostImages().observe(this, this::bindImageData);

    // 观察评论数据
    viewModel.getComments().observe(this, this::bindCommentData);
  }

  private void bindPostData(Post post) {
    if (post != null) {
      tvTitle.setText(post.getTitle());
      tvContent.setText(post.getContent());
      tvPostTime.setText(formatTime(post.getCreateTime()));
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
      Glide.with(this).load(author.getAvatarUrl()).into(ivAuthorAvatar);
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
    }
  }

  private String formatTime(long timeStamp) {
    return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
       .format(new java.util.Date(timeStamp));
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
}
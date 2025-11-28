package com.example.rednotedemo.presentation.view;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rednotedemo.R;
import com.example.rednotedemo.data.database.AppDatabase;
import com.example.rednotedemo.data.dao.PostDao;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;
import com.example.rednotedemo.presentation.view.adapter.ImageSelectionAdapter;
import com.example.rednotedemo.common.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PublishActivity extends AppCompatActivity {

  private EditText etTitle, etContent;
  private RecyclerView rvImages;
  private Button btnPublish;
  private ImageSelectionAdapter imageAdapter;
  private AppDatabase database;
  private PostDao postDao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_publish);

    // 初始化数据库
    database = AppDatabase.getInstance(this);
    postDao = database.postDao();

    initViews();
    setupImagePicker();
    setupClickListeners();

    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        if (!etTitle.getText().toString().isEmpty() || !imageAdapter.getImageUris().isEmpty()) {
          new AlertDialog.Builder(PublishActivity.this)
             .setTitle("放弃发布？")
             .setMessage("草稿将不会保存")
             .setPositiveButton("放弃", (d, w) -> finish())
             .setNegativeButton("继续编辑", null)
             .show();
        } else {
          finish();
        }
      }
    });
  }

  private void initViews() {
    etTitle = findViewById(R.id.etTitle);
    etContent = findViewById(R.id.etContent);
    rvImages = findViewById(R.id.rvImages);
    btnPublish = findViewById(R.id.btnPublish);
  }

  private void setupImagePicker() {
    imageAdapter = new ImageSelectionAdapter(new ImageSelectionAdapter.OnImageClickListener() {
      @Override
      public void onAddImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 100);
      }

      @Override
      public void onDeleteImage(int position) {
        imageAdapter.removeImage(position);
      }
    });

    rvImages.setLayoutManager(new GridLayoutManager(this, 4));
    rvImages.setAdapter(imageAdapter);
  }

  private void setupClickListeners() {
    btnPublish.setOnClickListener(v -> publishPost());
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
      List<Uri> uris = new ArrayList<>();
      if (data.getClipData() != null) {
        ClipData clipData = data.getClipData();
        for (int i = 0; i < clipData.getItemCount(); i++) {
          uris.add(clipData.getItemAt(i).getUri());
        }
      } else if (data.getData() != null) {
        uris.add(data.getData());
      }
      for (Uri uri : uris) {
        imageAdapter.addImage(uri);
      }
    }
  }

  private void publishPost() {
    String title = etTitle.getText().toString().trim();
    String content = etContent.getText().toString().trim();

    if (title.isEmpty()) {
      Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
      return;
    }

    if (imageAdapter.getImageUris().isEmpty()) {
      Toast.makeText(this, "请至少选择一张图片", Toast.LENGTH_SHORT).show();
      return;
    }

    // 禁用发布按钮，防止重复点击
    btnPublish.setEnabled(false);
    btnPublish.setText("发布中...");

    // 异步保存到数据库
    savePostToDatabase(title, content, imageAdapter.getImageUris());
  }

  /**
   * 异步保存帖子到数据库
   */
  private void savePostToDatabase(String title, String content, List<Uri> imageUris) {
    Executors.newSingleThreadExecutor().execute(() -> {
      try {
        // 1. 创建 Post 对象
        Post post = new Post();
        post.setUserId(5); // 写死用户ID为5
        post.setTitle(title);
        post.setContent(content);
        post.setVideoUrl(null);
        post.setVideo(false);
        post.setCoverUrl(null); // 第一张图片会作为封面
        post.setCreateTime(System.currentTimeMillis());
        post.setUpdateTime(System.currentTimeMillis());

        // 2. 插入 Post 并获取生成的ID
        long postId = postDao.insertPost(post);

        // 3. 处理图片并保存到 PostImage 表
        List<PostImage> postImages = new ArrayList<>();
        for (int i = 0; i < imageUris.size(); i++) {
          Uri imageUri = imageUris.get(i);

          // 保存图片到内部存储并获取文件路径
          String fileName = ImageUtils.generateImageFileName((int) postId, i);
          String imagePath = ImageUtils.saveImageToInternalStorage(this, imageUri, fileName);

          if (imagePath != null) {
            PostImage postImage = new PostImage((int) postId, imagePath, i);
            postImages.add(postImage);

            // 第一张图片设置为封面
            if (i == 0) {
              post.setCoverUrl(imagePath);
              postDao.insertPost(post); // 更新封面URL
            }
          }
        }

        // 4. 批量插入图片记录
        if (!postImages.isEmpty()) {
          postDao.insertPostImages(postImages);
        }

        // 5. 发布成功，返回主线程
        runOnUiThread(() -> {
          Toast.makeText(this, "发布成功！", Toast.LENGTH_SHORT).show();
          setResult(RESULT_OK);
          finish();
        });

      } catch (Exception e) {
        e.printStackTrace();
        // 发布失败
        runOnUiThread(() -> {
          Toast.makeText(this, "发布失败，请重试", Toast.LENGTH_SHORT).show();
          btnPublish.setEnabled(true);
          btnPublish.setText("发布");
        });
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (database != null) {
      database.close();
    }
  }
}
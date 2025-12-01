package com.example.rednotedemo.presentation.view;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rednotedemo.R;
import com.example.rednotedemo.data.database.AppDatabase;
import com.example.rednotedemo.data.dao.PostDao;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;
import com.example.rednotedemo.presentation.view.adapter.ImageSelectionAdapter;
import com.example.rednotedemo.common.util.GiteeImageUploader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PublishActivity extends AppCompatActivity {

    private static final String TAG = "PublishActivity";

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

        // 先上传所有图片，再插入帖子
        uploadImagesFirst(title, content, imageAdapter.getImageUris());
    }

    /**
     * 先上传图片，获取所有图片URL后再插入帖子
     */
    private void uploadImagesFirst(String title, String content, List<Uri> imageUris) {
        // 检查配置是否有效
        if (!GiteeImageUploader.isConfigValid()) {
            Toast.makeText(this, "图片上传配置不完整，请检查配置", Toast.LENGTH_SHORT).show();
            resetPublishButton();
            return;
        }

        GiteeImageUploader uploader = new GiteeImageUploader(this);
        List<String> uploadedImageUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);
        AtomicBoolean hasError = new AtomicBoolean(false);

        Log.d(TAG, "开始上传图片，共 " + imageUris.size() + " 张");

        for (int i = 0; i < imageUris.size(); i++) {
            Uri imageUri = imageUris.get(i);
            // 使用时间戳生成唯一文件名，避免postId未知的问题
            String fileName = GiteeImageUploader.generateImageFileName(System.currentTimeMillis(), i);

            final int currentIndex = i; // 用于在回调中识别图片索引

            uploader.uploadImage(imageUri, fileName, new GiteeImageUploader.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    Log.d(TAG, "图片 " + (currentIndex + 1) + " 上传成功: " + imageUrl);
                    uploadedImageUrls.add(imageUrl);

                    int count = uploadCount.incrementAndGet();
                    Log.d(TAG, "已完成 " + count + "/" + imageUris.size() + " 张图片上传");

                    if (count == imageUris.size() && !hasError.get()) {
                        // 所有图片上传完成，插入帖子
                        Log.d(TAG, "所有图片上传完成，开始插入帖子");
                        insertPostWithImages(title, content, uploadedImageUrls);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "图片 " + (currentIndex + 1) + " 上传失败: " + error);
                    hasError.set(true);

                    runOnUiThread(() -> {
                        Toast.makeText(PublishActivity.this, "图片上传失败: " + error, Toast.LENGTH_SHORT).show();
                        resetPublishButton();
                    });
                }
            });
        }
    }

    /**
     * 插入帖子及其图片记录
     */
    private void insertPostWithImages(String title, String content, List<String> imageUrls) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Log.d(TAG, "开始插入帖子到数据库");

                // 1. 创建 Post 对象
                Post post = new Post();
                post.setUserId(5); // 写死用户ID为5
                post.setTitle(title);
                post.setContent(content);
                post.setVideoUrl(null);
                post.setVideo(false);
                // 第一张图片作为封面
                post.setCoverUrl(imageUrls.isEmpty() ? null : imageUrls.get(0));
                post.setCreateTime(System.currentTimeMillis());
                post.setUpdateTime(System.currentTimeMillis());

                // 2. 插入 Post 并获取生成的ID
                long postId = postDao.insertPost(post);
                Log.d(TAG, "帖子插入成功，ID: " + postId);

                // 3. 创建并插入图片记录
                List<PostImage> postImages = new ArrayList<>();
                for (int i = 0; i < imageUrls.size(); i++) {
                    PostImage postImage = new PostImage((int) postId, imageUrls.get(i), i);
                    postImages.add(postImage);
                    Log.d(TAG, "创建图片记录: postId=" + postId + ", url=" + imageUrls.get(i) + ", order=" + i);
                }

                if (!postImages.isEmpty()) {
                    postDao.insertPostImages(postImages);
                    Log.d(TAG, "图片记录插入成功，共 " + postImages.size() + " 张");
                }

                // 4. 发布成功
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "发布成功！", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                Log.e(TAG, "插入帖子失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "发布失败，请重试", Toast.LENGTH_SHORT).show();
                    resetPublishButton();
                });
            }
        });
    }

    /**
     * 重置发布按钮状态
     */
    private void resetPublishButton() {
        btnPublish.setEnabled(true);
        btnPublish.setText("发布");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}
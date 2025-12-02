package com.example.rednotedemo.presentation.view;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.rednotedemo.R;
import com.example.rednotedemo.common.util.GiteeUploader;
import com.example.rednotedemo.data.database.AppDatabase;
import com.example.rednotedemo.data.dao.PostDao;
import com.example.rednotedemo.entity.Post;
import com.example.rednotedemo.entity.PostImage;
import com.example.rednotedemo.presentation.view.fragment.BasePublishFragment;
import com.example.rednotedemo.presentation.view.fragment.ImagePostFragment;
import com.example.rednotedemo.presentation.view.fragment.VideoPostFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class PublishActivity extends AppCompatActivity implements BasePublishFragment.OnPublishFragmentListener {

    private static final String TAG = "PublishActivity";

    // UI Components
    private EditText etTitle, etContent;
    private Button btnPublish;
    private Toolbar toolbar;
    private RadioGroup rgPostType;

    // Fragments
    private ImagePostFragment imagePostFragment;
    private VideoPostFragment videoPostFragment;
    private Fragment currentFragment;

    // Database
    private AppDatabase database;
    private PostDao postDao;

    // Fragment tags
    private static final String TAG_IMAGE_FRAGMENT = "ImagePostFragment";
    private static final String TAG_VIDEO_FRAGMENT = "VideoPostFragment";

    // 用于上传的临时变量
    private String coverFileName = null;
    private String videoFileName = null;

    // 重试机制相关
    private Map<Integer, Integer> retryCountMap = new HashMap<>(); // key: 图片索引, value: 重试次数
    private Map<Integer, Long> uploadTimestamps = new HashMap<>(); // key: 图片索引, value: 时间戳

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        // 初始化数据库
        database = AppDatabase.getInstance(this);
        postDao = database.postDao();

        initViews();
        setupFragments();
        setupClickListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                checkDraftAndExit();
            }
        });
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnPublish = findViewById(R.id.btnPublish);
        toolbar = findViewById(R.id.toolbar);
        rgPostType = findViewById(R.id.rgPostType);

        // 设置工具栏
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("发布笔记");
        }

        // 监听标题输入变化
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePublishButtonState();
            }
        });
    }

    private void setupFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // 尝试恢复现有的Fragment
        imagePostFragment = (ImagePostFragment) fragmentManager.findFragmentByTag(TAG_IMAGE_FRAGMENT);
        videoPostFragment = (VideoPostFragment) fragmentManager.findFragmentByTag(TAG_VIDEO_FRAGMENT);

        // 如果不存在，创建新的Fragment
        if (imagePostFragment == null) {
            imagePostFragment = new ImagePostFragment();
        }
        if (videoPostFragment == null) {
            videoPostFragment = new VideoPostFragment();
        }

        // 设置监听器
        imagePostFragment.setListener(this);
        videoPostFragment.setListener(this);

        // 默认显示图文帖子Fragment
        showFragment(imagePostFragment, TAG_IMAGE_FRAGMENT);
        currentFragment = imagePostFragment;

        // 默认选中图文帖子
        rgPostType.check(R.id.rbImagePost);
    }

    private void showFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 隐藏所有Fragment
        if (imagePostFragment.isAdded()) {
            transaction.hide(imagePostFragment);
        }
        if (videoPostFragment.isAdded()) {
            transaction.hide(videoPostFragment);
        }

        // 显示目标Fragment
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragmentContainer, fragment, tag);
        }

        // 提交事务
        transaction.commitNowAllowingStateLoss();
        currentFragment = fragment;
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> checkDraftAndExit());
        btnPublish.setOnClickListener(v -> publishPost());

        // 帖子类型切换监听
        rgPostType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbImagePost) {
                showFragment(imagePostFragment, TAG_IMAGE_FRAGMENT);
            } else if (checkedId == R.id.rbVideoPost) {
                showFragment(videoPostFragment, TAG_VIDEO_FRAGMENT);
            }
        });
    }

    private void publishPost() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = rgPostType.getCheckedRadioButtonId();

        if (checkedId == R.id.rbImagePost) {
            if (!imagePostFragment.validateInput()) {
                Toast.makeText(this, "请至少选择一张图片", Toast.LENGTH_SHORT).show();
                return;
            }
            imagePostFragment.prepareForPublish();
            publishImagePost(title, content);
        } else if (checkedId == R.id.rbVideoPost) {
            if (!videoPostFragment.validateInput()) {
                Toast.makeText(this, "请选择一个视频", Toast.LENGTH_SHORT).show();
                return;
            }
            videoPostFragment.prepareForPublish();
            publishVideoPost(title, content);
        } else {
            Toast.makeText(this, "请选择帖子类型", Toast.LENGTH_SHORT).show();
            return;
        }

        // 禁用发布按钮
        btnPublish.setEnabled(false);
        btnPublish.setText("发布中...");

        // 重置重试计数和时间戳
        retryCountMap.clear();
        uploadTimestamps.clear();
    }

    // ========== Fragment 监听器方法 ==========
    @Override
    public void onFragmentDataChanged() {
        updatePublishButtonState();
    }

    private void updatePublishButtonState() {
        boolean isEnabled = true;

        // 检查标题
        if (etTitle.getText().toString().trim().isEmpty()) {
            isEnabled = false;
        }

        // 检查当前Fragment的验证
        if (currentFragment instanceof ImagePostFragment) {
            isEnabled = isEnabled && imagePostFragment.validateInput();
        } else if (currentFragment instanceof VideoPostFragment) {
            isEnabled = isEnabled && videoPostFragment.validateInput();
        }

        btnPublish.setEnabled(isEnabled);
    }

    // ========== 上传和发布逻辑 ==========

    private void publishImagePost(String title, String content) {
        List<Uri> imageUris = imagePostFragment.getImageUris();
        Uri coverUri = imagePostFragment.getSelectedCoverUri();
        boolean isCustomCover = imagePostFragment.isCustomCover();

        uploadImagesFirst(title, content, imageUris, coverUri, isCustomCover);
    }

    private void publishVideoPost(String title, String content) {
        Uri videoUri = videoPostFragment.getSelectedVideoUri();
        Uri coverUri = videoPostFragment.getSelectedCoverUri();
        Bitmap coverBitmap = videoPostFragment.getVideoCoverBitmap();
        boolean isCustomCover = videoPostFragment.isCustomCover();
        long videoDuration = videoPostFragment.getVideoDuration();

        uploadVideoAndCover(title, content, videoUri, coverUri, coverBitmap, videoDuration, isCustomCover);
    }

    private void uploadImagesFirst(String title, String content, List<Uri> imageUris,
                                   Uri coverUri, boolean isCustomCover) {
        // 检查配置是否有效
        if (!GiteeUploader.isConfigValid()) {
            Toast.makeText(this, "图片上传配置不完整，请检查配置", Toast.LENGTH_SHORT).show();
            resetPublishButton();
            return;
        }

        GiteeUploader uploader = new GiteeUploader(this);
        List<String> uploadedImageUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);
        AtomicBoolean hasError = new AtomicBoolean(false);
        final String[] customCoverUrl = new String[1]; // 用于存储封面URL

        Log.d(TAG, "开始上传图片，共 " + imageUris.size() + " 张");

        // 先上传封面（如果有自定义封面）
        if (isCustomCover && coverUri != null) {
            long timestamp = System.currentTimeMillis();
            coverFileName = GiteeUploader.generateCoverFileName(timestamp);

            uploader.uploadCover(coverUri, coverFileName, new GiteeUploader.UploadCallback() {
                @Override
                public void onSuccess(String url) {
                    customCoverUrl[0] = url;
                    Log.d(TAG, "自定义封面上传成功: " + url);
                    // 继续上传其他图片
                    uploadOtherImages(title, content, imageUris, uploadedImageUrls, uploadCount, hasError, customCoverUrl);
                }

                @Override
                public void onFailure(String error) {
                    Log.e(TAG, "自定义封面上传失败: " + error);
                    hasError.set(true);
                    runOnUiThread(() -> {
                        Toast.makeText(PublishActivity.this, "封面上传失败: " + error, Toast.LENGTH_SHORT).show();
                        resetPublishButton();
                    });
                }
            });
        } else {
            // 没有自定义封面，直接上传所有图片
            uploadOtherImages(title, content, imageUris, uploadedImageUrls, uploadCount, hasError, customCoverUrl);
        }
    }

    private void uploadOtherImages(String title, String content, List<Uri> imageUris,
                                   List<String> uploadedImageUrls, AtomicInteger uploadCount,
                                   AtomicBoolean hasError, final String[] customCoverUrl) {

        // 为每张图片生成初始时间戳
        long baseTimestamp = System.currentTimeMillis();

        for (int i = 0; i < imageUris.size(); i++) {
            uploadTimestamps.put(i, baseTimestamp);
            retryCountMap.put(i, 0); // 初始化重试次数为0
        }

        for (int i = 0; i < imageUris.size(); i++) {
            Uri imageUri = imageUris.get(i);
            long timestamp = uploadTimestamps.get(i);

            // 生成初始文件名
            String fileName = GiteeUploader.generateImageFileName(timestamp, i);

            final int currentIndex = i;

            // 开始上传，传入初始重试次数0
            uploadImageWithRetry(imageUri, fileName, currentIndex, title, content,
                    imageUris, uploadedImageUrls, uploadCount, hasError, customCoverUrl);
        }
    }

    /**
     * 带重试机制的图片上传方法
     */
    private void uploadImageWithRetry(Uri imageUri, String fileName, int currentIndex,
                                      String title, String content, List<Uri> imageUris,
                                      List<String> uploadedImageUrls, AtomicInteger uploadCount,
                                      AtomicBoolean hasError, final String[] customCoverUrl) {

        GiteeUploader uploader = new GiteeUploader(this);
        int retryCount = retryCountMap.getOrDefault(currentIndex, 0);

        // 更新文件名：每次重试都添加重试标记
        String finalFileName;
        if (retryCount == 0) {
            finalFileName = fileName;
        } else {
            // 重试时，修改文件名
            long originalTimestamp = uploadTimestamps.get(currentIndex);
            // 在文件名中添加重试标记
            finalFileName = GiteeUploader.generateImageFileName(originalTimestamp, currentIndex)
                    .replace(".jpg", "_retry" + retryCount + ".jpg");
        }

        Log.d(TAG, String.format("开始上传图片 %d (重试次数: %d, 文件名: %s)",
                currentIndex + 1, retryCount, finalFileName));

        uploader.uploadImage(imageUri, finalFileName, new GiteeUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "图片 " + (currentIndex + 1) + " 上传成功: " + imageUrl);
                uploadedImageUrls.add(imageUrl);

                // 如果没有自定义封面，第一张图片作为封面
                if (customCoverUrl[0] == null && currentIndex == 0) {
                    customCoverUrl[0] = imageUrl;
                }

                int count = uploadCount.incrementAndGet();
                Log.d(TAG, "已完成 " + count + "/" + imageUris.size() + " 张图片上传");

                // 清除该图片的重试计数
                retryCountMap.remove(currentIndex);

                if (count == imageUris.size() && !hasError.get()) {
                    // 所有图片上传完成，插入帖子
                    Log.d(TAG, "所有图片上传完成，开始插入帖子");
                    insertPostWithImages(title, content, uploadedImageUrls, customCoverUrl[0]);
                }
            }

            // 在 onFailure 方法中修复：
            @Override
            public void onFailure(String error) {
                // 获取当前重试次数
                int currentRetryCount = retryCountMap.getOrDefault(currentIndex, 0);
                Log.e(TAG, "图片 " + (currentIndex + 1) + " 上传失败: " + error + " (重试次数:" + currentRetryCount + ")");

                // 重试机制：最多重试3次
                if (currentRetryCount < 3) {
                    // 创建最终的局部变量副本
                    final int finalRetryCount = currentRetryCount;
                    final int finalIndex = currentIndex;
                    final Uri finalUri = imageUri;

                    // 延迟后重试
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!hasError.get()) { // 只在没有全局错误时重试
                            // 递增重试次数
                            int newRetryCount = finalRetryCount + 1;
                            retryCountMap.put(finalIndex, newRetryCount);

                            // 生成新的文件名
                            long timestamp = System.currentTimeMillis(); // 使用新的时间戳
                            uploadTimestamps.put(finalIndex, timestamp);

                            String newFileName = GiteeUploader.generateImageFileName(timestamp, finalIndex)
                                    .replace(".jpg", "_retry" + newRetryCount + ".jpg");

                            uploadImageWithRetry(finalUri, newFileName, finalIndex, title, content,
                                    imageUris, uploadedImageUrls, uploadCount, hasError, customCoverUrl);
                        }
                    }, (currentRetryCount + 1) * 1000L);
                } else {
                    // 超过最大重试次数
                    hasError.set(true);
                    runOnUiThread(() -> {
                        Toast.makeText(PublishActivity.this,
                                String.format("图片 %d 上传失败，已重试3次", currentIndex + 1),
                                Toast.LENGTH_SHORT).show();
                        resetPublishButton();
                    });
                }
            }
        });
    }

    private void uploadVideoAndCover(String title, String content, Uri videoUri,
                                     Uri coverUri, Bitmap coverBitmap, long duration,
                                     boolean isCustomCover) {
        // 先上传封面
        if (isCustomCover && coverUri != null) {
            // 上传自定义封面
            uploadCustomCover(title, content, coverUri, duration);
        } else if (coverBitmap != null) {
            // 上传提取的视频封面
            uploadVideoCover(title, content, coverBitmap, duration);
        } else {
            // 没有封面，重新提取
            runOnUiThread(() -> {
                Toast.makeText(this, "请选择或提取封面", Toast.LENGTH_SHORT).show();
                resetPublishButton();
            });
        }
    }

    private void uploadCustomCover(String title, String content, Uri coverUri, long duration) {
        if (!GiteeUploader.isConfigValid()) {
            Toast.makeText(this, "上传配置不完整", Toast.LENGTH_SHORT).show();
            resetPublishButton();
            return;
        }

        GiteeUploader uploader = new GiteeUploader(this);
        long timestamp = System.currentTimeMillis();
        coverFileName = GiteeUploader.generateCoverFileName(timestamp);

        uploader.uploadCover(coverUri, coverFileName, new GiteeUploader.UploadCallback() {
            @Override
            public void onSuccess(String coverUrl) {
                Log.d(TAG, "自定义封面上传成功: " + coverUrl);
                // 封面上传成功后上传视频
                Uri videoUri = videoPostFragment.getSelectedVideoUri();
                uploadVideoFile(title, content, coverUrl, videoUri, duration);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "自定义封面上传失败: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "封面上传失败: " + error, Toast.LENGTH_SHORT).show();
                    resetPublishButton();
                });
            }
        });
    }

    private void uploadVideoCover(String title, String content, Bitmap coverBitmap, long duration) {
        if (!GiteeUploader.isConfigValid()) {
            Toast.makeText(this, "上传配置不完整", Toast.LENGTH_SHORT).show();
            resetPublishButton();
            return;
        }

        GiteeUploader uploader = new GiteeUploader(this);
        long timestamp = System.currentTimeMillis();
        coverFileName = GiteeUploader.generateCoverFileName(timestamp);

        uploader.uploadCover(coverBitmap, coverFileName, new GiteeUploader.UploadCallback() {
            @Override
            public void onSuccess(String coverUrl) {
                Log.d(TAG, "视频封面上传成功: " + coverUrl);
                // 封面上传成功后上传视频
                Uri videoUri = videoPostFragment.getSelectedVideoUri();
                uploadVideoFile(title, content, coverUrl, videoUri, duration);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "视频封面上传失败: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "封面上传失败: " + error, Toast.LENGTH_SHORT).show();
                    resetPublishButton();
                });
            }
        });
    }

    private void uploadVideoFile(String title, String content, String coverUrl, Uri videoUri, long duration) {
        GiteeUploader uploader = new GiteeUploader(this);
        long timestamp = System.currentTimeMillis();
        videoFileName = GiteeUploader.generateVideoFileName(timestamp);

        uploader.uploadVideo(videoUri, videoFileName, new GiteeUploader.UploadCallback() {
            @Override
            public void onSuccess(String videoUrl) {
                Log.d(TAG, "视频上传成功: " + videoUrl);
                // 视频上传成功，插入帖子
                insertVideoPost(title, content, videoUrl, coverUrl, duration);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "视频上传失败: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "视频上传失败: " + error, Toast.LENGTH_SHORT).show();
                    resetPublishButton();
                });
            }
        });
    }

    private void insertPostWithImages(String title, String content, List<String> imageUrls, String coverUrl) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Log.d(TAG, "开始插入帖子到数据库");

                // 确保数据库连接有效
                if (database == null || !database.isOpen()) {
                    Log.w(TAG, "数据库未初始化或已关闭，重新初始化");
                    database = AppDatabase.getInstance(PublishActivity.this);
                    postDao = database.postDao();
                }

                // 1. 创建Post对象
                Post post = new Post();
                post.setUserId(2); // 写死用户ID
                post.setTitle(title);
                post.setContent(content);
                post.setVideoUrl(null);
                post.setVideo(false);
                post.setCoverUrl(coverUrl);
                post.setCreateTime(System.currentTimeMillis());
                post.setUpdateTime(System.currentTimeMillis());
                post.setVideoDuration(0);

                // 2. 插入Post并获取生成的ID
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

    private void insertVideoPost(String title, String content, String videoUrl, String coverUrl, long duration) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Log.d(TAG, "开始插入视频帖子到数据库");

                // 确保数据库连接有效
                if (database == null || !database.isOpen()) {
                    Log.w(TAG, "数据库未初始化或已关闭，重新初始化");
                    database = AppDatabase.getInstance(PublishActivity.this);
                    postDao = database.postDao();
                }

                // 创建视频Post对象
                Post post = new Post();
                post.setUserId(3); // 写死用户ID
                post.setTitle(title);
                post.setContent(content);
                post.setVideoUrl(videoUrl);
                post.setVideo(true);
                post.setCoverUrl(coverUrl);
                post.setVideoDuration(duration);
                post.setCreateTime(System.currentTimeMillis());
                post.setUpdateTime(System.currentTimeMillis());

                // 插入Post
                long postId = postDao.insertPost(post);
                Log.d(TAG, "视频帖子插入成功，ID: " + postId);

                // 发布成功
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "视频发布成功！", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {
                Log.e(TAG, "插入视频帖子失败", e);
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "视频发布失败，请重试", Toast.LENGTH_SHORT).show();
                    resetPublishButton();
                });
            }
        });
    }

    private void checkDraftAndExit() {
        boolean hasDraft = !etTitle.getText().toString().isEmpty() ||
                !etContent.getText().toString().isEmpty() ||
                !imagePostFragment.getImageUris().isEmpty() ||
                videoPostFragment.getSelectedVideoUri() != null;

        if (hasDraft) {
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

    /**
     * 重置发布按钮状态
     */
    private void resetPublishButton() {
        btnPublish.setEnabled(true);
        btnPublish.setText("发布");
        // 清空重试计数和时间戳
        retryCountMap.clear();
        uploadTimestamps.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清空重试计数和时间戳
        retryCountMap.clear();
        uploadTimestamps.clear();
    }
}
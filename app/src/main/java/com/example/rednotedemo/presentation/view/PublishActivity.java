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
import com.example.rednotedemo.presentation.view.adapter.ImageSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// PublishActivity.java
public class PublishActivity extends AppCompatActivity {

  private EditText etTitle, etContent;
  private RecyclerView rvImages;
  private Button btnPublish;
  private ImageSelectionAdapter imageAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_publish); // 注意：是 activity_ 开头

    initViews();
    setupImagePicker();
    setupClickListeners();
    // ✅ 使用 OnBackPressedDispatcher 处理返回键
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
    if (title.isEmpty()) {
      Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
      return;
    }

    if (imageAdapter.getImageUris().isEmpty()) {
      Toast.makeText(this, "请至少选择一张图片", Toast.LENGTH_SHORT).show();
      return;
    }

    // TODO: 保存到数据库
    // insertToDatabase(...);

    setResult(RESULT_OK); // 可选：通知上一页刷新
    finish(); // 关闭当前 Activity，返回上一页
  }
}

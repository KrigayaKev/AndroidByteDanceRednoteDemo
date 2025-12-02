// VideoPostFragment.java
package com.example.rednotedemo.presentation.view.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rednotedemo.R;
import com.example.rednotedemo.common.util.GiteeUploader;
import com.example.rednotedemo.common.util.VideoUtils;

public class VideoPostFragment extends BasePublishFragment {

  private static final int REQUEST_PICK_VIDEO = 100;
  private static final int REQUEST_PICK_COVER = 101;

  // UI Components
  private ImageView ivVideoPreview;
  private Button btnSelectVideo;
  private TextView tvVideoInfo;
  private TextView tvVideoCoverHint;
  private LinearLayout llVideoCoverContainer;
  private ImageView ivCoverPreview;
  private Button btnSelectCover, btnExtractCover;
  private TextView tvCoverInfo;

  // Data
  private Uri selectedVideoUri = null;
  private Uri selectedCoverUri = null;
  private Bitmap videoCoverBitmap = null;
  private String videoFileName = null;
  private String coverFileName = null;
  private long videoDuration = 0;
  private boolean isCustomCover = false;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_video_post, container, false);
  }

  @Override
  protected void setupViews(View rootView) {
    ivVideoPreview = rootView.findViewById(R.id.ivVideoPreview);
    btnSelectVideo = rootView.findViewById(R.id.btnSelectVideo);
    tvVideoInfo = rootView.findViewById(R.id.tvVideoInfo);
    tvVideoCoverHint = rootView.findViewById(R.id.tvVideoCoverHint);
    llVideoCoverContainer = rootView.findViewById(R.id.llVideoCoverContainer);
    ivCoverPreview = rootView.findViewById(R.id.ivCoverPreview);
    btnSelectCover = rootView.findViewById(R.id.btnSelectCover);
    btnExtractCover = rootView.findViewById(R.id.btnExtractCover);
    tvCoverInfo = rootView.findViewById(R.id.tvCoverInfo);

    setupClickListeners();
  }

  private void setupClickListeners() {
    btnSelectVideo.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
      startActivityForResult(intent, REQUEST_PICK_VIDEO);
    });

    btnSelectCover.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      intent.setType("image/*");
      startActivityForResult(intent, REQUEST_PICK_COVER);
    });

    btnExtractCover.setOnClickListener(v -> {
      if (selectedVideoUri != null) {
        extractVideoCover();
      } else {
        Toast.makeText(getContext(), "请先选择视频", Toast.LENGTH_SHORT).show();
      }
    });

    ivVideoPreview.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
      startActivityForResult(intent, REQUEST_PICK_VIDEO);
    });

    ivCoverPreview.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      intent.setType("image/*");
      startActivityForResult(intent, REQUEST_PICK_COVER);
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode != getActivity().RESULT_OK || data == null) return;

    if (requestCode == REQUEST_PICK_VIDEO) {
      handleVideoSelection(data);
    } else if (requestCode == REQUEST_PICK_COVER) {
      handleCoverSelection(data);
    }
  }

  private void handleVideoSelection(Intent data) {
    selectedVideoUri = data.getData();
    if (selectedVideoUri != null) {
      extractVideoCover();
      notifyDataChanged();
    }
  }

  private void handleCoverSelection(Intent data) {
    selectedCoverUri = data.getData();
    if (selectedCoverUri != null) {
      ivCoverPreview.setImageURI(selectedCoverUri);
      videoCoverBitmap = null;
      isCustomCover = true;
      tvCoverInfo.setText("已选择自定义封面");
      notifyDataChanged();
    }
  }

  private void extractVideoCover() {
    VideoUtils.extractVideoFirstFrame(getContext(), selectedVideoUri, new VideoUtils.VideoFrameCallback() {
      @Override
      public void onFrameExtracted(Bitmap frameBitmap, long duration) {
        if (getActivity() != null) {
          getActivity().runOnUiThread(() -> {
            ivVideoPreview.setImageBitmap(frameBitmap);
            ivCoverPreview.setImageBitmap(frameBitmap);
            videoCoverBitmap = frameBitmap;
            videoDuration = duration;
            isCustomCover = false;

            // 生成文件名
            long timestamp = System.currentTimeMillis();
            videoFileName = GiteeUploader.generateVideoFileName(timestamp);
            coverFileName = GiteeUploader.generateCoverFileName(timestamp);

            tvVideoInfo.setText("视频已选择");
            tvCoverInfo.setText("已提取视频第一帧作为封面");

            // 显示封面容器
            llVideoCoverContainer.setVisibility(View.VISIBLE);
            notifyDataChanged();
          });
        }
      }

      @Override
      public void onFrameExtractFailed(String error) {
        if (getActivity() != null) {
          getActivity().runOnUiThread(() -> {
            Toast.makeText(getContext(), "无法提取视频封面: " + error, Toast.LENGTH_SHORT).show();
            clearSelection();
          });
        }
      }
    });
  }

  public Uri getSelectedVideoUri() {
    return selectedVideoUri;
  }

  public Uri getSelectedCoverUri() {
    return selectedCoverUri;
  }

  public Bitmap getVideoCoverBitmap() {
    return videoCoverBitmap;
  }

  public String getVideoFileName() {
    return videoFileName;
  }

  public String getCoverFileName() {
    return coverFileName;
  }

  public long getVideoDuration() {
    return videoDuration;
  }

  public boolean isCustomCover() {
    return isCustomCover;
  }

  @Override
  public boolean validateInput() {
    // 只返回验证结果，不回调！
    return selectedVideoUri != null;
  }

  @Override
  public void clearSelection() {
    selectedVideoUri = null;
    selectedCoverUri = null;
    videoCoverBitmap = null;
    videoFileName = null;
    coverFileName = null;
    videoDuration = 0;
    isCustomCover = false;

    if (ivVideoPreview != null) {
      ivVideoPreview.setImageResource(R.drawable.ic_video_placeholder);
    }
    if (ivCoverPreview != null) {
      ivCoverPreview.setImageResource(R.drawable.ic_cover_placeholder);
    }
    if (tvVideoInfo != null) {
      tvVideoInfo.setText("点击选择视频文件");
    }
    if (tvCoverInfo != null) {
      tvCoverInfo.setText("请选择封面或提取视频帧");
    }
    if (llVideoCoverContainer != null) {
      llVideoCoverContainer.setVisibility(View.GONE);
    }
  }

  @Override
  public void prepareForPublish() {
    // 对于视频帖子，如果没有选择封面，提取第一帧
    if (selectedCoverUri == null && videoCoverBitmap == null && selectedVideoUri != null) {
      // 在实际发布时会处理
    }
  }

  private void notifyDataChanged() {
    if (listener != null) {
      listener.onFragmentDataChanged();
    }
  }
}
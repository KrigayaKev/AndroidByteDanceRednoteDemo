package com.example.rednotedemo.presentation.view.fragment;

import android.content.ClipData;
import android.content.Intent;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rednotedemo.R;
import com.example.rednotedemo.presentation.view.adapter.ImageSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImagePostFragment extends BasePublishFragment {

  private static final int REQUEST_PICK_IMAGES = 100;
  private static final int REQUEST_PICK_COVER_FOR_IMAGE = 101;

  // UI Components
  private RecyclerView rvImages;
  private TextView tvImageCoverHint;
  private LinearLayout llImageCoverContainer;
  private ImageView ivImageCoverPreview;
  private Button btnImageCover;

  // Adapter and Data
  private ImageSelectionAdapter imageAdapter;
  private Uri selectedCoverUri = null;
  private boolean isCustomCover = false;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_image_post, container, false);
    return view;
  }

  @Override
  protected void setupViews(View rootView) {
    rvImages = rootView.findViewById(R.id.rvImages);
    tvImageCoverHint = rootView.findViewById(R.id.tvImageCoverHint);
    llImageCoverContainer = rootView.findViewById(R.id.llImageCoverContainer);
    ivImageCoverPreview = rootView.findViewById(R.id.ivImageCoverPreview);
    btnImageCover = rootView.findViewById(R.id.btnImageCover);

    setupImagePicker();
    setupClickListeners();
    updateImageCoverHint();
  }

  private void setupImagePicker() {
    // 设置LayoutManager
    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
    rvImages.setLayoutManager(layoutManager);

    // 创建Adapter
    imageAdapter = new ImageSelectionAdapter(new ImageSelectionAdapter.OnImageClickListener() {
      @Override
      public void onAddImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_PICK_IMAGES);
      }

      @Override
      public void onDeleteImage(int position) {
        if (imageAdapter != null) {
          imageAdapter.removeImage(position);
          updateImageCoverHint();
          notifyDataChanged();
        }
      }
    });

    // 设置Adapter
    rvImages.setAdapter(imageAdapter);
  }

  private void setupClickListeners() {
    if (btnImageCover != null) {
      btnImageCover.setOnClickListener(v -> {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_COVER_FOR_IMAGE);
      });
    }

    if (ivImageCoverPreview != null) {
      ivImageCoverPreview.setOnClickListener(v -> {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_COVER_FOR_IMAGE);
      });
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode != getActivity().RESULT_OK || data == null) return;

    if (requestCode == REQUEST_PICK_IMAGES) {
      handleImageSelection(data);
    } else if (requestCode == REQUEST_PICK_COVER_FOR_IMAGE) {
      handleCoverSelection(data);
    }
  }

  private void handleImageSelection(Intent data) {
    List<Uri> uris = new ArrayList<>();
    if (data.getClipData() != null) {
      ClipData clipData = data.getClipData();
      for (int i = 0; i < clipData.getItemCount(); i++) {
        uris.add(clipData.getItemAt(i).getUri());
      }
    } else if (data.getData() != null) {
      uris.add(data.getData());
    }

    if (imageAdapter != null) {
      for (Uri uri : uris) {
        if (imageAdapter.canAddMoreImages()) {
          imageAdapter.addImage(uri);
        }
      }

      updateImageCoverHint();
      notifyDataChanged();
    }
  }

  private void handleCoverSelection(Intent data) {
    selectedCoverUri = data.getData();
    if (selectedCoverUri != null && ivImageCoverPreview != null) {
      ivImageCoverPreview.setImageURI(selectedCoverUri);
      isCustomCover = true;
      tvImageCoverHint.setText("已选择自定义封面");
      llImageCoverContainer.setVisibility(View.VISIBLE);
      notifyDataChanged();
    }
  }

  private void updateImageCoverHint() {
    if (tvImageCoverHint == null || llImageCoverContainer == null) {
      return;
    }

    if (imageAdapter == null || imageAdapter.getCurrentImageCount() == 0) {
      tvImageCoverHint.setText("请先选择图片");
      llImageCoverContainer.setVisibility(View.GONE);
    } else {
      tvImageCoverHint.setText("封面将使用第一张图片，如需自定义请选择封面");
      if (selectedCoverUri != null || (ivImageCoverPreview != null && ivImageCoverPreview.getDrawable() != null)) {
        llImageCoverContainer.setVisibility(View.VISIBLE);
      } else {
        llImageCoverContainer.setVisibility(View.GONE);
      }
    }
  }

  public List<Uri> getImageUris() {
    if (imageAdapter != null) {
      return imageAdapter.getImageUris();
    }
    return new ArrayList<>();
  }

  public Uri getSelectedCoverUri() {
    return selectedCoverUri;
  }

  public boolean isCustomCover() {
    return isCustomCover;
  }

  @Override
  public boolean validateInput() {
    // 只返回验证结果，不回调！
    return getImageUris().size() > 0;
  }

  @Override
  public void clearSelection() {
    if (imageAdapter != null) {
      imageAdapter.clearImages();
    }
    selectedCoverUri = null;
    isCustomCover = false;
    if (ivImageCoverPreview != null) {
      ivImageCoverPreview.setImageResource(R.drawable.ic_cover_placeholder);
    }
    updateImageCoverHint();
    if (llImageCoverContainer != null) {
      llImageCoverContainer.setVisibility(View.GONE);
    }
  }

  @Override
  public void prepareForPublish() {
    // 对于图文帖子，如果没有选择自定义封面，使用第一张图片作为封面
    if (selectedCoverUri == null && !getImageUris().isEmpty()) {
      selectedCoverUri = getImageUris().get(0);
      isCustomCover = false;
    }
  }

  private void notifyDataChanged() {
    if (listener != null) {
      listener.onFragmentDataChanged();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }
}
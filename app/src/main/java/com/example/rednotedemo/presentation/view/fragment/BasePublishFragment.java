// BasePublishFragment.java
package com.example.rednotedemo.presentation.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rednotedemo.R;
import com.example.rednotedemo.presentation.view.PublishActivity;

public abstract class BasePublishFragment extends Fragment {

  protected PublishActivity parentActivity;
  protected OnPublishFragmentListener listener;

  public interface OnPublishFragmentListener {
    void onFragmentDataChanged();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getActivity() instanceof PublishActivity) {
      parentActivity = (PublishActivity) getActivity();
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupViews(view);
  }

  protected abstract void setupViews(View rootView);

  public abstract boolean validateInput();

  public abstract void clearSelection();

  public abstract void prepareForPublish();

  public void setListener(OnPublishFragmentListener listener) {
    this.listener = listener;
  }
}
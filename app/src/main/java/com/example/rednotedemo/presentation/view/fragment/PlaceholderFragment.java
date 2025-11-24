package com.example.rednotedemo.presentation.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rednotedemo.R;

public class PlaceholderFragment extends Fragment {

  private String title = "功能暂未实现";

  public static PlaceholderFragment newInstance(String title) {
    PlaceholderFragment fragment = new PlaceholderFragment();
    Bundle args = new Bundle();
    args.putString("title", title);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      title = getArguments().getString("title", "功能暂未实现");
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
    TextView textView = view.findViewById(R.id.placeholder_text);
    textView.setText(title);
    return view;
  }
}
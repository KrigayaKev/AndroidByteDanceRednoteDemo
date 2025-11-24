package com.example.rednotedemo.presentation.view.adapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

  private final List<Fragment> fragments;

  public MainPagerAdapter(@NonNull FragmentActivity fa, List<Fragment> fragments) {
    super(fa);
    this.fragments = fragments;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return fragments.get(position);
  }

  @Override
  public int getItemCount() {
    return fragments.size(); // 固定为 2
  }
}

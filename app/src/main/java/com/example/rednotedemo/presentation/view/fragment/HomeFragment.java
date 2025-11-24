package com.example.rednotedemo.presentation.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.example.rednotedemo.R;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.presentation.view.adapter.PostListAdapter;
import com.example.rednotedemo.presentation.viewmodel.HomeViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class HomeFragment extends Fragment {

  private RecyclerView recyclerView;
  private PostListAdapter adapter;
  private HomeViewModel viewModel;
  private SwipeRefreshLayout swipeRefreshLayout;
  private static final int TAB_FOLLOW = 1;      // 关注
  private static final int TAB_DISCOVER = 2;    // 发现（默认）
  private static final int TAB_GUANGZHOU = 3;   // 广州
  

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_home, container, false);

    // 初始化 TabLayout
    TabLayout tabLayout = view.findViewById(R.id.tabLayout);
    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_horizontal_lines));
    tabLayout.addTab(tabLayout.newTab().setText("关注"));
    tabLayout.addTab(tabLayout.newTab().setText("发现"));
    tabLayout.addTab(tabLayout.newTab().setText("广州"));
    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_search));



    // 默认选中“发现”
    tabLayout.getTabAt(TAB_DISCOVER).select();

    // 初始化视图
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    // 初始化 RecyclerView
    RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

    // 初始化 Adapter
    adapter = new PostListAdapter(new ArrayList<>());
    recyclerView.setAdapter(adapter);

    // 获取 ViewModel
    viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

    // 观察数据变化
    viewModel.getPosts().observe(getViewLifecycleOwner(), resource -> {
      if (resource.isLoading) {
        swipeRefreshLayout.setRefreshing(true);
      } else {
        swipeRefreshLayout.setRefreshing(false);
        if (resource.data != null) {
          adapter.updateData(resource.data);
        }
        // TODO可选：处理错误 resource.error
      }
    });

    // 下拉刷新监听
    swipeRefreshLayout.setOnRefreshListener(() -> {
      viewModel.loadPosts(); // 触发重新加载
    });

    // 加载数据
    viewModel.loadPosts();
    return view;
  }
}

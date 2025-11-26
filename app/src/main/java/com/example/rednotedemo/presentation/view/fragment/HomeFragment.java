package com.example.rednotedemo.presentation.view.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingData;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.rednotedemo.R;
import com.example.rednotedemo.enums.FilterType;
import com.example.rednotedemo.entity.vo.PostListItemVO;
import com.example.rednotedemo.presentation.view.adapter.PostListAdapter;
import com.example.rednotedemo.presentation.viewmodel.HomeViewModel;
import com.example.rednotedemo.util.FlowCollectorKt;
import com.google.android.material.tabs.TabLayout;

import kotlin.Unit;

public class HomeFragment extends Fragment {

  private RecyclerView recyclerView;
  private PostListAdapter adapter;
  private SwipeRefreshLayout swipeRefreshLayout;
  private TabLayout tabLayout;
  private HomeViewModel viewModel;
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_home, container, false);

    tabLayout = view.findViewById(R.id.tabLayout);
    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_horizontal_lines));
    tabLayout.addTab(tabLayout.newTab().setText("关注"));
    tabLayout.addTab(tabLayout.newTab().setText("发现"));
    tabLayout.addTab(tabLayout.newTab().setText("广州"));
    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_search));
    tabLayout.getTabAt(2).select(); // 默认选中“发现”

    recyclerView = view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

    adapter = new PostListAdapter();
    recyclerView.setAdapter(adapter);

    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    // 初始化 ViewModel（固定使用 DISCOVER）
    viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
      @NonNull
      @Override
      public <T extends androidx.lifecycle.ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(FilterType.DISCOVER);
      }
    }).get(HomeViewModel.class);
    

    // 下拉刷新
    swipeRefreshLayout.setOnRefreshListener(() -> {
      adapter.refresh();
    });
    // 首次加载数据
    collectFlow(viewModel.getPosts());
    

    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override public void onTabSelected(TabLayout.Tab tab) {}
      @Override public void onTabUnselected(TabLayout.Tab tab) {}
      @Override public void onTabReselected(TabLayout.Tab tab) {}
    });

    return view;
  }

  private void collectFlow(kotlinx.coroutines.flow.Flow<PagingData<PostListItemVO>> flow) {
    FlowCollectorKt.collectPagingData(
       getViewLifecycleOwner(),
       flow,
       pagingData -> {
         Log.d("HomeFragment", "collectFlow: " + pagingData);
         adapter.submitData(getLifecycle(), pagingData);
         swipeRefreshLayout.setRefreshing(false);
         return Unit.INSTANCE;
       }
    );
  }
  
  
}
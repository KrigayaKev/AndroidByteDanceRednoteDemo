package com.example.rednotedemo.presentation.view.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.rednotedemo.R;
import com.example.rednotedemo.enums.FilterType;
import com.example.rednotedemo.presentation.view.adapter.PagingLoadStateAdapter;
import com.example.rednotedemo.presentation.view.adapter.PostListAdapter;
import com.example.rednotedemo.presentation.viewmodel.HomeViewModel;
import com.google.android.material.tabs.TabLayout;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class HomeFragment extends Fragment {

  private static final String TAG = "HomeFragment";

  private RecyclerView recyclerView;
  private PostListAdapter adapter;
  private SwipeRefreshLayout swipeRefreshLayout;
  private HomeViewModel viewModel;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private TabLayout tabLayout;
  private PagingLoadStateAdapter pagingLoadStateAdapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView: 创建 HomeFragment");
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    Log.d(TAG, "onViewCreated: 初始化 HomeFragment");

    // 初始化 View
    initViews(view);

    // 设置 RecyclerView 和 Adapter（只初始化一次）
    if (adapter == null) {
      adapter = new PostListAdapter(new PostListAdapter.MyComparator(), requireContext());
      pagingLoadStateAdapter = new PagingLoadStateAdapter(getContext(),adapter);
      adapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
        @Override
        public Unit invoke(CombinedLoadStates combinedLoadStates) {
          LoadState loadState = combinedLoadStates.getRefresh();
          if (loadState instanceof LoadState.NotLoading) {
            swipeRefreshLayout.setRefreshing(false);
          } else if (loadState instanceof LoadState.Loading) {
            swipeRefreshLayout.setRefreshing(true);

          } else if (loadState instanceof LoadState.Error) {
            swipeRefreshLayout.setRefreshing(false);
            LoadState.Error error = (LoadState.Error) loadState;
            Toast.makeText(getContext(), "Error:" + error.getError().getMessage(), Toast.LENGTH_SHORT).show();
          }
          return null;
        }
      });
      ConcatAdapter concatAdapter = adapter.withLoadStateFooter(pagingLoadStateAdapter);
      recyclerView.setAdapter(concatAdapter);
    }

    // 只订阅一次（避免重复订阅）
    if (compositeDisposable.size() == 0) {
      subscribeToPagingData();
    }

    // 下拉刷新：调用 adapter.refresh() 即可触发重新加载第一页
    swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());
  }
  
  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume: 恢复 HomeFragment-");
  }
  
  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "onPause: 暂停 HomeFragment");
  }
  
  @Override
  public void onStop() {
    super.onStop();
    Log.d(TAG, "onStop: 停止 HomeFragment");
  }

  private void initViews(@NonNull View view) {
    Log.d(TAG, "initViews: 初始化 HomeFragment 中的 View");
    recyclerView = view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

    // 初始化 TabLayout,但未绑定功能
    tabLayout = view.findViewById(R.id.tabLayout);
    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_horizontal_lines));
    tabLayout.addTab(tabLayout.newTab().setText("关注"));
    tabLayout.addTab(tabLayout.newTab().setText("发现"));
    tabLayout.addTab(tabLayout.newTab().setText("广州"));
    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_search));
    tabLayout.getTabAt(2).select(); // 默认选中“发现”
  }

  private void subscribeToPagingData() {
    viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

    compositeDisposable.add(
       viewModel.getPagingData(FilterType.DISCOVER)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(pagingData -> {
            //使用 getViewLifecycleOwner().getLifecycle()
            adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
          }, throwable -> {
            Log.e(TAG, "加载失败", throwable);
            // 可选：显示错误提示
          })
    );
  }

  @Override
  public void onDestroyView() {
    Log.d(TAG, "onDestroyView: 销毁 HomeFragment");
    super.onDestroyView();
    // ❌ 不要 clear()！否则切回来会丢失状态
    // compositeDisposable.clear(); // ← 注释掉这行！

    // 但如果你担心极端情况下的泄漏，可以保留，但会导致每次重建都重新加载
    // 建议：保留订阅，让 Paging 自己管理
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    // Fragment 完全销毁时清理
    compositeDisposable.clear();
  }
}
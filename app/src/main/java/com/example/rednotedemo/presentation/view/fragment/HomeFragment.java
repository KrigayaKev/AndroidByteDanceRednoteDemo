package com.example.rednotedemo.presentation.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.CombinedLoadStates;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.rednotedemo.R;
import com.example.rednotedemo.common.enums.FilterType;
import com.example.rednotedemo.presentation.view.MainActivity;
import com.example.rednotedemo.presentation.view.PublishActivity;
import com.example.rednotedemo.presentation.view.adapter.PagingLoadStateAdapter;
import com.example.rednotedemo.presentation.view.adapter.PostListAdapter;
import com.example.rednotedemo.presentation.view.layout.SafeStaggeredGridLayoutManager;
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

  // 空状态视图
  private View emptyStateView;
  private TextView tvEmptyMessage;
  private Button btnRetryEmpty;
  private Button btnPublishFirst;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView: 创建 HomeFragment");
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

    Log.d(TAG, "onViewCreated: 初始化 HomeFragment");

    // 初始化 View
    initViews(view);

    // 设置 RecyclerView 和 Adapter
    if (adapter == null) {
      adapter = new PostListAdapter(new PostListAdapter.MyComparator(), requireContext());
      pagingLoadStateAdapter = new PagingLoadStateAdapter(getContext(), adapter);

      // 设置加载状态监听器
      adapter.addLoadStateListener(new Function1<CombinedLoadStates, Unit>() {
        @Override
        public Unit invoke(CombinedLoadStates combinedLoadStates) {
          handleLoadState(combinedLoadStates);
          return null;
        }
      });

      ConcatAdapter concatAdapter = adapter.withLoadStateFooter(pagingLoadStateAdapter);
      recyclerView.setAdapter(concatAdapter);
    }

    // 观察数据状态
    observeViewModel();

    // 只订阅一次（避免重复订阅）
    if (compositeDisposable.size() == 0) {
      subscribeToPagingData();
    }

    // 下拉刷新
    swipeRefreshLayout.setOnRefreshListener(() -> {
      adapter.refresh();
      viewModel.refreshDatabaseState();
    });
  }

  private void initViews(@NonNull View view) {
    Log.d(TAG, "initViews: 初始化 HomeFragment 中的 View");
    recyclerView = view.findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new SafeStaggeredGridLayoutManager(requireContext(), 2, SafeStaggeredGridLayoutManager.VERTICAL));

    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

    // 初始化空状态视图
    emptyStateView = view.findViewById(R.id.emptyStateView);
    tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
    btnRetryEmpty = view.findViewById(R.id.btnRetryEmpty);
    btnPublishFirst = view.findViewById(R.id.btnPublishFirst);

    // 设置空状态按钮点击事件
    btnRetryEmpty.setOnClickListener(v -> {
      adapter.retry(); // 重试加载
      hideEmptyState();
    });

    // 给空状态发布按钮添加点击事件 - 修改为通过 MainActivity 跳转
    btnPublishFirst.setOnClickListener(v -> {
      // 通过 MainActivity 跳转到发布页面
      if (getActivity() instanceof MainActivity) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.navigateToPublish();
      } else {
        // 备用方案：直接跳转
        Toast.makeText(getContext(), "跳转到发布页面", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), PublishActivity.class);
        startActivity(intent);
      }
    });

    // 初始化 TabLayout
    tabLayout = view.findViewById(R.id.tabLayout);
    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_horizontal_lines));
    tabLayout.addTab(tabLayout.newTab().setText("关注"));
    tabLayout.addTab(tabLayout.newTab().setText("发现"));
    tabLayout.addTab(tabLayout.newTab().setText("广州"));
    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_search));
    tabLayout.getTabAt(2).select(); // 默认选中"发现"
  }

  private void handleLoadState(CombinedLoadStates combinedLoadStates) {
    LoadState refresh = combinedLoadStates.getRefresh();
    LoadState append = combinedLoadStates.getAppend();

    // 处理刷新状态
    if (refresh instanceof LoadState.NotLoading) {
      swipeRefreshLayout.setRefreshing(false);
      checkAndShowEmptyState();
    } else if (refresh instanceof LoadState.Loading) {
      swipeRefreshLayout.setRefreshing(true);
      // 加载中隐藏空状态，除非是第一次加载且没有数据
      if (adapter.getItemCount() == 0) {
        // 第一次加载，显示加载中的空状态
        showEmptyState("正在加载...");
        btnRetryEmpty.setVisibility(View.GONE);
        btnPublishFirst.setVisibility(View.GONE);
      } else {
        hideEmptyState();
      }
    } else if (refresh instanceof LoadState.Error) {
      swipeRefreshLayout.setRefreshing(false);
      LoadState.Error error = (LoadState.Error) refresh;
      String errorMessage = "加载失败: " + error.getError().getMessage();

      if (adapter.getItemCount() == 0) {
        // 没有数据时显示错误空状态
        showEmptyState(errorMessage);
        btnRetryEmpty.setVisibility(View.VISIBLE);
        btnPublishFirst.setVisibility(View.GONE);
      } else {
        // 有数据但刷新失败，只显示Toast
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        hideEmptyState();
      }
    }

    // 处理追加加载状态（分页加载）
    if (append instanceof LoadState.NotLoading) {
      // 检查是否已加载完所有数据
      if (append.getEndOfPaginationReached() && adapter.getItemCount() == 0) {
        showEmptyState("还没有帖子，快去发布第一个吧！");
        btnRetryEmpty.setVisibility(View.GONE);
        btnPublishFirst.setVisibility(View.VISIBLE);
      }
    } else if (append instanceof LoadState.Error) {
      LoadState.Error error = (LoadState.Error) append;
      if (adapter.getItemCount() == 0) {
        showEmptyState("加载失败: " + error.getError().getMessage());
        btnRetryEmpty.setVisibility(View.VISIBLE);
        btnPublishFirst.setVisibility(View.GONE);
      } else {
        // 分页加载失败，但已有数据，可以显示Toast提示
        Toast.makeText(getContext(), "加载更多失败", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void checkAndShowEmptyState() {
    if (adapter.getItemCount() == 0) {
      // 检查数据库状态
      Boolean isEmpty = viewModel.getIsEmptyState().getValue();
      if (isEmpty != null && isEmpty) {
        showEmptyState("还没有帖子，快去发布第一个吧！");
        btnRetryEmpty.setVisibility(View.GONE);
        btnPublishFirst.setVisibility(View.VISIBLE);
      } else {
        showEmptyState("暂无帖子内容");
        btnRetryEmpty.setVisibility(View.VISIBLE);
        btnPublishFirst.setVisibility(View.GONE);
      }
    } else {
      hideEmptyState();
    }
  }

  /**
   * 观察ViewModel中的数据变化，更新UI状态
   * 包括数据库空状态和空状态消息的观察
   * 当数据库为空且适配器没有数据时显示空状态界面
   * 当空状态消息更新时更新空状态界面的提示信息
   */
  private void observeViewModel() {

    // 观察数据库空状态
    viewModel.getIsEmptyState().observe(getViewLifecycleOwner(), isEmpty -> {
      if (isEmpty && adapter.getItemCount() == 0) {
        showEmptyState("还没有帖子，快去发布第一个吧！");
        btnRetryEmpty.setVisibility(View.GONE);
        btnPublishFirst.setVisibility(View.VISIBLE);
      }
    });

    // 观察空状态消息
    viewModel.getEmptyStateMessage().observe(getViewLifecycleOwner(), message -> {
      if (emptyStateView.getVisibility() == View.VISIBLE) {
        tvEmptyMessage.setText(message);
      }
    });
  }

  private void subscribeToPagingData() {
    compositeDisposable.add(
       viewModel.getPagingData(FilterType.DISCOVER)
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(pagingData -> {
            adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
          }, throwable -> {
            Log.e(TAG, "加载失败", throwable);
            if (adapter.getItemCount() == 0) {
              showEmptyState("数据加载失败: " + throwable.getMessage());
              btnRetryEmpty.setVisibility(View.VISIBLE);
              btnPublishFirst.setVisibility(View.GONE);
            }
          })
    );
  }

  private void showEmptyState(String message) {
    emptyStateView.setVisibility(View.VISIBLE);
    recyclerView.setVisibility(View.GONE);
    tvEmptyMessage.setText(message);
  }

  private void hideEmptyState() {
    emptyStateView.setVisibility(View.GONE);
    recyclerView.setVisibility(View.VISIBLE);
  }

  /**
   * 刷新数据的方法，供 MainActivity 调用
   */
  public void refreshData() {
    Log.d(TAG, "手动刷新数据");
    if (adapter != null) {
      adapter.refresh();
    }

    // 同时刷新数据库状态
    if (viewModel != null) {
      viewModel.refreshDatabaseState();
    }
  }

  @Override
  public void onDestroyView() {
    Log.d(TAG, "onDestroyView: 销毁 HomeFragment");
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    compositeDisposable.clear();
  }
}
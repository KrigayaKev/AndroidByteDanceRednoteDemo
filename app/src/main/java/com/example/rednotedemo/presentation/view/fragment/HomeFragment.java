package com.example.rednotedemo.presentation.view.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.rednotedemo.R;
import com.example.rednotedemo.enums.FilterType;
import com.example.rednotedemo.presentation.view.adapter.PostListAdapter;
import com.example.rednotedemo.presentation.viewmodel.HomeViewModel;
import com.google.android.material.tabs.TabLayout;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;

public class HomeFragment extends Fragment {

  private RecyclerView recyclerView;
  private PostListAdapter adapter;
  private SwipeRefreshLayout swipeRefreshLayout;
  private TabLayout tabLayout;
  private HomeViewModel viewModel;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
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
    adapter = new PostListAdapter(new PostListAdapter.MyComparator(), getContext());
    recyclerView.setAdapter(adapter);

    viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

    compositeDisposable.add(
          viewModel.getPagingData(FilterType.DISCOVER)
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(pagingData -> {
                      adapter.submitData(getLifecycle(), pagingData);
                  }, throwable -> {
                      Log.e("HomeFragment", "加载失败", throwable);
                      // 可选：显示错误提示
                  })
    );



    // 下拉刷新
    swipeRefreshLayout.setOnRefreshListener(() -> {
      adapter.refresh();
      swipeRefreshLayout.setRefreshing(false);
    });


    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override public void onTabSelected(TabLayout.Tab tab) {}
      @Override public void onTabUnselected(TabLayout.Tab tab) {}
      @Override public void onTabReselected(TabLayout.Tab tab) {}
    });

    return view;
  }



}
package com.example.rednotedemo.presentation.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rednotedemo.R;
import com.example.rednotedemo.presentation.view.viewholder.LoadViewHolder;

import org.jetbrains.annotations.NotNull;

import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;

public class PagingLoadStateAdapter extends LoadStateAdapter<LoadViewHolder> {

  private final Context context;
  private final PostListAdapter adapter;
  private LoadViewHolder loadViewHolder;

  public PagingLoadStateAdapter(Context context,PostListAdapter adapter) {
    this.context = context;
    this.adapter=adapter;
  }

  @Override
  public void onBindViewHolder(@NotNull LoadViewHolder myViewHolder,
                               @NotNull LoadState loadState) {
    myViewHolder.bind(loadState);
  }

  @NotNull
  @Override
  public LoadViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup,
                                                                @NotNull LoadState loadState) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_load_state, viewGroup, false);
    loadViewHolder = new LoadViewHolder(view,adapter);
    return loadViewHolder;
  }

  @Override
  public boolean displayLoadStateAsItem(@NotNull LoadState loadState) {
    return (loadState instanceof LoadState.Loading || loadState instanceof LoadState.Error || loadState instanceof LoadState.NotLoading && loadState
       .getEndOfPaginationReached());
  }
}

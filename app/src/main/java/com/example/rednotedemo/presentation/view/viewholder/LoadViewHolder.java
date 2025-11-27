package com.example.rednotedemo.presentation.view.viewholder;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rednotedemo.R;
import com.example.rednotedemo.presentation.view.adapter.PostListAdapter;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.RecyclerView;

public  class LoadViewHolder extends RecyclerView.ViewHolder {
  private PostListAdapter adapter;
  private TextView mErrorMsg;
  private TextView mStatus;
  private Button mRetry;
  private ProgressBar progressBar;

  public LoadViewHolder(@NonNull View itemView,PostListAdapter adapter) {
    super(itemView);
    this.adapter = adapter;
    mErrorMsg = itemView.findViewById(R.id.errorMsg);
    mRetry = itemView.findViewById(R.id.retryButton);
    progressBar = itemView.findViewById(R.id.progressBar);
    mStatus = itemView.findViewById(R.id.tv_stats);
  }
  /**
   * 功能描述 加载状态显示
   *
   * @param loadState 加载状态
   * @since 1.0
   */
  public void bind(LoadState loadState) {
    Log.d("LoadViewHolder","加载动画组件bind中");
    if (loadState instanceof LoadState.NotLoading) {
      if (loadState.getEndOfPaginationReached()) {
        progressBar.setVisibility(View.GONE);
        mStatus.setVisibility(View.VISIBLE);
        mStatus.setText("数据加载完毕");
        mRetry.setVisibility(View.GONE);
        mErrorMsg.setVisibility(View.GONE);
      }
    }

    if (loadState instanceof LoadState.Loading) {
      progressBar.setVisibility(View.VISIBLE);
    } else {
      progressBar.setVisibility(View.GONE);
    }
    if (loadState instanceof LoadState.Error) {
      mErrorMsg.setVisibility(View.VISIBLE);
      mRetry.setVisibility(View.VISIBLE);
      mRetry.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          adapter.retry();
        }
      });
      mStatus.setVisibility(View.GONE);
      LoadState.Error loadStateError = (LoadState.Error) loadState;
      mErrorMsg.setText(loadStateError.getError().getLocalizedMessage());
    } else {
      mErrorMsg.setVisibility(View.GONE);
      mRetry.setVisibility(View.GONE);
    }

  }

}

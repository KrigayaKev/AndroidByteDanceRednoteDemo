package com.example.rednotedemo.common.util;

import androidx.annotation.Nullable;

/**
 * 资源类，用于封装数据加载状态
 * @param <T> 数据类型
 */
public class Resource<T> {
  @Nullable public final T data;
  public final boolean isLoading;
  @Nullable
  public final String error;

  private Resource(@Nullable T data, boolean isLoading, @Nullable String error) {
    this.data = data;
    this.isLoading = isLoading;
    this.error = error;
  }

  public static <T> Resource<T> loading() {
    return new Resource<>(null, true, null);
  }

  public static <T> Resource<T> success(T data) {
    return new Resource<>(data, false, null);
  }

  public static <T> Resource<T> error(String error) {
    return new Resource<>(null, false, error);
  }
}

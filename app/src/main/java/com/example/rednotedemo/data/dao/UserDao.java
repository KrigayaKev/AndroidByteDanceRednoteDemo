package com.example.rednotedemo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.rednotedemo.entity.User;

import java.util.List;

@Dao
public interface UserDao {

  /**
   * 插入单个用户
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insert(User user);

  /**
   * 插入多个用户
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  List<Long> insertAll(List<User> users);

  /**
   * 更新用户信息
   */
  @Update
  int update(User user);

  /**
   * 删除用户
   */
  @Delete
  int delete(User user);

  /**
   * 根据ID查询用户
   */
  @Query("SELECT * FROM user WHERE id = :id")
  User getUserById(int id);

  /**
   * 根据ID查询用户（LiveData）
   */
  @Query("SELECT * FROM user WHERE id = :id")
  LiveData<User> getUserByIdLiveData(int id);

  /**
   * 查询所有用户
   */
  @Query("SELECT * FROM user ORDER BY create_time DESC")
  List<User> getAllUsers();

  /**
   * 查询所有用户（LiveData）
   */
  @Query("SELECT * FROM user ORDER BY create_time DESC")
  LiveData<List<User>> getAllUsersLiveData();

  /**
   * 根据用户名查询用户
   */
  @Query("SELECT * FROM user WHERE username LIKE :username LIMIT 1")
  User getUserByUsername(String username);

  /**
   * 根据手机号查询用户
   */
  @Query("SELECT * FROM user WHERE phone = :phone LIMIT 1")
  User getUserByPhone(String phone);

  /**
   * 删除所有用户
   */
  @Query("DELETE FROM user")
  void deleteAllUsers();

  /**
   * 获取用户数量
   */
  @Query("SELECT COUNT(*) FROM user")
  int getUsersCount();

  /**
   * 搜索用户（按用户名模糊匹配）
   */
  @Query("SELECT * FROM user WHERE username LIKE '%' || :keyword || '%'")
  List<User> searchUsers(String keyword);
}
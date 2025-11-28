package com.example.rednotedemo;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.rednotedemo.data.database.AppDatabase;
import com.example.rednotedemo.data.dao.UserDao;
import com.example.rednotedemo.entity.User;
import com.example.rednotedemo.testdata.TestDataGenerator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class UserDatabaseTest {

  private AppDatabase database;
  private UserDao userDao;

  @Before
  public void createDb() {
    // 使用内存数据库进行测试
    database = Room.inMemoryDatabaseBuilder(
       ApplicationProvider.getApplicationContext(),
       AppDatabase.class
    ).build();
    userDao = database.userDao();
  }

  @After
  public void closeDb() {
    database.close();
  }

  /**
   * 测试插入和查询单个用户
   */
  @Test
  public void testInsertAndGetUser() {
    // 生成测试用户
    User testUser = TestDataGenerator.generateTestUser();

    // 插入用户
    long userId = userDao.insert(testUser);
    assertTrue(userId > 0);

    // 查询用户
    User retrievedUser = userDao.getUserById((int) userId);
    assertNotNull(retrievedUser);
    assertEquals(testUser.getUsername(), retrievedUser.getUsername());
    assertEquals(testUser.getAvatarUrl(), retrievedUser.getAvatarUrl());
  }

  /**
   * 测试插入多个用户
   */
  @Test
  public void testInsertMultipleUsers() {
    // 生成10个测试用户
    List<User> testUsers = TestDataGenerator.generateTestUsers(10);

    // 插入用户
    List<Long> userIds = userDao.insertAll(testUsers);
    assertEquals(10, userIds.size());

    // 验证用户数量
    int userCount = userDao.getUsersCount();
    assertEquals(10, userCount);
  }

  /**
   * 测试更新用户信息
   */
  @Test
  public void testUpdateUser() {
    // 插入测试用户
    User testUser = TestDataGenerator.generateTestUser();
    long userId = userDao.insert(testUser);
    testUser.setId((int) userId);

    // 更新用户信息
    String newUsername = "更新后的用户名";
    testUser.setUsername(newUsername);
    int updatedRows = userDao.update(testUser);

    assertEquals(1, updatedRows);

    // 验证更新结果
    User updatedUser = userDao.getUserById((int) userId);
    assertEquals(newUsername, updatedUser.getUsername());
  }

  /**
   * 测试删除用户
   */
  @Test
  public void testDeleteUser() {
    // 插入测试用户
    User testUser = TestDataGenerator.generateTestUser();
    long userId = userDao.insert(testUser);
    testUser.setId((int) userId);

    // 删除用户
    int deletedRows = userDao.delete(testUser);
    assertEquals(1, deletedRows);

    // 验证用户已被删除
    User deletedUser = userDao.getUserById((int) userId);
    assertNull(deletedUser);
  }

  /**
   * 测试搜索功能
   */
  @Test
  public void testSearchUsers() {
    // 插入一些测试用户
    List<User> testUsers = TestDataGenerator.generateTestUsers(5);
    userDao.insertAll(testUsers);

    // 插入一个特定用户名的用户用于搜索测试
    User searchUser = new User("测试用户123", "https://example.com/test.jpg");
    userDao.insert(searchUser);

    // 搜索用户
    List<User> searchResults = userDao.searchUsers("测试用户");
    assertFalse(searchResults.isEmpty());
    assertEquals("测试用户123", searchResults.get(0).getUsername());
  }

  /**
   * 测试清空所有用户
   */
  @Test
  public void testDeleteAllUsers() {
    // 插入一些测试用户
    List<User> testUsers = TestDataGenerator.generateTestUsers(5);
    userDao.insertAll(testUsers);

    // 验证插入成功
    assertEquals(5, userDao.getUsersCount());

    // 清空所有用户
    userDao.deleteAllUsers();

    // 验证清空成功
    assertEquals(0, userDao.getUsersCount());
  }
}
package com.example.rednotedemo;

import android.content.Context;
import android.graphics.BitmapFactory;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PostDaoTest {

  @Test
  public void testSpecificImageUrl() {
    Context context = ApplicationProvider.getApplicationContext();

    // 使用您提供的具体URL进行测试
    String testImageUrl = "/data/user/0/com.example.rednotedemo/files/images/post_1_image_0_1764317300512.jpg";

    File imageFile = new File(testImageUrl);

    // 测试1: 文件存在性
    assertTrue("图片文件应该存在", imageFile.exists());
    assertTrue("图片文件应该可读", imageFile.canRead());
    assertTrue("图片文件大小应该大于0", imageFile.length() > 0);

    // 测试2: 图片可解码性
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(testImageUrl, options);

    assertTrue("图片宽度应该大于0", options.outWidth > 0);
    assertTrue("图片高度应该大于0", options.outHeight > 0);

    System.out.println("✅ 图片测试通过!");
    System.out.println("   文件路径: " + testImageUrl);
    System.out.println("   文件大小: " + imageFile.length() + " bytes");
    System.out.println("   图片尺寸: " + options.outWidth + "x" + options.outHeight);
    System.out.println("   图片类型: " + options.outMimeType);
  }
}
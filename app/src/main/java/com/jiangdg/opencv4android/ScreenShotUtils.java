package com.jiangdg.opencv4android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @Description: 进行截屏工具类
 * @author wjj
 * @time 2013/09/29
 */
public class ScreenShotUtils {
    private  static Context context;
    private static final String SAVE_PIC_PATH= Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
    private static final String SAVE_REAL_PATH = SAVE_PIC_PATH+ "/good/savePic";//保存的确切位置
    /**
     * 进行截取屏幕
     * @param pActivity
     * @return bitmap
     */
    public static String takeScreenShot(Activity pActivity) {
        context = pActivity;
        Bitmap bitmap=null;
        View view=pActivity.getWindow().getDecorView();
        // 设置是否可以进行绘图缓存
        view.setDrawingCacheEnabled(true);
        // 如果绘图缓存无法，强制构建绘图缓存
        view.buildDrawingCache();
        // 返回这个缓存视图
        bitmap=view.getDrawingCache();

        // 获取状态栏高度
        Rect frame=new Rect();
        // 测量屏幕宽和高
        view.getWindowVisibleDisplayFrame(frame);
        int stautsHeight = 100;
        Log.d("jiangqq", "状态栏的高度为:"+stautsHeight);
        int width = pActivity.getWindowManager().getDefaultDisplay().getWidth();
        int height = (int)width*7/5;         //截图时控制距离，距离屏幕底部多高距离
        bitmap = Bitmap.createBitmap(bitmap, 0, stautsHeight, width, height);
        Log.d("截图截图截图截图", "截图width:"+width+"截图height:"+height+"顶部开始Y坐标"+stautsHeight);
        String path = SaveSdCardUtil.saveMyBitmap(System.currentTimeMillis() + ".jpg",bitmap);
        return path;
    }
    //好用的保存
    public static String getSavePath(Bitmap bitmap) {
        String path = SaveSdCardUtil.saveMyBitmap(System.currentTimeMillis() + ".jpg",bitmap);
        return path;
    }
    /** 首先默认个文件保存路径 */
    public static void saveFile(Bitmap bm, String fileName, String path) throws IOException {
        String fileName1 =  System.currentTimeMillis() + ".jpg";
        String subForder = SAVE_REAL_PATH + fileName1;
        File foder = new File(subForder);
        if (!foder.exists()) {
            foder.mkdirs();
        }
        File myCaptureFile = new File(subForder, fileName);
        if (!myCaptureFile.exists()) {
            myCaptureFile.createNewFile();
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
    }
    public static String createImageFromBitmap(Bitmap bitmap) {
        String fileName = System.currentTimeMillis() + ".jpg";
        Uri uri;
        try {
            String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +fileName;
            File appDir = new File(storePath);
            if (!appDir.exists()) {
                appDir.mkdir();
            }

            File file = new File(appDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        Log.i("fileName", fileName);
        return fileName.toString();

    }
    /**
     * 保存图片到sdcard中
     * @param pBitmap
     */
    public static boolean savePic(Bitmap pBitmap, String strName) {
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +strName;
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        FileOutputStream fos=null;
        try {
            fos=new FileOutputStream(strName);
            if(null!=fos)
            {
                pBitmap.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                fos.flush();
                fos.close();
                return true;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    //保存文件到指定路径
    public static boolean saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + System.currentTimeMillis()+".jpg";
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos);
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
//            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 获取一个 View 的缓存视图
     */
    public static Bitmap getCacheBitmapFromView(View v) {
        //第一种方案   返回的bitmap不为空
        if (v.getLayoutParams().width<=0||v.getLayoutParams().height<=0){
            return null;
        }
        Bitmap b = Bitmap.createBitmap( v.getLayoutParams().width, v.getLayoutParams().height, Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

}
    package com.jiangdg.opencv4android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenShot {
    // 获取指定Activity的截屏，保存到png文件
    static String newFilePath;

    private static Bitmap takeScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        Log.i("TAG", "" + statusBarHeight);

        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay()
                .getHeight();
        // 去掉标题栏
        // Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455);
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();


        return b;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Bitmap getBitmap(SurfaceView surfaceView) {
        Bitmap bitmap = Bitmap.createBitmap(surfaceView.getWidth(), surfaceView.getHeight(), Bitmap.Config.ARGB_8888);

        PixelCopy.request(surfaceView, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
            @Override
            public void onPixelCopyFinished(int copyResult){
                if (PixelCopy.SUCCESS == copyResult) {
                    // onSuccessCallback(bitmap)
                } else {
                    // onErrorCallback()
                }
            }
        }, surfaceView.getHandler());
        return bitmap;
    }

    public static Bitmap captureScreen(Activity activity) {

        activity.getWindow().getDecorView().setDrawingCacheEnabled(true);

        Bitmap bmp=activity.getWindow().getDecorView().getDrawingCache();

        return bmp;

    }
    // 保存到sdcard
    private static void savePic(Bitmap b, String strFileName) {
        FileOutputStream fos = null;
        try {
//判断是否存在该文件夹和文件，如果没有新建
            File file = new File(strFileName);
            //获取父目录
            File fileParent = file.getParentFile();
            //判断是否存在
            if (!fileParent.exists()) {
//                创建父目录文件
                fileParent.mkdirs();
            }
            file.createNewFile();
            fos = new FileOutputStream(new File(strFileName));
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //用于获取当前最新的截图
    public static String getPath(){
        return newFilePath;
    }

    // 截屏方法
    public static void shoot(Activity a) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fileName = df.format(date)+".png";
        String sdCardPath = Environment.getExternalStorageDirectory().getPath();
        String filePath = sdCardPath + File.separator + "xinlv" + File.separator + "Screenshoots"+ File.separator + fileName;
        newFilePath = filePath;
        ScreenShot.savePic(ScreenShot.captureScreen(a), filePath);
    }

}
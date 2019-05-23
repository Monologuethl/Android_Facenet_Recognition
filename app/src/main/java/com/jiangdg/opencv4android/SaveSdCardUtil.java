package com.jiangdg.opencv4android;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wjj on 2017/9/28.
 * @Description  图片存储工具类
 */

public  class SaveSdCardUtil {

    public static String saveMyBitmap(String bitName, Bitmap mBitmap){
        String filePath = "";
        File f = new File("/sdcard/" + bitName );
        filePath = f.getAbsolutePath();
        try {
            f.createNewFile();
        } catch (IOException e) {
            Log.e("在保存图片时出错","在保存图片时出错"+e.toString());
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }


}

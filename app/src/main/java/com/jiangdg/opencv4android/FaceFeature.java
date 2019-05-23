package com.jiangdg.opencv4android;
/* by cjf 1801615 352871242@qq.com*/

import android.util.Log;

import java.io.Serializable;

/**
 * 人脸特征(512维特征值)
 * 相似度取特征向量之间的欧式距离.
 */
public class FaceFeature implements Serializable {
    public static final int DIMS=512;
    private float fea[];

    FaceFeature(){
        fea=new float[DIMS];
    }

    public float[] getFeature(){
        return fea;
    }

    //比较当前特征和另一个特征之间的相似度
    public double compare(FaceFeature ff){
        //for (int i=0;i<DIMS;i++)
        //{
        //    Log.e("error", "float fea[]:"+ ff.getFeature()[i]);
        //}
        long t1= System.currentTimeMillis();
        double dist=0;
        for (int i=0;i<DIMS;i++)
            dist+= Math.pow(fea[i]-ff.fea[i],2);
            //dist+=(fea[i]-ff.fea[i])*(fea[i]-ff.fea[i]);
        dist= Math.sqrt(dist);
        Log.e("error", "计算分数时间:"+ String.valueOf(System.currentTimeMillis()-t1));
        Log.e("error", "分数:"+ String.valueOf(dist));
        return dist;
    }



}

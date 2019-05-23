package com.jiangdg.opencv4android.Interface;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public interface OnFaceDetectorListener {
    // 检测到一个人脸的回调
    void onFace(Mat mat, Rect rect);
}
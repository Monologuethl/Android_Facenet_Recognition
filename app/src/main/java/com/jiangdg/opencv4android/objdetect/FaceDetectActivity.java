package com.jiangdg.opencv4android.objdetect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.jiangdg.opencv4android.FaceFeature;
import com.jiangdg.opencv4android.Facenet;
import com.jiangdg.opencv4android.R;
import com.jiangdg.opencv4android.natives.DetectionBasedTracker;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.jiangdg.opencv4android.FaceNetActivity.ReStoreFeature;

/**
 * 人脸检测
 * <p>
 * Created by jiangdongguo on 2018/1/4.
 */

public class FaceDetectActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final int JAVA_DETECTOR = 0;
    private static final int NATIVE_DETECTOR = 1;

    private static final String TAG = "FaceDetectActivity";
    @BindView(R.id.cameraView_face)
    CameraBridgeViewBase mCameraView;
    private Mat mGray;
    private Mat mRgba;
    private int mDetectorType = NATIVE_DETECTOR;
    private int mAbsoluteFaceSize = 0;
    private float mRelativeFaceSize = 0.2f;
    private DetectionBasedTracker mNativeDetector;
    private CascadeClassifier mJavaDetector;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private Facenet facenet;
    private FaceFeature ft;

    private List<Map<String,FaceFeature>> List_Map_Facefeature = new ArrayList<>();
    private File mCascadeFile;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    // OpenCV初始化加载成功，再加载本地so库
                    System.loadLibrary("opencv341");
                    try {
                        // 加载人脸检测模式文件
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        byte[] buffer = new byte[4096];
                        int byteesRead;
                        while ((byteesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, byteesRead);
                        }
                        is.close();
                        os.close();
                        // 使用模型文件初始化人脸检测引擎
                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "加载cascade classifier失败");
                            mJavaDetector = null;
                        } else {
                            Log.d(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
                        }
                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(),"", 0);
                        cascadeDir.delete();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // 开启渲染Camera
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_facedetect);

        facenet=new Facenet(getAssets());
        List_Map_Facefeature = ReStoreFeature();
        // 绑定View
        ButterKnife.bind(this);
        mCameraView.setCameraIndex(-1);
        mCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        // 注册Camera渲染事件监听器
        mCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 静态初始化OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "无法加载OpenCV本地库，将使用OpenCV Manager初始化");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "成功加载OpenCV本地库");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 停止渲染Camera
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止渲染Camera
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // 灰度图像
        mGray = new Mat();
        // R、G、B彩色图像
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        // 设置脸部大小
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }
        // 获取检测到的脸部数据
        MatOfRect faces = new MatOfRect();
        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null) {
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2,
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            }
        } else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null) {
                mNativeDetector.detect(mGray, faces);
            }
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }
        Rect[] facesArray = faces.toArray();
        // 绘制检测框
        for (Rect rect : facesArray) {
            Imgproc.rectangle(mRgba, rect.tl(), rect.br(), FACE_RECT_COLOR, 1);
        }

        String name="";
        FaceRecognitiion(facesArray);

        return mRgba;
    }

    public FaceFeature MTCNNandFaceNet(Bitmap bitmap1){

        try {
            long t2= System.currentTimeMillis();
            FaceFeature ff1=facenet.recognizeImage(bitmap1);
            Log.e("error", "facenet获取特征时间："+ String.valueOf(System.currentTimeMillis()-t2));
            return ff1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("LongLogTag")
    public double FaceRecognitiion(Rect[] facesArray) {
        if (facesArray.length > 0) {
            Rect biggestRect = facesArray[0];
            if(facesArray.length>1){
                for (int j = 1; j < facesArray.length; j++) {
                    double area1 = facesArray[j].area();
                    double area = biggestRect.area();
                    if (area1 > area)
                        biggestRect = facesArray[j];
                }
            }
            String name="";
            List<List>Scores_Path = null;
            long file_name = System.currentTimeMillis();
            saveImage(mRgba, biggestRect, String.valueOf(file_name));
            Bitmap bitmap = getImage(String.valueOf(file_name));
            FaceFeature ff = MTCNNandFaceNet(bitmap);

            double dist = 0;
            float[] fea = ff.getFeature();
            List<String> bitmap2_path = new ArrayList<>();
            List<Double> scores = new ArrayList<Double>();

            for (Map<String,FaceFeature> map_face :List_Map_Facefeature){
                Set<String> ks = map_face.keySet();
                for (String key : ks) {
                    FaceFeature ff2 = map_face.get(key);
                    for (int i=0;i<512;i++){
                        dist+= Math.pow(fea[i]-ff2.getFeature()[i],2);
                    }
                    dist= Math.sqrt(dist);
                    scores.add(dist);
                    bitmap2_path.add(key);
                    dist=0;
                }
            }
            Log.e("scores:", String.valueOf(scores));
            double min = 2;
            int m = 0;
            for (m = 0; m < scores.size(); m++) {
                if (scores.get(m) < min) {
                    min = scores.get(m);
                }
            }
            int index = scores.indexOf(min);
            name=bitmap2_path.get(index);
            Log.e("name:",name);
            name=name.substring(25);
            double output=(1.4-min)*100;
            DecimalFormat df = new DecimalFormat("0.00");

            if (min<1)
            {
                for (Rect rect : facesArray) {
                    Imgproc.rectangle(mRgba, rect.tl(), rect.br(), FACE_RECT_COLOR, 1);
                }
                Imgproc.putText(mRgba, name, biggestRect.tl(), Core.FONT_HERSHEY_PLAIN, 1, FACE_RECT_COLOR, 1);
            }
            Log.e("----------置信度为----------", String.valueOf(df.format(output))+"%");
//            Log.e("scores.length:", String.valueOf(scores.size()));
            scores.clear();

            return min;
        }else {
            return 0;
        }
    }

    public boolean saveImage(Mat image, Rect rect, String fileName) {
        try {
            String PATH = Environment.getExternalStorageDirectory() + "/FaceDetect/" + fileName + ".jpg";
            // 把检测到的人脸重新定义大小后保存成文件
            Mat sub = image.submat(rect);
            Mat mat = new Mat();
            Size size = new Size(100, 100);
            Imgproc.resize(sub, mat, size);
            Imgcodecs.imwrite(PATH, mat);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Bitmap getImage(String fileName) {
        try {
            return BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/FaceDetect/" + fileName + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

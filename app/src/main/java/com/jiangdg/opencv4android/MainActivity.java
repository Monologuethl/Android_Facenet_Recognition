package com.jiangdg.opencv4android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jiangdg.opencv4android.imgproc.ImgProcessActivity;
import com.jiangdg.opencv4android.objdetect.FaceDetectActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.jiangdg.opencv4android.FaceNetActivity.ReStoreFeature;
import static com.jiangdg.opencv4android.FaceNetActivity.getImagePathFromSD;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btn_hello)
    Button mBtnHello;
    @BindView(R.id.btn_faceDetect)
    Button mBtnFaceDetect;
    @BindView((R.id.button3))
    Button button3;
    @BindView(R.id.button4)
    Button button4;


    public static MTCNN mtcnn;
    public  Facenet facenet;
    public static String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        facenet=new Facenet(getAssets());
        mtcnn=new MTCNN(getAssets());
        // 绑定View
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_hello,R.id.btn_faceDetect,R.id.button3,R.id.button4})
    public void onViewClick(View view) {
        int vId = view.getId();
        switch (vId) {
            case R.id.btn_hello:
                Intent intentHello = new Intent(MainActivity.this, ImgProcessActivity.class);
                startActivity(intentHello);
                break;
            case R.id.btn_faceDetect:
                Intent intentFd = new Intent(MainActivity.this,FaceDetectActivity.class);
                startActivity(intentFd);
                break;
            case  R.id.button3:

                GetFeature();

                break;
            case  R.id.button4:
                Intent intentRegister = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intentRegister);
                break;
        }
    }

    public void GetFeature(){
        List<String> getImagePath = getImagePathFromSD(filePath);
        List<Bitmap> bitmap_set = new ArrayList<Bitmap>();
        Map<String,FaceFeature > map1 = new HashMap<String,FaceFeature >();
        List<Map<String,FaceFeature>> List_map = new ArrayList<>();
        for(String data:getImagePath){
            try {
                FileInputStream fis  = new FileInputStream(data);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                bitmap_set.add(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.e("error",data);
        }
        int i=0;
        for (Bitmap bm:bitmap_set) {

            Log.e("error", "正在提取第"+ String.valueOf(i)+"个特征.......");
            Vector<Box> boxes=mtcnn.detectFaces(bm,40);
            if (boxes.size()==0) {
                Log.e("error","未检测到人脸");;
            }
            else {
                Rect rect1=boxes.get(0).transform2Rect();
                Utils.rectExtend(bm,rect1,20);
                Bitmap face_=Utils.crop(bm,rect1);
                FaceFeature face=facenet.recognizeImage(face_);
                map1.put(getImagePath.get(i),face);
                List_map.add(map1);
                i++;
//                progressBar2.incrementProgressBy((int)(i/bitmap_set.size()*100));
            }
        }
        Log.e("error","提取特征结束，开始保存......");
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE"+ File.separator+"storefeature.txt");
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            for(Map<String,FaceFeature> map : List_map) {os.writeObject(map);}
            os.writeObject(null);
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("error", "总共特征数为："+ String.valueOf(List_map.size()));
        Log.e("error","-----------------保存结束-----------------");
        map1.clear();
        bitmap_set.clear();
        List_map.clear();
    }


}

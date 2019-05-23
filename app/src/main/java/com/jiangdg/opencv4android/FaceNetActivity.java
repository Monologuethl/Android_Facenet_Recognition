package com.jiangdg.opencv4android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import static android.hardware.Camera.open;

public class FaceNetActivity extends AppCompatActivity {

    public static String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE";
    public static String MTCN_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE"+ File.separator+"MTCNN.txt";
    public static String FaceNet_path= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE"+ File.separator+"FaceNet.txt";

    private SurfaceView sfv_preview;
    private Button btn_take;
    private Button btn_init;
    private Button btn_register;

    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView imageView3;
    private ImageView imageView4;
    private ProgressBar progressBar2;
    public Bitmap bitmap1;
    public Bitmap bitmap2;

    private Camera camera = null;

    public static MTCNN mtcnn;
    public  Facenet facenet;
    public List<FaceFeature> faceFeatures;
    public float[] fea;
    public long t1;
    public FaceFeature ft;
    public List<Map<String,FaceFeature>> List_Map_Facefeature = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_together);

        sfv_preview = findViewById(R.id.sfv_preview);
        sfv_preview.getHolder().addCallback(cpHolderCallback);
        imageView1 =findViewById(R.id.imageView7);
        imageView2 =findViewById(R.id.imageView11);

        imageView3 =findViewById(R.id.imageView2);
        imageView4 =findViewById(R.id.imageView);

        btn_take = findViewById(R.id.take);
        btn_init = findViewById(R.id.init);
        btn_register = findViewById(R.id.register);
        progressBar2 =findViewById(R.id.progressBar2);


        facenet=new Facenet(getAssets());
        mtcnn=new MTCNN(getAssets());

        List_Map_Facefeature = ReStoreFeature();

        bindViews();

    }

    private void bindViews() {

        btn_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap2=null;
                t1= System.currentTimeMillis();
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        String path = "";
                        if ((path = saveFile(data)) != null) {
                            imageView1.setImageURI(Uri.fromFile(new File(path)));
                            bitmap1 =((BitmapDrawable) imageView1.getDrawable()).getBitmap();
                            if(List_Map_Facefeature.size()!=0){
                                ft = MTCNNandFaceNet(bitmap1);
                                if(ft!=null){
                                    fea = ft.getFeature();
                                    List<Double> scores =new ArrayList<Double>();
                                    double dist=0;
                                    List<String> bitmap2_path = new ArrayList<>();
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
                                    Log.e("error", String.valueOf(scores));
                                    double min = 2;
                                    int m=0;
                                    for (m=0;m<scores.size();m++) {
                                        if(scores.get(m)<min)
                                        {
                                            min=scores.get(m);
                                        }
                                    }
                                    double s = min;
                                    try {
                                        int index =scores.indexOf(s);
                                        imageView2.setImageURI(Uri.fromFile(new File(bitmap2_path.get(index))));
                                        showScore(s, System.currentTimeMillis()-t1);

                                        Log.e("error", "index:"+ String.valueOf(scores.indexOf(s)));
                                        Log.e("error", "image_name:"+bitmap2_path.get(index));
                                        Log.e("error", "score:"+ String.valueOf(s));
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    bitmap2_path.clear();
                                    scores.clear();
                                }else{
                                    Toast.makeText(FaceNetActivity.this, "未检测出人脸", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                List_Map_Facefeature = ReStoreFeature();
                                Toast.makeText(FaceNetActivity.this, "初始化成功", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(FaceNetActivity.this, "保存照片失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });


        btn_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GetFeature();

                List_Map_Facefeature = ReStoreFeature();
            }
        });


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(FaceNetActivity.this, RegisterActivity.class);
                startActivity(it);
            }
        });

    }

    //  --------------------------------------------------------------------------------------------
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

    public static List<Map<String, FaceFeature>> ReStoreFeature() {
        List<Map<String,FaceFeature>> List_map =new ArrayList<>();
        Map<String,FaceFeature > map1 = new HashMap<String,FaceFeature >();
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE"+ File.separator+"storefeature.txt"));
            while((map1= (Map<String, FaceFeature>) is.readObject())!=null) {
                List_map.add(map1);
            }
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Log.e("error", "恢复特征成功！，恢复特征总数为："+ String.valueOf(List_map.size()));
        return List_map;
    }

    public void showScore(double score,long time){
        TextView textView=(TextView)findViewById(R.id.result);
//        textView.setText("[*]人脸检测+识别 运行时间:"+time+"\n");
        textView.setText("");
        if (score<=0){
            if (score<-1.5)
                textView.append("[*]数据库中图片检测不到人脸");
            else textView.append("[*]检测不到人脸");
        }
        else{
            if (score>1.1){
                float fenshu = (float) (1.1-score)*100;
                imageView2.setImageBitmap(null);
                textView.append("置信度为:"+fenshu+"过低，请重新识别  \t识别时间为："+time);
            }else {
                float fenshu = (float) (1.1-score)*100;
                textView.append("[*]置信度:"+fenshu+"%"+" |识别时间为："+time);
            }

        }
    }

    public FaceFeature MTCNNandFaceNet(Bitmap bitmap1){
            imageView3.setImageBitmap(null);
            //Rect rect2 = FaceDetect.detectBiggestFace(bitmap1);
            //if(rect2!=null){
            //    String dectect_info="left:"+String.valueOf(rect2.left)+"  right:"+String.valueOf(rect2.right)+"  top:"+String.valueOf(rect2.top)+"  bottom"+String.valueOf(rect2.bottom);
            //    Log.e("Android_dectect_info", dectect_info);
            //    int margin=20;
            //    Utils.rectExtend(bitmap1,rect2,margin);
            //    Bitmap face1=Utils.crop(bitmap1,rect2);
            //    Log.e("error", "检测人脸时间:"+String.valueOf(System.currentTimeMillis()-t1));
            //    long t2=System.currentTimeMillis();
            //    FaceFeature ff1=facenet.recognizeImage(face1);
            //    imageView3.setImageBitmap(face1);
            //    Log.e("error", "facenet获取特征时间："+String.valueOf(System.currentTimeMillis()-t2));
            //    return ff1;
            //}else{
            //    return null;
            //}
            long t1= System.currentTimeMillis();
            Vector<Box> boxes=mtcnn.detectFaces(bitmap1,40);
            if (boxes.size()==0) {
                Log.e("error","没有检测到人脸");
                return null;
            }
            Rect rect1=boxes.get(0).transform2Rect();
            int margin=20;
            Utils.rectExtend(bitmap1,rect1,margin);
            Bitmap face1=Utils.crop(bitmap1,rect1);
            Log.e("error", "检测人脸时间:"+ String.valueOf(System.currentTimeMillis()-t1));
            imageView3.setImageBitmap(face1);
            long t2= System.currentTimeMillis();
            FaceFeature ff1=facenet.recognizeImage(face1);
            Log.e("error", "facenet获取特征时间："+ String.valueOf(System.currentTimeMillis()-t2));
            return ff1;
    }

    public  static List<String> getImagePathFromSD(String filePath) {
        // 图片列表
        List<String> imagePathList = new ArrayList<String>();
        // 得到sd卡内image文件夹的路径   File.separator(/)
        //String filePath = Environment.getExternalStorageDirectory().toString() + File.separator+ "FACE";
        // Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE";
        // 得到该路径文件夹下所有的文件
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
        for (File file : files) {
            if (checkIsImageFile(file.getPath())) {
                imagePathList.add(file.getPath());
            }
        }
        // 返回得到的图片列表
        return imagePathList;
    }


    public static boolean checkIsImageFile(String fName) {
        boolean isImageFile;
        isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toLowerCase();
        isImageFile = FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif") || FileEnd.equals("jpeg") || FileEnd.equals("bmp");
        return isImageFile;
    }

    //  --------------------------------------------------------------------------------------------

    //保存临时文件的方法
    private String saveFile(byte[] bytes){
        try {
            File file = File.createTempFile("img","");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    //开始预览
    private void startPreview(){
        camera = open();
        try {
            camera.setPreviewDisplay(sfv_preview.getHolder());
            camera.setDisplayOrientation(0);   //让相机旋转90度
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //停止预览
    private void stopPreview() {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private SurfaceHolder.Callback cpHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            startPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopPreview();
        }
    };


}

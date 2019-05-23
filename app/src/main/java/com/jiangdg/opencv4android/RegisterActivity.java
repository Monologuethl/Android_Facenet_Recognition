package com.jiangdg.opencv4android;

import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static android.hardware.Camera.open;
import static com.jiangdg.opencv4android.ImgUtils.saveImageToGallery;

public class RegisterActivity  extends AppCompatActivity {
    private SurfaceView sfv_preview;
    private Button btn_take;
    private Camera camera = null;
    public Bitmap bitmap1 ;
    public ImageView imageView4;
    public EditText editText;
    public MTCNN mtcnn;
    public Facenet facenet;
    public static String Photo_Path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_view);
        imageView4 =findViewById(R.id.imageView4);
        bindViews();
    }

    private void bindViews() {
        mtcnn=new MTCNN(getAssets());
        sfv_preview = (SurfaceView) findViewById(R.id.sfv_preview);
        btn_take = (Button) findViewById(R.id.btn_take);
        sfv_preview.getHolder().addCallback(cpHolderCallback);

        btn_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        String path = "";
                        if ((path = saveFile(data)) != null) {
                            imageView4.setImageURI(Uri.fromFile(new File(path)));
                            bitmap1 =((BitmapDrawable) imageView4.getDrawable()).getBitmap();
                            bitmap1=Getface(bitmap1);
                            imageView4.setImageBitmap(bitmap1);
                            if(bitmap1==null)
                            {
                                Toast.makeText(RegisterActivity.this, "未检测到人脸", Toast.LENGTH_SHORT).show();

                            }else {
                               String file_name=saveImageToGallery(RegisterActivity.this, bitmap1);


                            }

                            Toast.makeText(RegisterActivity.this, "保存照片成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "保存照片失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    public Bitmap Getface(Bitmap face){
        Vector<Box> boxes=mtcnn.detectFaces(face,40);
        if (boxes.size()==0) return null;
        Rect rect1=boxes.get(0).transform2Rect();
        int margin=20;
        Utils.rectExtend(bitmap1,rect1,margin);
        return Utils.crop(bitmap1,rect1);
    }

    public void StoreFeature(Bitmap bitmap1, String file_name) throws IOException {
        Vector<Box> boxes=mtcnn.detectFaces(bitmap1,40);
        Map<String,FaceFeature > map1 = new HashMap<String,FaceFeature >();
        if (boxes.size()==0) {
            Log.e("error","未检测到人脸");;
        }else{
            Rect rect1=boxes.get(0).transform2Rect();
            Utils.rectExtend(bitmap1,rect1,20);
            Bitmap face_=Utils.crop(bitmap1,rect1);
            FaceFeature face=facenet.recognizeImage(face_);
            map1.put(Photo_Path+ File.separator+file_name,face);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "FACE"+ File.separator+"storefeature.txt");
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file,true));
            os.writeObject(map1);
            os.writeObject(null);
            os.close();
        }

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

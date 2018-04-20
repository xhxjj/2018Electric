package com.example.administrator.cameratest;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2018/4/19/019.
 */

public class AdultCamera implements SurfaceHolder.Callback {
    private Camera camera = null;
    private String imagePath;//图片保存路径
    private SurfaceView surfaceView;//图像的预览视图
    private SurfaceHolder holder;//预览视图管理对象
    private Camera.PictureCallback pictureCallback;

    public AdultCamera(SurfaceView surfaceView, String imagePath) {
        this.surfaceView = surfaceView;
        this.imagePath = imagePath;

        if (camera == null)
            camera = getCamera();
        initPictureCallback();//初始化图片返回对象
        holder = surfaceView.getHolder();//初始化相机管理对象
        holder.addCallback(this);//为相机管理对象绑定相片回调方法
    }

    public boolean takePicture() {
        Camera.Parameters parameters = camera.getParameters(); // 利用Parameters对相机的参数进行设置
        parameters.setPictureFormat(ImageFormat.JPEG); //设置照片格式
        Camera.Size size = parametersset(camera); // 这里利用自定义函数parametersset（）获得相机支持的最大图片大小
        parameters.setPictureSize(size.width, size.height);// 对图片的大小进行设置，越大则图片越清晰
        parameters.setJpegQuality(100);//设置照片质量，1-100，
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);// 设置照片对焦方式
        camera.setParameters(parameters);// 让相机的对象完成刚才的设置
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                if (b) {
                    camera.takePicture(null, null, pictureCallback);
                }
            }
        });
        return true;
    }

    private Camera.Size parametersset(Camera camera) { // 获取相机所支持的最大照片大小
        List<Camera.Size> pictureSizes = camera.getParameters().getSupportedPictureSizes();
        Camera.Size psize = pictureSizes.get(0);
        for (Camera.Size size : pictureSizes) {
            psize = size.width + size.height > psize.height + psize.width ? size : psize;
        }
        return psize;
    }

    private void initPictureCallback() {
        pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                File image = new File(imagePath);
                FileOutputStream fileOutputStream;
                try {
                    fileOutputStream = new FileOutputStream(image);
                    fileOutputStream.write(bytes);
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("tag", "图片保存失败！");
                }

            }
        };
    }

    public boolean cameraOnResume() {// 需要在Activity的OnResume()方法中调用
        if (camera == null) {
            camera = getCamera();
            if (holder != null) {
                setStartPreview(camera, holder);
            } else
                return false;
        }
        return true;
    }

    //开始预览相机内容
    private boolean setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            //将系统camera 预览角度旋转90度
            camera.setDisplayOrientation(90);
            camera.startPreview();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open();

        } catch (Exception e) {
            camera = null;
        }
        return camera;
    }

    private boolean releseCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null); // 将相机的回调置空，取消surfaceview 和相机的关联操作
            camera.stopPreview();//取消相机的取景功能
            camera.release();//释放相机所占用的系统资源
            camera = null;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        setStartPreview(camera, surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        camera.stopPreview();
        setStartPreview(camera, this.holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releseCamera();
    }
}

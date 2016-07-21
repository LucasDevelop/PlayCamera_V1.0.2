package org.yanzi.camera;

import java.io.IOException;
import java.util.List;

import org.yanzi.util.CamParaUtil;
import org.yanzi.util.DisplayUtil;
import org.yanzi.util.FileUtil;
import org.yanzi.util.ImageUtil;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class CameraInterface {
    private static final String TAG = "YanZi";
    private static Point screenMetrics;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private float mPreviwRate = -1f;
    private static CameraInterface mCameraInterface;
    //竖屏
    public static final int PORTRAIT = 90;
    //横屏
    public static final int LANDSCAPE = 0;

    //聚焦框的位置
    private Point focusPoint = new Point(0, 0);
    private Application mApplication;

    public void setFocusPoint(int x, int y) {
        focusPoint.x = x;
        focusPoint.y = y;
    }

    public void setCurrentOrientation(int currentOrientation) {
        mCurrentOrientation = currentOrientation;
    }

    //当前屏幕方向
    private int mCurrentOrientation = PORTRAIT;

    public interface CamOpenOverCallback {
        void cameraHasOpened();
    }

    private CameraInterface() {

    }

    public static synchronized CameraInterface getInstance() {
        if (mCameraInterface == null) {
            mCameraInterface = new CameraInterface();
        }
        return mCameraInterface;
    }

    /**
     * 打开Camera
     *
     * @param callback
     */
    public void doOpenCamera(CamOpenOverCallback callback) {
        Log.i(TAG, "Camera open....");
        mCamera = Camera.open();
        Log.i(TAG, "Camera open over....");
        callback.cameraHasOpened();
    }

    /**
     * 使用Surfaceview开启预览
     *
     * @param holder
     * @param previewRate
     */
    public void doStartPreview(SurfaceHolder holder, float previewRate) {
        Log.i(TAG, "doStartPreview...");
        if (isPreviewing) {
            mCamera.stopPreview();
            return;
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            initCamera(previewRate);
        }


    }

    /**
     * 使用TextureView预览Camera
     *
     * @param surface
     * @param previewRate
     */
    public void doStartPreview(SurfaceTexture surface, float previewRate) {
        Log.i(TAG, "doStartPreview...");
        if (isPreviewing) {
            mCamera.stopPreview();
            return;
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            initCamera(previewRate);
        }

    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mPreviwRate = -1f;
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 拍照
     */
    public void doTakePicture() {
        if (isPreviewing && (mCamera != null)) {
            mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
        }
    }

    int DST_RECT_WIDTH, DST_RECT_HEIGHT;

    public void doTakePicture(int w, int h) {
        if (isPreviewing && (mCamera != null)) {
            Log.i(TAG, "矩形拍照尺寸:width = " + w + " h = " + h);
            DST_RECT_WIDTH = w;
            DST_RECT_HEIGHT = h;
            mCamera.takePicture(mShutterCallback, null, mRectJpegPictureCallback);
        }
    }

    public Point doGetPictureSize() {
        Size s = mCamera.getParameters().getPictureSize();
        return new Point(s.width, s.height);
    }


    private void initCamera(float previewRate) {
        if (mCamera != null) {

            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
//			CamParaUtil.getInstance().printSupportPictureSize(mParams);
//			CamParaUtil.getInstance().printSupportPreviewSize(mParams);
            //设置PreviewSize和PictureSize
            Size pictureSize = CamParaUtil.getInstance().getPropPictureSize(
                    mParams.getSupportedPictureSizes(), previewRate, 800);
            mParams.setPictureSize(pictureSize.width, pictureSize.height);
            Size previewSize = CamParaUtil.getInstance().getPropPreviewSize(
                    mParams.getSupportedPreviewSizes(), previewRate, 800);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

            mCamera.setDisplayOrientation(mCurrentOrientation);

//			CamParaUtil.getInstance().printSupportFocusMode(mParams);
            List<String> focusModes = mParams.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            mCamera.setParameters(mParams);
            mCamera.startPreview();//开启预览


            isPreviewing = true;
            mPreviwRate = previewRate;

            mParams = mCamera.getParameters(); //重新get一次
            Log.i(TAG, "最终设置:PreviewSize--With = " + mParams.getPreviewSize().width
                    + "Height = " + mParams.getPreviewSize().height);
            Log.i(TAG, "最终设置:PictureSize--With = " + mParams.getPictureSize().width
                    + "Height = " + mParams.getPictureSize().height);
        }
    }


    /*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
    ShutterCallback mShutterCallback = new ShutterCallback()
            //快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
    {
        public void onShutter() {
            // TODO Auto-generated method stub
            Log.i(TAG, "myShutterCallback:onShutter...");
        }
    };
    PictureCallback mRawCallback = new PictureCallback()
            // 拍摄的未压缩原数据的回调,可以为null
    {

        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "myRawCallback:onPictureTaken...");

        }
    };
    /**
     * 常规拍照
     */
    PictureCallback mJpegPictureCallback = new PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            Log.i(TAG, "myJpegCallback:onPictureTaken...");
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
                isPreviewing = false;
            }
            //保存图片到sdcard
            if (null != b) {
                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
                //图片竟然不能旋转了，故这里要旋转下
                Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
                FileUtil.saveBitmap(rotaBitmap);
            }
            //再次进入预览
            mCamera.startPreview();
            isPreviewing = true;
        }
    };

    public void setScreenMetrics(Point screenMetrics){
        CameraInterface.screenMetrics = screenMetrics;
    }

    /**
     * 拍摄指定区域的Rect
     */
    PictureCallback mRectJpegPictureCallback = new PictureCallback()
            //对jpeg图像数据的回调,最重要的一个回调
    {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.i(TAG, "myJpegCallback:onPictureTaken...");
            Bitmap b = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
                mCamera.stopPreview();
                isPreviewing = false;
            }
            //保存图片到sdcard
            if (null != b) {
                //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
                //图片竟然不能旋转了，故这里要旋转下
                Bitmap rotaBitmap = ImageUtil.getRotateBitmap(b, 90.0f);
//                int x = rotaBitmap.getWidth() / 2 - DST_RECT_WIDTH / 2;
//                int y = rotaBitmap.getHeight() / 2 - DST_RECT_HEIGHT / 2;
                int x = (int) ((rotaBitmap.getWidth()*1f)/screenMetrics.x*focusPoint.x);
                int y = (int) ((rotaBitmap.getHeight()*1f)/screenMetrics.y*focusPoint.y);
                Log.i(TAG, "rotaBitmap.getWidth() = " + rotaBitmap.getWidth()
                        + " rotaBitmap.getHeight() = " + rotaBitmap.getHeight());
//                Toast.makeText(mApplication, x + "-" + y, Toast.LENGTH_SHORT).show();
                Bitmap rectBitmap = Bitmap.createBitmap(rotaBitmap, x, y, DST_RECT_WIDTH, DST_RECT_HEIGHT);
                FileUtil.saveBitmap(rectBitmap);
                if (rotaBitmap.isRecycled()) {
                    rotaBitmap.recycle();
                    rotaBitmap = null;
                }
                if (rectBitmap.isRecycled()) {
                    rectBitmap.recycle();
                    rectBitmap = null;
                }
            }
            //再次进入预览
            mCamera.startPreview();
            isPreviewing = true;
            if (!b.isRecycled()) {
                b.recycle();
                b = null;
            }

        }
    };

    public void setApp(Application application){
        mApplication = application;
    }


}

package org.yanzi.activity;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;

import org.yanzi.camera.CameraInterface;
import org.yanzi.camera.CameraInterface.CamOpenOverCallback;
import org.yanzi.camera.preview.CameraSurfaceView;
import org.yanzi.playcamera.R;
import org.yanzi.ui.ControllerView;
import org.yanzi.ui.MaskView;
import org.yanzi.util.DisplayUtil;

public class CameraActivity extends Activity implements CamOpenOverCallback {
	private static final String TAG = "YanZi";
	CameraSurfaceView surfaceView = null;
//	ImageButton shutterBtn;
	ControllerView maskView = null;
	float previewRate = -1f;
	int DST_CENTER_RECT_WIDTH = 200; //单位是dip
	int DST_CENTER_RECT_HEIGHT = 310;//单位是dip
	Point rectPictureSize = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread openThread = new Thread(){
			@Override
			public void run() {
				CameraInterface.getInstance().doOpenCamera(CameraActivity.this);
			}
		};
		openThread.start();
		setContentView(R.layout.activity_camera);
		initUI();
		initViewParams();
//		shutterBtn.setOnClickListener(new BtnListeners());
	}

	private void initUI(){
		surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
//		shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
		maskView = (ControllerView)findViewById(R.id.controller);
		maskView.setOnCheckedListener(new ControllerView.OnCheckedListener() {
			@Override
			public void onComplete(View view) {
				if(rectPictureSize == null){
					rectPictureSize = createCenterPictureRect(DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_WIDTH)
							,DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_HEIGHT));
				}
				CameraInterface.getInstance().doTakePicture(rectPictureSize.x, rectPictureSize.y);
			}
		});
	}

	private void initViewParams(){
		LayoutParams params = surfaceView.getLayoutParams();
		Point p = DisplayUtil.getScreenMetrics(this);
		params.width = p.x;
		params.height = p.y;
		Log.i(TAG, "screen: w = " + p.x + " y = " + p.y);
		previewRate = DisplayUtil.getScreenRate(this); //默认全屏的比例预览
		surfaceView.setLayoutParams(params);

		//手动设置拍照ImageButton的大小为120dip×120dip,原图片大小是64×64
//		LayoutParams p2 = shutterBtn.getLayoutParams();
//		p2.width = DisplayUtil.dip2px(this, 80);
//		p2.height = DisplayUtil.dip2px(this, 80);;
//		shutterBtn.setLayoutParams(p2);
	}

	@Override
	public void cameraHasOpened() {
		SurfaceHolder holder = surfaceView.getSurfaceHolder();
		CameraInterface.getInstance().doStartPreview(holder, previewRate);
		if(maskView != null){
			Rect screenCenterRect = createCenterScreenRect(DisplayUtil.dip2px(this, DST_CENTER_RECT_WIDTH)
					,DisplayUtil.dip2px(this, DST_CENTER_RECT_HEIGHT));
			maskView.setCenterRect(screenCenterRect);
		}
	}
//	private class BtnListeners implements OnClickListener{
//
//		@Override
//		public void onClick(View v) {
//			switch(v.getId()){
//			case R.id.btn_shutter:
//				if(rectPictureSize == null){
//					rectPictureSize = createCenterPictureRect(DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_WIDTH)
//							,DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_HEIGHT));
//				}
//				CameraInterface.getInstance().doTakePicture(rectPictureSize.x, rectPictureSize.y);
//				break;
//			default:break;
//			}
//		}
//
//	}
	
	/**生成拍照后图片的中间矩形的宽度和高度
	 * @param w 屏幕上的矩形宽度，单位px
	 * @param h 屏幕上的矩形高度，单位px
	 * @return
	 */
	private Point createCenterPictureRect(int w, int h){
		
		int wScreen = DisplayUtil.getScreenMetrics(this).x;
		int hScreen = DisplayUtil.getScreenMetrics(this).y;
		int wSavePicture = CameraInterface.getInstance().doGetPictureSize().y; //因为图片旋转了，所以此处宽高换位
		int hSavePicture = CameraInterface.getInstance().doGetPictureSize().x; //因为图片旋转了，所以此处宽高换位
		float wRate = (float)(wSavePicture) / (float)(wScreen);
		float hRate = (float)(hSavePicture) / (float)(hScreen);
		float rate = (wRate <= hRate) ? wRate : hRate;//也可以按照最小比率计算
		
		int wRectPicture = (int)( w * wRate);
		int hRectPicture = (int)( h * hRate);
		return new Point(wRectPicture, hRectPicture);
		
	}
	/**
	 * 生成屏幕中间的矩形
	 * @param w 目标矩形的宽度,单位px
	 * @param h	目标矩形的高度,单位px
	 * @return
	 */
	private Rect createCenterScreenRect(int w, int h){
		int x1 = DisplayUtil.getScreenMetrics(this).x / 10;
		int y1 = DisplayUtil.getScreenMetrics(this).y / 20;
		int x2 = x1 + w;
		int y2 = y1 + h;
		return new Rect(x1, y1, x2, y2);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CameraInterface.getInstance().doStopCamera();
	}
}

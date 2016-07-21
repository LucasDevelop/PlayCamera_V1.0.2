package org.yanzi.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.yanzi.camera.CameraInterface;
import org.yanzi.playcamera.R;
import org.yanzi.util.DisplayUtil;

/**
 * Author：lucas on 2016/7/20 14:44
 * Email：lucas_developer@163.com
 * Description：控制器层
 */
public class ControllerView extends RelativeLayout implements View.OnClickListener {

    private int mScreenX;
    private int mScreenY;
    private Paint mInnerPaint;
    private Rect mCenterRect;
    private Context mContext;
    private Paint mExternalPaint;
    //每次位移距离  px
    private int moveDistance = 10;
    private OnCheckedListener mListener;

    public ControllerView(Context context) {
        super(context, null);
    }

    public ControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint(context);
    }

    private void initPaint(Context context) {
        //获取屏幕的宽高
        Point point = DisplayUtil.getScreenMetrics(context);
        mScreenX = point.x;
        mScreenY = point.y;
        //初始化画笔
        mInnerPaint = new Paint();
        mInnerPaint.setColor(Color.RED);
        mInnerPaint.setStyle(Paint.Style.STROKE);
        mInnerPaint.setStrokeWidth(5f);
        mInnerPaint.setAlpha(50);
        //消除锯齿
        mInnerPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mExternalPaint = new Paint();
        mExternalPaint.setColor(Color.GRAY);
        mExternalPaint.setStyle(Paint.Style.FILL);
        mExternalPaint.setAlpha(180);
        mExternalPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        setBackgroundColor(Color.TRANSPARENT);
        View.inflate(mContext, R.layout.view_controller, this);
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.rootView);
        findViewById(R.id.top).setOnClickListener(this);
        findViewById(R.id.left).setOnClickListener(this);
        findViewById(R.id.right).setOnClickListener(this);
        findViewById(R.id.bottom).setOnClickListener(this);
        findViewById(R.id.calibration).setOnClickListener(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rootView.setLayoutParams(params);
        CameraInterface.getInstance().setScreenMetrics(DisplayUtil.getScreenMetrics(mContext));
    }

    //设置聚焦匡大小
    public void setCenterRect(Rect rect) {
        mCenterRect = rect;
        //刷新界面
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("ControllerView", "mCenterRect:" + mCenterRect);
        if (mCenterRect == null)
            return;
        //绘制阴影部分
        canvas.drawRect(0, 0, mScreenX, mCenterRect.top, mExternalPaint);
        canvas.drawRect(0, mCenterRect.bottom, mScreenX, mScreenY, mExternalPaint);
        canvas.drawRect(0, mCenterRect.top, mCenterRect.left, mCenterRect.bottom, mExternalPaint);
        canvas.drawRect(mCenterRect.right, mCenterRect.top, mScreenX, mCenterRect.bottom, mExternalPaint);

        canvas.drawRect(mCenterRect, mInnerPaint);
        super.onDraw(canvas);
    }


    @Override
    public void onClick(View v) {
        int rectWidth = mCenterRect.right - mCenterRect.left;
        int rectHeight = mCenterRect.bottom - mCenterRect.top;
        switch (v.getId()) {
            case R.id.top:
                int newTop = mCenterRect.top - moveDistance;
                mCenterRect.top = newTop < 0 ? 0 : newTop;
                mCenterRect.bottom = newTop < 0 ? mCenterRect.top + rectHeight : mCenterRect.bottom - moveDistance;
                break;
            case R.id.left:
                int newLeft = mCenterRect.left - moveDistance;
                mCenterRect.left = newLeft < 0 ? 0 : newLeft;
                mCenterRect.right = newLeft < 0 ? mCenterRect.left + rectWidth : mCenterRect.right - moveDistance;
                break;
            case R.id.right:
                int newRight = mCenterRect.right + moveDistance;
                mCenterRect.right = newRight > mScreenX ? mScreenX : newRight;
                mCenterRect.left = newRight > mScreenX ? mCenterRect.right - rectWidth : mCenterRect.left + moveDistance;
                break;
            case R.id.bottom:
                int newBottom = mCenterRect.bottom + moveDistance;
                mCenterRect.bottom = newBottom > mScreenY ? mScreenY : newBottom;
                mCenterRect.top = newBottom > mScreenY ? mCenterRect.bottom - rectHeight : mCenterRect.top + moveDistance;
                break;
            case R.id.calibration:
                if (mListener!=null)
                    mListener.onComplete(v);
                break;
            default:
                break;
        }
        CameraInterface.getInstance().setFocusPoint(mCenterRect.left,mCenterRect.top);
        invalidate();
    }

    public void setOnCheckedListener(OnCheckedListener listener){
        mListener = listener;
    }

    public interface OnCheckedListener{
        void onComplete(View view);
    }

}

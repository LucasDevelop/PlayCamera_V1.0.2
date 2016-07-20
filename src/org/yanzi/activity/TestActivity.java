package org.yanzi.activity;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.Toast;

import org.yanzi.playcamera.R;
import org.yanzi.ui.ControllerView;
import org.yanzi.util.DisplayUtil;

/**
 * Author��lucas on 2016/7/20 16:38
 * Email��lucas_developer@163.com
 * Description��TODO
 */
public class TestActivity extends Activity {
    int DST_CENTER_RECT_WIDTH = 200; //��λ��dip
    int DST_CENTER_RECT_HEIGHT = 310;//��λ��dip

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ControllerView view = (ControllerView) findViewById(R.id.controller);
        Rect screenCenterRect = createCenterScreenRect(DisplayUtil.dip2px(this, DST_CENTER_RECT_WIDTH)
                ,DisplayUtil.dip2px(this, DST_CENTER_RECT_HEIGHT));
        view.setCenterRect(screenCenterRect);
    }

    /**
     * ������Ļ�м�ľ���
     * @param w Ŀ����εĿ��,��λpx
     * @param h	Ŀ����εĸ߶�,��λpx
     * @return
     */
    private Rect createCenterScreenRect(int w, int h){
        int x1 = DisplayUtil.getScreenMetrics(this).x / 10;
        int y1 = DisplayUtil.getScreenMetrics(this).y / 20;
        int x2 = x1 + w;
        int y2 = y1 + h;
        return new Rect(x1, y1, x2, y2);
    }
}

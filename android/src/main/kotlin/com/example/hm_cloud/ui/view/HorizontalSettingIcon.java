package com.example.hm_cloud.ui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.hm_cloud.R;
import com.example.hm_cloud.utils.TouchUtils;
import com.orhanobut.logger.Logger;

/**
 * <pre>
 *     author: blankj
 *     blog  : http://blankj.com
 *     time  : 2019/08/26
 *     desc  :
 * </pre>
 */
public class HorizontalSettingIcon extends RelativeLayout {

    public HorizontalSettingIcon(Context context) {
        super(context);
        inflate(getContext(), R.layout.layout_setting_icon, this);
        TouchUtils.setOnTouchListener(this, new TouchUtils.OnTouchUtilsListener() {

            private int rootViewWidth;
            private int rootViewHeight;
            private int viewWidth;
            private int viewHeight;
            private int statusBarHeight;

            @Override
            public boolean onDown(View view, int x, int y, MotionEvent event) {
                viewWidth = view.getWidth();
                viewHeight = view.getHeight();
                View contentView = view.getRootView().findViewById(android.R.id.content);
                rootViewWidth = contentView.getWidth();
                rootViewHeight = contentView.getHeight();
//                statusBarHeight = getResources().getIdentifier("status_bar_height", "dimen", "android");
                processScale(view, true);
                return true;
            }

            @Override
            public boolean onMove(View view, int direction, int x, int y, int dx, int dy, int totalX, int totalY, MotionEvent event) {
                view.setX(Math.min(Math.max(0, view.getX() + dx), rootViewWidth - viewWidth));
                view.setY(Math.min(Math.max(statusBarHeight, view.getY() + dy), rootViewHeight - viewHeight));
                return true;
            }

            @Override
            public boolean onStop(View view, int direction, int x, int y, int totalX, int totalY, int vx, int vy, MotionEvent event) {
                stick2HorizontalSide(view);
                processScale(view, false);
                return true;
            }

            private void stick2HorizontalSide(View view) {
                view.animate()
                        .setInterpolator(new DecelerateInterpolator())
                        .translationX(view.getX() + viewWidth / 2f > rootViewWidth / 2f ? rootViewWidth - viewWidth : 0)
                        .setDuration(300)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                savePosition();
                            }
                        })
                        .start();
            }

            private void processScale(final View view, boolean isDown) {
                float value = isDown ? 1 - 0.1f : 1;
                view.animate()
                        .scaleX(value)
                        .scaleY(value)
                        .setDuration(100)
                        .start();
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        wrapPosition();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        savePosition();
    }

    private void savePosition() {
//        DebugConfig.saveViewX(this, (int) getX());
//        DebugConfig.saveViewY(this, (int) getY());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        wrapPosition();
    }

    private void wrapPosition() {
        post(new Runnable() {
            @Override
            public void run() {
                View contentView = getRootView().findViewById(android.R.id.content);
                if (contentView == null) return;
                setY(contentView.getHeight() * 0.70f);
                setX(getX() + getWidth() / 2f > contentView.getWidth() / 2f ? contentView.getWidth() - getWidth() : 0);
            }
        });
    }
}
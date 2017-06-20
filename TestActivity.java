package com.gif57.android.ui.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.gif57.android.R;
import com.gif57.android.ui.base.BaseActivity;
import com.gif57.android.utils.GLog;
import com.gif57.android.utils.GToast;
import com.gif57.android.utils.ScreenUtils;
import com.gif57.android.view.ProgressPostView;

/**
 * Created by Mr.Zdy on 2017/6/18.
 */

public class TestActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        ProgressPostDialog progressPostDialog = new ProgressPostDialog(thisActivity);
        progressPostDialog.show();
    }


    class ProgressPostDialog extends AlertDialog {


        ProgressPostView progressPostView;

        protected ProgressPostDialog(@NonNull Context context) {
            super(context, R.style.Dialog_Fullscreen);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.dialog_progress_post);

            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(thisActivity, android.R.color.transparent));


            findViewById(R.id.root).setBackground(new ShapeDrawable(new Shape() {
                @Override
                public void draw(Canvas canvas, Paint paint) {

                    paint.setColor(0xA0FFFFFF);
                    canvas.drawRoundRect(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()),
                            ScreenUtils.dip2px(getContext(), 6), ScreenUtils.dip2px(getContext(), 6), paint);

                }
            }));

            progressPostView = (ProgressPostView) findViewById(R.id.button1);
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
            valueAnimator.setDuration(2000);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    progressPostView.setCurrentProgress(animatedValue);
                }
            });
            valueAnimator.setTarget(progressPostView);
            valueAnimator.start();

//            final Handler handler = new Handler() {
//                @Override
//                public void handleMessage(Message msg) {
//                    super.handleMessage(msg);
//                    sendEmptyMessageDelayed(msg.what + 5, 1000);
//                    progressPostView.setCurrentProgress(msg.what);
//
//                }
//            };
//            handler.sendEmptyMessage(5);

        }
    }
}

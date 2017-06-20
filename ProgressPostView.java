package com.gif57.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.gif57.android.R;
import com.gif57.android.ui.GIFApplication;
import com.gif57.android.utils.GLog;
import com.gif57.android.utils.ScreenUtils;

/**
 * Created by Mr.Zdy on 2017/6/20.
 */

public class ProgressPostView extends View {
    private int color = 0xFFfff1b7;


    int viewWidth;
    int viewHeight;


    int drawableWidth;
    int drawableHeight;

    int currentProgress = 70;


    public ProgressPostView(Context context) {
        this(context, null);
    }

    public ProgressPostView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressPostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        viewWidth = (int) (ScreenUtils.getScreenWidth(GIFApplication.getINSTANCE()) / 2.4f);
        viewHeight = (int) (viewWidth * 1.2f);

        drawableWidth = viewWidth / 2;

        initView();
    }

    private void initView() {
        p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);

        temp = BitmapFactory.decodeResource(getResources(), R.drawable.progress_post_drawable_transparent);

        drawableHeight = temp.getHeight() * drawableWidth / temp.getWidth();

        temp = Bitmap.createScaledBitmap(temp, drawableWidth, drawableHeight, false);

        mask = BitmapFactory.decodeResource(getResources(), R.drawable.progress_post_drawable_white);
        mask = Bitmap.createScaledBitmap(mask, drawableWidth, drawableHeight, false);


    }

    private void refreshResource() {
        raw = Bitmap.createBitmap(temp.getWidth(), temp.getWidth(), Bitmap.Config.ARGB_8888);
        raw = Bitmap.createScaledBitmap(raw, drawableWidth, drawableHeight, false);

        Canvas c = new Canvas(raw);
        p.setXfermode(null);
        drawVector(c);
        c.drawBitmap(temp, 0, 0, p);
    }

    private void drawVector(Canvas c) {

        int arcHeight = drawableHeight / 10;

        int additionHeight = drawableHeight - currentProgress * drawableHeight / 100;

        GLog.v(String.valueOf(additionHeight));

        c.drawRect(new Rect(0, additionHeight, c.getWidth(), c.getHeight()), p);
    }


    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }

    Bitmap temp;

    Bitmap raw;

    Bitmap mask;

    Paint p;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        refreshResource();

        int offestX = (viewWidth - drawableWidth) / 2;
        int offestY = (int) (viewHeight * 0.1f);

        int layerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        {
            p.setXfermode(null);
            canvas.drawBitmap(mask, offestX, offestY, p);

            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(raw, offestX, offestY, p);
        }
        canvas.restoreToCount(layerId);


    }


}

package com.feaoes.gesturesvg.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.SurfaceHolder;

/**
 * Created by ff on 2015/11/24.
 */
public class OLPath {
    private Path mPath = new Path();

    public Paint getmPaint() {
        return mPaint;
    }

    public Path getmPath() {
        return mPath;
    }

    private Paint mPaint = new Paint();

    private float mX=0, mY=0;
    private static final float TOUCH_TOLERANCE = 4;


    public OLPath(float width,int color){
        super();
        initPaint(width,color);
    }


    private void initPaint(float width, int color) {
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(width);
        mPaint.setColor(color);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setDither(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setPath(Path path){
        this.mPath = path;
    }

    public void addPath(Path path){
        this.mPath.addPath(path);
    }
    public void draw(Canvas canvas){
        canvas.drawPath(mPath, mPaint);
    }
    public void draw(SurfaceHolder holder){
        Canvas canvas = holder.lockCanvas();
        canvas.drawPath(mPath,mPaint);
        holder.unlockCanvasAndPost(canvas);
    }

    public void touchDown(float x, float y) {
        mPath.moveTo(x, y);
//        mPath.lineTo(x, y);
        mX = x;
        mY = y;
    }

    public void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
//            mPath.lineTo(x, y);
        }
    }

    public void touchUp(float x, float y) {
//        mPath.lineTo(mX, mY);
        mX=x;
        mY=y;
    }
}

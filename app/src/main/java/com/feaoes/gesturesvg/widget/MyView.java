package com.feaoes.gesturesvg.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.feaoes.gesturesvg.R;
import com.feaoes.gesturesvg.util.MemoryInt;
import com.feaoes.gesturesvg.util.OLPath;
import com.feaoes.gesturesvg.util.Paths;

import svgandroid.SVGParser;


/**
 * Created by ff on 2015/11/27.
 */
public class MyView extends View implements View.OnTouchListener {

    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mPaint;
    private Path mPath;
    private ScaleGestureDetector scaleDetector;
    private GestureDetector mDetector;
    private Matrix mMatrix;


    //-------------------
    private float mInitScale = 1.0f;
    private float mMidScale = 2.0f;
    private float mMaxScale = 4.0f;
    private float mLastX;
    private float mLastY;


    //------------
    private Touch_Type touchType = Touch_Type.NONE;
    private boolean isAutoScale;
    private Bitmap svgBitmap;
    private Canvas svgCanvas;

    //------------
    private MemoryInt mPointNum;

    private enum Touch_Type{
        ZOOM,MOVE,SWIPE,NONE

    }

    private class AutoScaleRunnable implements  Runnable{


        private float mTargetScale;
        private float x;
        private float y;

        private final float BIGGER = 1.15f;
        private final float SMALL = 0.8f;

        private float tempScale = 1.0f;
        public AutoScaleRunnable(float mTargetScale,float x,float y){
            this.mTargetScale = mTargetScale;
            this.x = x;
            this.y = y;


            if(getScale()<mTargetScale){
                tempScale = BIGGER;
            }
            if(getScale()>mTargetScale){
                tempScale = SMALL;
            }
        }
        @Override
        public void run() {
            float currentScale = getScale();
            mMatrix.postScale(mTargetScale/currentScale,mTargetScale/currentScale,x,y);
            checkBorderAndCenterWhenScale();
            postInvalidate();
            isAutoScale = false;
            touchType= Touch_Type.NONE;
        }
    }

    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        svgBitmap =Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
        svgCanvas = new Canvas(svgBitmap);
        mCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.aa), 0, 0, null);
        svgCanvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.aa), 0, 0, null);
    }

    public void clickDrawPath(){
        OLPath olPath = new OLPath(3, Color.WHITE);
        mPath = SVGParser.parsePath(Paths.INDOMINUS_REX);

        Matrix m = new Matrix();
        m.setScale(0.5f,0.5f);
        mPath.transform(m);
        olPath.addPath(mPath);
        olPath.draw(mCanvas);
        olPath.draw(svgCanvas);
        postInvalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawColor(Color.DKGRAY);
        drawLine(canvas);
        switch (touchType){
            case MOVE:
            case ZOOM:
            case SWIPE:
                canvas.drawBitmap(mBitmap, mMatrix, null);
                break;
            case NONE:
                clearCanvas(svgCanvas);
                if(mPath!=null){
                    OLPath newPath = new OLPath(3 * getScale(), Color.WHITE);
                    mPath.transform(mMatrix, newPath.getmPath());
                    svgCanvas.drawPath(newPath.getmPath(),newPath.getmPaint());
                }

                canvas.drawBitmap(svgBitmap, 0,0, null);
        }
    }


    private void drawLine(Canvas canvas) {
        //TODO
    }

    /**
     * 获取Matrix的缩放值
     * @return
     */
    private float getScale(){
        float[] floats = new float[9];
        mMatrix.getValues(floats);
        return floats[Matrix.MSCALE_X];
    }



    /**
     * 计算应该显示的缩放值
     * @return 缩放值
     */
    private float calcDesScale(){
        float scale = getScale();

        //当前窗口大小小于初始化时的大小
        if(scale<mInitScale){
            return mInitScale/scale;
        }else if(scale>mMaxScale){
            return mMaxScale/scale;
        }else{
            return 1.0f;
        }

    }

    /**
     * 获取缩放后的宽和高，以及l,t,r,b
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix tempMatrix = mMatrix;
        RectF rectF = new RectF();

        int width = svgCanvas.getWidth();
        int height = svgCanvas.getHeight();

        rectF.set(0,0,width,height);
        tempMatrix.mapRect(rectF);
        return rectF;
    }


    private void init(Context context) {
        mPaint = new Paint();

        mMatrix = new Matrix();//记录当前canvas的缩放，大小和位置

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);


        setOnTouchListener(this);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                    float scaleFactor = detector.getScaleFactor();
                if(getScale()>=mMaxScale&&scaleFactor>1){
                    return true;
                }
                mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                postInvalidate();
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                touchType = Touch_Type.ZOOM;
                Log.e("onTouch","onScaleBegin");
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                touchType = Touch_Type.NONE;
                float desScale = calcDesScale();
                Log.e("onTouch","onScaleEnd");
                mMatrix.postScale(desScale, desScale, detector.getFocusX(), detector.getFocusY());
                checkBorderAndCenterWhenScale();
                postInvalidate();
            }
        });
        mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {

                if(isAutoScale) return true;
                float x = e.getX();
                float y = e.getY();
                touchType= Touch_Type.SWIPE;
                isAutoScale = true;
                if(getScale()<mMidScale||getScale()==mInitScale){
                    postDelayed(new AutoScaleRunnable(mMidScale, x, y), 10);
                }else if(getScale()<mMaxScale||getScale()==mMidScale){
                    postDelayed(new AutoScaleRunnable(mMaxScale, x, y), 10);
                }else{
                    postDelayed(new AutoScaleRunnable(mInitScale, x, y), 10);
                }

                return true;
            }

        });

    }

    /**
     * 清除画布
     * @param mCanvas
     */
    private void clearCanvas(Canvas mCanvas) {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }


    /**
     * 边界检测
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rect = getMatrixRectF();

        float dx = 0;
        float dy = 0;

        int width = getWidth();
        int height = getHeight();
        //放大后，drawable大小比控件大
        if(rect.width()>=width){
            if(rect.left>0){
                dx = -rect.left;
            }
            if(rect.right<width){
                dx = width-rect.right;
            }
        }

        if(rect.height()>=height){

            if(rect.top>0){
                dy = -rect.top;
            }
            if(rect.bottom<height){
                dy = height-rect.bottom;
            }
        }
        //缩小后，drawable大小比控件小
        if(rect.width()<width){
            dx = width/2-rect.left-rect.width()/2;
        }
        if(rect.height()<height){
            dy = height/2-rect.top-rect.height()/2;
        }

        mMatrix.postTranslate(dx,dy);
    }

    /**
     * 边界检测，当在位移的时候
     */
    private void checkBorderWhenTranslate() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        if(rectF.top>0 ){
            deltaY = -rectF.top;
        }
        if(rectF.bottom<height){
            deltaY = height - rectF.bottom;
        }

        if(rectF.left>0 ){
            deltaX = -rectF.left;
        }

        if(rectF.right<width){
            deltaX = width - rectF.right;
        }

        mMatrix.postTranslate(deltaX,deltaY);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        mDetector.onTouchEvent(event);

        if(touchType== Touch_Type.ZOOM){
            return true;
        }
        //touch_type = MOVE or NONE
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_MOVE:
                if(mPointNum.isDecreased()){//从多手势缩放ACTION_POINTER_UP后调用的，所以，当再次调用ACTION_DOWN之前，都不走MOVE事件
                    return true;
                }
                Log.e("onTouch","ACTION_MOVE");
                touchType = Touch_Type.MOVE;
                float dx = Math.abs(x - mLastX);
                float dy = Math.abs(y - mLastY);

                if(dx>3||dy>3){
                    mMatrix.postTranslate(x-mLastX,y-mLastY);
                    checkBorderWhenTranslate();
                    postInvalidate();
                }

                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_DOWN:
                Log.e("onTouch","ACTION_DOWN");
                mPointNum = new MemoryInt(1);
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_POINTER_UP://
                mPointNum.decrease();
                touchType = Touch_Type.NONE;
                Log.e("onTouch","ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mPointNum.increase();
                touchType = Touch_Type.ZOOM;
                Log.e("onTouch","ACTION_POINTER_DOWN");
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                Log.e("onTouch","ACTION_UP");
                touchType = Touch_Type.NONE;
                mPointNum.setCurrentNum(0);
                RectF rectF = getMatrixRectF();
                if(calcDesScale()>mInitScale){//处于放大的状态
                    //TODO
                }else if(calcDesScale()<=mInitScale){//处于缩小的状态,最小为1.0f
                    //TODO
                }
                postInvalidate();

                break;
        }

        return true;
    }
    
}

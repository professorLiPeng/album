package com.example.mchenys.myalbumlist.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by chenys on 2015/6/20.
 * 用到的API:
 * 1.OnGlobalLayoutListener用于对图片的初始缩放
 * 2.OnScaleGestureListener用于处理多点触控
 * 3.OnTouchListener用于捕获触摸事件
 * 4.Matrix处理平移和缩放
 * 5.GestureDetector处理双击放大缩小
 * 6.通过子线程实现缓慢的双击放大和缩小的效果
 */
public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {
    //---------------------------图片的缩放-------------------------
    /**
     * 第一次进来
     */
    private boolean mOnce = false;
    /**
     * 初始化时缩放的值,即最小值
     */
    private float mInitScale;
    /**
     * 双击放大达到的值
     */
    private float mMidScale;
    /**
     * 放大的最大值
     */
    private float mMaxScale;

    private Matrix mScaleMatrix;

    /**
     * 用于多点触控的类,可以捕获用户想要缩放图片的比例
     */
    private ScaleGestureDetector mScaleGestureDetector;

    //----------------------图片发大后的自由移动------------------------
    /**
     * 记录上一次多点触控的数量
     */
    private int mLastPointerCount;

    //记录最后一次的触控点的中心点坐标位置
    private float mLastX;
    private float mLastY;
    /**
     * 是否需要检测图片放大后移动过程中的上下左右的边界
     */
    private boolean isCheckLeftAndReight;
    private boolean isCheckTopAndBotton;
    /**
     * 最小滑动距离
     */
    private int mTouchSlop;
    /**
     * 标记是否可以滑动
     */
    private boolean isCanDrag;

    //----------------------双击放大与缩小-------------------------------
    private GestureDetector mGestureDetector;
    private boolean isAutoScale; //用于表示正在双击变大的标记
    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //创建Matrix
        mScaleMatrix = new Matrix();
        //设置ScaleType
        setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        //设置触摸监听
        setOnTouchListener(this);

        //最小滑动距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        //初始化GestureDetector
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            //SimpleOnGestureListener是GestureDetector的空实现类
            //双击
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale) {
                    return true;
                }
                //获取用户双击时的坐标位置,作为缩放的中心点
                float x = e.getX();
                float y = e.getY();

                if (getScale() < mMidScale) {
//                    mScaleMatrix.postScale(mMidScale / getScale(), mMidScale / getScale(), x, y);
//                    setImageMatrix(mScaleMatrix);
                    //当前缩放比例小于中间值,代表想放大,通过线程实现缓慢的放大
                    postDelayed(new AutoScaleRunnable(mMidScale, x, y), 16);
                    isAutoScale = true;
                } else {
//                    mScaleMatrix.postScale(mInitScale / getScale(), mInitScale / getScale(), x, y);
//                    setImageMatrix(mScaleMatrix);
                    //当前缩放比例大于中间值,代表想缩小,通过线程实现缓慢的缩小
                    postDelayed(new AutoScaleRunnable(mInitScale, x, y), 16);
                    isAutoScale = true;
                }
                return true;
            }
        });
    }

    /**
     * 通过线程实现双击缓慢放大图片的效果
     */
    private class AutoScaleRunnable implements Runnable {
        /**
         * 缩放的目标值
         */
        private float mTargetScale;
        /**
         * 缩放的中心点
         */
        private float x,y;
        /**
         * 缩放的梯度
         */
        private final float BIGGER = 1.07f;
        private final float SMALL = 0.93f;
        /**
         * 临时缩放比例
         */
        private  float tempScale;
        public AutoScaleRunnable(float mTargetScale, float y, float x) {
            this.mTargetScale = mTargetScale;
            this.y = y;
            this.x = x;
            if (getScale() < mTargetScale) {
                //代表想放大
                tempScale = BIGGER;
            }
            if (getScale() > mTargetScale) {
                //代表想缩小
                tempScale = SMALL;
            }
        }
        @Override
        public void run() {
            //进行缩放
            mScaleMatrix.postScale(tempScale, tempScale, x, y);
            checkBorderAndCenterWhenScanle();
            setImageMatrix(mScaleMatrix);
            float currentScale = getScale();
            if ((tempScale > 1.0f && currentScale < mTargetScale) || (tempScale < 1.0f && currentScale > mTargetScale)) {
                //正常范围,允许操作,通过postDelayed每16毫秒执行一次线程的run方法
                postDelayed(this, 16);
            } else {
                //达到目标值,设定为目标值
                float scale = mTargetScale / currentScale;
                mScaleMatrix.postScale(scale, scale, x, y);
                checkBorderAndCenterWhenScanle();
                setImageMatrix(mScaleMatrix);
                isAutoScale = false;
            }
        }
    }

    /**
     * 当view显示在window时回调
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    /**
     * 当view在window中移除时回调
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 全局的布局完成后的监听回调,对图片进行缩放
     */
    @Override
    public void onGlobalLayout() {
        //获取imageview加载完成的图片,根据图片的大小和屏幕的大小进行图片的缩放
        if (!mOnce) {
            //得到控件的宽和高
            int width = getWidth();
            int height = getHeight();
            //得到图片及宽和高
            Drawable drawable = getDrawable();
            if (drawable == null) {
                return;
            }
            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            float scale = 1.0f;
            /**
             * 如果图片的宽度大于控件的宽度,但是高度小于控件的高度,将其按宽度缩小
             */
            if (drawableWidth > width && drawableHeight < height) {
                scale = width * 1.0f / drawableWidth;
            }
            /**
             * 如果图片的高度大于控件的高度,但是宽度小于控件的宽度,将其按高度缩小
             */
            if (drawableHeight > height && drawableWidth < width) {
                scale = height * 1.0f / drawableHeight;
            }
            /**
             * 如果图片的宽度和高度都大于控件的宽度和高度,按图片宽高的最小值进行缩小
             */
            if (drawableWidth > width && drawableHeight > height) {
                scale = Math.min(width * 1.0f / drawableWidth, height * 1.0f / drawableHeight);
            }
            /**
             * 如果图片的宽高都小于于控件的宽高,按图片宽高的最小值进行放大
             */
            if (drawableWidth < width && drawableHeight < height) {
                scale = Math.min(width * 1.0f / drawableWidth, height * 1.0f / drawableHeight);
            }
            /**
             * 得到初始化时缩放的比例
             */
            mInitScale = scale;
            /**
             * 设置最大和最小缩放比例
             */
            mMaxScale = mInitScale * 4;
            mMidScale = mInitScale * 2;

            //移动图片到控件的中央,图片开始时时在0,0坐标点的位置显示的.要移动到控件的中央,移动的x和y距离如下
            int dx = getWidth() / 2 - drawableWidth / 2;
            int dy = getHeight() / 2 - drawableHeight / 2;

            //通过Matrix的post方法对图片进行平移和缩放
            mScaleMatrix.postTranslate(dx, dy);//平移
            mScaleMatrix.postScale(mInitScale, mInitScale, width / 2, height / 2);//缩放,最后2个参数是指定缩放的中心点
            //应用Matrix
            setImageMatrix(mScaleMatrix);

            mOnce = true;
        }

    }

    /**
     * 获取当前图片的缩放比例,区间在[initScale,maxScale]
     */
    public float getScale() {
        float[] values = new float[9];
        //通过ScaleMatrix拿到所有的Matrix,设置到values数组中,Matrix是一个3*3的矩阵,里面包含了缩放和平移的值
        mScaleMatrix.getValues(values);
        //返回缩放值,Matrix.MSCALE_X表示x轴方向的缩放值
        return values[Matrix.MSCALE_X];
    }

    /**
     * OnScaleGestureListener的3个监听方法,用于处理多点触控
     *
     * @param detector
     * @return
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        //拿到当前缩放的比例,区间[initScale,maxScale]
        float scale = getScale();
        //拿到用户手指的缩放值
        float scaleFactor = detector.getScaleFactor();
        if (getDrawable() == null) {
            return true;
        }
        //缩放范围的控制
        if ((scale < mMaxScale && scaleFactor > 1.0f) || (scale > mInitScale && scaleFactor < 1.0f)) {
            //表示允许用户放大和允许用户缩小

            if (scale * scaleFactor < mInitScale) {
                //当前缩放*用户的缩放小于了最小的缩放,则将用户的缩放设置为mInitScale / scale
                scaleFactor = mInitScale / scale;
            }
            if (scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / scale;
            }
        }

        //应用缩放到图片
        mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());//最后2个参数表示,以用户的手指作为缩放中心点
        //在缩放的过程中不断的调整图片的位置,使其一直保持在中央显示
        checkBorderAndCenterWhenScanle();
        setImageMatrix(mScaleMatrix);
        return true;//保证事件的继续
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;//必须true
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    /**
     * 触摸监听的回调方法
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (mGestureDetector.onTouchEvent(event)) {
            //将事件传递给mGestureDetector,如果是双击的时候是不能进行移动和多点放大缩小的,因此return true;表示消化当前事件
            return true;
        }
        //把触摸的event对象传递给ScaleGestureDetector.onTouchEvent,让ScaleGestureDetector可以操作触摸事件
        mScaleGestureDetector.onTouchEvent(event);

        //----------------以下是对放大的图片进行自由移动的处理代码--------------------------
        //定义2个用于存储多点触控的中心点
        float x = 0;
        float y = 0;

        //获取触控的点个数
        int pointerCount = event.getPointerCount();

        for (int i = 0; i < pointerCount; i++) {
            //累加每个触控点的x,y坐标位置
            x += event.getX(i);
            y += event.getY(i);
        }
        //求出多个触控点的平均中心点位置
        x /= pointerCount;
        y /= pointerCount;
        if (mLastPointerCount != pointerCount) {
            //进来表示手指的个数发生了改变
            isCanDrag = false;
            //记录最后一次的触控点的中心点坐标位置
            mLastX = x;
            mLastY = y;
        }
        mLastPointerCount = pointerCount;
        RectF rectF = getMatrixRectF();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //处理与viewpage的滑动事件冲突,不允许父控件对图片放大后平移事件的拦截,如果不做处理,那么图片放大后就不能实现自由平移了,而是直接切换到下一张或上一张图片了
                //判断的依据就是获取当前图片显示的区域与控件大小做比对
                if (rectF.width() > getWidth()+0.01 || rectF.height() > getHeight()+0.01) {
                    if (getParent() instanceof ViewParent) {
                        getParent().requestDisallowInterceptTouchEvent(true);//请求不允许父控件拦截
                    }
                }

                float dx = x - mLastX; //x方向移动的距离
                float dy = y - mLastY; //y方向移动的距离
                if (!isCanDrag) {
                    //与系统的滑动最小距离做判断,看看用户当前的滑动是否有效
                    isCanDrag = isMoveAction(dx, dy);
                }
                if (isCanDrag) {
                    //完成图片的移动

                    if (null != getDrawable()) {
                        isCheckLeftAndReight = isCheckTopAndBotton = true;
                        if (rectF.width() < getWidth()) {
                            //如果宽度小于控件宽度,不允许横向移动
                            isCheckLeftAndReight = false;
                            dx = 0;
                        }
                        if (rectF.height() < getHeight()) {
                            //如果高度小于控件的高度,不允许纵向移动
                            isCheckTopAndBotton = false;
                            dy = 0;
                        }
                        //应用移动
                        mScaleMatrix.postTranslate(dx, dy);
                        checkBorderAndCenterWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mLastPointerCount = 0;
                break;
            case MotionEvent.ACTION_DOWN:
                //处理与viewpage的滑动事件冲突,不允许父控件对图片放大后平移事件的拦截,如果不做处理,那么图片放大后就不能实现自由平移了,而是直接切换到下一张或上一张图片了
                //判断的依据就是获取当前图片显示的区域与控件大小做比对
                if (rectF.width() > getWidth()+0.01 || rectF.height() > getHeight()+0.01) {
                    if (getParent() instanceof ViewParent) {
                        getParent().requestDisallowInterceptTouchEvent(true);//请求不允许父控件拦截
                    }

                }
                break;
        }

        return true;//必须
    }


    /**
     * 判断是否足以触发move事件
     * 根据dx和dy的勾股定理求出滑动的距离与系统的最小滑动距离作比较
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isMoveAction(float dx, float dy) {

        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }

    /**
     * 获取图片缩放后的宽高,及l,t,r,b四个顶点的坐标位置记录到矩形中
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = this.mScaleMatrix;
        RectF rectF = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

            //通过Matrix获取图片缩放后的区域,保存到rectF中
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    /**
     * 在缩放的过程中不断的调整图片的位置,使其一直保持在中央显示
     */
    private void checkBorderAndCenterWhenScanle() {
        //获取图片缩放后的位置区域
        RectF rectF = getMatrixRectF();
        //定义2个变量代表图片缩放后超出屏幕的x,y距离
        float delx = 0;
        float dely = 0;

        //控件的宽高
        int width = getWidth();
        int height = getHeight();

        //如果图片的宽度超出了控件的宽,那么这需要对图片进行水平方向平移操作
        if (rectF.width() >= width) {
            if (rectF.left > 0) {
                //代表屏幕左边有空隙,需要向左平移,弥补空隙
                delx = -rectF.left;

            }
            if (rectF.right < width) {
                //代表屏幕的右边有空隙,需要向右平移,弥补空隙
                delx = width - rectF.right;
            }
        }
        //如果图片的高度超出了控件的高,那么这需要对图片进行垂直方向平移操作
        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                //代表屏幕的顶部有空隙,需要向上平移,弥补空隙
                dely = -rectF.top;
            }
            if (rectF.bottom < height) {
                //代表屏幕的底部有间隙,需要向下平移,弥补空隙
                dely = height - rectF.bottom;
            }
        }
        //如果宽度或者高度小于控件的宽或者高,则让其居中显示
        if (rectF.width() <= width) {
//            //水平方向偏移量
//            float destance = rectF.left + rectF.width() / 2.0f;
//            if (destance < width / 2.0f) {
//                //图片在屏幕一半的左边,需要向右平移到水平正中
//                delx = (width / 2.0f - rectF.right) + rectF.width() / 2.0f;
//            } else if (destance > width / 2.0f) {
//                //图片在屏幕一半的右边,需要向左平移到屏幕正中
//                delx = -(rectF.left - width / 2.0f + rectF.width() / 2.0f);
//            } =
            delx = width / 2.0f - rectF.right + rectF.width() / 2.0f;
        } else if (rectF.height() <= height) {
//            //垂直方向偏移量
//            float destance = rectF.top + rectF.height() / 2.0f;
//            if (destance < height) {
//                //图片在屏幕一半的上边,需要向下平移到屏幕正中
//                dely = height / 2.0f - rectF.bottom + rectF.height() / 2.0f;
//            } else if (destance > height) {
//                //图片在屏幕一半的下边,需要向上平移到屏幕正中
//                dely = -(rectF.top - height / 2.0f + rectF.height() / 2.0f);
//            }
            dely = height / 2.0f - rectF.bottom + rectF.height() / 2.0f;
        }
        //对图片进行平移操作
        mScaleMatrix.postTranslate(delx, dely);
    }

    /**
     * 在移动时进行边界检查,当图片的宽高大于控件时
     */

    private void checkBorderAndCenterWhenTranslate() {
        RectF rectF = getMatrixRectF();
        //偏移量
        float delx = 0, dely = 0;

        //控件宽高
        int width = getWidth();
        int height = getHeight();

        if (rectF.top > 0 && isCheckTopAndBotton) {
            dely = -rectF.top;//往上移动
        }
        if (rectF.bottom < height && isCheckTopAndBotton) {
            dely = height - rectF.bottom;//向下移动
        }
        if (rectF.left > 0 && isCheckLeftAndReight) {
            delx = -rectF.left; //向左移动
        }
        if (rectF.right < width && isCheckLeftAndReight) {
            delx = width - rectF.right;//向右移动
        }
        mScaleMatrix.postTranslate(delx, dely);
    }

}

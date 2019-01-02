package com.wuc.weixin;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * @author: wuchao
 * @date: 2018/12/31 21:28
 * @desciption: 自定义图标和文本颜色渐变 进行绘图操作，并向外提供改变透明度和图标的方法
 */
public class ChangeColorIconWithText extends View {

    private static final String INSTANCE_STATUS = "instance_status";
    private static final String STATUS_ALPHA = "status_alpha";
    /**
     * 图标默认背景色
     */
    private final int DEFAULT_ICON_BACKGROUND_COLOR = 0xFF45C01A;
    /**
     * 图标背景色
     */
    private int mColor = 0xFF45C01A;
    /**
     * 图标
     */
    private Bitmap mIconBitmap;
    /**
     * 图标底部文本
     */
    private String mText = "微信";
    /**
     * 默认字体12sp
     */
    private int mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            12, getResources().getDisplayMetrics());
    private Bitmap mBitmap;
    /**
     * 透明度（0.0-1.0）
     */
    private float mAlpha;
    /**
     * 图标绘制区域
     */
    private Rect mIconRect;
    /**
     * 文本绘制区域
     */
    private Rect mTextRect;
    /**
     * 文本画笔
     */
    private Paint mTextPaint;

    public ChangeColorIconWithText(Context context) {
        this(context, null);
    }

    public ChangeColorIconWithText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChangeColorIconWithText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChangeColorIconWithText);
        mColor = typedArray.getColor(R.styleable.ChangeColorIconWithText_color, DEFAULT_ICON_BACKGROUND_COLOR);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) typedArray.getDrawable(R.styleable.ChangeColorIconWithText_icon);
        if (bitmapDrawable != null) {
            mIconBitmap = bitmapDrawable.getBitmap();
        }
        mText = typedArray.getString(R.styleable.ChangeColorIconWithText_text);
        //默认字体大小12sp
        mTextSize = (int) typedArray.getDimension(R.styleable.ChangeColorIconWithText_text_size,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        12, getResources().getDisplayMetrics()));
        typedArray.recycle();

        init();
    }

    /**
     * 初始化画笔、文本显示范围
     */
    private void init() {
        mTextRect = new Rect();
        mTextPaint = new Paint();
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mColor);
        //将TextView 的文本放入一个矩形中， 测量TextView的高度和宽度
        mTextPaint.getTextBounds(mText, 0, mText.length(), mTextRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制原图标
        canvas.drawBitmap(mIconBitmap, null, mIconRect, null);
        //math.ceil(x)返回大于等于参数x的最小整数,即对浮点数向上取整(math.ceil(8.4)=9)
        int alpha = (int) Math.ceil(255 * mAlpha);
        //内存去准备mBitmap，setAlpha，纯色，xfermode，图标
        setupTargetBitmap(alpha);
        // 1、绘制原文本 ； 2、绘制变色的文本
        drawSourceText(canvas, alpha);
        drawTargetText(canvas, alpha);
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    /**
     * 在内存中绘制可变色的Icon
     * 在mBitmap上绘制以mColor颜色为Dst，DST_IN模式下的图标
     *
     * @param alpha Src颜色的透明度
     */
    private void setupTargetBitmap(int alpha) {
        mBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);

        Paint paint = new Paint();
        paint.setColor(mColor);
        //抗锯齿
        paint.setAntiAlias(true);
        //防抖动
        paint.setDither(true);
        //设置画笔的透明度,取值范围为0~255，数值越小越透明
        paint.setAlpha(alpha);
        //在图标背后先绘制一层mColor颜色的背景
        canvas.drawRect(mIconRect, paint);
        //过度模式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        paint.setAlpha(255);
        //在mBitmap上绘制以iconBackgroundColor颜色为Dst，DST_IN模式下的图标
        canvas.drawBitmap(mIconBitmap, null, mIconRect, paint);
    }

    /**
     * 绘制默认状态下的字体
     *
     * @param canvas Canvas
     * @param alpha  字体颜色透明度
     */
    private void drawSourceText(Canvas canvas, int alpha) {
        mTextPaint.setColor(0xff333333);
        mTextPaint.setAlpha(255 - alpha);
        int x = getMeasuredWidth() / 2 - mTextRect.width() / 2;
        int y = mIconRect.bottom + mTextRect.height();
        canvas.drawText(mText, x, y, mTextPaint);

    }

    /**
     * 绘制滑动到该标签时的字体
     *
     * @param canvas Canvas
     * @param alpha  字体颜色透明度
     */
    private void drawTargetText(Canvas canvas, int alpha) {
        mTextPaint.setColor(mColor);
        mTextPaint.setAlpha(alpha);
        int x = getMeasuredWidth() / 2 - mTextRect.width() / 2;
        int y = mIconRect.bottom + mTextRect.height();
        canvas.drawText(mText, x, y, mTextPaint);

    }

    /**
     * 保存状态
     *
     * @return Parcelable
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState());
        bundle.putFloat(STATUS_ALPHA, mAlpha);
        return bundle;
    }

    /**
     * 恢复状态
     *
     * @param state Parcelable
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mAlpha = bundle.getFloat(STATUS_ALPHA);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //因为图标是正方形且需要居中显示的，所以View的大小去掉padding和文字所占空间后，
        //剩余的空间的宽和高的最小值才是图标的边长
        int iconWidth = Math.min(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(),
                getMeasuredHeight() - getPaddingTop() - getPaddingBottom() - mTextRect.height());
        int left = getMeasuredWidth() / 2 - iconWidth / 2;
        int top = getMeasuredHeight() / 2 - (mTextRect.height() + iconWidth) / 2;
        //获取图标的绘制范围
        mIconRect = new Rect(left, top, left + iconWidth, top + iconWidth);
    }

    /**
     * 设置图标透明度并重绘
     *
     * @param alpha 透明度
     */
    public void setIconAlpha(float alpha) {
        this.mAlpha = alpha;
        invalidateView();
    }

    /**
     * 判断当前是否为UI线程，是则直接重绘，否则调用postInvalidate()利用Handler来重绘
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }
}

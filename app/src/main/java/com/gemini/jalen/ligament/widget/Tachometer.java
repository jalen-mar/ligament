package com.gemini.jalen.ligament.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.gemini.jalen.ligament.R;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static java.lang.Math.PI;

public class Tachometer extends View {
    private int mFinalHeight;//控件最终高度
    private int mFinalWidth;//控件最终宽度

    private int circleWidth = 5;//中间圆圈线条宽度
    private int mCircleRadius;//中间圆圈的半径
    private DrawFilter mDrawFilter;

    private int mPaintDefaultColor;//射线和圆圈的默认颜色
    private int mSolidCircleRadius;//内部实体圆的半径

    private int mCentreX;//画布中点x坐标
    private int mCentreY;//画布中点Y坐标

    private int mRayOutRadius;//射线外半径
    private Region mRayInnerRegion;//射线内侧区域坐标值集合
    private Region mRayOuterRegion;//射线外侧区域坐标值集合
    private float mRadianByPos;


    private SweepGradient mLowValueSg;
    private SweepGradient mNormalValueSg;
    private SweepGradient mHighValueSg;

    private float max, min, optimalMax, optimalMin, unitScale;
    private String unit, title;
    private boolean alarm;

    enum _Quadrant {
        eQ_NONE,                                    //  在坐标轴上
        eQ_ONE,                                        //  第一象限
        eQ_TWO,                                        //	第二象限
        eQ_THREE,                                    //	第三象限
        eQ_FOUR                                        //	第四象限
    }

    public Tachometer(Context context) {
        super(context);
    }

    public Tachometer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
        mPaintDefaultColor = context.getResources().getColor(R.color.tachometerBackground);
        int lowStartColor = Color.parseColor("#d32f2f");//低血糖颜色
        int lowEndColor = Color.parseColor("#e91e63");//偏低颜色

        int normalStartColor = Color.parseColor("#009688");
        int normalEndColor = Color.parseColor("#009688");

        int highStartColor = Color.parseColor("#f44336");
        int highEndColor = Color.parseColor("#d32f2f");

        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        //偏低范围渐变颜色组
        mLowValueSg = new SweepGradient(0, 0, new int[]{lowStartColor, lowEndColor}, null);
        //正常范围渐变颜色组
        mNormalValueSg = new SweepGradient(0, 0, new int[]{normalStartColor, normalEndColor}, null);
        //偏高范围渐变颜色组
        mHighValueSg = new SweepGradient(0, 0, new int[]{highStartColor, highEndColor}, null);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Tachometer);
        max = a.getFloat(R.styleable.Tachometer_max, 0);
        min = a.getFloat(R.styleable.Tachometer_min, 0);
        float value = a.getFloat(R.styleable.Tachometer_value, 0);
        optimalMin = a.getFloat(R.styleable.Tachometer_optimalMin, 0);
        optimalMax = a.getFloat(R.styleable.Tachometer_optimalMax, 0);
        unit = a.getString(R.styleable.Tachometer_unit);
        title = a.getString(R.styleable.Tachometer_title);
        alarm = a.getBoolean(R.styleable.Tachometer_alarm, false);
        a.recycle();
        unit = unit == null ? "" : unit;
        title = title == null ? "" : title;
        unitScale = 360.0F / (max - min);
        setValue(value);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int measure = Math.max(widthSize, heightSize);
        setMeasuredDimension(measure, measure);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mFinalWidth = w;
        mFinalHeight = h;
        int measure = Math.min(mFinalWidth, mFinalHeight);
        setMeasuredDimension(measure, measure);
        mRayOutRadius = (mFinalHeight - 4 * circleWidth) / 2;
        mSolidCircleRadius = mRayOutRadius - 110;
        mCircleRadius = mRayOutRadius - 85;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(mDrawFilter);
        mCentreX = mFinalWidth / 2;
        mCentreY = mFinalHeight / 2;
        canvas.translate(mCentreX, mCentreY);
        //画默认射线
        drawRayAndCircle(canvas);
        //画默认圆圈
        baseCircle(canvas);
        //画实体圆
        drawSolidCircle(canvas);
        //画值和单位
        drawValueText(canvas);

    }

    private void drawValueText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(mCircleRadius / 2.0F);
        float value = mRadianByPos / unitScale;
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        int lowCompare = bigDecimal.compareTo(BigDecimal.valueOf(optimalMin));
        int highCompare = bigDecimal.compareTo(BigDecimal.valueOf(optimalMax));
        if (alarm) {
            paint.setShader(mHighValueSg);
        } else {
            if (lowCompare == -1) {
                paint.setShader(mLowValueSg);
            } else if (highCompare == 1) {
                paint.setShader(mHighValueSg);
            } else {
                paint.setShader(mNormalValueSg);
            }
        }
        String valueStr = round(BigDecimal.valueOf(value));
        float[] size = getSize(valueStr, paint);
        canvas.drawText(valueStr, -size[0] / 2, size[1] / 2, paint);
        paint.setTextSize(mCircleRadius / 3.5F);
        paint.setTextSkewX(-0.3F);
        size = getSize(unit, paint);
        canvas.drawText(unit, -size[0] / 2.0F, size[1] * 2F, paint);
        paint.setTextSkewX(0);
        paint.setTextSize(mCircleRadius / 4.5F);
        size = getSize(title, paint);
        canvas.drawText(title, -size[0] / 2.0F, -size[1] * 2F, paint);
    }

    private void drawSolidCircle(Canvas canvas) {
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, mSolidCircleRadius, mPaint);
    }

    private float[] getSize(String value, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(value, 0, value.length(), rect);
        float height = rect.height();//字符串的高度
        float width = getTextWidth(paint, value);
        return new float[]{width, height};
    }


    private void drawRayAndCircle(Canvas canvas) {
        Paint mPaint = new Paint();
        mPaint.setColor(mPaintDefaultColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(circleWidth);
        // 去除画笔锯齿

        mPaint.setAntiAlias(true);

        Path rayInnerPath = new Path();
        Path rayOuterPath = new Path();
        mRayInnerRegion = new Region();
        mRayOuterRegion = new Region();

        Region viewRegion = new Region(-mRayOutRadius, -mRayOutRadius, mRayOutRadius, mRayOutRadius);//整个控件区域内的所有点坐标集合

        //画默认射线
        for (float i = 0; i < 360; i += 10) {
            double rad = i * PI / 180;
            //射线内侧起点
            float startX = (float) (((mRayOutRadius - 35) - circleWidth) * Math.sin(rad));
            float startY = -(float) (((mRayOutRadius - 35) - circleWidth) * Math.cos(rad));
            //射线外侧终点,所以射线长度为 35px
            float stopX = (float) (mRayOutRadius * Math.sin(rad) + 1);
            float stopY = -(float) (mRayOutRadius * Math.cos(rad) + 1);

            //取的是射线区域内侧100px的区域的所有点坐标
            rayInnerPath.addCircle(0, 0, mRayOutRadius - 100, Path.Direction.CW);
            mRayInnerRegion.setPath(rayInnerPath, viewRegion);
            //取的是射线区域外侧50px的区域的所有点坐标
            rayOuterPath.addCircle(0, 0, mRayOutRadius + 50, Path.Direction.CW);
            mRayOuterRegion.setPath(rayOuterPath, viewRegion);

            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
        }

        RectF rf = new RectF(-mCircleRadius, -mCircleRadius, mCircleRadius, mCircleRadius);
        float radian = getRadian();
        BigDecimal bigDecimal = BigDecimal.valueOf(radian);
        int lowCompare = bigDecimal.compareTo(BigDecimal.valueOf(optimalMin * unitScale));
        int highCompare = bigDecimal.compareTo(BigDecimal.valueOf(optimalMax * unitScale));
        if (alarm) {
            mPaint.setShader(mHighValueSg);
        } else {
            if (lowCompare == -1) {
                mPaint.setShader(mLowValueSg);
            } else if (highCompare == 1) {
                mPaint.setShader(mHighValueSg);
            } else {
                mPaint.setShader(mNormalValueSg);
            }
        }
        for (float i = 0; i <= radian; i += 10) {
            double rad = i * PI / 180;
            double deg = rad * 180 / PI;

            float startX = (float) (((mRayOutRadius - 35) - circleWidth) *
                    Math.sin(rad));
            float startY = -(float) (((mRayOutRadius - 35) - circleWidth) *
                    Math.cos(rad));

            float stopX = (float) (mRayOutRadius * Math.sin(rad) + 1);
            float stopY = -(float) (mRayOutRadius * Math.cos(rad) + 1);
            //值射线
            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
            //值圆弧
            canvas.drawArc(rf, -90, (float) deg, false, mPaint);
        }
    }

    private float getRadian() {
        float value = mRadianByPos / unitScale;
        if (value < min) {
            return min * unitScale;
        }
        if (value > max) {
            return max * unitScale;
        }
        return value * unitScale;
    }

    private void baseCircle(Canvas canvas) {
        Paint mPaint = new Paint();
        mPaint.setColor(mPaintDefaultColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(0.5f);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(0, 0, mCircleRadius, mPaint);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //由于画布原点移到了控件中心，所以要矫正触点坐标
//        int eventX = (int) (event.getX() - mCentreX);
//        int eventY = (int) (mCentreY - event.getY());
//        //只有当触摸在控制的射线区域才处理滑动事件
//        if (mRayOuterRegion != null && mRayInnerRegion != null) {
//            if (mRayOuterRegion.contains(eventX, eventY) && !mRayInnerRegion.contains(eventX,
//                    eventY)) {
//                Point point = new Point(eventX, eventY);
//                mRadianByPos = GetRadianByPos(point);
//                invalidate();
//                if (mValueChangeListener != null) {
//                    mValueChangeListener.currentValue(mRadianByPos / unitScale);
//                }
//            }
//            return true;
//        }
//        return false;
//    }


    /**
     * @param point
     * @return 获得点所在角度（点与坐标轴原点连线与Y正半轴的顺时针夹角）单位为度数
     */
    public int GetRadianByPos(Point point) {
        return (int) (GetRadianByPosEx(point) * (360 / (2 * PI)));
    }

    /**
     * @param point
     * @return 获得点所在角度（点与坐标轴原点连线与Y正半轴的顺时针夹角）单位为弧度
     */
    private static double GetRadianByPosEx(Point point) {
        if (point.x == 0 && point.y == 0) {
            return 0;
        }
        double Sin = point.x / Math.sqrt(point.x * point.x + point.y * point.y);
        double dAngle = Math.asin(Sin);
        switch (GetQuadrant(point)) {
            case eQ_NONE: {
                if (point.x == 0 && point.y == 0) {
                    return 0;
                }

                if (point.x == 0) {
                    if (point.y > 0) {
                        return 0;
                    } else {
                        return PI;
                    }
                }

                if (point.y == 0) {
                    if (point.x > 0) {
                        return PI / 2;
                    } else {
                        return (float) (1.5 * PI);
                    }
                }
            }
            break;
            case eQ_ONE: {
                return dAngle;
            }
            case eQ_TWO: {
                dAngle = PI - dAngle;
            }
            break;
            case eQ_THREE: {
                dAngle = PI - dAngle;
            }
            break;
            case eQ_FOUR: {
                dAngle += 2 * PI;
            }
            break;
        }

        return dAngle;

    }

    /**
     * @param point
     * @return 获得Point点所在象限
     */
    public static _Quadrant GetQuadrant(Point point) {
        if (point.x == 0 || point.y == 0) {
            return _Quadrant.eQ_NONE;
        }

        if (point.x > 0) {
            if (point.y > 0) {
                return _Quadrant.eQ_ONE;
            } else {
                return _Quadrant.eQ_TWO;
            }

        } else {
            if (point.y < 0) {
                return _Quadrant.eQ_THREE;
            } else {
                return _Quadrant.eQ_FOUR;
            }
        }
    }

    /**
     * 获取字符串长度
     *
     * @param mPaint
     * @param str
     * @return
     */
    public float getTextWidth(Paint mPaint, String str) {
        float sum = 0;
        if (str != null && !str.equals("")) {
            int len = str.length();
            float widths[] = new float[len];
            mPaint.getTextWidths(str, widths);
            for (int i = 0; i < len; i++) {
                sum += Math.ceil(widths[i]);
            }
        }
        return sum;
    }

    /**
     * 值保留2位小数
     * @param target
     * @return
     */
    private String round(BigDecimal target){
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(target);
    }

    /**
     * 初始化显示的值
     *
     * @param targetValue
     */
    public void setValue(float targetValue) {
        mRadianByPos = targetValue * unitScale;
        invalidate();
    }

    public void setAlarm(boolean alarm) {
        this.alarm = alarm;
        invalidate();
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}

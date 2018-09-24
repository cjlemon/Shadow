package example.chenj.com.shadow;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author chenjun
 * create at 2018/9/22
 */
public class ShadowContainer extends ViewGroup {
    private final float deltaLength;
    private final float cornerRadius;
    private final Paint mShadowPaint;

    public ShadowContainer(Context context) {
        this(context, null);
    }

    public ShadowContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShadowContainer);
        int shadowColor = a.getColor(R.styleable.ShadowContainer_containerShadowColor, Color.RED);
//        int shadowColor = Color.RED;
        float shadowRadius = a.getDimension(R.styleable.ShadowContainer_containerShadowRadius, 0);
        deltaLength = a.getDimension(R.styleable.ShadowContainer_containerDeltaLength, 0);
        cornerRadius = a.getDimension(R.styleable.ShadowContainer_containerCornerRadius, 0);
        float dx = a.getDimension(R.styleable.ShadowContainer_deltaX, 0);
        float dy = a.getDimension(R.styleable.ShadowContainer_deltaY, 0);
        a.recycle();
        mShadowPaint = new Paint();
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setColor(shadowColor);
        mShadowPaint.setShadowLayer(shadowRadius, dx, dy, shadowColor);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (getLayerType() != LAYER_TYPE_SOFTWARE) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        View child = getChildAt(0);
        int left = child.getLeft();
        int top = child.getTop();
        int right = child.getRight();
        int bottom = child.getBottom();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, mShadowPaint);
        } else {
            Path drawablePath = new Path();
            drawablePath.moveTo(left + cornerRadius, top);
            drawablePath.arcTo(new RectF(left, top, left + 2 * cornerRadius, top + 2 * cornerRadius), -90, -90, false);
            drawablePath.lineTo(left, bottom - cornerRadius);
            drawablePath.arcTo(new RectF(left, bottom - 2 * cornerRadius, left + 2 * cornerRadius, bottom), 180, -90, false);
            drawablePath.lineTo(right - cornerRadius, bottom);
            drawablePath.arcTo(new RectF(right - 2 * cornerRadius, top - 2 * cornerRadius, right, top), 90, -90, false);
            drawablePath.lineTo(right, top - cornerRadius);
            drawablePath.arcTo(new RectF(right - 2 * cornerRadius, top, right, top + 2 * cornerRadius), 0, -90, false);
            drawablePath.close();
            canvas.drawPath(drawablePath, mShadowPaint);
        }
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() != 1) {
            throw new IllegalStateException("子View只能有一个");
        }
        View child = getChildAt(0);
        LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
        int childBottomMargin = layoutParams.bottomMargin;
        int childLeftMargin = layoutParams.leftMargin;
        int childRightMargin = layoutParams.rightMargin;
        int childTopMargin = layoutParams.topMargin;
        int widthMeasureSpecMode;
        int widthMeasureSpecSize;
        int heightMeasureSpecMode;
        int heightMeasureSpecSize;
        if (layoutParams.width == LayoutParams.MATCH_PARENT) {
            widthMeasureSpecMode = MeasureSpec.EXACTLY;
            widthMeasureSpecSize = getMeasuredWidth() - layoutParams.leftMargin - layoutParams.rightMargin;
        } else if (LayoutParams.WRAP_CONTENT == layoutParams.width) {
            widthMeasureSpecMode = MeasureSpec.AT_MOST;
            widthMeasureSpecSize = getMeasuredWidth() - layoutParams.leftMargin - layoutParams.rightMargin;
        } else {
            widthMeasureSpecMode = MeasureSpec.EXACTLY;
            widthMeasureSpecSize = layoutParams.width;
        }
        if (layoutParams.height == LayoutParams.MATCH_PARENT) {
            heightMeasureSpecMode = MeasureSpec.EXACTLY;
            heightMeasureSpecSize = getMeasuredHeight() - layoutParams.bottomMargin - layoutParams.topMargin;
        } else if (LayoutParams.WRAP_CONTENT == layoutParams.height) {
            heightMeasureSpecMode = MeasureSpec.AT_MOST;
            heightMeasureSpecSize = getMeasuredHeight() - layoutParams.bottomMargin - layoutParams.topMargin;
        } else {
            heightMeasureSpecMode = MeasureSpec.EXACTLY;
            heightMeasureSpecSize = layoutParams.height;
        }
        measureChild(child, MeasureSpec.makeMeasureSpec(widthMeasureSpecSize, widthMeasureSpecMode), MeasureSpec.makeMeasureSpec(heightMeasureSpecSize, heightMeasureSpecMode));
        int parentWidthMeasureSpec = MeasureSpec.getMode(widthMeasureSpec);
        int parentHeightMeasureSpec = MeasureSpec.getMode(heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int childHeight = child.getMeasuredHeight();
        int childWidth = child.getMeasuredWidth();
        if (parentHeightMeasureSpec == MeasureSpec.AT_MOST){
            height = childHeight + childTopMargin + childBottomMargin;
        }
        if (parentWidthMeasureSpec == MeasureSpec.AT_MOST){
            width = childWidth + childRightMargin + childLeftMargin;
        }
        if (width < childWidth + 2 * deltaLength){
            width = (int) (childWidth + 2 * deltaLength);
        }
        if (height < childHeight + 2 * deltaLength){
            height = (int) (childHeight + 2 * deltaLength);
        }
        if (height != getMeasuredHeight() || width != getMeasuredWidth()){
            setMeasuredDimension(width, height);
        }
    }

    static class LayoutParams extends MarginLayoutParams{

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View child = getChildAt(0);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int childMeasureWidth = child.getMeasuredWidth();
        int childMeasureHeight = child.getMeasuredHeight();
        child.layout((measuredWidth - childMeasureWidth) / 2, (measuredHeight - childMeasureHeight) / 2, (measuredWidth + childMeasureWidth) / 2, (measuredHeight + childMeasureHeight) / 2);
    }
}

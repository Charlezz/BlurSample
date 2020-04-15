package com.charlezz.blur;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * source come from https://github.com/mmin18/RealtimeBlurView/blob/master/library/src/com/github/mmin18/widget/RealtimeBlurView.java
 */
public class LiveBlurView extends View {

    private float downsampleFactor; // default 4
    private int overlayColor; // default #aaffffff
    private float radius; // default 10dp (0 < r <= 25)

    private final BlurEngine blurEngine;
    private boolean mDirty;
    private Bitmap bitmapToBlur, blurredBitmap;
    private Canvas blurringCanvas;
    private boolean isRendering;
    private Paint paint;
    private final Rect rectSrc = new Rect(), mRectDst = new Rect();
    // decorView는 액티비티의 root view로 지정한다(dialog처럼 다른 윈도우에 있는 경우에도)
    private View decorView;
    // 만약 view가 다른 root view상에 있다면 (보통은 PopupWindow일 때)
    // 수동으로 onPreDraw()에서 invalidate()를 호출해야한다. 그렇지 않으면 변경되지 않는다.
    private boolean differentRoot;
    private static int renderingCount; //LiveBlurView 위에 또 다른 LiveBlurView가 겹치는지 알기 위해 static 변수 사용

    public LiveBlurView(Context context, AttributeSet attrs) {
        super(context, attrs);

        blurEngine = new StackBlur();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LiveBlurView);
        radius = a.getDimension(R.styleable.LiveBlurView_realtimeBlurRadius, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics()));
        downsampleFactor = a.getFloat(R.styleable.LiveBlurView_realtimeDownsampleFactor, 4);
        overlayColor = a.getColor(R.styleable.LiveBlurView_realtimeOverlayColor, 0x00FFFFFF);
        a.recycle();

        paint = new Paint();
    }

    public void setBlurRadius(float radius) {
        if (this.radius != radius) {
            this.radius = radius;
            mDirty = true;
            invalidate();
        }
    }

    public void setDownsampleFactor(float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Downsample factor must be greater than 0.");
        }

        if (downsampleFactor != factor) {
            downsampleFactor = factor;
            mDirty = true; // may also change blur radius
            releaseBitmap();
            invalidate();
        }
    }

    public void setOverlayColor(int color) {
        if (overlayColor != color) {
            overlayColor = color;
            invalidate();
        }
    }

    private void releaseBitmap() {
        if (bitmapToBlur != null) {
            bitmapToBlur.recycle();
            bitmapToBlur = null;
        }
        if (blurredBitmap != null) {
            blurredBitmap.recycle();
            blurredBitmap = null;
        }
    }

    protected void release() {
        releaseBitmap();
//        blurEngine.release();
    }

    protected boolean prepare() {
        if (radius == 0) {
            release();
            return false;
        }

        float downsampleFactor = this.downsampleFactor;
        float radius = this.radius / downsampleFactor;
        if (radius > 25) {
            downsampleFactor = downsampleFactor * radius / 25;
            radius = 25;
        }

        final int width = getWidth();
        final int height = getHeight();

        int scaledWidth = Math.max(1, (int) (width / downsampleFactor));
        int scaledHeight = Math.max(1, (int) (height / downsampleFactor));

        boolean dirty = mDirty;

        if (blurringCanvas == null || blurredBitmap == null
                || blurredBitmap.getWidth() != scaledWidth
                || blurredBitmap.getHeight() != scaledHeight) {
            dirty = true;
            releaseBitmap();

            boolean r = false;
            try {
                bitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                if (bitmapToBlur == null) {
                    return false;
                }
                blurringCanvas = new Canvas(bitmapToBlur);

                blurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                if (blurredBitmap == null) {
                    return false;
                }

                r = true;
            } catch (OutOfMemoryError e) {
                // Bitmap.createBitmap()를 호출하다보면 OOM 에러가 발생할 수 있는데, 그냥 무시하자
            } finally {
                if (!r) {
                    release();
                    return false;
                }
            }
        }

        if (dirty) {
//            if (blurEngine.prepare(getContext(), bitmapToBlur, radius)) {
            mDirty = false;
//            } else {
//                return false;
//            }
        }

        return true;
    }

    protected void blur(Bitmap bitmapToBlur, Bitmap blurredBitmap) {
        this.blurredBitmap = blurEngine.blur(bitmapToBlur, (int) radius);
    }

    private final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
            final int[] locations = new int[2];
//            Bitmap oldBmp = blurredBitmap;
            View decor = decorView;
            if (decor != null && isShown() && prepare()) {
//                boolean redrawBitmap = blurredBitmap != oldBmp;
//                oldBmp = null;
                decor.getLocationOnScreen(locations);
                int x = -locations[0];
                int y = -locations[1];

                getLocationOnScreen(locations);
                x += locations[0];
                y += locations[1];

                // just erase transparent
                bitmapToBlur.eraseColor(overlayColor & 0xffffff);

                int rc = blurringCanvas.save();
                isRendering = true;
                renderingCount++;
                try {
                    blurringCanvas.scale(1.f * bitmapToBlur.getWidth() / getWidth(), 1.f * bitmapToBlur.getHeight() / getHeight());
                    blurringCanvas.translate(-x, -y);
                    if (decor.getBackground() != null) {
                        decor.getBackground().draw(blurringCanvas);
                    }
                    decor.draw(blurringCanvas);
                } catch (StopException e) {
                } finally {
                    isRendering = false;
                    renderingCount--;
                    blurringCanvas.restoreToCount(rc);
                }

                blur(bitmapToBlur, blurredBitmap);

//                if (redrawBitmap || differentRoot) {
                invalidate();
//                }
            }

            return true;
        }
    };

    protected View getActivityDecorView() {
        Context ctx = getContext();
        for (int i = 0; i < 4 && ctx != null && !(ctx instanceof Activity) && ctx instanceof ContextWrapper; i++) {
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        if (ctx instanceof Activity) {
            return ((Activity) ctx).getWindow().getDecorView();
        } else {
            return null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        decorView = getActivityDecorView();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            differentRoot = decorView.getRootView() != getRootView();
            if (differentRoot) {
                decorView.postInvalidate();
            }
        } else {
            differentRoot = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (decorView != null) {
            decorView.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
        }
        release();
        super.onDetachedFromWindow();
    }

    @Override
    public void draw(Canvas canvas) {
        if (isRendering) {
            // Quit here, don't draw views above me
            throw STOP_EXCEPTION;
        } else if (renderingCount > 0) {
            // LiveBlurView 위에 또 다른 LiveBlurView 가 겹치는 것은 지원하지 않음.
        } else {
            super.draw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBlurredBitmap(canvas, blurredBitmap, overlayColor);
    }

    /**
     * Custom draw the blurred bitmap and color to define your own shape
     *
     * @param canvas
     * @param blurredBitmap
     * @param overlayColor
     */
    protected void drawBlurredBitmap(Canvas canvas, Bitmap blurredBitmap, int overlayColor) {
        if (blurredBitmap != null) {
            rectSrc.right = blurredBitmap.getWidth();
            rectSrc.bottom = blurredBitmap.getHeight();
            mRectDst.right = getWidth();
            mRectDst.bottom = getHeight();
            canvas.drawBitmap(blurredBitmap, rectSrc, mRectDst, null);
        }
        paint.setColor(overlayColor);
        canvas.drawRect(mRectDst, paint);
    }

    private static class StopException extends RuntimeException {
    }

    private static StopException STOP_EXCEPTION = new StopException();
}

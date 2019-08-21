package com.lingc.nfloatingtile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.lingc.nfloatingtile.R;

/**
 * Create by LingC on 2019/8/7 18:14
 */
public class RoundImageView extends AppCompatImageView {

    // 缩放类型
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private static final int COLORDRAWABLE_DIMENSION = 2;

    private float circleRadius;
    private Bitmap bitmap;
    private Paint bitmapPaint;
    private BitmapShader bitmapShader;
    private RectF drawableRect;
    private int bitmapWidth;
    private int bitmapHeight;
    private Matrix mShaderMatrix;

    private Paint borderPaint;
    private int borderColor;
    private int borderWidth;
    private RectF borderRect;
    private float borderRadius;
    private boolean hasBorder;


    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //自定义属性，关联attr.xml中<declare-styleable name="CircleImage">中的属性
        //TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleImage);

        //获取布局文件中的自定义属性
        hasBorder = false;
        if (hasBorder) {
            borderWidth = 20;
            borderColor = Color.RED;
        }
        //typedArray.recycle();
    }

    /**
     * 构造方法中还不能拿到View的宽高，在这个方法中可以得到，所以在这儿实例化画圆所需要的一切配置
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //这儿一定不要调用父类的onDraw方法，调用这个方法系统会直接将ImageView绘画出来，这就会出现两张图片
        //  super.onDraw(canvas);

        Log.i("CircleImage3", "onDraw :");

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, circleRadius, bitmapPaint);
        if (hasBorder) {

            canvas.drawCircle(getWidth() / 2, getHeight() / 2, borderRadius, borderPaint);
        }
    }


    /**
     * Drawable转Bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            // 通常来说 我们的代码就是执行到这里就返回了。返回的就是我们最原始的bitmap
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION,
                        COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * 关键代码就在这儿，画笔初始化，圆半径计算，图片压缩，构建Shader，用Bitmap填充圆
     */
    private void initPaint() {
        if (getDrawable() != null) {
            bitmap = getBitmapFromDrawable(getDrawable());
        }
        if (bitmap == null) {
            throw new IllegalArgumentException("the bitmap of imageView is null !");
        }
        if (hasBorder) {
            /**
             * 初始化外圆画笔的属性，外圆半径的计算
             */
            borderPaint = new Paint();
            //外圆所在矩形：这儿用一个矩形去控制外圆,Android中的View都是矩形，所以画圆也是在一个矩形里面画内心圆
            borderRect = new RectF();
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setAntiAlias(true);
            //外圆不需要用图片去填充，而是用想要的颜色去填充
            borderPaint.setColor(borderColor);
            borderPaint.setStrokeWidth(borderWidth);
            //RectF类中计算，矩形的宽 = right坐标-left坐标，高 = bottom坐标-top坐标，
            // 所以这儿只需要将本ImageView的宽值赋给矩形的right坐标，高赋值给bottom坐标即可
            borderRect.set(0, 0, getWidth(), getHeight());
            //外圆的半径是算法（外圆所在矩形的宽和高这两边的短边的一半减去外圆到内圆的距离）
            borderRadius = Math.min((borderRect.width() - borderWidth) / 2, (borderRect.height() - borderWidth) / 2);
        }

        //初始化内圆的画笔属性，半径计算
        bitmapPaint = new Paint();
        //同外圆一样，画一个矩形的内心圆
        drawableRect = new RectF();
        //赋给shader的矩阵，用于压缩图片，让图片的中心部位去填充内圆
        mShaderMatrix = new Matrix();
        bitmapWidth = bitmap.getWidth();
        bitmapHeight = bitmap.getHeight();
        // 构建渲染器，用mBitmap位图来填充绘制区域 ，参数值代表如果图片太小的话 就直接拉伸
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        bitmapPaint.setAntiAlias(true);
        // 设置图片画笔渲染器
        bitmapPaint.setShader(bitmapShader);

        if (hasBorder) {
            //内圆所属矩形的宽和高应该分别是外圆所属矩形的宽减去外圆到内圆的距离和高减去外圆到内圆的距离
            drawableRect.set(0, 0, borderRect.width() - borderWidth, borderRect.height() - borderWidth);
        } else {
            drawableRect.set(0, 0, getWidth(), getHeight());
        }

        circleRadius = Math.min(drawableRect.height() / 2, drawableRect.width() / 2);
        //压缩并位移图片用于填充内部圆
        setBitmapShaderMtrix();
    }

    private void setBitmapShaderMtrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        //x方向压缩还是Y方向压缩判断
        if (bitmapWidth * drawableRect.height() > drawableRect.width() * bitmapHeight) {
            // y轴缩放 x轴平移 使得图片的y轴方向的边的尺寸缩放到图片显示区域（mDrawableRect）一样）
            scale = drawableRect.height() / (float) bitmapHeight;
            dx = (drawableRect.width() - bitmapWidth * scale) * 0.5f;

        } else {
            // x轴缩放 y轴平移 使得图片的x轴方向的边的尺寸缩放到图片显示区域（mDrawableRect）一样）
            scale = drawableRect.width() / (float) bitmapWidth;
            dy = (drawableRect.height() - bitmapHeight * scale) * 0.5f;
        }

        // shaeder的变换矩阵，我们这里主要用于放大或者缩小。
        mShaderMatrix.setScale(scale, scale);

        // 平移
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + drawableRect.left, (int) (dy + 0.5f) + drawableRect.top);

        // 设置变换矩阵
        bitmapShader.setLocalMatrix(mShaderMatrix);

    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
    }

}

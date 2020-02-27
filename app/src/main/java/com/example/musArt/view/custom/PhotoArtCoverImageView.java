package com.example.musArt.view.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class PhotoArtCoverImageView extends ImageView {

    public static float radius = 17.0f;

    public PhotoArtCoverImageView(Context context) {
        super(context);
    }

    public PhotoArtCoverImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoArtCoverImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        @SuppressLint("DrawAllocation")
        Path clipPath = new Path();
        @SuppressLint("DrawAllocation")
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }

}
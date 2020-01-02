package com.app.khoaluan.noizy.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.app.khoaluan.noizy.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.app.khoaluan.noizy.utils.AppConfig.ANIMATION_INTERVAL;

public class Speedometer extends androidx.appcompat.widget.AppCompatImageView {
    private Context context;
    private float scaleWidth, scaleHeight;
    private int newWidth, newHeight;
    private Matrix mMatrix = new Matrix();
    private Bitmap indicatorBitmap;
    private Paint dbValuePaint = new Paint(), influencePaint = new Paint();
    DecimalFormat df1;
    private NoiseLevel level = new NoiseLevel();

    public Speedometer(Context context) {
        super(context);
        this.context = context;
    }

    public Speedometer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    private void init() {
        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_speedometer_needle);
        int bitmapWidth = myBitmap.getWidth();
        int bitmapHeight = myBitmap.getHeight();
        newWidth = getWidth();
        newHeight = getHeight();
        scaleWidth = ((float) newWidth) /(float) bitmapWidth;  // Get the zoom ratio
        scaleHeight = ((float) newHeight) /(float) bitmapHeight;  //Get the zoom ratio
        mMatrix.postScale(scaleWidth, scaleHeight);   //Set the scale of mMatrix
        indicatorBitmap = Bitmap.createBitmap(myBitmap, 0, 0, bitmapWidth, bitmapHeight, mMatrix,true);  //Get the same and background width and height of the pointer map bitmap

        dbValuePaint = new Paint();
        dbValuePaint.setTextSize(112);
        dbValuePaint.setAntiAlias(true);  //Anti-aliasing
        dbValuePaint.setTextAlign(Paint.Align.CENTER);
        dbValuePaint.setColor(Color.WHITE);

        influencePaint = new Paint();
        influencePaint.setTextSize(54);
        influencePaint.setAntiAlias(true);  //Anti-aliasing
        influencePaint.setTextAlign(Paint.Align.CENTER);
        influencePaint.setColor(Color.WHITE);

        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        df1 = new DecimalFormat("##0.0", otherSymbols);
    }

    public void refresh() {
        postInvalidateDelayed(ANIMATION_INTERVAL); //Sub-thread refresh view
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (indicatorBitmap == null) {
            init();
        }
        mMatrix.setRotate(getAngle(Global.lastDb), newWidth / 2, newHeight * 215 / 460);   //The relative position of the sheet
        canvas.drawBitmap(indicatorBitmap, mMatrix, dbValuePaint);

        //Mức độ ảnh hưởng
        canvas.drawText(context.getString(level.getInfluenceLevel(Global.lastDb)), newWidth/2,newHeight*18/23, influencePaint); //Picture relative position
        //Độ ồn hiện tại
        canvas.drawText(df1.format(Global.lastDb)+" dB", newWidth/2,newHeight*22/23, dbValuePaint); //Picture relative position
    }

    private float getAngle(float db){
        return(db-85)*5/3;  //Say more are tears, online to find pictures. The They will not change the map, the code calculation
    }
}

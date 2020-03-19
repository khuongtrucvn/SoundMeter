package android.dbmeter.net.model;

import android.dbmeter.net.utils.AppConfig;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;

import android.dbmeter.net.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Speedometer extends androidx.appcompat.widget.AppCompatImageView {
    private Context context;
    private float scaleWidth, scaleHeight;
    private int newWidth, newHeight;
    private Matrix mMatrix = new Matrix();
    private Bitmap indicatorBitmap;
    private Paint dbValuePaint = new Paint(), influencePaint = new Paint();
    private DecimalFormat df1;
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
        Bitmap myBitmap = decodeAndResizeImage(R.drawable.img_speedometer_needle);
        int bitmapWidth = myBitmap.getWidth();
        int bitmapHeight = myBitmap.getHeight();
        newWidth = getWidth();
        newHeight = getHeight();
        //Tính tỉ lệ zoom so với nền đồng hồ
        scaleWidth = ((float) newWidth) /(float) bitmapWidth;
        scaleHeight = ((float) newHeight) /(float) bitmapHeight;
        mMatrix.postScale(scaleWidth, scaleHeight);
        //Ghép hình kim vào nền đồng hồ
        indicatorBitmap = Bitmap.createBitmap(myBitmap, 0, 0, bitmapWidth, bitmapHeight, mMatrix,true);

        dbValuePaint = new Paint();
        dbValuePaint.setTextSize(112);
        dbValuePaint.setAntiAlias(true);
        dbValuePaint.setTextAlign(Paint.Align.CENTER);
        dbValuePaint.setColor(Color.WHITE);

        influencePaint = new Paint();
        influencePaint.setTextSize(54);
        influencePaint.setAntiAlias(true);
        influencePaint.setTextAlign(Paint.Align.CENTER);
        influencePaint.setColor(Color.WHITE);

        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        df1 = new DecimalFormat("##0.0", otherSymbols);
    }

    //Hàm tải lại đồng hồ
    public void refresh() {
        postInvalidateDelayed(AppConfig.WAITING_TIME);
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
        mMatrix.setRotate(getAngle(Global.lastDb), (float)newWidth / 2, (float)newHeight * 215 / 460);   //The relative position of the sheet
        canvas.drawBitmap(indicatorBitmap, mMatrix, dbValuePaint);

        //Mức độ ảnh hưởng
        canvas.drawText(context.getString(level.getInfluenceLevel(Global.lastDb)), (float)newWidth/2,(float)newHeight*18/23, influencePaint); //Picture relative position
        //Độ ồn hiện tại
        canvas.drawText(df1.format(Global.lastDb)+" dB", (float)newWidth/2,(float)newHeight*22/23, dbValuePaint); //Picture relative position
    }

    //Hàm tính góc quay
    private float getAngle(float db){
        return(db-85)*5/3;
    }

    //Hàm giảm phân giải ảnh nếu ảnh gây lỗi OutOfMemory
    private Bitmap decodeAndResizeImage(int drawable){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        boolean done = false;
        int downsampleBy = 1;

        Bitmap myBitmap = null;
        while (!done) {
            options.inSampleSize = downsampleBy++;
            try {
                myBitmap = BitmapFactory.decodeResource(getResources(), drawable, options);
                done = true;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }

        return myBitmap;
    }
}

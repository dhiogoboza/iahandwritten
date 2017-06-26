package br.ufrn.ia.nnhandwritten;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;

public class CameraActivity extends FragmentActivity implements Runnable, Camera.PictureCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "CameraActivity";

    private CameraPreview mPreview;
    private CameraManager mCameraManager;
    private Button mCaptureButton;
    private ToggleButton mFlashToggle;
    private View mDigitImageView;
    private boolean mCaptured = false;

    private TextView mTextViewDigit;
    private TextView mTextViewMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        mCameraManager = new CameraManager(this);

        mPreview = new CameraPreview(this, mCameraManager.getCamera());
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        mTextViewDigit = (TextView) findViewById(R.id.digit);
        mTextViewMatch = (TextView) findViewById(R.id.digit_match);

        mCaptureButton = (Button) findViewById(R.id.capture);
        mCaptureButton.setOnClickListener(this);

        mFlashToggle = (ToggleButton) findViewById(R.id.flash_toggle);
        mFlashToggle.setOnCheckedChangeListener(this);

        mDigitImageView = findViewById(R.id.digit_image);

        //ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        //ex.scheduleWithFixedDelay(this, 1, 2, TimeUnit.SECONDS);
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            //FileOutputStream fos = new FileOutputStream(pictureFile);
            //fos.write(data);
            //fos.close();

            //Bitmap bMap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());

            Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);


            //Bitmap scaled = Bitmap.createBitmap(bMap, (int) mDigitImageView.getX(), (int) mDigitImageView.getY(), 168, 168);
            //Bitmap scaled = Bitmap.createBitmap(bMap, (int) mDigitImageView.getX(), (int) mDigitImageView.getY(), 168, 168);
            //scaled.getPixels();

            Log.d(TAG, "Tirou uma foto " + bMap.getHeight() + "x" + bMap.getWidth() + " - " + bMap.getByteCount());


            //int width = 280;
            //int height = 280;

            /*int pixels[] = new int[width * height * 4];
            //1920x2560 - 19660800
            // offset 1740970
            bMap.getPixels(pixels, 0, bMap.getWidth(), 170, 170, width, height);

            Bitmap bitmap = BitmapFactory.decodeByteArray(pixels, 0, );*/

            mCaptured = true;
            mCaptureButton.setText("Limpar");

            //int[] pixels = getBitmapPixels(bMap, 170, 170, width, height);

            Bitmap bitmap = Bitmap.createBitmap(bMap, 450, 600, 800, 800);

            //Bitmap bitmap = Bitmap.createBitmap(bMap, convertDpToPixel(100), convertDpToPixel(100), convertDpToPixel(150), convertDpToPixel(150));

            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 28, 28, true);

            Log.d(TAG, "pixel: " + Color.red(scaled.getPixel(0,0)));

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            matrix.preScale(1, -1);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaled , 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);

            int image_data[] = new int[28 * 28];
            int pixel, avg;

            for (int i = 0; i < rotatedBitmap.getHeight(); i++) {
                //for (int j = scaled.getHeight() - 1; j >= 0; j--) {
                for (int j = 0; j < rotatedBitmap.getWidth(); j++) {
                    pixel = rotatedBitmap.getPixel(i, j);
                    avg = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;

                    image_data[i * rotatedBitmap.getWidth() + j] = avg < 128? 1 : 0;
                    //image_data[index] = avg < 128? 1 : 0;
                    //index++;

                    pixel = scaled.getPixel(i, j);
                    avg = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                    if (avg < 128) {
                        scaled.setPixel(i, j, Color.BLACK);
                    } else {
                        scaled.setPixel(i, j, Color.WHITE);
                    }
                }
            }

            /*int image_data2[] = new int[28 * 28];
            index = 0;
            for (int j = 0; j < 28; j++) {
                for (int i = 27; i >= 0; i--) {
                    image_data2[index] = image_data[i * 28 + j];
                }
            }*/

            mDigitImageView.setBackground(new BitmapDrawable(scaled));

            DigitResolver.resolve(getApplicationContext(), image_data, mTextViewDigit, mTextViewMatch);

            //mDigitImageView.setDrawingCacheEnabled(true);
            //Bitmap b = mDigitImageView.getDrawingCache();
            //b.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(pictureFile));

            //showImage(pictureFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "Error accessing file: " + e.getMessage(), e);
        }

        mCaptureButton.setEnabled(true);
        mFlashToggle.setEnabled(true);
        updateFlash(mFlashToggle.isChecked());

        mCameraManager.getCamera().startPreview();
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public static int convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);

        return Math.round(px);
    }

    public static int[] getBitmapPixels(Bitmap bitmap, int x, int y, int width, int height) {
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), x, y,
                width, height);
        final int[] subsetPixels = new int[width * height];
        for (int row = 0; row < height; row++) {
            System.arraycopy(pixels, (row * bitmap.getWidth()),
                    subsetPixels, row * width, width);
        }
        return subsetPixels;
    }

    private void showImage(String path) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + path), "image/*");
        startActivity(intent);
    }

    @Override
    public void run() {
        Log.d(TAG, "No run");

        mCameraManager.getCamera().takePicture(null, null, this);
    }

    @Override
    public void onClick(View view) {
        if (!mCaptured) {
            mCaptureButton.setEnabled(false);
            mFlashToggle.setEnabled(false);
            run();
        } else {
            mDigitImageView.setBackground(getResources().getDrawable(R.drawable.red_border));
            mCaptureButton.setText("Capturar");
            mCaptured = false;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        updateFlash(checked);
    }

    private void updateFlash(boolean checked) {
        Camera camera = mCameraManager.getCamera();

        if (camera != null) {
            Camera.Parameters p = mCameraManager.getCamera().getParameters();

            p.setFlashMode(checked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);

            mCameraManager.getCamera().setParameters(p);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateFlash(false);
        mCameraManager.releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateFlash(false);
        mCameraManager.releaseCamera();
    }
}

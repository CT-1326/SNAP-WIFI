package com.example.unknown.snap_wifi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Cam extends AppCompatActivity implements SurfaceHolder.Callback {

    //Camera
    private SurfaceView mCameraView;
    private SurfaceHolder mCameraHolder;
    private Camera mCamera;
    private final static int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK;
    private TextView low,more;

    //Camera in Box for OCR
    private SurfaceView mSurfaceRoi;

    //OCR
    private TessBaseAPI mTess;
    String datapath = "" ;

    //OCR Result Value
    public static String aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        //AdMob
        MobileAds.initialize(this, "ca-app-pub-2725846173883391~7749234994");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Check GPS ON/OFF (OS 6.0 UP)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                alertDialogBuilder
                        .setMessage("계속 이용하시려면 GPS를 켜야 합니다.")
                        .setCancelable(false)
                        .setPositiveButton("설정",
                                new DialogInterface.OnClickListener() {
                                    //Go Settings -> Location Settings
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                                        startActivity(intent);
                                        //dialog.cancel();
                                    }
                                })
                        .setNegativeButton("종료",
                                new DialogInterface.OnClickListener() {
                                    //Exit
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        ActivityCompat.finishAffinity(Cam.this);
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }

        //Clear the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Language file path
        datapath = getFilesDir()+ "/tesseract/";
        //Check if training data is copied
        checkFile(new File(datapath + "tessdata/"));
        //Tesseract API
        String lang = "eng";
        mTess = new TessBaseAPI();
        mTess.init(datapath, lang);

        //View Camera
        mCameraView = (SurfaceView)findViewById(R.id.surfaceView);

        //Camera Zoom
        SeekBar seekBar=(SeekBar)findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mCamera.getParameters().isZoomSupported()) {
                    //Zoom + Auto Focus
                    Camera.Parameters params = mCamera.getParameters();
                    seekBar.setMax(params.getMaxZoom());
                    params.setZoom(progress);
                    mCamera.setParameters(params);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Touching the SeekBar, can see +,- icon
                low.setVisibility(View.VISIBLE);
                more.setVisibility(View.VISIBLE);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Not touching the SeekBar
                low.setVisibility(View.INVISIBLE);
                more.setVisibility(View.INVISIBLE);
            }
        });

        //Camera Button
        ImageButton imageButton=(ImageButton)findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        });

        //Setup +,- icon setting
        low = (TextView)findViewById(R.id.low);
        more = (TextView)findViewById(R.id.more);
        low.setVisibility(View.INVISIBLE);
        more.setVisibility(View.INVISIBLE);
    }

    //copy file to device
    private void copyFiles() {
        try{
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //check file on the device
    private void checkFile(File dir) {
        if(!dir.exists()&& dir.mkdirs()) {
            copyFiles();
        }
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
    }

    //Activity life cycle
    protected void onResume() {
        super.onResume();

        if(mCamera==null)
            init();

    }
   protected void onPause() {
        super.onPause();

        SeekBar seekBar=(SeekBar)findViewById(R.id.seekBar1);
        seekBar.setMax(0);
        if(mCamera!=null){
            mCamera.release();
            mCamera=null;
        }
    }

    //Camera Function
    private void init() {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);

        // surfaceview setting
        mCameraHolder = mCameraView.getHolder();
        mCameraHolder.addCallback(this);
        mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    //Camera Shot Method
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //Toast toast = Toast.makeText(getApplicationContext(), "인식 중...", Toast.LENGTH_SHORT); toast.show();
        }
    };
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //Create captured image and implement height-wide rear camera etc
            Bitmap bitmap= BitmapFactory.decodeByteArray(data,0,data.length);
            int orientation = setCameraDisplayOrientation(Cam.this,
                    CAMERA_FACING, camera);

            //Rotate the image in the direction of the device
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            //Implementing a letter recognition box
            mSurfaceRoi = (SurfaceView) findViewById(R.id.surface_roi);
            bitmap=resizeBitmap(bitmap,mSurfaceRoi.getHeight(),mSurfaceRoi.getWidth());
            bitmap=cropBitmap(bitmap,mSurfaceRoi.getHeight(),mSurfaceRoi.getWidth());

            //Runnig OCR 2sec
            String OCRresult = null;
            mTess.setImage(bitmap);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    //Use tess-two library method
                   mTess.stop();
                   mTess.end();
                }
            },2000);
            OCRresult = mTess.getUTF8Text();
            aa = new String(OCRresult);
            aa = aa.replaceAll("\\p{Z}", "");//Remove spaces
            //Go to next Activities
            Intent intent = new Intent(Cam.this, Pop.class);
            startActivity(intent);
        }
    };

    //Bitmap resizing
    public Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        if (bitmap.getWidth() != width || bitmap.getHeight() != height){
            float ratio = 1.0f;

            if (width > height) {
                ratio = (float)width / (float)bitmap.getWidth();
            } else {
                ratio = (float)height / (float)bitmap.getHeight();
            }

            bitmap = Bitmap.createScaledBitmap(bitmap,
                    (int)(((float)bitmap.getWidth()) * ratio), // Width
                    (int)(((float)bitmap.getHeight()) * ratio), // Height
                    false);
        }

        return bitmap;
    }

    //Bitmap cropping
    public Bitmap cropBitmap(Bitmap bitmap, int width, int height) {
        int originWidth = bitmap.getWidth();
        int originHeight = bitmap.getHeight();
        int orientation = setCameraDisplayOrientation(Cam.this,
                CAMERA_FACING, mCamera);

        // Crop Image
        int x = 0;
        int y = 0;
        //Precedence of forestry
        width=110;
        height=685;

        if (originWidth > width) {
            x = (originWidth - width) / 2;
        }

        if (originHeight > height) {
            y = (originHeight - height) / 2;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        //Create Bitmap
        Bitmap cropedBitmap = Bitmap.createBitmap(bitmap, x, y, width, height,matrix,true);
        return cropedBitmap;
    }

    //If it rotates horizontally, it rotates according to the surfaceview ratio.
    public static int setCameraDisplayOrientation(Cam activity,
                                                  int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    //Create surfaceview
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera == null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mCameraHolder.getSurface() == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }

        Camera.Parameters parameters = mCamera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCamera.setParameters(parameters);

        try {
            mCamera.setPreviewDisplay(mCameraHolder);
            mCamera.startPreview();
        } catch (Exception e) {
        }

    }

    //End surfaceview
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    //When you touch BackPress, app closes
    public void onBackPressed(){
        super.onBackPressed();
        ActivityCompat.finishAffinity(Cam.this);
    }

}

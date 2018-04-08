package com.example.ludvig.examensarbete;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    JavaCameraView cameraView;
    FeatureExtractor featureExtractor;
    Mat imgMat;
    Button menuButton;
    int hLowVal;
    int sLowVal;
    int vLowVal;
    int hHighVal;
    int sHighVal;
    int vHighVal;
    TextView hLowText;
    TextView sLowText;
    TextView vLowText;
    TextView hHighText;
    TextView sHighText;
    TextView vHighText;

    DrawMode drawMode = DrawMode.IMAGE;

    private static final String TAG = "main activity";
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();
                    featureExtractor = new FeatureExtractor();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowMode();
        setContentView(R.layout.activity_main);

        menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMenu();
            }
        });

        cameraView = (JavaCameraView)findViewById(R.id.camera_view);
        cameraView.setMaxFrameSize(800,480);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        initGUI();


    }

    private void setWindowMode(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        imgMat = new Mat();
        //create featureExtractor with width and height?
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {

        Mat res = featureExtractor.findSign(frame.rgba(),new Scalar(hLowVal,sLowVal,vLowVal),new Scalar(hHighVal,sHighVal,vHighVal), drawMode);
        //return featureExtractor.detectShapeCountCurve(res[2]);
        //return featureExtractor.detectShapeCountCurve(frame.rgba());
        return res;

        //return imgMat;
    }

    @Override
    public void onCameraViewStopped(){

    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    private void toggleMenu(){
        LinearLayoutCompat l = findViewById(R.id.menu_layout);
        if (l.getVisibility() == View.VISIBLE)
        {
            l.setVisibility(View.INVISIBLE);
        }
        else
        {
            l.setVisibility(View.VISIBLE);
        }
    }

    private void initGUI(){
        //populate spinner from drawmode enum
        final Spinner mySpinner = (Spinner) findViewById(R.id.spinner_drawmode);
        mySpinner.setAdapter(new ArrayAdapter<DrawMode>(this,android.R.layout.simple_spinner_dropdown_item,DrawMode.values()));
        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                drawMode = DrawMode.valueOf(mySpinner.getSelectedItem().toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        hLowText = findViewById(R.id.hLow_textView);
        sLowText = findViewById(R.id.sLow_textView);
        vLowText = findViewById(R.id.vLow_textView);
        hHighText = findViewById(R.id.hHigh_textView);
        sHighText = findViewById(R.id.sHigh_textView);
        vHighText = findViewById(R.id.vHigh_textView);



        SeekBar hLow = findViewById(R.id.hLow_seekBar);
        SeekBar sLow = findViewById(R.id.sLow_seekBar);
        SeekBar vLow = findViewById(R.id.vLow_seekBar);

        SeekBar hHigh = findViewById(R.id.hHigh_seekBar);
        SeekBar sHigh = findViewById(R.id.sHigh_seekBar);
        SeekBar vHigh = findViewById(R.id.vHigh_seekBar);
        hLow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                hLowText.setText(String.valueOf(i));
                hLowVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sLow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sLowText.setText(String.valueOf(i));
                sLowVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        vLow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                vLowText.setText(String.valueOf(i));
                vLowVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        hHigh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                hHighText.setText(String.valueOf(i));
                hHighVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sHigh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sHighText.setText(String.valueOf(i));
                sHighVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        vHigh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                vHighText.setText(String.valueOf(i));
                vHighVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        hLowText.setText(String.valueOf(hLow.getProgress()));
        sLowText.setText(String.valueOf(sLow.getProgress()));
        vLowText.setText(String.valueOf(vLow.getProgress()));
        hHighText.setText(String.valueOf(hHigh.getProgress()));
        sHighText.setText(String.valueOf(sHigh.getProgress()));
        vHighText.setText(String.valueOf(vHigh.getProgress()));


        hLowVal = hLow.getProgress();
        sLowVal = sLow.getProgress();
        vLowVal = vLow.getProgress();
        hHighVal = hHigh.getProgress();
        sHighVal = sHigh.getProgress();
        vHighVal = vHigh.getProgress();

    }



}

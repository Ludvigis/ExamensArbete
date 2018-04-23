package com.example.ludvig.examensarbete;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;

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

    TextView cannyLow;
    TextView cannyHigh;
    int cannyLowVal;
    int cannyHighVal;

    TextView epsilonText;
    Double epsilonVal;

    DrawMode drawMode = DrawMode.IMAGE;

    private static final String TAG = "main_activity";
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

        try {
            Memory mem = Memory.getInstance();
            mem.loadPersistentMem(this);
            mem.loadPersistentExp(this);
            //mem.loadPersistentExp(this);
            //mem.loadPersistentMem(this);
            /*int i = 0;
            while(i < 10){
                Sign s1 = new Sign(mem,HDVECTOR.aboveBelow,HDVECTOR.Sr,HDVECTOR.Sb,HDVECTOR.same);
                Node n = new Node(s1,s1,"ROOT");
                n.addEpisodeToExperience(s1.getEpisodeVector());
                i++;
                Log.e("Mamma", "ROUND "+i +" TEST");
            }
            */


            String s = String.valueOf(VSA.hammingDist(mem.find(HDVECTOR.Cr),mem.find(HDVECTOR.Cb)));

            Log.i("Mamma","----> " +s);
        } catch (ClassNotFoundException e) {
            Log.e("Mamma","class not found", e);
        } catch (IOException e) {
            Log.e("Mamma", "IOException", e);
        }

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
        /*Mat img = frame.rgba();
        Mat edges = new Mat();
        Imgproc.Canny(img,edges,cannyLowVal,cannyHighVal);
        return edges;*/
        return featureExtractor.extractFeatures(frame.rgba(),new Scalar(hLowVal,sLowVal,vLowVal),new Scalar(hHighVal,sHighVal,vHighVal),cannyLowVal,cannyHighVal,epsilonVal, drawMode);

    }

    @Override
    public void onCameraViewStopped(){

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraView != null)
            cameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraView!=null)
            cameraView.disableView();
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



        //Canny
        SeekBar cLow = findViewById(R.id.cannyLow_seekBar);
        SeekBar cHigh = findViewById(R.id.cannyHigh_seekBar);

        cannyLow = findViewById(R.id.cannyLowText_view);
        cannyHigh = findViewById(R.id.cannyHighText_view);

        cannyLow.setText(String.valueOf(cLow.getProgress()));
        cannyHigh.setText(String.valueOf(cHigh.getProgress()));

        cLow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cannyLow.setText(String.valueOf(i));
                cannyLowVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cHigh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cannyHigh.setText(String.valueOf(i));
                cannyHighVal = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        cannyLowVal = cLow.getProgress();
        cannyHighVal = cHigh.getProgress();


        //epsilon

        SeekBar epsilon = findViewById(R.id.epsilon_seekBar);
        epsilonText = findViewById(R.id.epsilon_textView);
        epsilonText.setText(String.valueOf((double)epsilon.getProgress()/100));
        epsilon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                epsilonVal = (double)i / 100;
                epsilonText.setText(String.valueOf(epsilonVal));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        epsilonVal = (double)epsilon.getProgress() / 100;


    }



}

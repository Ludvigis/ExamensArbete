package com.example.ludvig.examensarbete;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    JavaCameraView cameraView;
    FeatureExtractor featureExtractor;
    Mat imgMat;

    boolean hasCapturedImage = false;
    Mat capturedImage;
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
    AppMode appMode;
    MatSignTuple latestmatSignTuple;

    private static final String TAG = "main_activity";
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();
                    featureExtractor.init();
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
        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                LinearLayoutCompat menu = findViewById(R.id.menu_layout);
                if(menu.getVisibility() == View.VISIBLE){
                    toggleMenu();
                }
                return false;
            }
        });

        initGUI();
        try {
            featureExtractor = new FeatureExtractor(this);

        } catch (IOException e) {
            Log.e(TAG, "IO didn't work: "+ e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found: "+ e.getMessage());
        }

            /*
            Sign s = new Sign(mem,HDVECTOR.aboveBelow,HDVECTOR.Sr,HDVECTOR.Sb,HDVECTOR.same);
            Node n = new Node(s,s,"n");
            for(int i = 0; i < 10; i++){
                n.addEpisodeToExperience(s.getEpisodeVector());
            }
            mem.savePersistentExp(this);
            mem.savePersistentMem(this);
            */



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
        capturedImage =  new Mat();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        imgMat = frame.rgba();
        if(appMode == AppMode.TESTING) {
            MatSignTuple mst = featureExtractor.extractFeatures(imgMat, new Scalar(hLowVal, sLowVal, vLowVal), new Scalar(hHighVal, sHighVal, vHighVal), cannyLowVal, cannyHighVal, epsilonVal, drawMode);
            DIR best = featureExtractor.checkForBest(mst.leftSign,mst.rightSign);
            Imgproc.putText(mst.img,best.toString(),new Point(mst.img.width()/2,mst.img.height()-100), Core.FONT_HERSHEY_SIMPLEX,1.5,new Scalar(0,255,0));


            return mst.img;
        }else  if (appMode == AppMode.TRAINING){
            if(hasCapturedImage){

                return capturedImage;

            }else{
                return imgMat;  //preview...
            }


        }else{
            Log.e(TAG,"invalid appmode");
            return null;//return frame.rgba();
        }


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

            l.setVisibility(View.GONE);
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

        //Populates spinner with different appmodes from AppMode enum...
        final Spinner trainingSpinner = (Spinner) findViewById(R.id.spinner_appmode);
        trainingSpinner.setAdapter(new ArrayAdapter<AppMode>(this,android.R.layout.simple_spinner_dropdown_item,AppMode.values()));
        trainingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                appMode = AppMode.valueOf(trainingSpinner.getSelectedItem().toString());
                Log.i(TAG,appMode.toString());
                LinearLayoutCompat l = findViewById(R.id.training_layout);

                if(AppMode.valueOf(trainingSpinner.getSelectedItem().toString()) == AppMode.TRAINING){

                    l.setVisibility(View.VISIBLE);
                }else{

                    //featureExtractor.saveExp();
                    l.setVisibility(View.GONE);


                }

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
        Button capture = findViewById(R.id.capture_button);
        Button left = findViewById(R.id.left_button);
        Button right = findViewById(R.id.right_button);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hasCapturedImage = !hasCapturedImage;
                capturedImage = imgMat.clone();
                MatSignTuple mst = featureExtractor.extractFeatures(capturedImage, new Scalar(hLowVal, sLowVal, vLowVal), new Scalar(hHighVal, sHighVal, vHighVal), cannyLowVal, cannyHighVal, epsilonVal, drawMode);
                capturedImage = mst.img;
                LinearLayoutCompat shapeDesc = findViewById(R.id.shape_desc_layout);
                TextView txt = findViewById(R.id.detected_shapes);
                latestmatSignTuple = mst;
                txt.setText(mst.leftSign.toString() + mst.rightSign.toString());
                if(hasCapturedImage){
                    capture.setText("Cancel");
                    shapeDesc.setVisibility(View.VISIBLE);
                    left.setVisibility(View.VISIBLE);
                    right.setVisibility(View.VISIBLE);
                }else{
                    capture.setText("Capture");
                    shapeDesc.setVisibility(View.GONE);
                    left.setVisibility(View.GONE);
                    right.setVisibility(View.GONE);
                }

            }
        });
        left.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.i(TAG,"Left");
                hasCapturedImage = false;
                capture.setText("Capture");
                left.setVisibility(View.GONE);
                right.setVisibility(View.GONE);
                LinearLayoutCompat shapeDesc = findViewById(R.id.shape_desc_layout);
                shapeDesc.setVisibility(View.GONE);
                featureExtractor.train(latestmatSignTuple.leftSign,DIR.LEFT);
                Toast.makeText(getBaseContext(),"Left clicked",Toast.LENGTH_SHORT).show();
            }
        });

        right.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.i(TAG,"right");
                hasCapturedImage = false;
                capture.setText("Capture");
                left.setVisibility(View.GONE);
                right.setVisibility(View.GONE);
                LinearLayoutCompat shapeDesc = findViewById(R.id.shape_desc_layout);
                shapeDesc.setVisibility(View.GONE);
                featureExtractor.train(latestmatSignTuple.rightSign,DIR.RIGHT);
                Toast.makeText(getBaseContext(),"Right clicked",Toast.LENGTH_SHORT).show();
            }
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                featureExtractor.saveExp();
                Toast.makeText(getBaseContext(),"Saved exp",Toast.LENGTH_SHORT).show();
            }
        });
        Button loadButton = findViewById(R.id.load_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                featureExtractor.loadExp();
                Toast.makeText(getBaseContext(),"Loaded exp",Toast.LENGTH_SHORT).show();
            }
        });

    }



}

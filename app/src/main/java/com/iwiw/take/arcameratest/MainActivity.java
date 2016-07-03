package com.iwiw.take.arcameratest;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private MainActivity m_activity;
    private CameraBridgeViewBase m_cameraView;
    private ArrayAdapter<String> m_adapterCampus;
    private boolean m_isDisplayingDialog = false;
    private int m_cameraId = 0;
    private ImageProcessing m_imageProcessing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        m_activity = this;
        initListElements();
        m_cameraView = (CameraBridgeViewBase)findViewById(R.id.camera_view);
        m_cameraView.setVisibility(View.INVISIBLE);
        m_cameraView.disableView();
        m_cameraId = Utility.getRearCameraId();
        m_cameraView.setCameraIndex(m_cameraId);

        m_imageProcessing = new IPface(this, IPface.FILTER_TYPE.OVERLAY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BaseLoaderCallback m_loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    if (m_isDisplayingDialog == false) {
                        if(Utility.getPermissionCamera(m_activity) == true) {
                            m_cameraView.setCvCameraViewListener(m_activity);
                            m_cameraView.setVisibility(View.VISIBLE);
                            m_cameraView.enableView();
                        } else {
                            m_isDisplayingDialog = true;
                        }
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, m_loaderCallback);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        m_isDisplayingDialog = false;
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            m_cameraView.setVisibility(View.VISIBLE);
            m_cameraView.setCvCameraViewListener(this);
            m_cameraView.enableView();
        } else {

        }
    }

    @Override
    public void onPause() {
        if (m_cameraView != null) {
            m_cameraView.disableView();
        }
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_cameraView != null) {
            m_cameraView.disableView();
        }
    }
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mat = inputFrame.rgba();
        if(m_cameraId == Utility.getFrontCameraId()) {
            Core.flip(mat, mat, 1);
        }

        return m_imageProcessing.doProcess(mat);
    }


    private void initListElements(){
        m_adapterCampus = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        m_adapterCampus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinnerCampus = (Spinner)findViewById(R.id.spinner_function);
        spinnerCampus.setAdapter(m_adapterCampus);
        spinnerCampus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                switch ((int) spinner.getSelectedItemId()) {
                    default:
                    case 0:
                        m_imageProcessing = new IPface(m_activity, IPface.FILTER_TYPE.OVERLAY);
                        break;
                    case 1:
                        m_imageProcessing = new IPface(m_activity, IPface.FILTER_TYPE.RECTANGLE);
                        break;
                    case 2:
                        m_imageProcessing = new IPedge();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        m_adapterCampus.clear();
        m_adapterCampus.add("Laughing Man");
        m_adapterCampus.add("Face Detection");
        m_adapterCampus.add("Edge Detection");
    }


    public void onClickSwitchCamera(View view) {
        m_cameraView.disableView();
        Switch switchCamera = (Switch)view;
        if(switchCamera.isChecked()) {
            switchCamera.setText("Camera_Rear");
            m_cameraId = Utility.getRearCameraId();
        } else {
            switchCamera.setText("Camera_Front");
            m_cameraId = Utility.getFrontCameraId();
        }

        m_cameraView.setCameraIndex(m_cameraId);
        m_cameraView.enableView();
    }


}


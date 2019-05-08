package com.lyc.newtestapplication.newtestapplication.Camera2Test;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.OutputConfiguration;
import android.media.ImageReader;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.lyc.newtestapplication.newtestapplication.BaseActivity;
import com.lyc.newtestapplication.newtestapplication.FullscreenActivity;
import com.lyc.newtestapplication.newtestapplication.R;


import java.util.ArrayList;
import java.util.Arrays;

public class MyCameraActivity extends FullscreenActivity implements View.OnClickListener {

    private TextureView textureView;
    private Button captureButton;
    private CameraHelper cameraHelper;
    private CameraManager cameraManager;
    private ArrayList<Surface> outputs=new ArrayList<>();
    private ImageReader imageReader=ImageReader.newInstance(900,900,1,1);


    @Override
    public Class getCurrentActivityName() {
        return MyCameraActivity.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_camera);
        captureButton=findViewById(R.id.capture_button);
        captureButton.setOnClickListener(this);
        textureView=findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(textureListener);

        cameraManager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);

    }

    private TextureView.SurfaceTextureListener textureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d("MyCameraActivity","  ------------------ textureListener  onSurfaceTextureAvailable----------------");
            outputs.add(new Surface(surface));
            cameraHelper=new CameraHelper(cameraManager,outputs);
            cameraHelper.init();
            cameraHelper.openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            cameraHelper.releaseCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    public void onClick(View v) {
        switch(View.generateViewId()){
            case R.id.capture_button:
                cameraHelper.takePicture();
                break;
                default:break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraHelper.releaseCamera();
        cameraHelper=null;
    }
}

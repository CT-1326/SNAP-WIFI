package com.Dev.unknown.snapwifi;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Clear the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Use TedPermission
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                startActivity(new Intent(MainActivity.this,Cam.class));
            }
            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("권한이 거부되어 어플 이용이 불가능합니다...\n\n권한을 허용해주세요!\n[설정] > [권한]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
    }
}


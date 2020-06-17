package com.Dev.unknown.snapwifi;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.Dev.unknown.snapwifi.R;
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
                .setDeniedMessage("You have declined ... but you can grant permissions under Settings> Permissions")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION)
                .check();
    }
}


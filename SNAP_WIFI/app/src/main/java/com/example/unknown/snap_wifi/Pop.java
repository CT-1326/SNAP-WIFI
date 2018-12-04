package com.example.unknown.snap_wifi;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import static com.example.unknown.snap_wifi.Cam.aa;


public class Pop extends AppCompatActivity {

    //private InterstitialAd interstitialAd;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop);

        //Clear the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Admob
       /* interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.ad_unit_id));
        AdRequest adRequest1 = new AdRequest.Builder().build();
        interstitialAd.loadAd(adRequest1);
        //When close Admob, return Camera
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                finish();
                startActivity(new Intent(Pop.this,Cam.class));

            }
        });*/

        //Get OCR result value
        EditText txtText = (EditText) findViewById(R.id.txtText);
        txtText.setText(aa);
    }



    //Touch Cancel
    public void Roll(View v){
        //Keyboard down
        EditText tt=(EditText)findViewById(R.id.txtText);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tt.getWindowToken(),0);

        //View Interstitial (when WIFI or LTE turn on)
        /*ConnectivityManager manager =(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo LTE = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo WIFI = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(LTE.isConnected() || WIFI.isConnected()){
            if(interstitialAd.isLoaded()){
                interstitialAd.show();
            }
        }
        else{
            finish();
            startActivity(new Intent(Pop.this,Cam.class));
        }*/
        finish();
        startActivity(new Intent(Pop.this,Cam.class));

    }

    //Touch OK
    public void Ok(View v)
    {
        //Keyboard down
        EditText tt=(EditText)findViewById(R.id.txtText);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tt.getWindowToken(),0);

        aa=tt.getText().toString();//String modified by keyboard

        //Go to next Activities
        Intent intent=new Intent(Pop.this,Load.class);
        startActivity(intent);
    }

    //Stop Backpress
    @Override
    public void onBackPressed() {
        return;
    }

    //stop Outer layer
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction()== MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

}

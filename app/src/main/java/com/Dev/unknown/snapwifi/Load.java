package com.Dev.unknown.snapwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.developer.kalert.KAlertDialog;
import com.kakao.adfit.ads.ba.BannerAdView;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;


public class Load extends AppCompatActivity {
    private static final String TAG = "To WIFI";
    //BackPress values
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    //Adfit
    private BannerAdView adView;
    private BannerAdView adView2;
    // WIFI & network
    WifiManager WIFI_Manger;
    ConnectivityManager connectivityManager;
    NetworkInfo WIFI;
    //Save WIFI AP list
    private ArrayList List = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);

        //Load adfit
        adView = findViewById(R.id.adView);
        adView.setClientId("DAN-t4yy5bfqsj8i");
        adView.loadAd();
        adView2 = findViewById(R.id.addView);
        adView2.setClientId("DAN-t4yy5bfqsj8i");
        adView2.loadAd();

        WIFI_Manger = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //IF WIFI is OFF
        if (WIFI_Manger.isWifiEnabled() == false && Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
        {
            Log.d(TAG,"New OS");
            KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.WARNING_TYPE);
            pDialog.setTitleText("WIFI를 켜야해요!");
            pDialog.setConfirmText("확인");
            pDialog.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    if (WIFI_Manger.isWifiEnabled() == true)
                    {
                        kAlertDialog.cancel();
                        WIFI_Conneted();
                    }
                }
            });
            pDialog.setCancelable(false);
            pDialog.show();
        }
        else if (WIFI_Manger.isWifiEnabled() == false)
        {
            Log.d(TAG,"Old OS");
            WIFI_Manger.setWifiEnabled(true);
            WIFI_Conneted();
        }
        else
            WIFI_Conneted();
    }
    //Try to WIFI connect
    public void WIFI_Conneted()
    {
        if (WIFI_Manger.isWifiEnabled() == true)
        {
            connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
            WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //IF already WIFI connected
            if (WIFI.isConnected())
            {
                KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.WARNING_TYPE);
                pDialog.setTitleText("이미 WIFI가 사용중 입니다");
                pDialog.setConfirmText("종료");
                pDialog.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                    @Override
                    public void onClick(KAlertDialog kAlertDialog) {
                        ActivityCompat.finishAffinity(Load.this);
                    }
                });
                pDialog.setCancelText("처음으로");
                pDialog.setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                    @Override
                    public void onClick(KAlertDialog kAlertDialog) {
                        finish();
                        startActivity(new Intent(Load.this, Cam.class));
                    }
                });
                pDialog.setCancelable(false);
                pDialog.show();
            }
            else
            {
                new SpotsDialog.Builder()
                        .setContext(this)
                        .setMessage("WIFI 스캔중...")
                        .setCancelable(false)
                        .build()
                        .show();
                initWIFIScan();
            }
        }
    }
//    //Successful WIFI scan
//    public void getWIFIScanResult() {
//        mScanResult = WIFI_Manger.getScanResults(); //ScanResult List
//        Log.d("ScanResult : ", String.valueOf(mScanResult));
//        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        for (int i = 0; i < mScanResult.size(); i++)
//        {
//            ScanResult Result = mScanResult.get(i);
////            Log.d("ScanResult : ", String.valueOf(Result));
//            String Capabilities =  Result.capabilities;
//            //Blocking Free carrier WIFI
//            if(Capabilities.contains("EAP"))
//            {
//                continue;
//            }
//            //Automatic connection
//            else
//            {
//                List.add(Result.SSID);
//                WifiConfiguration WIFI_Config = new WifiConfiguration();
//                WIFI_Config.SSID = String.format("\"%s\"", Result.SSID);
//                WIFI_Config.preSharedKey = String.format("\"%s\"", Result_Text);
//                int netId = WIFI_Manger.addNetwork(WIFI_Config);
//                //wifimanager.disconnect();
//                WIFI_Manger.enableNetwork(netId,false);
//                WIFI_Manger.reconnect();
//
//                if (WIFI.isConnected())
//                    Show_Result();
//            }
//        }
//        unregisterReceiver(mReceiver); //Stop WIFI SCan
//    }
    //init WIFI SCAN
    public void initWIFIScan()
    {
        Log.d(TAG,"start to scan");
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    Log.d(TAG,"success!");
                    scanSuccess();
                } else {
                    // scan failure handling
                    Log.d(TAG,"fail...");
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = WIFI_Manger.startScan();
        if (!success) {
            Log.d(TAG,"scan start failed... ");
            // scan failure handling
            scanFailure();
        }
    }

    private void scanSuccess() {
        Log.d(TAG,"success scan method");
        List<ScanResult> results = WIFI_Manger.getScanResults();
        Log.d("Result success : ", String.valueOf(results));

        for (int i = 0; i < results.size(); i++)
        {
            ScanResult Result = results.get(i);
            String Capabilities =  Result.capabilities;
            Log.d("Index : ", String.valueOf(i));
            //Blocking Free carrier WIFI
            if(Capabilities.contains("EAP"))
            {
                continue;
            }
            //Automatic connection
            else
            {
                List.add(Result.SSID);
                Log.d("SSID : ", Result.SSID);
//                WifiConfiguration WIFI_Config = new WifiConfiguration();
//                WIFI_Config.SSID = String.format("\"%s\"", Result.SSID);
//                WIFI_Config.preSharedKey = String.format("\"%s\"", Result_Text);
//                int netId = WIFI_Manger.addNetwork(WIFI_Config);
//                //wifimanager.disconnect();
//                WIFI_Manger.enableNetwork(netId,false);
//                WIFI_Manger.reconnect();
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

//                if (WIFI.isConnected())
//                    Show_Result();
            }
        }
    }

    private void scanFailure() {
        Log.d(TAG,"fail scan method");
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = WIFI_Manger.getScanResults();
        Log.d("Result fail : ", String.valueOf(results));
    }

//    //Remove all AP except connected AP
//    private void Show_Result()
//    {
//        WifiInfo wifiInfo = WIFI_Manger.getConnectionInfo();
//        String SSID = new String(wifiInfo.getSSID());
//        SSID = SSID.substring(1, SSID.length()-1);
//        Log.d("List of SSID : ", SSID);
//
//        for (int i = 0; i < List.size(); i++)
//        {
//            WifiConfiguration WIFI_Config = new WifiConfiguration();
//            WIFI_Config.SSID = String.format("\"%s\"", List.get(i));
//            WIFI_Config.preSharedKey = String.format("\"%s\"", Result_Text);
//            int netId = WIFI_Manger.addNetwork(WIFI_Config);
//
//            if(SSID.equals(List.get(i)))
//            {
//                continue;
//            }
//            else
//            {
//                Log.d("WIFI AP?", (String) List.get(i));
//                WIFI_Manger.removeNetwork(netId);
//                WIFI_Manger.saveConfiguration();
//            }
//        }
//
//        connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
//        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        //Show wifi connection result
//        if (WIFI.isConnected())
//        {
//            KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE);
//            pDialog.setTitleText("WIFI 연결에 성공!");
//            pDialog.setConfirmText("종료");
//            pDialog.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
//                        @Override
//                        public void onClick(KAlertDialog kAlertDialog) {
//                            ActivityCompat.finishAffinity(Load.this);
//                        }
//                    });
//            pDialog.setCancelText("처음으로");
//            pDialog.setCancelClickListener(new KAlertDialog.KAlertClickListener() {
//                        @Override
//                        public void onClick(KAlertDialog kAlertDialog) {
//                            finish();
//                            startActivity(new Intent(Load.this, Cam.class));
//                        }
//                    });
//            pDialog.setCancelable(false);
//            pDialog.show();
//        }
//        else
//        {
//            KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE);
//            pDialog.setTitleText("WIFI 연결에 실패...");
//            pDialog.setContentText("현재 이용하려는 WIFI 연결상태 문제일 수도 있습니다");
//            pDialog.setConfirmText("종료");
//            pDialog.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
//                @Override
//                public void onClick(KAlertDialog kAlertDialog) {
//                    ActivityCompat.finishAffinity(Load.this);
//                }
//            });
//            pDialog.setCancelText("처음으로");
//            pDialog.setCancelClickListener(new KAlertDialog.KAlertClickListener() {
//                @Override
//                public void onClick(KAlertDialog kAlertDialog) {
//                    finish();
//                    startActivity(new Intent(Load.this, Cam.class));
//                }
//            });
//            pDialog.setCancelable(false);
//            pDialog.show();
//        }
//    }
    //When touch BackPress twice, app closes
    @Override
    public void onBackPressed()
    {
        long One_Tab = System.currentTimeMillis();
        long Double_Tab = One_Tab -backPressedTime;

        if (0 <= Double_Tab && FINISH_INTERVAL_TIME >= Double_Tab)
        {
            ActivityCompat.finishAffinity(this);
        }
        else
        {
            backPressedTime = One_Tab;
            Toast.makeText(getApplicationContext(), "한번 더 뒤로가기를 누르면 어플이 종료됩니다", Toast.LENGTH_SHORT).show();
        }
    }
}

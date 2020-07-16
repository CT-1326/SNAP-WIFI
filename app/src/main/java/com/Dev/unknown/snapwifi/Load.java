package com.Dev.unknown.snapwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import dmax.dialog.SpotsDialog;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.kalert.KAlertDialog;
import com.kakao.adfit.ads.ba.BannerAdView;

import java.util.ArrayList;
import java.util.List;

import static com.Dev.unknown.snapwifi.Cam.Result_Text;

public class Load extends AppCompatActivity {
    private static final String TAG = "Scan";
    //BackPress values
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    //Adfit
    private BannerAdView adView;
    private BannerAdView adView2;
    // WIFI & network
    private WifiManager WIFI_Manger;
    private ConnectivityManager connectivityManager;
    private NetworkInfo WIFI;
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
        if (WIFI_Manger.isWifiEnabled() == false)
        {
            WIFI_Manger.setWifiEnabled(true);
        }
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //IF already WIFI connected
        if (WIFI.isConnected())
        {
            new KAlertDialog(this, KAlertDialog.WARNING_TYPE)
                    .setTitleText("이미 WIFI가 사용중 입니다")
                    .setConfirmText("종료")
                    .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog sDialog) {
                            ActivityCompat.finishAffinity(Load.this);
                        }
                    })
                    .setCancelText("처음으로")
                    .setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            finish();
                        }
                    })
                    .show();
        }
        else
        {
            new SpotsDialog.Builder()
                    .setContext(this)
                    .setMessage("WIFI 스캔중...")
                    .build()
                    .show();
//            initWIFIScan();
        }
    }
    //Surrounding area WIFI scan
    private List<ScanResult> mScanResult; // ScanResult List
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        //Start WIFI scan
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                getWIFIScanResult(); // get WIFISCanResult
                WIFI_Manger.startScan(); // for refresh
            }
            else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
            {
                sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }
        }
    };
    //Successful WIFI scan
    public void getWIFIScanResult() {
        mScanResult = WIFI_Manger.getScanResults(); //ScanResult List
        Log.d("ScanResult : ", String.valueOf(mScanResult));
        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        for (int i = 0; i < mScanResult.size(); i++)
        {
            ScanResult Result = mScanResult.get(i);
//            Log.d("ScanResult : ", String.valueOf(Result));
            String Capabilities =  Result.capabilities;
            //Blocking Free carrier WIFI
            if(Capabilities.contains("EAP"))
            {
                continue;
            }
            //Automatic connection
            else
            {
                List.add(Result.SSID);
                WifiConfiguration WIFI_Config = new WifiConfiguration();
                WIFI_Config.SSID = String.format("\"%s\"", Result.SSID);
                WIFI_Config.preSharedKey = String.format("\"%s\"", Result_Text);
                int netId = WIFI_Manger.addNetwork(WIFI_Config);
                //wifimanager.disconnect();
                WIFI_Manger.enableNetwork(netId,false);
                WIFI_Manger.reconnect();

                if (WIFI.isConnected())
                    Show_Result();
            }
        }
        unregisterReceiver(mReceiver); //Stop WIFI SCan
    }
    //init WIFI SCAN
    public void initWIFIScan()
    {
        Log.d(TAG,"Start");
        final IntentFilter filter = new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        WIFI_Manger.startScan();
    }
    //Remove all AP except connected AP
    private void Show_Result()
    {
        WifiInfo wifiInfo = WIFI_Manger.getConnectionInfo();
        String SSID = new String(wifiInfo.getSSID());
        SSID = SSID.substring(1, SSID.length()-1);
        Log.d("List of SSID : ", SSID);

        for (int i = 0; i < List.size(); i++)
        {
            WifiConfiguration WIFI_Config = new WifiConfiguration();
            WIFI_Config.SSID = String.format("\"%s\"", List.get(i));
            WIFI_Config.preSharedKey = String.format("\"%s\"", Result_Text);
            int netId = WIFI_Manger.addNetwork(WIFI_Config);

            if(SSID.equals(List.get(i)))
            {
                continue;
            }
            else
            {
                Log.d("WIFI AP?", (String) List.get(i));
                WIFI_Manger.removeNetwork(netId);
                WIFI_Manger.saveConfiguration();
            }
        }

        connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //Show wifi connection result
        if (WIFI.isConnected())
        {
            new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE)
                    .setTitleText("WIFI 연결에 성공!")
                    .setConfirmText("종료")
                    .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog sDialog) {
                            ActivityCompat.finishAffinity(Load.this);
                        }
                    })
                    .setCancelText("처음으로")
                    .setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            finish();
                        }
                    })
                    .show();
        }
        else
        {
            new KAlertDialog(this, KAlertDialog.WARNING_TYPE)
                    .setTitleText("WIFI 연결에 실패...")
                    .setContentText("현재 이용하려는 WIFI 연결상태에 문제가 있을 수도 있습니다")
                    .setConfirmText("종료")
                    .setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog sDialog) {
                            ActivityCompat.finishAffinity(Load.this);
                        }
                    })
                    .setCancelText("처음으로")
                    .setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                        @Override
                        public void onClick(KAlertDialog kAlertDialog) {
                            finish();
                        }
                    })
                    .show();
        }
    }
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

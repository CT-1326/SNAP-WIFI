package com.example.unknown.snapwifi;


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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.kakao.adfit.ads.ba.BannerAdView;

import java.util.ArrayList;
import java.util.List;

import static com.example.unknown.snapwifi.Cam.RT;

public class Load extends AppCompatActivity {


    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;

    private BannerAdView adView;
    private BannerAdView addView;
    // Setup WIFI
    WifiManager wifimanager;

    //Setup Progressbar
    ProgressBar probar;
    TextView text;
    Handler handler;

    ArrayList list = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);

        adView = findViewById(R.id.adView);  // 배너 광고 뷰
        adView.setClientId("DAN-1hr5wkw0xrbjp");  // 할당 받은 광고 단위(clientId) 설정
        adView.loadAd();  // 광고 요청

        addView = findViewById(R.id.addView);  // 배너 광고 뷰
        addView.setClientId("DAN-1hr5wkw0xrbjp");  // 할당 받은 광고 단위(clientId) 설정
        addView.loadAd();  // 광고 요청


        wifimanager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        // Turn WIFI ON
        if (wifimanager.isWifiEnabled() == false)
            wifimanager.setWifiEnabled(true);

        probar = (ProgressBar) findViewById(R.id.pb);
        text = (TextView) findViewById(R.id.tv);

        ConnectivityManager manager =(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo WIFI = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        AlertDialog.Builder builder = new AlertDialog.Builder(Load.this);
        //If already WIFI connected
        if (WIFI.isConnected()) {
            builder.setTitle("Already use WIFI");
            //builder.setCancelable(false);
            builder.setPositiveButton("END",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.finishAffinity(Load.this);
                        }
                    });
            builder.setNegativeButton("Go First",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.show();
        }
        else {
            text.setText("Connecting WiFi...");
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //If progressbar is max
                    if (msg.arg1 == 600) {
                        show();
                    }
                }
            };

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i <= 600; i+=10) {
                        //Check real-time WIFI status
                        ConnectivityManager cm =(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo wf = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                        probar.setProgress(i);
                        Message msg = handler.obtainMessage();
                        msg.arg1 = i;
                        handler.sendMessage(msg);
                        //If the WIFI connection is successful
                        if(wf.isConnected()) {
                            msg.arg1=600;
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            t.start();
            initWIFIScan();
        }
    }


    //Surrounding area wifi scan
    private List<ScanResult> mScanResult; // ScanResult List
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        //NetworkInfo nifo;
        //Start WIFI scan
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                getWIFIScanResult(); // get WIFISCanResult
                wifimanager.startScan(); // for refresh
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }
        }
    };

    public void getWIFIScanResult() {
        mScanResult = wifimanager.getScanResults(); // ScanResult
        // Scan count 5
        for (int i = 0; i < 5; i++) {
            ScanResult result = mScanResult.get(i);
            String Capabilities =  result.capabilities;

            //Blocking Free carrier WIFI
            if(Capabilities.contains("EAP")) {
                continue;
            }
            //Automatic connection
            else {
                Log.d("asdf",result.SSID);
                list.add(result.SSID);
                WifiConfiguration wificonfig = new WifiConfiguration();
                wificonfig.SSID = String.format("\"%s\"", result.SSID);
                wificonfig.preSharedKey = String.format("\"%s\"", RT);
                int netId = wifimanager.addNetwork(wificonfig);
                //wifimanager.disconnect();
                wifimanager.enableNetwork(netId,false);
                wifimanager.reconnect();
            }
        }
        unregisterReceiver(mReceiver); // stop WIFISCan
    }

    public void initWIFIScan() {
        // init WIFISCAN
        final IntentFilter filter = new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        wifimanager.startScan();
    }

    private void show(){

        //Remove all ap but connected ap
        WifiManager wmanager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wmanager.getConnectionInfo();
        String ssid = new String(wifiInfo.getSSID());
        ssid = ssid.substring(1, ssid.length()-1);
        Log.d("asdfg", ssid);

        for (int i=0;i<list.size();i++) {
            String ss = new String(String.valueOf(list.get(i)));
            WifiConfiguration wificonfig = new WifiConfiguration();
            wificonfig.SSID = String.format("\"%s\"", list.get(i));
            wificonfig.preSharedKey = String.format("\"%s\"", RT);
            int netId = wifimanager.addNetwork(wificonfig);

            Log.d("asdfgh", String.valueOf(list.get(i)));


            if(ssid.equals(list.get(i))){
                Log.d("asdfghj","Yes");
                continue;
            }
            else{
                Log.d("asdfghj","No");
                wifimanager.removeNetwork(netId);
                wifimanager.saveConfiguration();
            }

        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ConnectivityManager manager =(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo WIFI = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(WIFI.isConnected()) {
                builder.setTitle("WIFI connection successful!");
                //builder.setCancelable(false);
                builder.setPositiveButton("END",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.finishAffinity(Load.this);
                            }
                        });
                builder.setNegativeButton("Go First",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                builder.show();
        }
        else{
                builder.setTitle("WIFI connection failed...\n(May be temporary error)");
                //builder.setCancelable(false);
                builder.setPositiveButton("END",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.finishAffinity(Load.this);
                            }
                        });
                builder.setNegativeButton("Go First",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                builder.show();
        }
    }

    //Stop Backpress
    @Override
    public void onBackPressed() {

        long a = System.currentTimeMillis();
        long b = a-backPressedTime;

        if (0 <= b && FINISH_INTERVAL_TIME >= b)
        {
            ActivityCompat.finishAffinity(this);
        }
        else
        {
            backPressedTime = a;
            Toast.makeText(getApplicationContext(), "Press the Back button again to exit", Toast.LENGTH_SHORT).show();
        }
    }

}

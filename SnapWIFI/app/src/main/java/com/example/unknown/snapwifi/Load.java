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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import static com.example.unknown.snapwifi.Cam.RT;

public class Load extends AppCompatActivity {

    // Setup WIFI
    WifiManager wifimanager;

    //private IntentFilter mFilter;

    ProgressBar probar;
    TextView text;
    Handler handler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        //Admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdView mmAdView = (AdView) findViewById(R.id.addView);
        //AdRequest aadRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        AdRequest aadRequest = new AdRequest.Builder().build();
        mmAdView.loadAd(aadRequest);

        wifimanager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        // WIFI ON
        if (wifimanager.isWifiEnabled() == false)
            wifimanager.setWifiEnabled(true);

        probar = (ProgressBar) findViewById(R.id.pb);
        text = (TextView) findViewById(R.id.tv);

        AlertDialog.Builder builder = new AlertDialog.Builder(Load.this);

        final ConnectivityManager manager =(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo WIFI = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        //If WIFI connected right
        if (WIFI.isConnected()) {
            builder.setTitle("Already use WIFI");
            builder.setCancelable(false);
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
        } else {

            text.setText("Connecting WiFi...");

            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.arg1 == 100) {
                        show();
                    }
                }
            };

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i <= 100; i += 10) {
                        probar.setProgress(i);
                        Message msg = handler.obtainMessage();
                        msg.arg1 = i;
                        handler.sendMessage(msg);
                        try {
                            Thread.sleep(2000);
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
        NetworkInfo nifo;
        //Get wifi lists.
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                getWIFIScanResult(); // get WIFISCanResult
                wifimanager.startScan(); // for refresh
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                sendBroadcast(new Intent("wifi.ON_NETWORK_STATE_CHANGED"));
            }

            //Check real-time WiFi status
            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                nifo = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                //Wifi is on
                if (nifo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    unregisterReceiver(mReceiver);
                }
            }
        }
    };

    ArrayList list = new ArrayList<String>();

    public void getWIFIScanResult() {
        mScanResult = wifimanager.getScanResults(); // ScanResult
        // Scan count
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
                //wifimanager.reconnect();
            }
        }

       /* try{
            Thread.sleep(7000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        /*WifiManager manager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();
        String ssid = new String(wifiInfo.getSSID());
        ssid = ssid.substring(1, ssid.length()-1);

       //String ssid = new String("dd");

        for (int i=0;i<list.size();i++) {
            Log.d("asdfg", ssid);
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

        }*/
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ConnectivityManager manager =(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo WIFI = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //If WIFI connected right
        if(WIFI.isConnected()) {
                builder.setTitle("WIFI connection successful!");
                builder.setCancelable(false);
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
        //Not connected wifi
        else{
                builder.setTitle("WIFI connection failed...\n(May be temporary error)");
                builder.setCancelable(false);
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
        return;
    }

}

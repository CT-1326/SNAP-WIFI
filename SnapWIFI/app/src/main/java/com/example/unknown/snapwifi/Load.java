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

import java.util.ArrayList;
import java.util.List;

import static com.example.unknown.snapwifi.Cam.RT;

public class Load extends AppCompatActivity {

    // Setup WIFI
    WifiManager wifimanager;
    //private IntentFilter mFilter;

    String RS;

    ProgressBar probar;
    TextView text;
    Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);

        //Admob
        AdView mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdView mmAdView = (AdView) findViewById(R.id.addView);
        //AdRequest aadRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        AdRequest aadRequest = new AdRequest.Builder().build();
        mmAdView.loadAd(aadRequest);

        wifimanager= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        // WIFI ON
        if (wifimanager.isWifiEnabled() == false)
            wifimanager.setWifiEnabled(true);

        //Setup check real-time WiFi status
        /*mFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(mReceiver, mFilter);*/

        probar=(ProgressBar)findViewById(R.id.pb);
        text=(TextView)findViewById(R.id.tv);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                text.setText("WIFI is connecting now");
                if(msg.arg1 == 100){
                    show();
                }
            }
        };

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<=100;i+=10){
                    //Log.d("asdf", String.valueOf(i));
                    probar.setProgress(i);
                    Message msg = handler.obtainMessage();
                    msg.arg1=i;
                    handler.sendMessage(msg);
                    try{
                        Thread.sleep(1000);
                        initWIFIScan();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            }
        });
        t.start();

        //Loading Window
       /* CheckTypesTask task = new CheckTypesTask();
        task.execute();*/
    }



    /*private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(
                Load.this);

        //View Loading Window
        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("WIFI is connecting now");

            asyncDialog.setCancelable(false);
            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }

        //Runnig Loading Window Maximum 30sec
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                for(int i=0;i<30; i++) {
                    if(ck==1)
                        break;
                    else {
                        Thread.sleep(1000);
                        initWIFIScan();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        //Close Loading Window
        @Override
        protected void onPostExecute(Void result) {
            asyncDialog.dismiss();
            super.onPostExecute(result);

            show();
        }
    }*/

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
        for (int i = 0; i < mScanResult.size(); i++) {
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
                //wificonfig.BSSID = result.BSSID;
                wificonfig.SSID = String.format("\"%s\"", result.SSID);
                wificonfig.preSharedKey = String.format("\"%s\"", RT);
                int netId = wifimanager.addNetwork(wificonfig);
                wifimanager.disconnect();
                wifimanager.enableNetwork(netId,false);
                wifimanager.reconnect();

                //give 5sec
              /*  try{
                    Thread.sleep(5000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Check WIFI stauts
                ConnectivityManager manager =(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo WIFI = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if(WIFI.isConnected()){
                    break;
                }
                else{
                    wifimanager.removeNetwork(netId);
                    wifimanager.saveConfiguration();
                }*/
            }
        }

        //give 7sec
        try{
            Thread.sleep(7000);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        WifiManager manager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = manager.getConnectionInfo();
        String ssid = new String(wifiInfo.getSSID());
        ssid = ssid.substring(1, ssid.length()-1);

        for (int i=0;i<list.size();i++) {
            Log.d("asdfg", ssid);
            String ss = new String(String.valueOf(list.get(i)));
                WifiConfiguration wificonfig = new WifiConfiguration();
                //wificonfig.BSSID = String.valueOf(list.get(i));
                wificonfig.SSID = String.format("\"%s\"", list.get(i));
                wificonfig.preSharedKey = String.format("\"%s\"", RT);
                int netId = wifimanager.addNetwork(wificonfig);

            Log.d("asdfgh", String.valueOf(list.get(i)));


                if(ssid.equals(list.get(i))){
                    Log.d("asdfghj","좆같네");
                    continue;
                }
                else{
                    wifimanager.removeNetwork(netId);
                    wifimanager.saveConfiguration();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ConnectivityManager manager =(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo WIFI = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //If WIFI connected right
        if(WIFI.isConnected()) {
            //Toast.makeText(getApplicationContext(),"연결 성공!",Toast.LENGTH_LONG).show();
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
           // Toast.makeText(getApplicationContext(),"연결 실패...",Toast.LENGTH_LONG).show();
                builder.setTitle("WIFI connection failed...\n(Temporary connection fail?)");
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

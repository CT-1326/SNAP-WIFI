package com.example.unknown.snap_wifi;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.List;

import static com.example.unknown.snap_wifi.Cam.aa;

/*
    <com.google.android.gms.ads.AdView
        android:id="@+id/lv"
        ads:adSize="BANNER"
        ads:adUnitId="@string/banner_ad_unit_id"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"></com.google.android.gms.ads.AdView>

    <com.google.android.gms.ads.AdView
        android:id="@+id/llv"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:layout_marginBottom="8dp"
        ads:adSize="BANNER"
        ads:adUnitId="@string/banner_ad_unit_id"
        ads:layout_constraintBottom_toBottomOf="parent"></com.google.android.gms.ads.AdView>
 */

public class Load extends AppCompatActivity {

    // Setup WIFI
    WifiManager wifimanager;
    private IntentFilter mFilter;

    private int ck=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);

        //Admob
       /* AdView mAdView = (AdView) findViewById(R.id.lv);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        //AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        AdView mmAdView = (AdView) findViewById(R.id.llv);
        AdRequest aadRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        //AdRequest aadRequest = new AdRequest.Builder().build();
        mmAdView.loadAd(aadRequest);*/

        wifimanager= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        // WIFI ON
        if (wifimanager.isWifiEnabled() == false)
            wifimanager.setWifiEnabled(true);

        //Setup check real-time WiFi status
        mFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        registerReceiver(mReceiver, mFilter);

        //Loading Window
        CheckTypesTask task = new CheckTypesTask();
        task.execute();
    }

    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(
                Load.this);

        //View Loading Window
        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("WIFI가 연결 중입니다.");

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

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    Load.this);

            //If WIFI connected right
            if(ck==1) {
                alertDialogBuilder
                        .setMessage("WIFI 연결 완료!")
                        .setCancelable(false)
                        .setPositiveButton("종료",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        ActivityCompat.finishAffinity(Load.this);
                                    }
                                })
                        .setNegativeButton("처음으로",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        startActivity(new Intent(Load.this, Cam.class));
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            //Not connected wifi
            else{
                alertDialogBuilder
                        .setMessage("WIFI 연결 실패...\n(일시적인 연결불량 혹은 비밀번호 오류!)")
                        .setCancelable(false)
                        .setPositiveButton("종료",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        ActivityCompat.finishAffinity(Load.this);
                                    }
                                })
                        .setNegativeButton("처음으로",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        startActivity(new Intent(Load.this,Cam.class));
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
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
                    ck=1;
                    unregisterReceiver(mReceiver);
                }
            }
        }
    };
    ArrayList<String> li = new ArrayList<String>();
    public void getWIFIScanResult() {
        mScanResult = wifimanager.getScanResults(); // ScanResult
        // Scan count
        for (int i = 0; i < mScanResult.size(); i++) {
            ScanResult result = mScanResult.get(i);
            String Capabilities =  result.capabilities;

            /*if(result.SSID.toString().matches(".*KT.*") || result.SSID.toString().matches(".*olleh.*") || result.SSID.toString().matches(".*U+.*")
                    || result.SSID.toString().matches(".*SK.*") || result.SSID.toString().matches("T"))
                    continue;*/

            //Blocking Free carrier WIFI
            if(Capabilities.contains("EAP")) {
                continue;
            }
            //Automatic connection
            else {
                li.add(result.SSID.toString());
                WifiConfiguration wificonfig = new WifiConfiguration();
                wificonfig.SSID = String.format("\"%s\"", result.SSID.toString());
                wificonfig.preSharedKey = String.format("\"%s\"", aa);
                int netId = wifimanager.addNetwork(wificonfig);
                wifimanager.disconnect();
                wifimanager.enableNetwork(netId,false);
                wifimanager.reconnect();

                //One ap give 10sec
                try{
                    Thread.sleep(10000);
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
                }
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

    //Stop Backpress
    @Override
    public void onBackPressed() {
        return;
    }

}

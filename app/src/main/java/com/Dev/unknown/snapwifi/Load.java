package com.Dev.unknown.snapwifi;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.developer.kalert.KAlertDialog;
import com.kakao.adfit.ads.ba.BannerAdView;

import java.util.ArrayList;
import java.util.List;

import static com.Dev.unknown.snapwifi.Cam.Result_Text;

public class Load extends AppCompatActivity {
    private static final String TAG = "To WIFI";
    //BackPress values
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    //AdFit
    private BannerAdView adView;
    private BannerAdView adView2;
    // WIFI & network
    WifiManager WIFI_Manger;
    ConnectivityManager connectivityManager;
    NetworkInfo WIFI;
    ConnectTask connectTask;
    //Save WIFI AP list
    private ArrayList List = new ArrayList<String>();
    //String files Text
    private String onWIFIing;
    private String Close;
    private String backFirst;
    private String backOnemoreClose;
    private String scanningWIFI;
    private String wifiConnectSuccess;
    private String wifiConnectFail;
    private String wifiConnectError;
    private String wifiSetting;

    KAlertDialog pDialogOK, pDialogError;
    int isConnected;

    int cnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load);

        Log.d(TAG, Result_Text);

        //Retrieve string files Text
        onWIFIing = getResources().getString(R.string.on_wifi_ing);
        String onWIFI = getResources().getString(R.string.on_wifi);
        String OK = getResources().getString(R.string.ok);
        Close = getResources().getString(R.string.close);
        backFirst = getResources().getString(R.string.back_first);
        backOnemoreClose = getResources().getString(R.string.back_onemore_close);
        scanningWIFI = getResources().getString(R.string.scanning_wifi);
        wifiConnectSuccess = getResources().getString(R.string.wifi_connect_success);
        wifiConnectFail = getResources().getString(R.string.wifi_connect_fial);
        ;
        wifiConnectError = getResources().getString(R.string.wifi_connect_error);
        ;
        wifiSetting = getResources().getString(R.string.wifi_setting);
        ;

        pDialogOK = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE);
        pDialogError = new KAlertDialog(this, KAlertDialog.SUCCESS_TYPE);

        //Load adfit
        adView = findViewById(R.id.adView);
        adView.setClientId("DAN-t4yy5bfqsj8i");
        adView.loadAd();
        adView2 = findViewById(R.id.addView);
        adView2.setClientId("DAN-t4yy5bfqsj8i");
        adView2.loadAd();

        //Retrieve WIFI & Network status
        WIFI_Manger = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        //IF WIFI is OFF
        if (WIFI_Manger.isWifiEnabled() == false) {
            WIFI_Manger.setWifiEnabled(true);
            KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.WARNING_TYPE);
            pDialog.setTitleText(onWIFI);
            pDialog.setConfirmText(OK);
            pDialog.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    if (WIFI_Manger.isWifiEnabled() == true) {
                        kAlertDialog.cancel();
                        WIFI_Connected();
                    }
                }
            });
            pDialog.setCancelable(false);
            pDialog.show();

            WIFI_Connected();
        } else
            WIFI_Connected();
    }

    //Try to WIFI connect
    public void WIFI_Connected() {
        if (WIFI_Manger.isWifiEnabled() == true) {
            //IF already WIFI connected
            if (WIFI.isConnected()) {
                KAlertDialog pDialog = new KAlertDialog(this, KAlertDialog.WARNING_TYPE);
                pDialog.setTitleText(onWIFIing);
                pDialog.setConfirmText(Close);
                pDialog.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                    @Override
                    public void onClick(KAlertDialog kAlertDialog) {
                        ActivityCompat.finishAffinity(Load.this);
                    }
                });
                pDialog.setCancelText(backFirst);
                pDialog.setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                    @Override
                    public void onClick(KAlertDialog kAlertDialog) {
                        finish();
                        startActivity(new Intent(Load.this, Cam.class));
                    }
                });
                pDialog.setCancelable(false);
                pDialog.show();
            } else {
//                new SpotsDialog.Builder()
//                        .setContext(this)
//                        .setMessage(scanningWIFI)
//                        .setCancelable(false)
//                        .build()
//                        .show();
                initWIFIScan();
            }
        }
    }

    //init WIFI SCAN
    public void initWIFIScan() {
        Log.d(TAG, "start to scan");
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    Log.d(TAG, "success!");
                    //scanSuccess();
                    cnt = 0;
                    cnt++;
                    Log.d("cnt_method", String.valueOf(cnt));
                    connectTask = new ConnectTask();
                    connectTask.execute();
                    pDialogError.dismiss();
                } else {
                    // scan failure handling
                    Log.d(TAG, "fail...");
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = WIFI_Manger.startScan();
        if (!success) {
            Log.d(TAG, "scan start failed... ");
            // scan failure handling
            scanFailure();
        }
    }

    //Success scan and try connect
    private void scanSuccess() {
        Log.d(TAG, "success scan method");
        List<ScanResult> results = WIFI_Manger.getScanResults();
        Log.d(TAG, "Result success : " + results);

        for (int i = 0; i < results.size(); i++) {
            pDialogError.dismiss();
            ScanResult Result = results.get(i);
            String Capabilities = Result.capabilities;
            Log.d(TAG, "Index : " + i);

            //Blocking Free carrier WIFI
            if (Capabilities.contains("EAP")) {
                continue;
            }
            //Automatic connection
            else {
                List.add(Result.SSID);
                Log.d(TAG, "SSID : " + Result.SSID);
                WifiConfiguration WIFI_Config = new WifiConfiguration();
                WIFI_Config.SSID = String.format("\"%s\"", Result.SSID);
                WIFI_Config.preSharedKey = String.format("\"%s\"", Result_Text);
                int netId = WIFI_Manger.addNetwork(WIFI_Config);
                //wifimanager.disconnect();
                WIFI_Manger.enableNetwork(netId, false);
                WIFI_Manger.reconnect();
            }
            connectTask.cancel(true);
        }
        Show_Result();
    }


    private class ConnectTask extends AsyncTask<Integer, Void, Void> {
        ProgressDialog dig = new ProgressDialog(Load.this);
        int isConnected;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
//            new SpotsDialog.Builder()
//                    .setContext(Load.this)
//                    .setMessage(scanningWIFI)
//                    .setCancelable(false)
//                    .build()
//                    .show();
            dig.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dig.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            if(Show_Result() == 1) {
                dig.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Integer... isConnected) {
            // TODO Auto-generated method stub
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanSuccess();
                }
            }, 0);
            return null;
        }

        protected void onCancelled() {
            super.onCancelled();
            dig.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (connectTask.getStatus() == AsyncTask.Status.RUNNING) {
                connectTask.cancel(true);
            } else {
            }
        } catch (Exception e) {
        }
    }

    //Fail scan
    private void scanFailure() {
        Log.d(TAG, "fail scan method");
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = WIFI_Manger.getScanResults();
        Log.d(TAG, "Result fail : " + results);
    }

    //Remove all AP except connected AP
    private int Show_Result() {
        int isConnected = 0;
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        Log.d(TAG, "Done!");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pDialogError.dismiss();


        //Show wifi connection result
        if (WIFI.isConnected()) {
            connectTask.cancel(true);
            pDialogError.dismiss();
            isConnected = 1;

            WifiInfo wifiInfo = WIFI_Manger.getConnectionInfo();
            String SSID = new String(wifiInfo.getSSID());
            SSID = SSID.substring(1, SSID.length() - 1);
            Log.d(TAG, "Now AP : " + SSID);

            for (int i = 0; i < List.size(); i++) {
                WifiConfiguration WIFI_Config = new WifiConfiguration();
                WIFI_Config.SSID = String.format("\"%s\"", List.get(i));
                WIFI_Config.preSharedKey = String.format("\"%s\"", Result_Text);
                int netId = WIFI_Manger.addNetwork(WIFI_Config);

                if (SSID.equals(List.get(i))) {
                    continue;
                } else {
                    Log.d(TAG, "Delete this AP : " + (String) List.get(i));
                    WIFI_Manger.removeNetwork(netId);
                    WIFI_Manger.saveConfiguration();
                }
            }
            pDialogOK.setTitleText(wifiConnectSuccess);
            pDialogOK.setConfirmText(Close);
            pDialogOK.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    moveTaskToBack(true);
                    finish();
                    Process.killProcess(Process.myPid());
                }
            });
            pDialogOK.setCancelText(backFirst);
            pDialogOK.setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    System.exit(0);
                }
            });
            pDialogOK.setCancelable(false);
            pDialogOK.show();
            connectTask.cancel(true);
        } else {

            pDialogError.dismiss();
            pDialogError.setTitleText(wifiConnectFail);
            pDialogError.setContentText(wifiConnectFail);
            pDialogError.setConfirmText(wifiSetting);
            pDialogError.setConfirmClickListener(new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    /*moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());*/
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                }
            });
            pDialogError.setCancelText(backFirst);
            pDialogError.setCancelClickListener(new KAlertDialog.KAlertClickListener() {
                @Override
                public void onClick(KAlertDialog kAlertDialog) {
                    System.exit(0);
                }
            });
            pDialogError.show();
        }
        return isConnected;
    }
}
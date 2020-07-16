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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.adfit.ads.ba.BannerAdView;

import java.util.ArrayList;
import java.util.List;

import static com.Dev.unknown.snapwifi.Cam.Result_Text;

public class Load extends AppCompatActivity {
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    //Adfit
    private BannerAdView adView;
    private BannerAdView adView2;
    // WIFI & network
    private WifiManager WIFI_Manger;
    private ConnectivityManager connectivityManager;
    private NetworkInfo WIFI;
    //Progressbar
    private ProgressBar probar;
    private TextView text;
    private Handler handler;
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

        WIFI_Manger = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        // Turn WIFI ON
        if (WIFI_Manger.isWifiEnabled() == false)
            WIFI_Manger.setWifiEnabled(true);

        probar = (ProgressBar) findViewById(R.id.pb);
        text = (TextView) findViewById(R.id.tv);

        AlertDialog.Builder builder = new AlertDialog.Builder(Load.this);
        //If already WIFI connected
        if (WIFI.isConnected())
        {
            builder.setTitle("이미 WIFI가 연결돼있습니다");
            //builder.setCancelable(false);
            builder.setPositiveButton("종료",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.finishAffinity(Load.this);
                        }
                    });
            builder.setNegativeButton("처음으로",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.show();
        }
        else
        {
            text.setText("WIFI 연결중...");
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //If progressbar is max
                    if (msg.arg1 == 600)
                    {
                        Show_Result();
                    }
                }
            };
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i <= 600; i+=10)
                    {
                        connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
                        WIFI = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        probar.setProgress(i);
                        Message msg = handler.obtainMessage();
                        msg.arg1 = i;
                        handler.sendMessage(msg);
                        //If the WIFI connection is successful
                        if(WIFI.isConnected())
                        {
                            msg.arg1 = 600;
                            break;
                        }
                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e)
                        {
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
    //Successful wifi scan
    public void getWIFIScanResult() {
        mScanResult = WIFI_Manger.getScanResults(); // ScanResult
        // Scan count 5
        for (int i = 0; i < 5; i++)
        {
            ScanResult Result = mScanResult.get(i);
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
                WifiConfiguration wificonfig = new WifiConfiguration();
                wificonfig.SSID = String.format("\"%s\"", Result.SSID);
                wificonfig.preSharedKey = String.format("\"%s\"", Result_Text);
                int netId = WIFI_Manger.addNetwork(wificonfig);
                //wifimanager.disconnect();
                WIFI_Manger.enableNetwork(netId,false);
                WIFI_Manger.reconnect();
            }
        }
        unregisterReceiver(mReceiver); // stop WIFI SCan
    }
    // init WIFI SCAN
    public void initWIFIScan()
    {
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
        Log.d("WIFI AP", SSID);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        connectivityManager=(ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        WIFI=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        //Show wifi connection result
        if(WIFI.isConnected())
        {
                builder.setTitle("WIFI 연결에 성공하였습니다!");
                //builder.setCancelable(false);
                builder.setPositiveButton("종료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.finishAffinity(Load.this);
                            }
                        });
                builder.setNegativeButton("처음으로",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                builder.show();
        }
        else
        {
            builder.setTitle("WIFI 연결에 실패했습니다...\n(해당 WIFI 연결상태 문제일수도 있습니다)");
            //builder.setCancelable(false);
            builder.setPositiveButton("종료",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.finishAffinity(Load.this);
                        }
                    });
            builder.setNegativeButton("처음으로",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.show();
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
            Toast.makeText(getApplicationContext(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        }
    }
}

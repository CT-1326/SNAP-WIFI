package com.Dev.unknown.snapwifi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uniquestudio.library.CircleCheckBox;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

public class ShowOnBoarding extends AppCompatActivity {

    private MaterialTapTargetPrompt.Builder builder;
    LinearLayout scanResultDialog;
    EditText editText;
    View Rect;
    CircleCheckBox checkBox;
    private String startScan, exStartScan, scanArea, exScanArea, zoomInOut,
            exZoomInOut, scanResult, exScanResult, startConnect, exStartConnect,
            Reload, exReload, notReload, exNotreload;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_layout);

        builder = new MaterialTapTargetPrompt.Builder(this);
        scanResultDialog = (LinearLayout) findViewById(R.id.scan_result_dialog);
        Rect = (View) findViewById(R.id.rect);
        checkBox = (CircleCheckBox) findViewById(R.id.check_show);

        startScan = getResources().getString(R.string.onboard_start_scan);
        exStartScan = getResources().getString(R.string.ex_start_scan);
        scanArea = getResources().getString(R.string.onboard_scan_area);
        exScanArea = getResources().getString(R.string.ex_scan_area);
        zoomInOut = getResources().getString(R.string.onboard_zoom_in_out);
        exZoomInOut = getResources().getString(R.string.ex_zoom_in_out);
        scanResult = getResources().getString(R.string.onboard_scan_result);
        exScanResult = getResources().getString(R.string.ex_scan_result);
        startConnect = getResources().getString(R.string.onboard_start_connect);
        exStartConnect = getResources().getString(R.string.ex_start_connect);
        Reload = getResources().getString(R.string.onboard_reload_ex);
        exReload = getResources().getString(R.string.ex_reload_ex);
        notReload = getResources().getString(R.string.onboard_not_reload_ex);
        exNotreload = getResources().getString(R.string.ex_not_reload_ex);

        showButton();
    }

    private void showButton() {
        builder.setTarget(R.id.startButton)
                .setPrimaryText(startScan)
                .setSecondaryText(exStartScan)
                .setPromptBackground(new RectanglePromptBackground())
                .setPromptFocal(new RectanglePromptFocal())
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                            Rect.setVisibility(View.VISIBLE);
                            showRect();
                        }
                    }
                })
                .show();
    }

    private void showRect() {
        builder.setTarget(R.id.rect)
                .setPrimaryText(scanArea)
                .setSecondaryText(exScanArea)
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                            showSeekBar();
                        }
                    }
                })
                .show();
    }

    private void showSeekBar() {
        new MaterialTapTargetPrompt.Builder(this)
                .setTarget(R.id.seekBar1)
                .setPrimaryText(zoomInOut)
                .setSecondaryText(exZoomInOut)
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                            scanResultDialog.setVisibility(View.VISIBLE);
                            showScanResultDialog();
                        }
                    }
                })
                .show();
    }

    private void showScanResultDialog() {
        editText = findViewById(R.id.edit_text);
        builder.setTarget(R.id.scan_result_dialog)
                .setPrimaryText(scanResult)
                .setSecondaryText(exScanResult)
                .setPromptBackground(new RectanglePromptBackground())
                .setPromptFocal(new RectanglePromptFocal())
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                            editText.setFocusable(false);
                            editText.setClickable(false);
                            showConfirmButton();
                        }
                    }
                })
                .show();
    }

    private void showConfirmButton() {
        new MaterialTapTargetPrompt.Builder(this)
                .setTarget(R.id.ok_btn)
                .setPrimaryText(startConnect)
                .setSecondaryText(exStartConnect)
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                            reloadOnboarding();
                        }
                    }
                })
                .show();
    }

    private void reloadOnboarding() {
        builder.setTarget(R.id.reload_onboarding)
                .setPrimaryText(Reload)
                .setSecondaryText(exReload)
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                            checkBox.setVisibility(View.VISIBLE);
                            showCheckBox();
                        }
                    }
                })
                .show();
    }

    private void showCheckBox() {
        builder.setTarget(R.id.check_show)
                .setPrimaryText(notReload)
                .setSecondaryText(exNotreload)
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                            checkBox.setListener(new CircleCheckBox.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(boolean isChecked) {
                                    if(isChecked) {
                                        Intent intent = new Intent(ShowOnBoarding.this, Cam.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        }
                    }
                })
                .show();
    }
}

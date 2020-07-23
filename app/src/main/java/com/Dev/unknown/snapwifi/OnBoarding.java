package com.Dev.unknown.snapwifi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class OnBoarding extends AppCompatActivity {

    private MaterialTapTargetPrompt.Builder builder;
    LinearLayout scanResultDialog;
    EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding_layout);

        builder = new MaterialTapTargetPrompt.Builder(this);
        scanResultDialog = (LinearLayout) findViewById(R.id.scan_result_dialog);

        builder.setTarget(R.id.startButton)
                .setPrimaryText("start button")
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            showSeekBar();
                        }
                    }
                })
                .show();
    }

    private void showSeekBar() {
        builder.setTarget(R.id.seekBar1)
                .setPrimaryText("seekbar")
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            scanResultDialog.setVisibility(View.VISIBLE);
                            showScanResultDialog();
                        }
                    }
                })
                .show();
    }

    private void showScanResultDialog() {
        editText = findViewById(R.id.edit_text);
        builder.setTarget(R.id.edit_text)
                .setPrimaryText("scan result dialog")
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            editText.setFocusable(false);
                            editText.setClickable(false);
                            showConfirmButton();
                        }
                    }
                })
                .show();
    }

    private void showConfirmButton() {
        builder.setTarget(R.id.ok_btn)
                .setPrimaryText("confirm button")
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED) {
                            Intent intent = new Intent(OnBoarding.this, Cam.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .show();
    }
}

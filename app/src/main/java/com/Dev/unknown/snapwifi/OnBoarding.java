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
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

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
                .setPrimaryText("스캔 시작")
                .setSecondaryText("이 버튼을 누르면 스캔을 시작 및 정지를 할 수 있습니다. 앱 실행 시 자동으로 스캔을 시작합니다.")
                .setPromptBackground(new RectanglePromptBackground())
                .setPromptFocal(new RectanglePromptFocal())
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
                .setPrimaryText("카메라 줌 인/아웃")
                .setSecondaryText("카메라 화면을 확대 및 축소를 할 수 있습니다.")
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
        builder.setTarget(R.id.edit_text)
                .setPrimaryText("비밀번호 스캔 결과")
                .setSecondaryText("이곳에 WIFI 비밀번호가 스캔되서 나옵니다. 수정도 가능합니다.")
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
                .setPrimaryText("연결 시작")
                .setSecondaryText(" \"확인\" 버튼을 누르면 와이파이 자동 연결이 시작됩니다.")
                .setPromptStateChangeListener(new MaterialTapTargetPrompt.PromptStateChangeListener() {
                    @Override
                    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
                        if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED || state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED) {
                            Intent intent = new Intent(OnBoarding.this, Cam.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .show();
    }
}

package com.Dev.unknown.snapwifi;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CustomDialog extends AppCompatActivity {

    private String text;
    private EditText et;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        text = intent.getExtras().getString("password");
        callFunction();
    }

    public void callFunction() {
        final Dialog dig = new Dialog(CustomDialog.this);
        dig.setCancelable(false);
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dig.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dig.setContentView(R.layout.activity_custom_dialog);
        dig.show();

        et = (EditText)dig.findViewById(R.id.et);
        final Button PositiveButton = (Button)dig.findViewById(R.id.ok_btn);
        final Button NegativeButton = (Button)dig.findViewById(R.id.cancel_btn);

        et.setText(text);

        PositiveButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = et.getText().toString();
                Intent intent = new Intent(CustomDialog.this, Load.class);
                intent.putExtra("pass2", text);
                startActivity(intent);
            }
        });

        NegativeButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                finish();
            }
        });
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
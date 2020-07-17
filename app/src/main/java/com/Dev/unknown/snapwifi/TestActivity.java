package com.Dev.unknown.snapwifi;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * 0.5); // Display 사이즈의 90%
        int height = (int) (dm.heightPixels * 0.1); // Display 사이즈의 90%
        getWindow().getAttributes().width = width;
        getWindow().getAttributes().height = height;

        setContentView(R.layout.activity_test);

        Button btn = (Button) findViewById(R.id.btn);
        EditText editText = (EditText) findViewById(R.id.edit_test);
        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                Toast.makeText(getApplicationContext(), "click", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TestActivity.this, Cam.class);
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                intent.putExtra("text", text);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
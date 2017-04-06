package moe.democyann.pixivformuzeiplus.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import moe.democyann.pixivformuzeiplus.R;
import moe.democyann.pixivformuzeiplus.settings.Setting;

public class MainActivity extends AppCompatActivity {

    private Button open_btn;
    private Button setting_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        open_btn=(Button)findViewById(R.id.open_btn);
        setting_btn=(Button)findViewById(R.id.setting_btn);
        open_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager packageManager = MainActivity.this.getPackageManager();
                Intent intent;
                intent =packageManager.getLaunchIntentForPackage("net.nurik.roman.muzei");
                startActivity(intent);
            }
        });
        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, Setting.class);
                startActivity(intent);
            }
        });

    }
}

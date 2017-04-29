package moe.democyann.pixivformuzeiplus.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Locale;

import moe.democyann.pixivformuzeiplus.R;
import moe.democyann.pixivformuzeiplus.settings.Setting;

public class MainActivity extends AppCompatActivity {

    private Button open_btn;
    private Button setting_btn;
    private TextView tv_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        open_btn=(Button)findViewById(R.id.open_btn);
        setting_btn=(Button)findViewById(R.id.setting_btn);
        tv_1=(TextView)findViewById(R.id.tv_1);

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

        String [] arr = {"KP","PRK","408","KR","KOR","410","ko","kor"};
        String ct= Locale.getDefault().getCountry();
        String lg=Locale.getDefault().getLanguage();
        for(String te:arr){
            if(ct.equals(te) || lg.equals(te)){
                tv_1.setText("The app is not currently available in your country");
            }

        }
    }
}

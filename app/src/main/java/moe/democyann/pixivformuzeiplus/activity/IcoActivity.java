package moe.democyann.pixivformuzeiplus.activity;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import moe.democyann.pixivformuzeiplus.R;
import moe.democyann.pixivformuzeiplus.util.ConfigManger;

public class IcoActivity extends AppCompatActivity {

    private ConfigManger conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ico);
        conf=new ConfigManger(this);
        if(conf.get_icon()){
            PackageManager p = getPackageManager();
            ComponentName test = new ComponentName("moe.democyann.pixivformuzeiplus", "moe.democyann.pixivformuzeiplus.activity.MainActivity");
            p.setComponentEnabledSetting(test,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            Log.i("ICO", "onCreate: DISABLED");
            Toast.makeText(getApplicationContext(), "隐藏图标",
                    Toast.LENGTH_SHORT).show();
            conf.set_icon(false);
        }else {
            PackageManager p = getPackageManager();
            ComponentName test = new ComponentName("moe.democyann.pixivformuzeiplus", "moe.democyann.pixivformuzeiplus.activity.MainActivity");
            p.setComponentEnabledSetting(test,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            Log.i("ICO", "onCreate: ENABLED");
            Toast.makeText(getApplicationContext(), "显示图标",
                    Toast.LENGTH_SHORT).show();
            conf.set_icon(true);
        }

        finish();
    }
}

package moe.democyann.pixivformuzeiplus.settings;

import android.app.Activity;
import android.os.Bundle;

public class Setting extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,new SettingFragment()).commit();

    }
}

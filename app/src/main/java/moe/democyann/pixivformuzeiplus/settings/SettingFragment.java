package moe.democyann.pixivformuzeiplus.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import moe.democyann.pixivformuzeiplus.R;

public class SettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
    }
}

package com.pustakey.pustakey.ui.item.search.searchlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.pustakey.pustakey.Config;
import com.pustakey.pustakey.R;
import com.pustakey.pustakey.databinding.ActivitySettingBinding;
import com.pustakey.pustakey.ui.common.PSAppCompactActivity;
import com.pustakey.pustakey.utils.Constants;
import com.pustakey.pustakey.utils.MyContextWrapper;

public class SearchListActivity extends PSAppCompactActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingBinding activityFilteringBinding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

        initUI(activityFilteringBinding);

    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initUI(ActivitySettingBinding binding) {

        String title = getIntent().getStringExtra(Constants.ITEM_NAME);

        if (title != null) {
            initToolbar(binding.toolbar, title);
        } else {
            initToolbar(binding.toolbar, getString(R.string.item_list_title));
        }

        setupFragment(new SearchListFragment());

        // setup Fragment

        // Or you can call like this
        //setupFragment(new NewsListFragment(), R.id.content_frame);

    }

}

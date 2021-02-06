package com.yashbuysell.psbuyandsell.ui.courier;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.databinding.ActivityCourierTrackingBinding;
import com.yashbuysell.psbuyandsell.databinding.ActivityItemEntryBinding;
import com.yashbuysell.psbuyandsell.ui.common.PSAppCompactActivity;
import com.yashbuysell.psbuyandsell.ui.item.entry.ItemEntryFragment;
import com.yashbuysell.psbuyandsell.utils.Constants;
import com.yashbuysell.psbuyandsell.utils.MyContextWrapper;

public class courierTracking extends PSAppCompactActivity {


    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courier_tracking)  ;
        ActivityCourierTrackingBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_courier_tracking);

        // Init all UI
        initUI(binding);

    }

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    //endregion


    //region Private Methods

    private void initUI(ActivityCourierTrackingBinding binding) {

        // Toolbar
        initToolbar(binding.toolbar, getString(R.string.item_track_toolbar));

        // setup Fragment
        setupFragment(new courierTrackingFragment());
        // Or you can call like this
        //setupFragment(new NewsListFragment(), R.id.content_frame);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
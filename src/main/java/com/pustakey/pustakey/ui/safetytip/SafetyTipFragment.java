package com.pustakey.pustakey.ui.safetytip;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.pustakey.pustakey.MainActivity;
import com.pustakey.pustakey.R;
import com.pustakey.pustakey.binding.FragmentDataBindingComponent;
import com.pustakey.pustakey.databinding.FragmentSafetyTipBinding;
import com.pustakey.pustakey.ui.common.PSFragment;
import com.pustakey.pustakey.utils.AutoClearedValue;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewmodel.aboutus.AboutUsViewModel;
import com.pustakey.pustakey.viewobject.AboutUs;

/**
 * A simple {@link Fragment} subclass.
 */
public class SafetyTipFragment extends PSFragment {

    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private AboutUsViewModel aboutUsViewModel;


    @VisibleForTesting
    private AutoClearedValue<FragmentSafetyTipBinding> binding;



    //endregion


    //region Override Methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FragmentSafetyTipBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_safety_tip, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        return binding.get().getRoot();
    }

    @Override
    protected void initUIAndActions() {

        if(getActivity() instanceof MainActivity)  {
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity)getActivity()).updateToolbarIconColor(Color.WHITE);
            ((MainActivity)getActivity()).updateMenuIconWhite();
        }
    }

    @Override
    protected void initViewModels() {
        aboutUsViewModel = new ViewModelProvider(this, viewModelFactory).get(AboutUsViewModel.class);
    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {
        aboutUsViewModel.setAboutUsObj("about us");
        aboutUsViewModel.getAboutUsData().observe(this, resource -> {

            if (resource != null) {

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        if (resource.data != null) {

                            fadeIn(binding.get().getRoot());

                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (resource.data != null) {

                            setAboutUsData(resource.data);
                        }

                        break;
                    case ERROR:
                        // Error State

                        break;
                    default:
                        // Default

                        break;
                }

            } else {

                // Init Object or Empty Data
                Utils.psLog("Empty Data");

            }


            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (resource != null) {
                Utils.psLog("Got Data Of About Us.");


            } else {
                //noinspection Constant Conditions
                Utils.psLog("No Data of About Us.");
            }
        });
    }


    private void setAboutUsData(AboutUs aboutUs) {
        binding.get().setAboutUs(aboutUs);
        binding.get().safetyTipTextView.setText(aboutUs.safetyTips);

    }
    //endregion

}



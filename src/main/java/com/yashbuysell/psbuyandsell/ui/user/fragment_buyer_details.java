package com.yashbuysell.psbuyandsell.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.binding.FragmentDataBindingComponent;
import com.yashbuysell.psbuyandsell.databinding.FragmentUserForgotPasswordBinding;
import com.yashbuysell.psbuyandsell.ui.common.PSFragment;
import com.yashbuysell.psbuyandsell.utils.AutoClearedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

public class fragment_buyer_details extends PSFragment {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    private AutoClearedValue<FragmentUserForgotPasswordBinding> binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    protected void initUIAndActions() {
    }

    @Override
    protected void initViewModels() {

    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

    }
}

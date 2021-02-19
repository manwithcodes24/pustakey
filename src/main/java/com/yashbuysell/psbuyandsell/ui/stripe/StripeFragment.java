package com.yashbuysell.psbuyandsell.ui.stripe;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.binding.FragmentDataBindingComponent;
import com.yashbuysell.psbuyandsell.databinding.FragmentStripeBinding;
import com.yashbuysell.psbuyandsell.ui.common.PSFragment;
import com.yashbuysell.psbuyandsell.utils.AutoClearedValue;
import com.yashbuysell.psbuyandsell.utils.Constants;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.stripe.android.Stripe;

import com.stripe.android.model.Card;
import com.stripe.android.model.Token;


/**
 * A simple {@link Fragment} subclass.
 */
public class StripeFragment extends PSFragment {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private Card card;
    private ProgressDialog progressDialog;
//    private PSAPPLoadingViewModel psappLoadingViewModel;
    private String stripePublishableKey;
    @VisibleForTesting
    private AutoClearedValue<Stripe> stripe;
    private AutoClearedValue<FragmentStripeBinding> binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        FragmentStripeBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_stripe, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        return binding.get().getRoot();

    }

    @Override
    protected void initUIAndActions() {

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Getting Token");
        progressDialog.setCancelable(false);

        binding.get().submitStripeButton.setOnClickListener(v -> {

            card = binding.get().cardInputWidget.getCard();

            if (card != null) {
                createTransaction();
            }
        });

    }

    @Override
    protected void initViewModels() {
//        psappLoadingViewModel = new ViewModelProvider(this,viewModelFactory).get(PSAPPLoadingViewModel.class);
    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

        getIntentData();

        Utils.psLog("Stripe Publishable Key ::::: " + stripePublishableKey);
        stripe = new AutoClearedValue<>(this, new Stripe(getContext(), stripePublishableKey));

    }

    private void createTransaction() {
        progressDialog.show();
        if(getContext() != null) {



        }
    }

    public void close(String token) {

        if(getActivity() != null) {
            navigationController.navigateBackToCheckoutFragment(getActivity(), token);

            getActivity().finish();
        }
    }

    private void getIntentData(){
        try {
            if(getActivity() != null){
                if(getActivity().getIntent().getExtras() != null){
                    stripePublishableKey = getActivity().getIntent().getExtras().getString(Constants.STRIPEPUBLISHABLEKEY);
                    Utils.psLog(stripePublishableKey);
                }
            }
        }catch (Exception e){
            Utils.psErrorLog("",e);
        }
    }
}

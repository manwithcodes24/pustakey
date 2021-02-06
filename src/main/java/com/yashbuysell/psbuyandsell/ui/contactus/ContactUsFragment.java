package com.yashbuysell.psbuyandsell.ui.contactus;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.MainActivity;
import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.binding.FragmentDataBindingComponent;
import com.yashbuysell.psbuyandsell.databinding.FragmentContactUsBinding;
import com.yashbuysell.psbuyandsell.ui.common.PSFragment;
import com.yashbuysell.psbuyandsell.utils.AutoClearedValue;
import com.yashbuysell.psbuyandsell.utils.PSDialogMsg;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewmodel.contactus.ContactUsViewModel;
import com.yashbuysell.psbuyandsell.viewobject.common.Status;


public class ContactUsFragment extends PSFragment {
    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ContactUsViewModel contactUsViewModel;
    private PSDialogMsg psDialogMsg;

    @VisibleForTesting
    private AutoClearedValue<FragmentContactUsBinding> binding;

    private AutoClearedValue<ProgressDialog> prgDialog;

    //endregion


    //region Override Methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FragmentContactUsBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_contact_us, container, false, dataBindingComponent);

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
        psDialogMsg = new PSDialogMsg(getActivity(), false);

        psDialogMsg = new PSDialogMsg(this.getActivity(), false);

        if (Config.SHOW_ADMOB && connectivity.isConnected()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            binding.get().adView.loadAd(adRequest);
        } else {
            binding.get().adView.setVisibility(View.GONE);
        }

        // Init Dialog
        prgDialog = new AutoClearedValue<>(this, new ProgressDialog(getActivity()));
        prgDialog.get().setMessage((Utils.getSpannableString(getContext(), getString(R.string.message__please_wait), Utils.Fonts.MM_FONT)));
        prgDialog.get().setCancelable(false);

        //fadeIn Animation
        fadeIn(binding.get().getRoot());

        binding.get().btnSubmit.setOnClickListener(view -> {

            if (connectivity.isConnected()) {
                String contactName = binding.get().contactNameTextInput.getText().toString().trim();
                String contactEmail = binding.get().contactEmailTextInput.getText().toString().trim();
                String contactDesc = binding.get().contactDescEditText.getText().toString().trim();
                String contactPhone = binding.get().contactPhoneTextInput.getText().toString().trim();

                if (contactName.equals("")) {

                    psDialogMsg.showWarningDialog(getString(R.string.error_message__blank_name), getString(R.string.app__ok));

                    psDialogMsg.show();

                    return;
                }

                if (contactEmail.equals("")) {

                    psDialogMsg.showWarningDialog(getString(R.string.error_message__blank_email), getString(R.string.app__ok));
                    psDialogMsg.show();
                    return;
                }

                if (contactDesc.equals("")) {

                    psDialogMsg.showWarningDialog(getString(R.string.error_message__blank_contact_message), getString(R.string.app__ok));
                    psDialogMsg.show();
                    return;
                }


                if (!contactUsViewModel.isLoading) {
                    contactUsViewModel.contactName = contactName;
                    contactUsViewModel.contactEmail = contactEmail;
                    contactUsViewModel.contactDesc = contactDesc;
                    contactUsViewModel.contactPhone = contactPhone;
                    doSubmit();
                }

            } else {

                psDialogMsg.showWarningDialog(getString(R.string.no_internet_error), getString(R.string.app__ok));
                psDialogMsg.show();
            }
        });
    }

    @Override
    protected void initViewModels() {
        contactUsViewModel = new ViewModelProvider(this, viewModelFactory).get(ContactUsViewModel.class);
    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

    }

    //endregion

    //region Methods

    private void doSubmit() {

        prgDialog.get().show();

        contactUsViewModel.postContactUs(Config.API_KEY, contactUsViewModel.contactName, contactUsViewModel.contactEmail, contactUsViewModel.contactDesc, contactUsViewModel.contactPhone);

        contactUsViewModel.getPostContactUsData().observe(this, state -> {
            if (state != null && state.status == Status.SUCCESS) {
                //Success
                binding.get().contactNameTextInput.setText("");
                binding.get().contactEmailTextInput.setText("");
                binding.get().contactPhoneTextInput.setText("");
                binding.get().contactDescEditText.setText("");

                psDialogMsg.showSuccessDialog(getString(R.string.message_sent), getString(R.string.app__ok));
                psDialogMsg.show();

                contactUsViewModel.setLoadingState(false);
            }
        });

        contactUsViewModel.getLoadingState().observe(this, state -> {
            if (state != null && state) {
                prgDialog.get().show();
            } else {
                prgDialog.get().cancel();
            }
        });

    }

    //endregion

}

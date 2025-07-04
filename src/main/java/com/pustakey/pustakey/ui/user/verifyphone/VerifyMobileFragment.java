package com.pustakey.pustakey.ui.user.verifyphone;


import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.pustakey.pustakey.MainActivity;
import com.pustakey.pustakey.R;
import com.pustakey.pustakey.binding.FragmentDataBindingComponent;
import com.pustakey.pustakey.databinding.FragmentVerifyMobileBinding;
import com.pustakey.pustakey.ui.common.PSFragment;
import com.pustakey.pustakey.utils.AutoClearedValue;
import com.pustakey.pustakey.utils.Constants;
import com.pustakey.pustakey.utils.PSDialogMsg;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewmodel.user.UserViewModel;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyMobileFragment extends PSFragment {


    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private UserViewModel userViewModel;

    private PSDialogMsg psDialogMsg;

    private String userPhoneNo, userName, phoneUserId, token;
    private FirebaseAuth mAuth;
    private String mVerificationId;

    @VisibleForTesting
    private AutoClearedValue<FragmentVerifyMobileBinding> binding;

    private AutoClearedValue<ProgressDialog> prgDialog;

    //endregion


    //region Override Methods

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        FragmentVerifyMobileBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_verify_mobile, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        if (getActivity() != null && getActivity().getIntent() != null) {
            userPhoneNo = getActivity().getIntent() .getStringExtra(Constants.USER_PHONE);
            userName = getActivity().getIntent() .getStringExtra(Constants.USER_NAME);

            if (getArguments() != null) {
                userPhoneNo = getArguments().getString(Constants.USER_PHONE);
                userName = getArguments().getString(Constants.USER_NAME);
            }

        }
        Log.d("userPhone" , userPhoneNo) ;
        sendVerificationCode(userPhoneNo);



        return binding.get().getRoot();
    }

    private void sendVerificationCode(String no) {
        no = "+91" + no;
        Log.d("phoneMsg" , String.valueOf(no.length())) ;
        mAuth = FirebaseAuth.getInstance();
        try {
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                                            .setPhoneNumber(no)       // Phone number to verify
                                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                            .setActivity(getActivity())                 // Activity (for callback binding)
                                                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                                .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }
        catch (Exception e){
            Log.d("userPhoneError" , e.toString()) ;
        }

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                if (binding.get().enterCodeEditText != null) {
                    binding.get().enterCodeEditText.setText(code);
                }
                //verifying the code

                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
        }


    };


    @Override
    protected void initUIAndActions() {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbarText(((MainActivity) getActivity()).binding.toolbar, getString(R.string.verify_phone__title));
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);

        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        psDialogMsg = new PSDialogMsg(getActivity(), false);

        prgDialog = new AutoClearedValue<>(this, new ProgressDialog(getActivity()));
        prgDialog.get().setMessage((Utils.getSpannableString(getContext(), getString(R.string.message__please_wait), Utils.Fonts.MM_FONT)));
        prgDialog.get().setCancelable(false);

        binding.get().phoneTextView.setText(userPhoneNo);

        binding.get().submitButton.setOnClickListener(v -> {

            String code = binding.get().enterCodeEditText.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                binding.get().enterCodeEditText.setError("Enter valid code");
                binding.get().enterCodeEditText.requestFocus();
                return;
            }

//                verifying the code entered manually
            verifyVerificationCode(code);

        });
    }

    private void verifyVerificationCode(String code) {
        //creating the credential

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //signing the user
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        if (getActivity() != null) {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(getActivity(), task -> {
                        if (task.isSuccessful()) {
                            try {
                                //verification successful we will start the profile activity
                                if (task.getResult() != null && task.getResult().getUser() != null) {
                                    phoneUserId = task.getResult().getUser().getUid();
                                    String userId  = phoneUserId ;


//                                    FirebaseMessaging.getInstance().getToken()
//                                            .addOnCompleteListener(new OnCompleteListener<String>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<String> task) {
//                                                    if (!task.isSuccessful()) {
//                                                        Log.w("tokenFailed", "Fetching FCM registration token failed", task.getException());
//                                                        return;
//                                                    }
//
//                                                    // Get new FCM registration token
//                                                    String usertoken = task.getResult();
//
//                                                    // Log and toast
//                                                    String msg = "userToken = " + usertoken;
//                                                    Log.d("userToken", msg);
//                                                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
//                                                    DatabaseReference users = mDatabase.child(userId) ;
//                                                    users.child("fcmtoken").setValue(usertoken) ;
//
//
//
//                                                }
//                                            });
//

                                    userViewModel.setPhoneLoginUser(phoneUserId, userName, userPhoneNo, token);
                                }
                            } catch (Exception e1) {
                                // Error occurred while creating the File
                                Toast.makeText(getContext(),e1.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }

                    });
        }
    }

    @Override
    protected void initViewModels() {
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {
        token = pref.getString(Constants.NOTI_TOKEN, Constants.USER_NO_DEVICE_TOKEN);

        userViewModel.getPhoneLoginData().observe(this, listResource -> {

            if (listResource != null) {

                Utils.psLog("Got Data" + listResource.message + listResource.toString());

                switch (listResource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        prgDialog.get().show();

                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (listResource.data != null) {
                            try {

                                Utils.updateUserLoginData(pref, listResource.data.user);
                                Utils.navigateAfterUserLogin(getActivity(),navigationController);

                            } catch (NullPointerException ne) {
                                Utils.psErrorLog("Null Pointer Exception.", ne);
                            } catch (Exception e) {
                                Utils.psErrorLog("Error in getting notification flag data.", e);
                            }

                            userViewModel.isLoading = false;
                            prgDialog.get().cancel();

                        }

                        break;
                    case ERROR:
                        // Error State

                        userViewModel.isLoading = false;
                        prgDialog.get().cancel();

                        psDialogMsg.showErrorDialog(listResource.message, getString(R.string.app__ok));
                        psDialogMsg.show();

                        break;
                    default:
                        // Default

                        break;
                }

            } else {

                // Init Object or Empty Data
                Utils.psLog("Empty Data");

            }

        });

    }
}

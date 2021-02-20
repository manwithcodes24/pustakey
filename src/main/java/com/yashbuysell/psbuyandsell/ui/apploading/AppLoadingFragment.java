package com.yashbuysell.psbuyandsell.ui.apploading;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.login.LoginManager;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.binding.FragmentDataBindingComponent;
import com.yashbuysell.psbuyandsell.databinding.FragmentAppLoadingBinding;
import com.yashbuysell.psbuyandsell.ui.common.PSFragment;
import com.yashbuysell.psbuyandsell.ui.item.itemlocation.ItemLocationFragment;
import com.yashbuysell.psbuyandsell.utils.AutoClearedValue;
import com.yashbuysell.psbuyandsell.utils.Constants;
import com.yashbuysell.psbuyandsell.utils.PSDialogMsg;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewmodel.apploading.PSAPPLoadingViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.clearalldata.ClearAllDataViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.user.UserViewModel;
import com.yashbuysell.psbuyandsell.viewobject.PSAppInfo;
import com.yashbuysell.psbuyandsell.viewobject.UserLogin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.facebook.FacebookSdk.getApplicationContext;


public class AppLoadingFragment extends PSFragment implements LocationListener {


    //region Variables

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private PSDialogMsg psDialogMsg;
    private String startDate = Constants.ZERO;
    private String endDate = Constants.ZERO;

    private PSAPPLoadingViewModel appLoadingViewModel;
    private ClearAllDataViewModel clearAllDataViewModel;
    private UserViewModel userViewModel;

    //endregion Variables
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    Geocoder geocoder;
    List<Address> addresses;
    protected LocationManager locationManager;
    protected boolean isGPSEnabled, isNetworkEnabled;

    protected LocationListener locationListener;
    private boolean locationFetched = false;
    private boolean gotMyLocation = true;
    private boolean isReturned = false;
    private boolean isBatterySaver ;

    private String city;
    private String postalcode;
    private String lat;
    private String lan;
    Location myLocation ;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentAppLoadingBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_loading, container, false, dataBindingComponent);
        AutoClearedValue<FragmentAppLoadingBinding> binding = new AutoClearedValue<>(this, dataBinding);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
//        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

    //        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//
//        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
////            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 500, this);
//
//        }




        return binding.get().getRoot();
    }



    @Override
    protected void initUIAndActions() {

        psDialogMsg = new PSDialogMsg(getActivity(), false);

//        if (force_update) {
//            navigationController.navigateToForceUpdateActivity(this.getActivity(), force_update_title, force_update_msg);
//        }
    }

    @Override
    protected void initViewModels() {
        appLoadingViewModel = new ViewModelProvider(this, viewModelFactory).get(PSAPPLoadingViewModel.class);
        clearAllDataViewModel = new ViewModelProvider(this, viewModelFactory).get(ClearAllDataViewModel.class);
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
    }

    @Override
    protected void initAdapters() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION :{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if(isNetworkEnabled) {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 500, this);
                            myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }
                        if(isGPSEnabled){
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 500, this);
                            myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            while (gotMyLocation){
                                if (myLocation == null){

                                        myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                }
                                else {
                                    gotMyLocation = false;
                                }
                            }
                        }
                        while (gotMyLocation){
                            if (myLocation == null){

                                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            }
                            else {
                                gotMyLocation = false;

                            }
                        }




                        if(myLocation != null) {

                            try {
                                this.lat = String.valueOf(myLocation.getLatitude());
                                this.lan  = String.valueOf(myLocation.getLongitude()) ;


//                addresses = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                this.city = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1).get(0).getLocality();
                                this.postalcode = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1).get(0).getPostalCode();
                                selected_location_id = "itm_loca" + postalcode ;

                                selectedLat = lat ;
                                selectedLng = lan ;

                                selected_location_name = city ;


                                if(city != "" && city != null){
                                    navigationController.navigateToMainActivity(getActivity(), selected_location_id,city, lat, lan);

                                }
                            }
                            catch (Exception e){
                                Log.d("locationError" , " Error in fetching location" + e.toString()) ;
                            }
                            locationFetched = true ;

                        }

                    }
                }
            }


        }

        }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),1 );


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        getActivity().finish();
                        System.exit(0);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   initData();
                }
            }, 1000);





        if (resultCode == 1) {
            switch (requestCode) {
                case 1:



                    break;
            }
        }

    }


    @Override
    protected void initData() {
        isBatterySaver = false ;
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ;

        if(!isBatterySaver) {
            try {

            if(getLocationMode(getApplicationContext()) == 2){
                isBatterySaver = true ;
            }

            } catch (Exception e) {


            }
        }





              if(!isGPSEnabled && !isBatterySaver) {
                  buildAlertMessageNoGps() ;

              }
              else {



//        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                  if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                      requestPermissions(
                              new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                      android.Manifest.permission.ACCESS_FINE_LOCATION},
                              MY_PERMISSIONS_REQUEST_LOCATION);
                  }

                  if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                      if(isNetworkEnabled || isBatterySaver) {
                          locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 500, this);
                          myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                      }
                      if(isGPSEnabled){
                          locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 500, this);
                          myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                          while (gotMyLocation){
                              if (myLocation == null){

                                  myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                              }
                              else {
                                  gotMyLocation = false;
                              }
                          }
                      }

                      while (gotMyLocation){
                          if (myLocation == null){

                              myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                          }
                          else {
                              gotMyLocation = false;

                          }
                      }


                      if (myLocation != null) {
                          try {
                              this.lat = String.valueOf(myLocation.getLatitude());
                              this.lan = String.valueOf(myLocation.getLongitude());


//                addresses = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                              this.city = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1).get(0).getLocality();
                              this.postalcode = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1).get(0).getPostalCode();
                              selected_location_id = "itm_loca" + postalcode ;
                              selectedLat = lat ;

                              selectedLng = lan ;
                              selected_location_name = city ;
                              if(city != "" && city != null){

                                  navigationController.navigateToMainActivity(getActivity(), selected_location_id, city, lat, lan);

                              }
                          } catch (Exception e) {
                              Log.d("locationError", "Error in fetching location" + e.toString());
                          }
                          locationFetched = true;

                      }

                      if (connectivity.isConnected()) {
                          if (startDate.equals(Constants.ZERO)) {

                              startDate = getDateTime();
                              Utils.setDatesToShared(startDate, endDate, pref);
                          }

                          endDate = getDateTime();
                          appLoadingViewModel.setDeleteHistoryObj(startDate, endDate, loginUserId);

                      } else {
                          if (!selected_location_id.isEmpty()) {
                              navigationController.navigateToMainActivity(getActivity(), selected_location_id, selected_location_name, selectedLat, selectedLng);

                          } else {
                          }
//
//                    try {
//                        Thread.sleep(1200);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                          if (getActivity() != null) {
                              getActivity().finish();
                          }
                      }

                      appLoadingViewModel.getDeleteHistoryData().observe(this, result -> {

                          if (result != null) {
                              switch (result.status) {

                                  case SUCCESS:

                                      if (result.data != null) {

                                          switch (result.data.userInfo.userStatus) {
                                              case Constants.USER_STATUS__DELECTED:
                                                  AppLoadingFragment.this.logout();
                                                  showErrorDialog(result.data, getString(R.string.error_message__user_deleted));
                                                  break;
                                              case Constants.USER_STATUS__BANNED:
                                                  AppLoadingFragment.this.logout();
                                                  showErrorDialog(result.data, getString(R.string.error_message__user_banned));
                                                  break;
                                              case Constants.USER_STATUS__UNPUBLISHED:
                                                  AppLoadingFragment.this.logout();
                                                  showErrorDialog(result.data, getString(R.string.error_message__user_unpublished));
                                                  break;
                                              default:
                                                  //default
                                                  appLoadingViewModel.psAppInfo = result.data;
                                                  checkVersionNumber(result.data);
                                                  startDate = endDate;
                                                  break;
                                          }

                                      }
                                      break;

                                  case ERROR:

                                      break;
                              }
                          }

                      });


                      clearAllDataViewModel.getDeleteAllDataData().observe(this, result -> {

                          if (result != null) {
                              switch (result.status) {

                                  case ERROR:

                                      break;

                                  case SUCCESS:

                                      checkForceUpdate(appLoadingViewModel.psAppInfo);
                                      break;
                              }
                          }
                      });

                      userViewModel.getLoginUser().observe(this, new Observer<List<UserLogin>>() {
                          @Override
                          public void onChanged(List<UserLogin> data) {
                              if (data != null) {
                                  if (data.size() > 0) {
                                      userViewModel.user = data.get(0).user;
                                  }
                              }
                          }
                      });
                  }
              }
      }

    private int getLocationMode(Context context) throws Settings.SettingNotFoundException {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

    }
    public void showErrorDialog(PSAppInfo psAppInfo, String message){
        psDialogMsg.showErrorDialog(message, getString(R.string.app__ok));
        psDialogMsg.show();

        psDialogMsg.okButton.setOnClickListener(view -> {
            psDialogMsg.cancel();
            appLoadingViewModel.psAppInfo = psAppInfo;
            checkVersionNumber(psAppInfo);
            startDate = endDate;
        });

    }

    private void logout() {
        userViewModel.deleteUserLogin(userViewModel.user).observe(this, status -> {
            if (status != null) {

                LoginManager.getInstance().logOut();
            }
        });
    }

    private void checkForceUpdate(PSAppInfo psAppInfo) {
        if (psAppInfo.psAppVersion.versionForceUpdate.equals(Constants.ONE)) {

            pref.edit().putString(Constants.APPINFO_PREF_VERSION_NO, psAppInfo.psAppVersion.versionNo).apply();
            pref.edit().putBoolean(Constants.APPINFO_PREF_FORCE_UPDATE, true).apply();
            pref.edit().putString(Constants.APPINFO_FORCE_UPDATE_TITLE, psAppInfo.psAppVersion.versionTitle).apply();
            pref.edit().putString(Constants.APPINFO_FORCE_UPDATE_MSG, psAppInfo.psAppVersion.versionMessage).apply();

            navigationController.navigateToForceUpdateActivity(this.getActivity(), psAppInfo.psAppVersion.versionTitle, psAppInfo.psAppVersion.versionMessage);
            if (getActivity() != null) {
                getActivity().finish();
            }
        } else if (psAppInfo.psAppVersion.versionForceUpdate.equals(Constants.ZERO)) {

            pref.edit().putBoolean(Constants.APPINFO_PREF_FORCE_UPDATE, false).apply();

            psDialogMsg.showAppInfoDialog(getString(R.string.update), getString(R.string.app__cancel), psAppInfo.psAppVersion.versionTitle, psAppInfo.psAppVersion.versionMessage);
            ShowDialog();
        }
    }

    private void checkVersionNumber(PSAppInfo psAppInfo) {
        if (!Config.APP_VERSION.equals(psAppInfo.psAppVersion.versionNo)) {

            if (psAppInfo.psAppVersion.versionNeedClearData.equals(Constants.ONE)) {
                psDialogMsg.cancel();
                clearAllDataViewModel.setDeleteAllDataObj();
            } else {
                checkForceUpdate(appLoadingViewModel.psAppInfo);
            }

        } else {
            pref.edit().putBoolean(Constants.APPINFO_PREF_FORCE_UPDATE, false).apply();
            if (!selected_location_id.isEmpty()) {
                navigationController.navigateToMainActivity(getActivity(), selected_location_id, selected_location_name, selectedLat, selectedLng);
            } else {
//                navigationController.navigateToLocationActivity(getActivity(), Constants.LOCATION_NOT_CLEAR_ICON, Constants.EMPTY_STRING);
            }
            if (getActivity() != null) {
                getActivity().finish();
            }
        }

    }

    private void ShowDialog() {
        psDialogMsg.show();

        psDialogMsg.okButton.setOnClickListener(v -> {
            psDialogMsg.cancel();

            if (!selected_location_id.isEmpty()) {
                navigationController.navigateToMainActivity(getActivity(), selected_location_id, selected_location_name, selectedLat, selectedLng);
            } else {
//                navigationController.navigateToLocationActivity(getActivity(), Constants.LOCATION_NOT_CLEAR_ICON, Constants.EMPTY_STRING);
            }


            if (getActivity() != null) {
                navigationController.navigateToPlayStore(AppLoadingFragment.this.getActivity());

                getActivity().finish();
            }

        });

        psDialogMsg.cancelButton.setOnClickListener(v -> {
            psDialogMsg.cancel();
            if (!selected_location_id.isEmpty()) {
                navigationController.navigateToMainActivity(getActivity(), selected_location_id, selected_location_name, selectedLat, selectedLng);
            } else {
//                navigationController.navigateToLocationActivity(getActivity(), Constants.LOCATION_NOT_CLEAR_ICON, Constants.EMPTY_STRING);
            }
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        psDialogMsg.getDialog().setOnCancelListener(dialog -> {
            if (!selected_location_id.isEmpty()) {
                navigationController.navigateToMainActivity(getActivity(), selected_location_id, selected_location_name, selectedLat, selectedLng);
            } else {
//                navigationController.navigateToLocationActivity(getActivity(), Constants.LOCATION_NOT_CLEAR_ICON, Constants.EMPTY_STRING);
            }
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }


    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
        Date date = new Date();
        return dateFormat.format(date);
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}

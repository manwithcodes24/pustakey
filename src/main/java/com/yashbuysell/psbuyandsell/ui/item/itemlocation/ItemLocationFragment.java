package com.yashbuysell.psbuyandsell.ui.item.itemlocation;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.binding.FragmentDataBindingComponent;
import com.yashbuysell.psbuyandsell.databinding.FragmentItemLocationBinding;
import com.yashbuysell.psbuyandsell.ui.common.DataBoundListAdapter;
import com.yashbuysell.psbuyandsell.ui.common.PSFragment;
import com.yashbuysell.psbuyandsell.utils.AutoClearedValue;
import com.yashbuysell.psbuyandsell.utils.Constants;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewmodel.itemlocation.ItemLocationViewModel;
import com.yashbuysell.psbuyandsell.viewobject.ItemLocation;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;
import com.yashbuysell.psbuyandsell.viewobject.common.Status;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static androidx.core.content.ContextCompat.getSystemService;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemLocationFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface , LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    Geocoder geocoder;
    List<Address> addresses;
    private ItemLocationViewModel itemLocationViewModel;
    public String locationId;
    private boolean searchKeywordOnFocus = false;
    // public ItemClickCallback callback;

    @VisibleForTesting
    private AutoClearedValue<FragmentItemLocationBinding> binding;
    private AutoClearedValue<ItemLocationAdapter> adapter;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private boolean locationFetched = false;
    private String city ;
    private String lat ;
    private String lan ;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentItemLocationBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_location, container, false, dataBindingComponent);
        geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
        binding = new AutoClearedValue<>(this, dataBinding);
//
//        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
//
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) getActivity().getApplicationContext());
//            Location myLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//            this.lat = String.valueOf(myLocation.getLatitude());
//            this.lan  = String.valueOf(myLocation.getLongitude()) ;
//            try {
//
//                addresses = geocoder.getFromLocation(myLocation.getLatitude(), myLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//
//                this.city = addresses.get(0).getLocality();
//                Log.d("locationCity" , city) ;
//                navigationController.navigateToMainActivity(ItemLocationFragment.this.getActivity(), "itm_loca" + UUID.randomUUID().toString(),city, lat, lan);
//            }
//            catch (Exception e){
//                Log.d("locationError" , " Error in fetching location") ;
//            }
//            locationFetched = true ;
//
//        }
//        else{
//            ActivityCompat.requestPermissions((Activity) getActivity(),
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_LOCATION);
//        }

        if (getActivity() != null) {

            Intent intent = getActivity().getIntent();
            this.locationId = intent.getStringExtra(Constants.ITEM_LOCATION_TYPE_ID);

        }

        return binding.get().getRoot();
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

    @Override
    protected void initUIAndActions() {

        if(getActivity() != null){
            if (getActivity().getIntent().getStringExtra(Constants.LOCATION_FLAG).equals(Constants.LOCATION_NOT_CLEAR_ICON) || getActivity().getIntent().getStringExtra(Constants.LOCATION_FLAG).equals(Constants.SELECT_LOCATION_FROM_HOME)) {
                setHasOptionsMenu(false);
                binding.get().selectTitleConstraintLayout.setVisibility(View.VISIBLE);
            } else {

                setHasOptionsMenu(true);
                binding.get().selectTitleConstraintLayout.setVisibility(View.GONE);

            }
        }

        binding.get().locationRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {

                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();

                    if (lastPosition == adapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !itemLocationViewModel.forceEndLoading) {

                            itemLocationViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                            int limit = Config.LIST_LOCATION_COUNT;

                            itemLocationViewModel.offset = itemLocationViewModel.offset + limit;

                            itemLocationViewModel.setNextPageLoadingStateObj( String.valueOf(limit), String.valueOf(itemLocationViewModel.offset));

                            itemLocationViewModel.setLoadingState(true);
                        }
                    }
                }
            }
        });

        binding.get().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.view__primary_line));
        binding.get().swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.global__primary));
        binding.get().swipeRefresh.setOnRefreshListener(() -> {

            itemLocationViewModel.loadingDirection = Utils.LoadingDirection.top;

            // reset productViewModel.offset
            itemLocationViewModel.offset = 0;

            // reset productViewModel.forceEndLoading
            itemLocationViewModel.forceEndLoading = false;

            // update live data
            itemLocationViewModel.setItemLocationListObj(String.valueOf(Config.LIST_LOCATION_COUNT),
                    String.valueOf(itemLocationViewModel.offset),loginUserId,binding.get().itemLocationSearchEditText.getText().toString(),
                    itemLocationViewModel.orderBy,itemLocationViewModel.orderType);

        });

        binding.get().itemLocationSearchEditText.setOnFocusChangeListener((v, hasFocus) -> {

            searchKeywordOnFocus = hasFocus;
            Utils.psLog("Focus " + hasFocus);
        });
        binding.get().itemLocationSearchEditText.setOnKeyListener((v, keyCode, event) -> {

            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                itemLocationViewModel.keyword = binding.get().itemLocationSearchEditText.getText().toString();

                binding.get().itemLocationSearchEditText.clearFocus();
                searchKeywordOnFocus = false;

                loadLocationList();

                Utils.psLog("Down");

                return false;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {

                Utils.psLog("Up");
            }
            return false;
        });

        binding.get().itemLocationSearchButton.setOnClickListener(view -> {

            itemLocationViewModel.keyword = binding.get().itemLocationSearchEditText.getText().toString();

            binding.get().itemLocationSearchEditText.clearFocus();
            searchKeywordOnFocus = false;

            loadLocationList();
        });

        binding.get().itemLocationCardViewFilter.setOnClickListener(v-> navigationController.navigateToItemLocationFilterActivity(getActivity(),
                binding.get().itemLocationSearchEditText.getText().toString(),
                itemLocationViewModel.orderType,itemLocationViewModel.orderBy));

}

    private void loadLocationList() {

        itemLocationViewModel.loadingDirection = Utils.LoadingDirection.top;

        // reset productViewModel.offset
        itemLocationViewModel.offset = 0;

        // reset productViewModel.forceEndLoading
        itemLocationViewModel.forceEndLoading = false;

        // update live data
        itemLocationViewModel.setItemLocationListObj(String.valueOf(Config.LIST_LOCATION_COUNT),
                String.valueOf(itemLocationViewModel.offset),
                loginUserId,
                itemLocationViewModel.keyword,
                itemLocationViewModel.orderBy,
                itemLocationViewModel.orderType);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.clear_button, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.clear) {
            this.locationId = "";

            initAdapters();

            initData();

            if(this.getActivity() != null)
            navigationController.navigateBackToItemLocationFragment(this.getActivity(), this.locationId, "", "", "");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initViewModels() {

        itemLocationViewModel = new ViewModelProvider(this, viewModelFactory).get(ItemLocationViewModel.class);
    }

    @Override
    protected void initAdapters() {

        ItemLocationAdapter nvadapter = new ItemLocationAdapter(dataBindingComponent,
                (itemLocation, id) -> {

                    if (getActivity() != null) {

                        if (getActivity().getIntent().getStringExtra(Constants.LOCATION_FLAG).equals(Constants.LOCATION_NOT_CLEAR_ICON)) {
                            navigationController.navigateToMainActivity(ItemLocationFragment.this.getActivity(), itemLocation.id, itemLocation.name, itemLocation.lat, itemLocation.lng);

                        } else if (getActivity().getIntent().getStringExtra(Constants.LOCATION_FLAG).equals(Constants.SELECT_LOCATION_FROM_HOME)) {
                            navigationController.navigateBackToMainActivity(ItemLocationFragment.this.getActivity(), itemLocation.id, itemLocation.name, itemLocation.lat, itemLocation.lng);

                        } else {
                            navigationController.navigateBackToItemLocationFragment(ItemLocationFragment.this.getActivity(), itemLocation.id, itemLocation.name, itemLocation.lat, itemLocation.lng);
                        }

                        ItemLocationFragment.this.getActivity().finish();

                    }
                }, this.locationId);
        this.adapter = new AutoClearedValue<>(this, nvadapter);
        binding.get().locationRecyclerView.setAdapter(nvadapter);

    }

    @Override
    protected void initData() {
        loadCategory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {

            if (requestCode == Constants.REQUEST_CODE__SEARCH_LOCATION_FILTER && resultCode == Constants.RESULT_CODE__SEARCH_LOCATION_FILTER) {
                itemLocationViewModel.keyword = data.getStringExtra(Constants.SEARCH_CITY_INTENT_KEYWORD);
                itemLocationViewModel.orderType = data.getStringExtra(Constants.SEARCH_CITY_INTENT_ORDER_TYPE);
                itemLocationViewModel.orderBy = data.getStringExtra(Constants.SEARCH_CITY_INTENT_ORDER_BY);

                binding.get().itemLocationSearchEditText.setText(itemLocationViewModel.keyword);

                // call set
                loadLocationList();


            }
        }
    }

    private void loadCategory() {

        // Load Category List
        itemLocationViewModel.categoryParameterHolder.cityId = selectedCityId;

        if (connectivity.isConnected()) {
            itemLocationViewModel.setItemLocationListObj(String.valueOf(Config.LIST_LOCATION_COUNT), String.valueOf(itemLocationViewModel.offset),loginUserId,binding.get().itemLocationSearchEditText.getText().toString(),itemLocationViewModel.orderBy,itemLocationViewModel.orderType);
        }

        LiveData<Resource<List<ItemLocation>>> news = itemLocationViewModel.getItemLocationListData();

        if (news != null) {

            news.observe(this, listResource -> {
                if (listResource != null) {

                    Utils.psLog("Got Data" + listResource.message + listResource.toString());

                    switch (listResource.status) {
                        case LOADING:
                            // Loading State
                            // Data are from Local DB

                            if (listResource.data != null) {
                                //fadeIn Animation
                                fadeIn(binding.get().getRoot());

                                // Update the data
                                replaceData(listResource.data);

                            }

                            break;

                        case SUCCESS:
                            // Success State
                            // Data are from Server

                            if (listResource.data != null) {
                                // Update the data
                                replaceData(listResource.data);
                            }

                            itemLocationViewModel.setLoadingState(false);

                            break;

                        case ERROR:
                            // Error State

                            itemLocationViewModel.setLoadingState(false);

                            break;
                        default:
                            // Default

                            break;
                    }

                } else {

                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");

                    if (itemLocationViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        itemLocationViewModel.forceEndLoading = true;
                    }

                }

            });
        }

        itemLocationViewModel.getNextPageLoadingStateData().observe(this, state -> {
            if (state != null) {
                if (state.status == Status.ERROR) {
                    Utils.psLog("Next Page State : " + state.data);

                    itemLocationViewModel.setLoadingState(false);
                    itemLocationViewModel.forceEndLoading = true;
                }
            }
        });

        itemLocationViewModel.getLoadingState().observe(this, loadingState -> {
            binding.get().setLoadingMore(itemLocationViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }
        });

    }

    private void replaceData(List<ItemLocation> categoryList) {

        adapter.get().replace(categoryList);
        binding.get().executePendingBindings();

    }

    @Override
    public void onDispatched() {
        if (itemLocationViewModel.loadingDirection == Utils.LoadingDirection.top) {
            binding.get();
            LinearLayoutManager layoutManager = (LinearLayoutManager)
                    binding.get().locationRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPosition(0);
            }
        }
    }
}
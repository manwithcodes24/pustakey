package com.pustakey.pustakey.ui.item.search.searchlist;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.pustakey.pustakey.Config;
import com.pustakey.pustakey.MainActivity;
import com.pustakey.pustakey.R;
import com.pustakey.pustakey.binding.FragmentDataBindingComponent;
import com.pustakey.pustakey.databinding.FragmentItemListBinding;
import com.pustakey.pustakey.ui.common.DataBoundListAdapter;
import com.pustakey.pustakey.ui.common.PSFragment;
import com.pustakey.pustakey.ui.item.adapter.ItemVerticalListAdapter;
import com.pustakey.pustakey.utils.AutoClearedValue;
import com.pustakey.pustakey.utils.Constants;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewmodel.item.ItemViewModel;
import com.pustakey.pustakey.viewobject.Item;
import com.pustakey.pustakey.viewobject.common.Resource;
import com.pustakey.pustakey.viewobject.common.Status;
import com.pustakey.pustakey.viewobject.holder.ItemParameterHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SearchListFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private boolean typeClicked = false;
    private boolean filterClicked = false;
    private boolean mapFilterClicked = false;
    private List<Item> clearRecyclerView = new ArrayList<>();
    private ItemViewModel itemViewModel;

    @VisibleForTesting
    private AutoClearedValue<FragmentItemListBinding> binding;
    private AutoClearedValue<ItemVerticalListAdapter> adapter;

    //region Override Methods
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FragmentItemListBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_list, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        if (Config.SHOW_ADMOB && connectivity.isConnected()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            binding.get().adView.loadAd(adRequest);
        } else {
            binding.get().adView.setVisibility(View.GONE);
        }
        return binding.get().getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == Constants.REQUEST_CODE__ITEM_LIST_FRAGMENT
                && resultCode == Constants.RESULT_CODE__CATEGORY_FILTER) {

            String catId = data.getStringExtra(Constants.CATEGORY_ID);
            if (catId != null) {
                itemViewModel.holder.cat_id = catId;
            }
            String subCatId = data.getStringExtra(Constants.SUBCATEGORY_ID);
            if (subCatId != null) {
                itemViewModel.holder.sub_cat_id = subCatId;
            }

            typeClicked = (itemViewModel.holder.cat_id != null && !itemViewModel.holder.cat_id.equals(""))
                    || (itemViewModel.holder.sub_cat_id != null && !itemViewModel.holder.sub_cat_id.equals(""));

            typeButtonClicked(typeClicked);

            replaceData(clearRecyclerView);

            loadItemList();

        } else if (requestCode == Constants.REQUEST_CODE__ITEM_LIST_FRAGMENT
                && resultCode == Constants.RESULT_CODE__SPECIAL_FILTER) {

            // Result From Filter
//
            if (data.getSerializableExtra(Constants.FILTERING_HOLDER) != null) {

                itemViewModel.holder = (ItemParameterHolder) data.getSerializableExtra(Constants.FILTERING_HOLDER);

            }

            filterClicked = !itemViewModel.holder.keyword.equals("") || !itemViewModel.holder.order_by.equals(Constants.FILTERING_FEATURE) ||
                    !itemViewModel.holder.order_type.equals(Constants.FILTERING_DESC);

            tuneButtonClicked(filterClicked);

            replaceData(clearRecyclerView);

            loadItemList();

        } else if (requestCode == Constants.REQUEST_CODE__MAP_FILTERING
                && resultCode == Constants.RESULT_CODE__MAP_FILTERING) {

            if (data.getSerializableExtra(Constants.ITEM_HOLDER) != null) {

                itemViewModel.holder = (ItemParameterHolder) data.getSerializableExtra(Constants.ITEM_HOLDER);

            }

            mapFilterClicked = !itemViewModel.holder.lat.equals("") || !itemViewModel.holder.lng.equals("") ||
                    !itemViewModel.holder.mapMiles.equals("");

            mapFilterButtonClicked(mapFilterClicked);

            replaceData(clearRecyclerView);

            loadItemList();

        }

    }


    @Override
    protected void initUIAndActions() {
        if (getActivity() instanceof MainActivity) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            ((MainActivity) this.getActivity()).binding.toolbar.setBackgroundColor(getResources().getColor(R.color.global__primary));
            ((MainActivity) getActivity()).updateToolbarIconColor(Color.WHITE);
            ((MainActivity) getActivity()).updateMenuIconWhite();
            ((MainActivity) getActivity()).refreshPSCount();
        }

        binding.get().typeButton.setOnClickListener(this::ButtonClick);

        binding.get().tuneButton.setOnClickListener(this::ButtonClick);

        binding.get().sortButton.setOnClickListener(this::ButtonClick);

        binding.get().newsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                GridLayoutManager layoutManager = (GridLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {

                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();

                    if (lastPosition == adapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !itemViewModel.forceEndLoading) {

                            itemViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                            int limit = Config.ITEM_COUNT;

                            itemViewModel.offset = itemViewModel.offset + limit;

                            loadNextPageItemList(String.valueOf(itemViewModel.offset));

                        }
                    }
                }
            }
        });

        binding.get().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.view__primary_line));
        binding.get().swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.global__primary));
        binding.get().swipeRefresh.setOnRefreshListener(() -> {

            itemViewModel.forceEndLoading = false;

            itemViewModel.loadingDirection = Utils.LoadingDirection.top;

            loadItemList();

        });
    }


    @Override
    protected void initViewModels() {
        // ViewModel need to get from ViewModelProviders
        itemViewModel = new ViewModelProvider(this, viewModelFactory).get(ItemViewModel.class);

    }

    @Override
    protected void initAdapters() {

        ItemVerticalListAdapter nvAdapter = new ItemVerticalListAdapter(dataBindingComponent, item -> navigationController.navigateToItemDetailActivity(SearchListFragment.this.getActivity(), item.id, item.title), this);

        this.adapter = new AutoClearedValue<>(this, nvAdapter);
        binding.get().newsList.setAdapter(nvAdapter);
    }

    @Override
    protected void initData() {

        if (getActivity() != null) {
            itemViewModel.holder = (ItemParameterHolder) getActivity().getIntent().getSerializableExtra(Constants.ITEM_PARAM_HOLDER_KEY);

            if (getArguments() != null) {
                itemViewModel.holder = (ItemParameterHolder) getArguments().getSerializable(Constants.ITEM_PARAM_HOLDER_KEY);
            }

            if (itemViewModel.holder != null) {
//                if (!itemViewModel.holder.keyword.isEmpty()) {
//                    binding.get().buttonLayout.setVisibility(View.GONE);
//                }
//                else {
                binding.get().buttonLayout.setVisibility(View.VISIBLE);
//                }
            }

            initItemData();

//            setTouchCount();

            filterClicked = !itemViewModel.holder.keyword.equals(Constants.FILTERING_INACTIVE) ||
                    !itemViewModel.holder.order_by.equals(Constants.FILTERING_INACTIVE) ||
                    !itemViewModel.holder.order_type.equals(Constants.FILTERING_INACTIVE);

            typeClicked = !itemViewModel.holder.cat_id.equals(Constants.FILTERING_INACTIVE) || !itemViewModel.holder.sub_cat_id.equals(Constants.FILTERING_INACTIVE);

            mapFilterClicked = !itemViewModel.holder.lat.equals("") || !itemViewModel.holder.lng.equals("") ||
                    !itemViewModel.holder.mapMiles.equals("");

            typeButtonClicked(typeClicked);

            tuneButtonClicked(filterClicked);

            mapFilterButtonClicked(mapFilterClicked);

        }
    }

    //endregion


    //region Private Methods

    private void initItemData() {

        loadItemList();

        LiveData<Resource<List<Item>>> news = itemViewModel.getItemListByKeyData();

        if (news != null) {

            news.observe(this, listResource -> {
                if (listResource != null) {
                    switch (listResource.status) {
                        case SUCCESS:

                            itemViewModel.setLoadingState(false);

                            if (listResource.data != null) {
                                if (listResource.data.size() == 0) {

                                    if (!binding.get().getLoadingMore()) {
                                        binding.get().noItemConstraintLayout.setVisibility(View.VISIBLE);
                                    }

                                } else {

                                    binding.get().noItemConstraintLayout.setVisibility(View.INVISIBLE);

                                }

                                fadeIn(binding.get().getRoot());

                                replaceData(listResource.data);

                                onDispatched();
                            }
                            break;

                        case LOADING:
                            if (listResource.data != null) {

                                binding.get().noItemConstraintLayout.setVisibility(View.INVISIBLE);

                                fadeIn(binding.get().getRoot());

                                replaceData(listResource.data);

                                itemViewModel.setLoadingState(true);

                                if (itemViewModel.forceEndLoading) {
                                    itemViewModel.forceEndLoading = false;
                                }

                                onDispatched();
                            }
                            break;

                        case ERROR:

                            itemViewModel.setLoadingState(false);
                            itemViewModel.forceEndLoading = true;

                            if (itemViewModel.getItemListByKeyData() != null) {
                                if (itemViewModel.getItemListByKeyData().getValue() != null) {
                                    if (itemViewModel.getItemListByKeyData().getValue().data != null) {
                                        if (!binding.get().getLoadingMore() && itemViewModel.getItemListByKeyData().getValue().data.size() == 0) {
                                            binding.get().noItemConstraintLayout.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }

                            break;

                        default:
                            break;
                    }
                }
            });
        }

        itemViewModel.getNextPageItemListByKeyData().observe(this, state -> {

            if (state != null) {
                if (state.status == Status.ERROR) {

                    itemViewModel.setLoadingState(false);
                    itemViewModel.forceEndLoading = true;

                }
            }
        });

        itemViewModel.getLoadingState().observe(this, loadingState -> {

            binding.get().setLoadingMore(itemViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }
        });

    }
    public static Double getDistance(double lat1, double lon1, double lat2, double lon2) {

        final int R = 6371; // Radius of the earth
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);


        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return(c * r);









    }

    private void replaceData(List<Item> newsList) {
//        ArrayList<Double> distanceList
//                = new ArrayList<Double>();
//
//        for (int x = 0 ; x < newsList.size() ; x++){
//
//            double dis = getDistance(Double.parseDouble(selectedLat) , Double.parseDouble(selectedLng) ,Double.parseDouble(newsList.get(x).lat) , Double.parseDouble(newsList.get(x).lng));
//            distanceList.add(dis);
//
//
//
//
//        }
        Collections.sort(newsList, new Comparator<Item>() {
            @Override
            public int compare(Item item, Item t1) {
                return Double.compare(getDistance(Double.parseDouble(item.lat),Double.parseDouble( item.lng) , Double.parseDouble(selectedLat) , Double.parseDouble(selectedLng)),
                        getDistance(Double.parseDouble(t1.lat),Double.parseDouble( t1.lng) , Double.parseDouble(selectedLat) , Double.parseDouble(selectedLng))
                );
            }
        });
//        Collections.sort(distanceList);
//
//        for(int i = 0  ; i < newsList.size() ; i++) {
//            for(int x = 0 ; x < distanceList.size() ; x++){
//
//                double dis2 = getDistance(Double.parseDouble(selectedLat) , Double.parseDouble(selectedLng) ,Double.parseDouble(newsList.get(i).lat) , Double.parseDouble(newsList.get(i).lng));
//                if(dis2 == distanceList.get(x)){
//                    double temp = dis2 ;
//
//                    Collections.swap(newsList, i, x);
//
//
//                }
//
//            }
//        }


        adapter.get().replace(newsList);
        binding.get().executePendingBindings();
    }


    private void typeButtonClicked(Boolean b) {
        if (b) {
            binding.get().typeButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.baseline_list_with_check_orange_24), null, null);
        } else {
            binding.get().typeButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.baseline_list_orange_24), null, null);
        }
    }

    private void tuneButtonClicked(Boolean b) {
        if (b) {

            binding.get().tuneButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.baseline_tune_with_check_orange_24), null, null);

        } else {
            binding.get().tuneButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.baseline_tune_orange_24), null, null);
        }
    }

    private void mapFilterButtonClicked(Boolean b) {
        if (b) {

            binding.get().sortButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.baseline_sort_with_check_orange_24), null, null);

        } else {
            binding.get().sortButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.baseline_sort_orange_24), null, null);
        }
    }

    private void ButtonClick(View view) {

        switch (view.getId()) {
            case R.id.typeButton:

                navigationController.navigateToTypeFilterFragment(SearchListFragment.this.getActivity(), itemViewModel.holder.cat_id,
                        itemViewModel.holder.sub_cat_id, itemViewModel.holder, Constants.FILTERING_TYPE_FILTER);
//                navigationController.navigateToCategoryActivity(getActivity());

                typeButtonClicked(typeClicked);

                break;

            case R.id.tuneButton:

                navigationController.navigateToTypeFilterFragment(SearchListFragment.this.getActivity(), itemViewModel.holder.cat_id,
                        itemViewModel.holder.sub_cat_id, itemViewModel.holder, Constants.FILTERING_SPECIAL_FILTER);

                tuneButtonClicked(filterClicked);

                break;

            case R.id.sortButton:

//                mBottomSheetDialog.get().show();
//                ButtonSheetClick();

                itemViewModel.holder.location_id = selected_location_id;
                if(itemViewModel.holder.lat.equals("") && itemViewModel.holder.lng.equals("")) {
                    itemViewModel.holder.lat = selectedLat;
                    itemViewModel.holder.lng = selectedLng;
                }
                navigationController.navigateToMapFiltering(this.getActivity(), itemViewModel.holder);

                break;

            default:
                Utils.psLog("No ID for Buttons");
        }
    }

    @Override
    public void onDispatched() {

        if (itemViewModel.loadingDirection == Utils.LoadingDirection.top) {

            if (binding.get() != null) {
                GridLayoutManager layoutManager = (GridLayoutManager)
                        binding.get().newsList.getLayoutManager();

                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(0, 0);
                }
            }
        }
    }

    private void resetLimitAndOffset() {
        itemViewModel.offset = 0;
    }

    private void loadNextPageItemList(String offset) {

        itemViewModel.setNextPageItemListByKeyObj(String.valueOf(Config.ITEM_COUNT), offset, Utils.checkUserId(loginUserId), itemViewModel.holder);


    }

    private void loadItemList() {

        resetLimitAndOffset();

        itemViewModel.setItemListByKeyObj(Utils.checkUserId(loginUserId), String.valueOf(Config.ITEM_COUNT), Constants.ZERO, itemViewModel.holder);

    }

    @Override
    public void onResume() {
        super.onResume();

        loadLoginUserId();
    }
}


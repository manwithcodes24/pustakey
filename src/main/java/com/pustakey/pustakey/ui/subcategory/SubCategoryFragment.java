package com.pustakey.pustakey.ui.subcategory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.pustakey.pustakey.Config;
import com.pustakey.pustakey.R;
import com.pustakey.pustakey.binding.FragmentDataBindingComponent;
import com.pustakey.pustakey.databinding.FragmentSubCategoryBinding;
import com.pustakey.pustakey.ui.common.DataBoundListAdapter;
import com.pustakey.pustakey.ui.common.PSFragment;
import com.pustakey.pustakey.ui.subcategory.adapter.SubCategoryAdapter;
import com.pustakey.pustakey.utils.AutoClearedValue;
import com.pustakey.pustakey.utils.Constants;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewmodel.itemsubcategory.ItemSubCategoryViewModel;
import com.pustakey.pustakey.viewobject.ItemSubCategory;
import com.pustakey.pustakey.viewobject.common.Resource;
import com.pustakey.pustakey.viewobject.common.Status;
import com.pustakey.pustakey.viewobject.holder.ItemParameterHolder;

import java.util.List;

public class SubCategoryFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ItemSubCategoryViewModel subCategoryViewModel;
    private ItemParameterHolder itemParameterHolder = new ItemParameterHolder();

    @VisibleForTesting
    private AutoClearedValue<FragmentSubCategoryBinding> binding;
    private AutoClearedValue<SubCategoryAdapter> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentSubCategoryBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sub_category, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        binding.get().setLoadingMore(connectivity.isConnected());

        return binding.get().getRoot();

    }

    @Override
    protected void initUIAndActions() {

        if (Config.SHOW_ADMOB && connectivity.isConnected()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            binding.get().adView.loadAd(adRequest);
        } else {
            binding.get().adView.setVisibility(View.GONE);
        }

        binding.get().subcategoryList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                GridLayoutManager layoutManager = (GridLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {

                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();
                    if (lastPosition == adapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !subCategoryViewModel.forceEndLoading) {

                            if (connectivity.isConnected()) {

                                subCategoryViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                                int limit = Config.LIST_SEARCH_SUB_CAT_COUNT;
                                subCategoryViewModel.offset = subCategoryViewModel.offset + limit;

                                subCategoryViewModel.setNextPageLoadingStateObj(subCategoryViewModel.catId, String.valueOf(Config.LIST_SEARCH_SUB_CAT_COUNT), String.valueOf(subCategoryViewModel.offset));
                            }
                        }
                    }
                }
            }
        });

        binding.get().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.view__primary_line));
        binding.get().swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.global__primary));
        binding.get().swipeRefresh.setOnRefreshListener(() -> {

            subCategoryViewModel.loadingDirection = Utils.LoadingDirection.top;

            // reset productViewModel.offset
            subCategoryViewModel.offset = 0;

            // reset productViewModel.forceEndLoading
            subCategoryViewModel.forceEndLoading = false;

            // update live data
            subCategoryViewModel.setSubCategoryListData(subCategoryViewModel.catId, String.valueOf(Config.LIST_SEARCH_SUB_CAT_COUNT), String.valueOf(subCategoryViewModel.offset));

        });
    }


    @Override
    protected void initViewModels() {
        // ViewModel need to get from ViewModelProviders
        subCategoryViewModel = new ViewModelProvider(this, viewModelFactory).get(ItemSubCategoryViewModel.class);
    }

    @Override
    protected void initAdapters() {
        SubCategoryAdapter nvAdapter = new SubCategoryAdapter(dataBindingComponent, new SubCategoryAdapter.SubCategoryClickCallback() {
            @Override
            public void onClick(ItemSubCategory subCategory) {

                itemParameterHolder.sub_cat_id = subCategory.id;
                itemParameterHolder.isPaid = Constants.PAIDITEMFIRST;



                navigationController.navigateToHomeFilteringActivity(SubCategoryFragment.this.getActivity(), itemParameterHolder, subCategory.name, selectedCityLat, selectedCityLng, Constants.MAP_MILES);

//                if (SubCategoryFragment.this.getActivity() != null) {
//                    SubCategoryFragment.this.getActivity().finish();
//                }

            }
        },this);
        this.adapter = new AutoClearedValue<>(this, nvAdapter);
        binding.get().subcategoryList.setAdapter(nvAdapter);
    }

    @Override
    protected void initData() {
        getIntentData();

        loadNews();
    }

    private void getIntentData() {
        try {
            if (getActivity() != null) {
                if (getActivity().getIntent().getExtras() != null) {
                    subCategoryViewModel.catId = getActivity().getIntent().getExtras().getString(Constants.CATEGORY_ID);
                    itemParameterHolder.cat_id = subCategoryViewModel.catId;
//                    itemParameterHolder.city_id = selectedCityId;
                }
            }
        } catch (Exception e) {
            Utils.psErrorLog("", e);
        }
    }
    private void loadNews() {

        // Load Sub Category
        subCategoryViewModel.setSubCategoryListData(subCategoryViewModel.catId, String.valueOf(Config.LIST_SEARCH_SUB_CAT_COUNT), String.valueOf(subCategoryViewModel.offset));

        LiveData<Resource<List<ItemSubCategory>>> news = subCategoryViewModel.getSubCategoryListData();

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

                            subCategoryViewModel.setLoadingState(false);

                            break;

                        case ERROR:
                            // Error State

                            subCategoryViewModel.setLoadingState(false);

                            break;
                        default:
                            // Default

                            break;
                    }

                } else {

                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");

                    if (subCategoryViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        subCategoryViewModel.forceEndLoading = true;
                    }

                }

            });
        }

        subCategoryViewModel.getNextPageLoadingStateData().observe(this, state -> {
            if (state != null) {
                if (state.status == Status.ERROR) {
                    Utils.psLog("Next Page State : " + state.data);

                    subCategoryViewModel.setLoadingState(false);
                    subCategoryViewModel.forceEndLoading = true;
                }
            }
        });

        subCategoryViewModel.getLoadingState().observe(this, loadingState -> {

            binding.get().setLoadingMore(subCategoryViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }

        });

    }

    private void replaceData(List<ItemSubCategory> newsList) {

        adapter.get().replace(newsList);
        binding.get().executePendingBindings();

    }

    @Override
    public void onDispatched() {

        if (subCategoryViewModel.loadingDirection == Utils.LoadingDirection.top) {

            if (binding.get().subcategoryList != null) {

                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        binding.get().subcategoryList.getLayoutManager();

                if (layoutManager != null) {
                    layoutManager.scrollToPosition(0);
                }
            }
        }
    }

    //endregion

}


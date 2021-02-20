package com.pustakey.pustakey.ui.item.itempricetype;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.pustakey.pustakey.Config;
import com.pustakey.pustakey.R;
import com.pustakey.pustakey.binding.FragmentDataBindingComponent;
import com.pustakey.pustakey.databinding.FragmentItemPriceTypeBinding;
import com.pustakey.pustakey.ui.common.DataBoundListAdapter;
import com.pustakey.pustakey.ui.common.PSFragment;
import com.pustakey.pustakey.utils.AutoClearedValue;
import com.pustakey.pustakey.utils.Constants;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewmodel.itempricetype.ItemPriceTypeViewModel;
import com.pustakey.pustakey.viewobject.ItemPriceType;
import com.pustakey.pustakey.viewobject.common.Resource;
import com.pustakey.pustakey.viewobject.common.Status;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemPriceTypeFragment extends PSFragment implements DataBoundListAdapter.DiffUtilDispatchedInterface{

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ItemPriceTypeViewModel itemPriceTypeViewModel;
    public String priceTypeId;

    @VisibleForTesting
    private AutoClearedValue<FragmentItemPriceTypeBinding> binding;
    private AutoClearedValue<ItemPriceTypeAdapter> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentItemPriceTypeBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item_price_type, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);

        setHasOptionsMenu(true);

        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            this.priceTypeId = intent.getStringExtra(Constants.ITEM_PRICE_TYPE_ID);
        }

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
    protected void initUIAndActions() {
        binding.get().itemPriceRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = (LinearLayoutManager)
                        recyclerView.getLayoutManager();

                if (layoutManager != null) {

                    int lastPosition = layoutManager
                            .findLastVisibleItemPosition();

                    if (lastPosition == adapter.get().getItemCount() - 1) {

                        if (!binding.get().getLoadingMore() && !itemPriceTypeViewModel.forceEndLoading) {

                            itemPriceTypeViewModel.loadingDirection = Utils.LoadingDirection.bottom;

                            int limit = Config.LIST_PRICE_TYPE_COUNT;

                            itemPriceTypeViewModel.offset = itemPriceTypeViewModel.offset + limit;

                            itemPriceTypeViewModel.setNextPageLoadingStateObj( String.valueOf(limit), String.valueOf(itemPriceTypeViewModel.offset));

                            itemPriceTypeViewModel.setLoadingState(true);
                        }
                    }
                }
            }
        });

        binding.get().swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.view__primary_line));
        binding.get().swipeRefresh.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.global__primary));
        binding.get().swipeRefresh.setOnRefreshListener(() -> {

            itemPriceTypeViewModel.loadingDirection = Utils.LoadingDirection.top;

            // reset productViewModel.offset
            itemPriceTypeViewModel.offset = 0;

            // reset productViewModel.forceEndLoading
            itemPriceTypeViewModel.forceEndLoading = false;

            // update live data
            itemPriceTypeViewModel.setItemPriceTypeListObj(String.valueOf(Config.LIST_PRICE_TYPE_COUNT), String.valueOf(itemPriceTypeViewModel.offset));

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.clear_button, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.clear) {
            this.priceTypeId = "";

            initAdapters();

            initData();

            if(this.getActivity() != null)
            navigationController.navigateBackToItemPriceTypeFragment(this.getActivity(), this.priceTypeId, "");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initViewModels() {

        itemPriceTypeViewModel = new ViewModelProvider(this, viewModelFactory).get(ItemPriceTypeViewModel.class);
    }

    @Override
    protected void initAdapters() {

        ItemPriceTypeAdapter nvadapter = new ItemPriceTypeAdapter(dataBindingComponent,
                (category, id) -> {
                    if(this.getActivity() != null)
                    navigationController.navigateBackToItemPriceTypeFragment(this.getActivity(), category.id, category.name);

                    if (getActivity() != null) {
                        this.getActivity().finish();
                    }
                }, this.priceTypeId);
        this.adapter = new AutoClearedValue<>(this, nvadapter);
        binding.get().itemPriceRecyclerView.setAdapter(nvadapter);

    }

    @Override
    protected void initData() {
        loadCategory();
    }

    private void loadCategory() {

        // Load Category List
        itemPriceTypeViewModel.categoryParameterHolder.cityId = selectedCityId;

        itemPriceTypeViewModel.setItemPriceTypeListObj("", String.valueOf(itemPriceTypeViewModel.offset));

        LiveData<Resource<List<ItemPriceType>>> news = itemPriceTypeViewModel.getItemPriceTypeListData();

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

                            itemPriceTypeViewModel.setLoadingState(false);

                            break;

                        case ERROR:
                            // Error State

                            itemPriceTypeViewModel.setLoadingState(false);

                            break;
                        default:
                            // Default

                            break;
                    }

                } else {

                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");

                    if (itemPriceTypeViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        itemPriceTypeViewModel.forceEndLoading = true;
                    }

                }

            });
        }

        itemPriceTypeViewModel.getNextPageLoadingStateData().observe(this, state -> {
            if (state != null) {
                if (state.status == Status.ERROR) {
                    Utils.psLog("Next Page State : " + state.data);

                    itemPriceTypeViewModel.setLoadingState(false);
                    itemPriceTypeViewModel.forceEndLoading = true;
                }
            }
        });

        itemPriceTypeViewModel.getLoadingState().observe(this, loadingState -> {
            binding.get().setLoadingMore(itemPriceTypeViewModel.isLoading);

            if (loadingState != null && !loadingState) {
                binding.get().swipeRefresh.setRefreshing(false);
            }
        });

    }

    private void replaceData(List<ItemPriceType> categoryList) {

        adapter.get().replace(categoryList);
        binding.get().executePendingBindings();

    }

    @Override
    public void onDispatched() {
        if (itemPriceTypeViewModel.loadingDirection == Utils.LoadingDirection.top) {
            binding.get();
            LinearLayoutManager layoutManager = (LinearLayoutManager)
                    binding.get().itemPriceRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPosition(0);
            }
        }
    }
}
package com.pustakey.pustakey.viewmodel.itemdealoption;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.pustakey.pustakey.repository.itemdealoption.ItemDealOptionRepository;
import com.pustakey.pustakey.utils.AbsentLiveData;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewmodel.common.PSViewModel;
import com.pustakey.pustakey.viewobject.ItemDealOption;
import com.pustakey.pustakey.viewobject.common.Resource;
import com.pustakey.pustakey.viewobject.holder.CategoryParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class ItemDealOptionViewModel extends PSViewModel {


    //region Variables

    private final LiveData<Resource<List<ItemDealOption>>> itemTypeListData;
    private MutableLiveData<ItemDealOptionViewModel.TmpDataHolder> itemTypeListObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> nextPageLoadingStateData;
    private MutableLiveData<ItemDealOptionViewModel.TmpDataHolder> nextPageLoadingStateObj = new MutableLiveData<>();


    public CategoryParameterHolder categoryParameterHolder = new CategoryParameterHolder();

    //endregion

    //region Constructors

    @Inject
    ItemDealOptionViewModel(ItemDealOptionRepository repository) {

        Utils.psLog("ItemDealOptionViewModel");

        itemTypeListData = Transformations.switchMap(itemTypeListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("ItemDealOptionViewModel : categories");
            return repository.getAllItemDealOptionList(obj.limit, obj.offset);
        });

        nextPageLoadingStateData = Transformations.switchMap(nextPageLoadingStateObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("Category List.");
            return repository.getNextPageItemDealOptionList(obj.limit, obj.offset);
        });

    }

    //endregion

    public void setItemDealOptionListObj(String limit, String offset) {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            itemTypeListObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<List<ItemDealOption>>> getItemDealOptionListData() {
        return itemTypeListData;
    }

    public void setNextPageLoadingStateObj(String limit, String offset) {

        if (!isLoading) {
            ItemDealOptionViewModel.TmpDataHolder tmpDataHolder = new ItemDealOptionViewModel.TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            nextPageLoadingStateObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<Boolean>> getNextPageLoadingStateData() {
        return nextPageLoadingStateData;
    }


    class TmpDataHolder {
        public String limit = "";
        public String offset = "";
        public String cityId = "";
    }
}

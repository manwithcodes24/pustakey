package com.pustakey.pustakey.viewmodel.offer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.pustakey.pustakey.repository.offer.OfferRepository;
import com.pustakey.pustakey.utils.AbsentLiveData;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewmodel.common.PSViewModel;
import com.pustakey.pustakey.viewobject.Offer;
import com.pustakey.pustakey.viewobject.common.Resource;
import com.pustakey.pustakey.viewobject.offerholder.OfferListParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class OfferViewModel extends PSViewModel {

    private final LiveData<Resource<List<Offer>>> offerListData;
    private MutableLiveData<OfferViewModel.TmpDataHolder> offerListObj = new MutableLiveData<>();

    private final LiveData<Resource<Boolean>> nextPageOfferListData;
    private MutableLiveData<TmpDataHolder> nextPageOfferListObj = new MutableLiveData<>();

    public OfferListParameterHolder holder = new OfferListParameterHolder();

    public Offer offer;

    @Inject
    OfferViewModel(OfferRepository repository) {

        Utils.psLog("OfferListViewModel");

        offerListData = Transformations.switchMap(offerListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("OfferListViewModel : offerListData");
            return repository.getOfferList(obj.userId, obj.offerListParameterHolder, obj.limit, obj.offset);
        });

        nextPageOfferListData = Transformations.switchMap(nextPageOfferListObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }

            Utils.psLog("OfferListViewModel: nextPageOfferListData");
            return repository.getNextPageOffer(obj.userId, obj.offerListParameterHolder, obj.limit, obj.offset);
        });

    }

    public void setOfferListObj(String userId, OfferListParameterHolder offerListParameterHolder, String limit, String offset) {
        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            tmpDataHolder.userId = userId;
            tmpDataHolder.offerListParameterHolder = offerListParameterHolder;

            offerListObj.setValue(tmpDataHolder);

            //start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<List<Offer>>> getOfferListData() {

        return offerListData;
    }

    //Get Latest Category Next Page
    public void setNextPageOfferFromSellerObj(String userId, OfferListParameterHolder offerListParameterHolder, String limit, String offset) {

        if (!isLoading) {
            TmpDataHolder tmpDataHolder = new TmpDataHolder();
            tmpDataHolder.offset = offset;
            tmpDataHolder.limit = limit;
            tmpDataHolder.userId = userId;
            tmpDataHolder.offerListParameterHolder = offerListParameterHolder;

            offerListObj.setValue(tmpDataHolder);

            // start loading
            setLoadingState(true);
        }
    }

    public LiveData<Resource<Boolean>> getNextPageOfferListData() {
        return nextPageOfferListData;
    }

    class TmpDataHolder {
        String limit = "";
        String offset = "";
        String userId = "";
        OfferListParameterHolder offerListParameterHolder;
    }
}

package com.yashbuysell.psbuyandsell.repository.offer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.yashbuysell.psbuyandsell.AppExecutors;
import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.api.ApiResponse;
import com.yashbuysell.psbuyandsell.api.PSApiService;
import com.yashbuysell.psbuyandsell.db.OfferDao;
import com.yashbuysell.psbuyandsell.db.PSCoreDb;
import com.yashbuysell.psbuyandsell.repository.common.NetworkBoundResource;
import com.yashbuysell.psbuyandsell.repository.common.PSRepository;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewobject.Offer;
import com.yashbuysell.psbuyandsell.viewobject.OfferMap;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;
import com.yashbuysell.psbuyandsell.viewobject.offerholder.OfferListParameterHolder;

import java.util.List;

import javax.inject.Inject;

public class OfferRepository extends PSRepository {

    private OfferDao offerDao;

    @Inject
    OfferRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, OfferDao offerDao) {
        super(psApiService, appExecutors, db);

        Utils.psLog("Inside OfferListRepository");

        this.offerDao = offerDao;
    }

    public LiveData<Resource<List<Offer>>> getOfferList(String userId, OfferListParameterHolder offerListParameterHolder, String limit, String offset) {
        return new NetworkBoundResource<List<Offer>, List<Offer>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<Offer> offer) {

                Utils.psLog("SaveCallResult of getOfferList.");

                db.runInTransaction(() -> {

                    try {

                        String mapKey = offerListParameterHolder.getMapKey();

                        offerDao.deleteByMapKey(mapKey);

                        offerDao.insertAll(offer);

                        String dateTime = Utils.getDateTime();

                        for (int i = 0; i < offer.size(); i++) {
                            offerDao.insert(new OfferMap(mapKey + offer.get(i).id, mapKey, offer.get(i).id, i + 1, dateTime));
                        }
                    } catch (Exception e) {
                        Utils.psErrorLog("Error in doing transaction of getOfferList.", e);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Offer> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<Offer>> loadFromDb() {
                Utils.psLog("Load OfferList From Db");
                String mapKey = offerListParameterHolder.getMapKey();
                return offerDao.getOfferListByKey(mapKey);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<Offer>>> createCall() {
                return psApiService.getOfferList(Config.API_KEY, userId, offerListParameterHolder.returnType, limit, offset);
            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed of " + message);

                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> db.runInTransaction(new Runnable() {
                            @Override
                            public void run() {

                                String mapKey = offerListParameterHolder.getMapKey();

                                offerDao.deleteByMapKey(mapKey);
                            }
                        }));

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }

        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> getNextPageOffer(String userId, OfferListParameterHolder offerListParameterHolder, String limit, String offset) {
        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();
        LiveData<ApiResponse<List<Offer>>> apiResponse = psApiService.getOfferList(Config.API_KEY, userId, offerListParameterHolder.returnType, limit, offset);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);


            if (response.isSuccessful() && response.body != null) {

                appExecutors.diskIO().execute(() ->
                        db.runInTransaction(() -> {

                            try {

                                offerDao.insertAll(response.body);

                                int finalIndex = db.itemMapDao().getMaxSortingByValue(offerListParameterHolder.getMapKey());

                                int startIndex = finalIndex + 1;

                                String mapKey = offerListParameterHolder.getMapKey();
                                String dateTime = Utils.getDateTime();

                                for (int i = 0; i < response.body.size(); i++) {
                                    offerDao.insert(new OfferMap(mapKey + response.body.get(i).id, mapKey, response.body.get(i).id, startIndex + i, dateTime));
                                }

                                statusLiveData.postValue(Resource.success(true));

                            } catch (Exception e) {
                                statusLiveData.postValue(Resource.error(response.errorMessage, null));
                                Utils.psErrorLog("Error in doing transaction of getOfferList.", e);
                            }
                        }));

            } else {
                statusLiveData.postValue(Resource.error(response.errorMessage, null));
            }

        });

        return statusLiveData;
    }

}

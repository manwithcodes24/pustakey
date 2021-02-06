package com.yashbuysell.psbuyandsell.repository.paypal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yashbuysell.psbuyandsell.AppExecutors;
import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.api.ApiResponse;
import com.yashbuysell.psbuyandsell.api.PSApiService;
import com.yashbuysell.psbuyandsell.db.PSCoreDb;
import com.yashbuysell.psbuyandsell.repository.common.PSRepository;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewobject.ApiStatus;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Response;

public class PaypalRepository extends PSRepository {


    @Inject
    PaypalRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db) {
        super(psApiService, appExecutors, db);

        Utils.psLog("Inside PaypalRepository");
    }

    public LiveData<Resource<Boolean>> getPaypalToken() {

        final MutableLiveData<Resource<Boolean>> statusLiveData = new MutableLiveData<>();

        appExecutors.networkIO().execute(() -> {

            Response<ApiStatus> response;

            try {
                response = psApiService.getPaypalToken(Config.API_KEY).execute();

                ApiResponse<ApiStatus> apiResponse = new ApiResponse<>(response);

                if (apiResponse.isSuccessful()) {
                    if(apiResponse.body != null) {
                        statusLiveData.postValue(Resource.successWithMsg(apiResponse.body.message, true));
                    }else {
                        statusLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                    }
                } else {
                    statusLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                }

            } catch (IOException e) {
                statusLiveData.postValue(Resource.error(e.getMessage(), false));
            }

        });

        return statusLiveData;
    }
}

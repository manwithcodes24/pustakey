package com.pustakey.pustakey.viewmodel.pscount;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.pustakey.pustakey.Config;
import com.pustakey.pustakey.repository.pscount.PSCountRepository;
import com.pustakey.pustakey.utils.AbsentLiveData;
import com.pustakey.pustakey.utils.Utils;
import com.pustakey.pustakey.viewmodel.common.PSViewModel;
import com.pustakey.pustakey.viewobject.PSCount;

import com.pustakey.pustakey.viewobject.common.Resource;

import javax.inject.Inject;

/**
 * Created by Panacea-Soft on 2019-08-28.
 * Contact Email : teamps.is.cool@gmail.com
 */


public class PSCountViewModel extends PSViewModel {

    private final LiveData<Resource<PSCount>> psCountData;
    private MutableLiveData<TmpDataHolder> psCountObj = new MutableLiveData<>();

    public String appSettingLat;
    public String appSettingLng;

    @Inject
    PSCountViewModel(PSCountRepository repository) {
        psCountData = Transformations.switchMap(psCountObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("PSCountViewModel");
            return repository.getPSCount(Config.API_KEY, obj.userId, obj.deviceToken);
        });
    }

    public void setPsCountObj(String userId, String deviceToken) {

        TmpDataHolder tmpDataHolder = new TmpDataHolder(userId, deviceToken);

        this.psCountObj.setValue(tmpDataHolder);
    }

    public LiveData<Resource<PSCount>> getPSCount() {
        return psCountData;
    }

    class TmpDataHolder {
        String userId, deviceToken;

        private TmpDataHolder(String userId, String deviceToken) {
            this.userId = userId;
            this.deviceToken = deviceToken;
        }
    }

}
package com.yashbuysell.psbuyandsell.viewmodel.paypal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.yashbuysell.psbuyandsell.repository.paypal.PaypalRepository;
import com.yashbuysell.psbuyandsell.utils.AbsentLiveData;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewmodel.common.PSViewModel;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;

import javax.inject.Inject;

public class PaypalViewModel extends PSViewModel {

    private final LiveData<Resource<Boolean>> paypalTokenData;
    private MutableLiveData<Boolean> paypalTokenObj = new MutableLiveData<>();


    @Inject
    PaypalViewModel(PaypalRepository repository) {
        paypalTokenData = Transformations.switchMap(paypalTokenObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            Utils.psLog("paypalTokenData");
            return repository.getPaypalToken();
        });
    }

    public void setPaypalTokenObj() {
        this.paypalTokenObj.setValue(true);
    }

    public LiveData<Resource<Boolean>> getPaypalTokenData() {
        return paypalTokenData;
    }

}

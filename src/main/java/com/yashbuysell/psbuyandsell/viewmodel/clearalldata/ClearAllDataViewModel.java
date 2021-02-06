package com.yashbuysell.psbuyandsell.viewmodel.clearalldata;

import com.yashbuysell.psbuyandsell.repository.clearpackage.ClearPackageRepository;
import com.yashbuysell.psbuyandsell.utils.AbsentLiveData;
import com.yashbuysell.psbuyandsell.viewmodel.common.PSViewModel;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class ClearAllDataViewModel extends PSViewModel {

    private final LiveData<Resource<Boolean>> deleteAllDataData;
    private MutableLiveData<Boolean> deleteAllDataObj = new MutableLiveData<>();


    @Inject
    public ClearAllDataViewModel(ClearPackageRepository repository) {

        deleteAllDataData = Transformations.switchMap(deleteAllDataObj, obj -> {
            if (obj == null) {
                return AbsentLiveData.create();
            }
            return repository.clearAllTheData();
        });
    }

    public void setDeleteAllDataObj() {

        this.deleteAllDataObj.setValue(true);
    }

    public LiveData<Resource<Boolean>> getDeleteAllDataData() {
        return deleteAllDataData;
    }


}

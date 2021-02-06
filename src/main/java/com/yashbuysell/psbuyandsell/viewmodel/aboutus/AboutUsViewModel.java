package com.yashbuysell.psbuyandsell.viewmodel.aboutus;

import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.repository.aboutus.AboutUsRepository;
import com.yashbuysell.psbuyandsell.utils.AbsentLiveData;
import com.yashbuysell.psbuyandsell.viewmodel.common.PSViewModel;
import com.yashbuysell.psbuyandsell.viewobject.AboutUs;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

/**
 * Created by Panacea-Soft on 12/30/17.
 * Contact Email : teamps.is.cool@gmail.com
 */

public class AboutUsViewModel extends PSViewModel {


    //region Variables

    // Get AboutUs
    private final LiveData<Resource<AboutUs>> aboutUsLiveData;
    private MutableLiveData<String> aboutUsObj = new MutableLiveData<>();

    public String aboutId;
    //endregion


    //region Constructors

    @Inject
    AboutUsViewModel(AboutUsRepository repository) {

        aboutUsLiveData = Transformations.switchMap(aboutUsObj, newsId -> {
            if (newsId.isEmpty()) {
                return AbsentLiveData.create();
            }
            return repository.getAboutUs(Config.API_KEY);
        });

    }

    //endregion


    //region Public Methods

    public void setAboutUsObj(String aboutUsObj) {
        this.aboutUsObj.setValue(aboutUsObj);
    }

    public LiveData<Resource<AboutUs>> getAboutUsData() {
        return aboutUsLiveData;
    }

    //endregion

}

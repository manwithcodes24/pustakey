package com.yashbuysell.psbuyandsell.repository.clearpackage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.yashbuysell.psbuyandsell.AppExecutors;
import com.yashbuysell.psbuyandsell.api.PSApiService;
import com.yashbuysell.psbuyandsell.db.PSCoreDb;
import com.yashbuysell.psbuyandsell.repository.common.PSRepository;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;

import javax.inject.Inject;

public class ClearPackageRepository extends PSRepository {

    @Inject
    ClearPackageRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db) {
        super(psApiService, appExecutors, db);

        Utils.psLog("Inside CityCategoryRepository");
    }

    public LiveData<Resource<Boolean>> clearAllTheData() {

        final MutableLiveData<Resource<Boolean>> statusLiveData = new MutableLiveData<>();

        appExecutors.networkIO().execute(() -> {

//            try {
//                db.beginTransaction();
//
//                db.cityDao().deleteAll();
//                db.itemDao().deleteAll();
//                db.blogDao().deleteAll();
//                db.deletedObjectDao().deleteAll();
//                db.imageDao().deleteTable();
//                db.notificationDao().deleteAllNotificationList();
//                db.specsDao().deleteAll();
//                db.psAppInfoDao().deleteAll();
//                db.psAppVersionDao().deleteAll();
//                db.ratingDao().deleteAll();
//                db.setTransactionSuccessful();
//            } catch (NullPointerException ne) {
//                Utils.psErrorLog("Null Pointer Exception : ", ne);
//
//                statusLiveData.postValue(Resource.error(ne.getMessage(), false));
//            } catch (Exception e) {
//                Utils.psErrorLog("Exception : ", e);
//
//                statusLiveData.postValue(Resource.error(e.getMessage(), false));
//            } finally {
//                db.endTransaction();
//            }

            try {
                db.runInTransaction(() -> {
                    db.cityDao().deleteAll();
                    db.itemDao().deleteAll();
                    db.blogDao().deleteAll();
                    db.deletedObjectDao().deleteAll();
                    db.imageDao().deleteTable();
                    db.notificationDao().deleteAllNotificationList();
                    db.specsDao().deleteAll();
                    db.psAppInfoDao().deleteAll();
                    db.psAppVersionDao().deleteAll();
                    db.ratingDao().deleteAll();
                });
            } catch (Exception ex) {
                Utils.psErrorLog("Error at ", ex);
            }

            statusLiveData.postValue(Resource.success(true));


        });

        return statusLiveData;
    }

}

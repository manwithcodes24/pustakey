package com.yashbuysell.psbuyandsell.repository.itemsubcategory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.yashbuysell.psbuyandsell.AppExecutors;
import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.api.ApiResponse;
import com.yashbuysell.psbuyandsell.api.PSApiService;
import com.yashbuysell.psbuyandsell.db.ItemSubCategoryDao;
import com.yashbuysell.psbuyandsell.db.PSCoreDb;
import com.yashbuysell.psbuyandsell.repository.common.NetworkBoundResource;
import com.yashbuysell.psbuyandsell.repository.common.PSRepository;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewobject.ItemSubCategory;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemSubCategoryRepository extends PSRepository {

    private final ItemSubCategoryDao itemSubCategoryDao;

    @Inject
    ItemSubCategoryRepository(PSApiService psApiService, AppExecutors appExecutors, PSCoreDb db, ItemSubCategoryDao subCategoryDao) {
        super(psApiService, appExecutors, db);

        Utils.psLog("Inside SubCategoryRepository");

        this.itemSubCategoryDao = subCategoryDao;
    }

    public LiveData<Resource<List<ItemSubCategory>>> getAllItemSubCategoryList(String apiKey) {
        return new NetworkBoundResource<List<ItemSubCategory>, List<ItemSubCategory>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<ItemSubCategory> itemList) {

                Utils.psLog("SaveCallResult of getAllSubCategoryList.");

//                try {
//                    db.beginTransaction();
//
//                    itemSubCategoryDao.deleteAllSubCategory();
//
//                    itemSubCategoryDao.insertAll(itemList);
//
//                    for (ItemSubCategory item : itemList) {
//                        itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
//                    }
//
//                    db.setTransactionSuccessful();
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error in doing transaction of getAllSubCategoryList.", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> {
                        itemSubCategoryDao.deleteAllSubCategory();

                        itemSubCategoryDao.insertAll(itemList);

                        for (ItemSubCategory item : itemList) {
                            itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                        }
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<ItemSubCategory> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemSubCategory>> loadFromDb() {
                return itemSubCategoryDao.getAllSubCategory();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemSubCategory>>> createCall() {
                return psApiService.getAllSubCategoryList(apiKey);
            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed of " + message);

                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> db.runInTransaction(() -> db.itemSubCategoryDao().deleteAllSubCategory()));

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }

        }.asLiveData();
    }

    public LiveData<Resource<List<ItemSubCategory>>> getSubCategoryList(String apiKey, String catId, String limit, String offset) {
        return new NetworkBoundResource<List<ItemSubCategory>, List<ItemSubCategory>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<ItemSubCategory> itemList) {

                Utils.psLog("SaveCallResult of getSubCategoryList.");

//                try {
//                    db.beginTransaction();
//
//                    itemSubCategoryDao.deleteAllSubCategory();
//
//                    itemSubCategoryDao.insertAll(itemList);
//
//                    for (ItemSubCategory item : itemList) {
//                        itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
//                    }
//
//                    db.setTransactionSuccessful();
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error in doing transaction of recent product list.", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> {
                        itemSubCategoryDao.deleteAllSubCategory();

                        itemSubCategoryDao.insertAll(itemList);

                        for (ItemSubCategory item : itemList) {
                            itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                        }
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<ItemSubCategory> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemSubCategory>> loadFromDb() {
                return itemSubCategoryDao.getSubCategoryList(catId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemSubCategory>>> createCall() {
                return psApiService.getSubCategoryList(apiKey, catId, limit, offset);
            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed of " + message);

                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> db.runInTransaction(() -> db.itemSubCategoryDao().deleteAllSubCategory()));

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }

        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> getNextPageSubCategory(String catId, String limit, String offset) {
        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();
        LiveData<ApiResponse<List<ItemSubCategory>>> apiResponse = psApiService.getSubCategoryList(Config.API_KEY, catId, limit, offset);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

                    try {
                        db.runInTransaction(() -> {
                            if (response.body != null) {
                                for (ItemSubCategory item : response.body) {
                                    itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                                }

                                db.itemSubCategoryDao().insertAll(response.body);
                            }

                        });
                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }

                    statusLiveData.postValue(Resource.success(true));
                });

            } else {
                statusLiveData.postValue(Resource.error(response.errorMessage, null));
            }

        });

        return statusLiveData;
    }


    public LiveData<Resource<List<ItemSubCategory>>> getSubCategoriesWithCatId(String offset, String catId) {
        return new NetworkBoundResource<List<ItemSubCategory>, List<ItemSubCategory>>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull List<ItemSubCategory> itemList) {

                Utils.psLog("SaveCallResult of Sub Category.");

//                try {
//                    db.beginTransaction();
//
//                    itemSubCategoryDao.insertAll(itemList);
//
//                    for (ItemSubCategory item : itemList) {
//                        itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
//                    }
//
//                    db.setTransactionSuccessful();
//                } catch (Exception e) {
//                    Utils.psErrorLog("Error in doing transaction of recent product list.", e);
//                } finally {
//                    db.endTransaction();
//                }

                try {
                    db.runInTransaction(() -> {
                        itemSubCategoryDao.deleteAllSubCategory();
                        itemSubCategoryDao.insertAll(itemList);

                        for (ItemSubCategory item : itemList) {
                            itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                        }
                    });
                } catch (Exception ex) {
                    Utils.psErrorLog("Error at ", ex);
                }
            }

            @Override
            protected boolean shouldFetch(@Nullable List<ItemSubCategory> data) {
                return connectivity.isConnected();
            }

            @NonNull
            @Override
            protected LiveData<List<ItemSubCategory>> loadFromDb() {
                return itemSubCategoryDao.getSubCategoryList(catId);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<ItemSubCategory>>> createCall() {
                return psApiService.getSubCategoryListWithCatId(Config.API_KEY, catId, "", offset);
            }

            @Override
            protected void onFetchFailed(int code, String message) {
                Utils.psLog("Fetch Failed of " + message);

                if (code == Config.ERROR_CODE_10001) {
                    try {
                        appExecutors.diskIO().execute(() -> db.runInTransaction(() -> db.itemSubCategoryDao().deleteAllSubCategory()));

                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }
                }
            }

        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> getNextPageSubCategoriesWithCatId(String limit, String offset, String catId) {
        final MediatorLiveData<Resource<Boolean>> statusLiveData = new MediatorLiveData<>();
        LiveData<ApiResponse<List<ItemSubCategory>>> apiResponse = psApiService.getSubCategoryListWithCatId(Config.API_KEY, catId, limit, offset);

        statusLiveData.addSource(apiResponse, response -> {

            statusLiveData.removeSource(apiResponse);

            //noinspection Constant Conditions
            if (response.isSuccessful()) {

                appExecutors.diskIO().execute(() -> {

                    try {
                        db.runInTransaction(() -> {
                            if (response.body != null) {
                                for (ItemSubCategory item : response.body) {
                                    itemSubCategoryDao.insert(new ItemSubCategory(item.id, item.name, item.status, item.addedDate, item.addedUserId, item.updatedDate, item.cityId, item.catId, item.deletedFlag, item.updatedUserId, item.updatedFlag, item.addedDateStr, item.defaultPhoto, item.defaultIcon));
                                }

                                itemSubCategoryDao.insertAll(response.body);
                            }
                        });
                    } catch (Exception ex) {
                        Utils.psErrorLog("Error at ", ex);
                    }

                    statusLiveData.postValue(Resource.success(true));
                });

            } else {
                statusLiveData.postValue(Resource.error(response.errorMessage, null));
            }

        });

        return statusLiveData;
    }

}

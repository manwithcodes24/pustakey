package com.pustakey.pustakey.db;

import com.pustakey.pustakey.viewobject.PSAppVersion;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface PSAppVersionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PSAppVersion psAppVersion);

    @Query("DELETE FROM PSAppVersion")
    void deleteAll();
}

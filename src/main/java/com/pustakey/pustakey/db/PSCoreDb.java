package com.pustakey.pustakey.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.pustakey.pustakey.db.common.Converters;
import com.pustakey.pustakey.viewobject.AboutUs;
import com.pustakey.pustakey.viewobject.Blog;
import com.pustakey.pustakey.viewobject.ChatHistory;
import com.pustakey.pustakey.viewobject.ChatHistoryMap;
import com.pustakey.pustakey.viewobject.City;
import com.pustakey.pustakey.viewobject.CityMap;
import com.pustakey.pustakey.viewobject.DeletedObject;
import com.pustakey.pustakey.viewobject.Image;
import com.pustakey.pustakey.viewobject.Item;
import com.pustakey.pustakey.viewobject.ItemCategory;
import com.pustakey.pustakey.viewobject.ItemCollection;
import com.pustakey.pustakey.viewobject.ItemCollectionHeader;
import com.pustakey.pustakey.viewobject.ItemCondition;
import com.pustakey.pustakey.viewobject.ItemCurrency;
import com.pustakey.pustakey.viewobject.ItemDealOption;
import com.pustakey.pustakey.viewobject.ItemFavourite;
import com.pustakey.pustakey.viewobject.ItemFromFollower;
import com.pustakey.pustakey.viewobject.ItemHistory;
import com.pustakey.pustakey.viewobject.ItemLocation;
import com.pustakey.pustakey.viewobject.ItemMap;
import com.pustakey.pustakey.viewobject.ItemPaidHistory;
import com.pustakey.pustakey.viewobject.ItemPriceType;
import com.pustakey.pustakey.viewobject.ItemSpecs;
import com.pustakey.pustakey.viewobject.ItemSubCategory;
import com.pustakey.pustakey.viewobject.ItemType;
import com.pustakey.pustakey.viewobject.Noti;
import com.pustakey.pustakey.viewobject.Offer;
import com.pustakey.pustakey.viewobject.OfferMap;
import com.pustakey.pustakey.viewobject.OfflinePayment;
import com.pustakey.pustakey.viewobject.OfflinePaymentMethodHeader;
import com.pustakey.pustakey.viewobject.PSAppInfo;
import com.pustakey.pustakey.viewobject.PSAppSetting;
import com.pustakey.pustakey.viewobject.PSAppVersion;
import com.pustakey.pustakey.viewobject.PSCount;
import com.pustakey.pustakey.viewobject.Rating;
import com.pustakey.pustakey.viewobject.User;
import com.pustakey.pustakey.viewobject.UserLogin;
import com.pustakey.pustakey.viewobject.UserMap;
import com.pustakey.pustakey.viewobject.messageHolder.Message;


/**
 * Created by Panacea-Soft on 11/20/17.
 * Contact Email : teamps.is.cool@gmail.com
 */

@Database(entities = {
        Image.class,
        User.class,
        UserLogin.class,
        AboutUs.class,
        ItemFavourite.class,
        Noti.class,
        ItemHistory.class,
        Blog.class,
        Rating.class,
        PSAppInfo.class,
        PSAppVersion.class,
        DeletedObject.class,
        City.class,
        CityMap.class,
        Item.class,
        ItemMap.class,
        ItemCategory.class,
        ItemCollectionHeader.class,
        ItemCollection.class,
        ItemSubCategory.class,
        ItemSpecs.class,
        ItemCurrency.class,
        ItemPriceType.class,
        ItemType.class,
        ItemLocation.class,
        ItemDealOption.class,
        ItemCondition.class,
        ItemFromFollower.class,
        Message.class,
        ChatHistory.class,
        ChatHistoryMap.class,
        Offer.class,
        OfferMap.class,
        PSAppSetting.class,
        UserMap.class,
        PSCount.class,
        ItemPaidHistory.class,
        OfflinePaymentMethodHeader.class,
        OfflinePayment.class

}, version = 11, exportSchema = false)
// app version 2.9 = db version 11
// app version 2.8 = db version 10
// app version 2.7 = db version 9
// app version 2.6 = db version 8
// app version 2.5 = db version 7
// app version 2.4 = db version 7
// app version 2.3 = db version 6
// app version 2.2 = db version 6
// app version 2.1 = db version 6
// app version 2.0 = db version 6
// app version 1.9 = db version 6
// app version 1.8 = db version 5
// app version 1.7 = db version 4
// app version 1.6 = db version 4
// app version 1.5 = db version 4
// app version 1.4 = db version 3
// app version 1.3 = db version 3
// app version 1.2 = db version 2
// app version 1.0 = db version 1


@TypeConverters({Converters.class})

public abstract class PSCoreDb extends RoomDatabase {

    abstract public UserDao userDao();

    abstract public UserMapDao userMapDao();

    abstract public HistoryDao historyDao();

    abstract public SpecsDao specsDao();

    abstract public AboutUsDao aboutUsDao();

    abstract public ImageDao imageDao();

    abstract public ItemDealOptionDao itemDealOptionDao();

    abstract public ItemConditionDao itemConditionDao();

    abstract public ItemLocationDao itemLocationDao();

    abstract public ItemCurrencyDao itemCurrencyDao();

    abstract public ItemPriceTypeDao itemPriceTypeDao();

    abstract public OfflinePaymentDao offlinePaymentDao();

    abstract public ItemTypeDao itemTypeDao();

    abstract public RatingDao ratingDao();

    abstract public NotificationDao notificationDao();

    abstract public BlogDao blogDao();

    abstract public PSAppInfoDao psAppInfoDao();

    abstract public PSAppVersionDao psAppVersionDao();

    abstract public DeletedObjectDao deletedObjectDao();

    abstract public CityDao cityDao();

    abstract public CityMapDao cityMapDao();

    abstract public ItemDao itemDao();

    abstract public ItemMapDao itemMapDao();

    abstract public ItemCategoryDao itemCategoryDao();

    abstract public ItemCollectionHeaderDao itemCollectionHeaderDao();

    abstract public ItemSubCategoryDao itemSubCategoryDao();

    abstract public ChatHistoryDao chatHistoryDao();

    abstract public OfferDao offerListDao();

    abstract public MessageDao messageDao();

    abstract public PSCountDao psCountDao();

    abstract public ItemPaidHistoryDao itemPaidHistoryDao();


//    /**
//     * Migrate from:
//     * version 1 - using Room
//     * to
//     * version 2 - using Room where the {@link } has an extra field: addedDateStr
//     */
//    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE news "
//                    + " ADD COLUMN addedDateStr INTEGER NOT NULL DEFAULT 0");
//        }
//    };

    /* More migration write here */
}
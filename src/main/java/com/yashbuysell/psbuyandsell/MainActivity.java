package com.yashbuysell.psbuyandsell;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.login.LoginManager;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yashbuysell.psbuyandsell.databinding.ActivityMainBinding;
import com.yashbuysell.psbuyandsell.ui.chat.chat.ChatActivity;
import com.yashbuysell.psbuyandsell.ui.chat.chat.ChatFragment;
import com.yashbuysell.psbuyandsell.ui.city.selectedcity.SelectedCityFragment;
import com.yashbuysell.psbuyandsell.ui.common.NavigationController;
import com.yashbuysell.psbuyandsell.ui.common.PSAppCompactActivity;
import com.yashbuysell.psbuyandsell.utils.AppLanguage;
import com.yashbuysell.psbuyandsell.utils.Constants;
import com.yashbuysell.psbuyandsell.utils.MyContextWrapper;
import com.yashbuysell.psbuyandsell.utils.PSDialogMsg;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.viewmodel.common.NotificationViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.pscount.PSCountViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.user.UserViewModel;
import com.yashbuysell.psbuyandsell.viewobject.PSCount;
import com.yashbuysell.psbuyandsell.viewobject.User;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;
import com.yashbuysell.psbuyandsell.viewobject.holder.ItemParameterHolder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Provider;

import javax.inject.Inject;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
import static com.facebook.FacebookSdk.getApplicationContext;

public class MainActivity extends PSAppCompactActivity {


    //region Variables

    @Inject
    SharedPreferences pref;

    @Inject
    AppLanguage appLanguage;
    private Boolean notificationSetting = false;
    private String token = "";
    private UserViewModel userViewModel;
    private NotificationViewModel notificationViewModel;
    private User user;
    private PSDialogMsg psDialogMsg;
    public boolean isLogout = false;
    Drawable notificationIconDrawable = null;
    ActionBarDrawerToggle drawerToggle;
    public String selectedLocationId, selectedLocationName, selected_lat, selected_lng;
    private String loginUserId;
    private String locationId;
    private String locationName , lat,lng;
    final String CHANNEL_ID1 = "1" ;
    final String CHANNEL_ID2 = "2" ;

    public String notificationItemId, notificationBuyerId, notificationSellerId, notificationMsg, notificationFlag, notificationSenderName, notificationSenderUrl, userId;
    String receiverId = Constants.EMPTY_STRING;
    String receiverName = Constants.EMPTY_STRING;
    String receiverUrl = Constants.EMPTY_STRING;
    int requestCode = 0;
    String flag = Constants.EMPTY_STRING;
    private ConsentForm form;
    private String userIdToVerify;
    private GoogleSignInClient mGoogleSignInClient;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;
    public static String srToken ;
    public ActivityMainBinding binding;
    private PSCountViewModel psCountViewModel;
    private TextView messageNotificationTextView;
    private TextView notificationTextView;
    private ImageView notificationIconImageView;

    String notificationCount = "0";
    private int toolbarIconColor = Color.BLACK;
    //endregion


    //region Override Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_PSTheme);

        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
//                new IntentFilter("com.yashbuysell.psbuyandsell_FCM-message"));




        psCountViewModel = new ViewModelProvider(this, viewModelFactory).get(PSCountViewModel.class);

        LiveData<Resource<PSCount>> chatHistoryListData = psCountViewModel.getPSCount();

        if (chatHistoryListData != null) {

            chatHistoryListData.observe(this, psCountResource -> {
                if (psCountResource != null) {

//                    Utils.psLog("Got Data" + psCountResource.message + psCountResource.toString());

                    switch (psCountResource.status) {
                        case LOADING:
                            // Loading State
                            // Data are from Local DB

                            if (psCountResource.data != null) {
                                // Update the data
                                // Notification
                                notificationCount = psCountResource.data.blogNotiUnreadCount;

                                int sellerCount = Integer.valueOf(psCountResource.data.sellerUnreadCount);
                                int buyerCount = Integer.valueOf(psCountResource.data.buyerUnreadCount);



                                int totalCount = sellerCount + buyerCount;


                            } else {
                                messageNotificationTextView.setVisibility(View.GONE);
                                if (notificationTextView != null) {
                                    notificationTextView.setVisibility(View.GONE);
                                }
                            }

                            break;

                        case SUCCESS:
                            // Success State
                            // Data are from Server


                            break;

                        case ERROR:
                            // Error State
//

                            break;
                        default:
                            // Default

                            break;
                    }

                } else {

                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");

                    if (psCountViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        psCountViewModel.forceEndLoading = true;
                    }

                }

            });
        }











        initUIAndActions();

        initModels();

        initData();

        checkConsentStatus();



        if(loginUserId != null && loginUserId != "") {
            String userId  = loginUserId ;


            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w("tokenFailed", "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String usertoken = task.getResult();

                            // Log and toast
                            String msg = "userToken = " + usertoken;
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
                            DatabaseReference users = mDatabase.child(userId) ;
                            users.child("fcmtoken").setValue(usertoken) ;


                        }
                    });



        }








    }

//    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            String message = intent.getStringExtra("message");
//            Intent notificationIntent = new Intent(getApplicationContext(), ChatActivity.class);
//            PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);
//
//        }
//    };
//
    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(newBase);
        String CURRENT_LANG_CODE = preferences.getString(Constants.LANGUAGE_CODE, Config.DEFAULT_LANGUAGE);
        String CURRENT_LANG_COUNTRY_CODE = preferences.getString(Constants.LANGUAGE_COUNTRY_CODE, Config.DEFAULT_LANGUAGE_COUNTRY_CODE);

        super.attachBaseContext(MyContextWrapper.wrap(newBase, CURRENT_LANG_CODE, CURRENT_LANG_COUNTRY_CODE, true));
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshUserData();

    }

    public void refreshPSCount() {
        if (!loginUserId.isEmpty()) {
            psCountViewModel.setPsCountObj(loginUserId, token);
        }
    }

    public void refreshUserData() {
        try {
            loginUserId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);
        } catch (Exception e) {
            Utils.psErrorLog("", e);
        }

        refreshPSCount();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);

            if(fragment != null)
            {
                if(fragment instanceof SelectedCityFragment)
                {
                    String message = getBaseContext().getString(R.string.message__want_to_quit);
                    String okStr =getBaseContext().getString(R.string.message__ok_close);
                    String cancelStr = getBaseContext().getString(R.string.message__cancel_close);

                    psDialogMsg.showConfirmDialog(message, okStr,cancelStr );

                    psDialogMsg.show();

                    psDialogMsg.okButton.setOnClickListener(view -> {

                        finishActivity(1);
                        finish();

                    });
                    psDialogMsg.cancelButton.setOnClickListener(view -> psDialogMsg.cancel());

                }else {
                    setSelectMenu(R.id.nav_home);
                    showBottomNavigation();
                    binding.bottomNavigationView.setSelectedItemId(R.id.home_menu);
                    Utils.addToolbarScrollFlag(binding.toolbar);
//                    binding.addItemButton.setVisibility(View.VISIBLE);
                    setToolbarText(binding.toolbar, Constants.EMPTY_STRING);
                    navigationController.navigateToHome(MainActivity.this, false, selectedLocationId, selectedLocationName,false);

                }

            }
        }
        return  true;
    }

    //endregion


    //region Private Methods

    /**
     * Initialize Models
     */
    private void initModels() {

        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);
        notificationViewModel = new ViewModelProvider(this, viewModelFactory).get(NotificationViewModel.class);

    }


    /**
     * Show alert message to user.
     *
     * @param msg Message to show to user
     */
    private void showAlertMessage(String msg) {

        psDialogMsg.showNotiDefaultDialog(msg, getString(R.string.app__ok) );

        psDialogMsg.show();

        psDialogMsg.okButton.setOnClickListener(view -> psDialogMsg.cancel());

    }

    /**
     * Show alert message to user.
     *
     * @param message Message to show to user
     */
    private void showNotiMessage(String message) {
        psDialogMsg.showNotiDialog(message, getString(R.string.app__noti_open),getString(R.string.app__cancel) );

        psDialogMsg.show();

        psDialogMsg.okButton.setOnClickListener(view -> {
            psDialogMsg.cancel();
            navigationController.navigateToNotificationList(MainActivity.this);
        });
        psDialogMsg.cancelButton.setOnClickListener(view -> psDialogMsg.cancel());

    }

    private void showChatMessage(String message) {
        psDialogMsg.showNotiDialog(message, getString(R.string.app__noti_open),getString(R.string.app__cancel) );

        psDialogMsg.show();

        psDialogMsg.okButton.setOnClickListener(view -> {
            psDialogMsg.cancel();
            if (loginUserId.isEmpty()) {

                psDialogMsg.showInfoDialog(getString(R.string.error_message__login_first), getString(R.string.app__ok));
                psDialogMsg.show();
                psDialogMsg.okButton.setOnClickListener(v1 -> {
                    psDialogMsg.cancel();
                    navigationController.navigateToUserLoginActivity(MainActivity.this);
                });

            } else {
                navigationController.navigateToChatActivity(MainActivity.this, notificationItemId, receiverId, receiverName, "", "", "",
                        "", "", flag, notificationSenderUrl, requestCode);
            }
        });
        psDialogMsg.cancelButton.setOnClickListener(view -> psDialogMsg.cancel());

    }


    /**
     * This function will initialize UI and Event Listeners
     */
    private void initUIAndActions() {

        psDialogMsg = new PSDialogMsg(this, false);

        initToolbar(binding.toolbar, Constants.EMPTY_STRING);

        userIdToVerify = pref.getString(Constants.USER_ID_TO_VERIFY, Constants.EMPTY_STRING);

        initDrawerLayout();

        initNavigationView();

        navigationController.navigateToCityList(this);
        showBottomNavigation();

        setSelectMenu(R.id.nav_home);

        getIntentData();
        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) binding.bottomNavigationView.getChildAt(0);
        View bTMView = bottomNavigationMenuView.getChildAt(3);
        BottomNavigationItemView itemView = (BottomNavigationItemView) bTMView;

        View badgeView = LayoutInflater.from(this)
                .inflate(R.layout.notification_badge, itemView, true);
        messageNotificationTextView = badgeView.findViewById(R.id.notifications_badge);
        messageNotificationTextView.setVisibility(View.GONE);

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()) {
                case R.id.home_menu:
                    //layout_scrollFlags
                    Utils.addToolbarScrollFlag(binding.toolbar);
//                    binding.addItemButton.setVisibility(View.VISIBLE);
                    navigationController.navigateToHome(MainActivity.this, false, selectedLocationId, selectedLocationName,false);
                    setToolbarText(binding.toolbar, Constants.EMPTY_STRING);

                    break;
                case R.id.message_menu:
                    Utils.addToolbarScrollFlag(binding.toolbar);
                    binding.addItemButton.setVisibility(View.GONE);

                    Utils.navigateOnUserVerificationAndMessageFragment(pref,user,navigationController,this);

                    break;

                case R.id.interest_menu:
                    Utils.addToolbarScrollFlag(binding.toolbar);
                    binding.addItemButton.setVisibility(View.GONE);
                    navigationController.navigateToInterest(MainActivity.this);
                    setToolbarText(binding.toolbar, getString(R.string.menu__interest));

                    break;

                case R.id.addItemButton:
                    Utils.navigateOnUserVerificationActivity(userIdToVerify, loginUserId, psDialogMsg, this, navigationController, () -> {
                        try {
                            locationId = pref.getString(Constants.SELECTED_LOCATION_ID, Constants.EMPTY_STRING);
                            locationName = pref.getString(Constants.SELECTED_LOCATION_NAME, Constants.EMPTY_STRING);

                        } catch (Exception e) {
                            Utils.psErrorLog("", e);
                        }
                        navigationController.navigateToItemEntryActivity(MainActivity.this, Constants.ADD_NEW_ITEM, locationId, locationName, selected_lat , selected_lng);

                    });
//                    Utils.addToolbarScrollFlag(binding.toolbar);
//                    binding.addItemButton.setVisibility(View.GONE);
//                    navigationController.navigateToItemSell(pref,user,navigationController,this);
//                    setToolbarText(binding.toolbar, getString(R.string.menu__search));

                    break;

                case R.id.me_menu:
                    Utils.addToolbarScrollFlag(binding.toolbar);
                    binding.addItemButton.setVisibility(View.GONE);

                    Utils.navigateOnUserVerificationFragment(pref,user,navigationController,this);

                    break;

                default:


                    break;
            }

            return true;
        });

        binding.addItemButton.setTypeface(Utils.getTypeFace(this, Utils.Fonts.ROBOTO));
//        binding.addItemButton.setOnClickListener(v -> {
//
//            Utils.navigateOnUserVerificationActivity(userIdToVerify, loginUserId, psDialogMsg, this, navigationController, () -> {
//                try {
//                    locationId = pref.getString(Constants.SELECTED_LOCATION_ID, Constants.EMPTY_STRING);
//                    locationName = pref.getString(Constants.SELECTED_LOCATION_NAME, Constants.EMPTY_STRING);
//
//                } catch (Exception e) {
//                    Utils.psErrorLog("", e);
//                }
//
//                navigationController.navigateToItemEntryActivity(MainActivity.this, Constants.ADD_NEW_ITEM, locationId, locationName);
//
//            });
//
//        });

        // Configure Google Sign In
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    private void checkUserId() {
        if (!userId.isEmpty()) {
            if (userId.equals(notificationBuyerId)) {
                receiverId = notificationSellerId;
                receiverName = notificationSenderName;
                receiverUrl = notificationSenderUrl;
                requestCode = Constants.REQUEST_CODE__SELLER_CHAT_FRAGMENT;
                flag = Constants.CHAT_FROM_SELLER;
            }
            if (userId.equals(notificationSellerId)) {
                receiverId = notificationBuyerId;
                receiverName = notificationSenderName;
                receiverUrl = notificationSenderUrl;
                requestCode = Constants.REQUEST_CODE__BUYER_CHAT_FRAGMENT;
                flag = Constants.CHAT_FROM_BUYER;
            }

        }
    }

    private void getIntentData() {
        loginUserId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);

        selectedLocationId = getIntent().getStringExtra(Constants.SELECTED_LOCATION_ID);
        selectedLocationName = getIntent().getStringExtra(Constants.SELECTED_LOCATION_NAME);
        selected_lat = getIntent().getStringExtra(Constants.LAT);
        selected_lng = getIntent().getStringExtra(Constants.LNG);

        pref.edit().putString(Constants.SELECTED_LOCATION_ID, selectedLocationId).apply();
        pref.edit().putString(Constants.SELECTED_LOCATION_NAME, selectedLocationName).apply();
        pref.edit().putString(Constants.LAT, selected_lat).apply();
        pref.edit().putString(Constants.LNG, selected_lng).apply();

        notificationItemId = getIntent().getStringExtra(Constants.NOTI_ITEM_ID);
        notificationMsg = getIntent().getStringExtra(Constants.NOTI_MSG);
        notificationFlag = getIntent().getStringExtra(Constants.NOTI_FLAG);
        notificationBuyerId = getIntent().getStringExtra(Constants.NOTI_BUYER_ID);
        notificationSellerId = getIntent().getStringExtra(Constants.NOTI_SELLER_ID);
        notificationSenderName = getIntent().getStringExtra(Constants.NOTI_SENDER_NAME);
        notificationSenderUrl = getIntent().getStringExtra(Constants.NOTI_SENDER_URL);

        userId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);

        checkUserId();

        if(notificationFlag != null) {
            switch (notificationFlag) {
                case Constants.NOTI_CHAT:
                    showChatMessage(notificationMsg);

                    break;
                case Constants.NOTI_APPROVAL:
                    showAlertMessage(notificationMsg);

                    break;
                case Constants.NOTI_BROADCAST:
                    showNotiMessage(notificationMsg);

                    break;
                default:
                    showAlertMessage(notificationMsg);
                    break;
            }
        }

    }

    private void initDrawerLayout() {

        drawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.app__drawer_open, R.string.app__drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(false);
        drawerToggle.setHomeAsUpIndicator(R.drawable.baseline_menu_grey_24);

        drawerToggle.setToolbarNavigationClickListener(view -> binding.drawerLayout.openDrawer(GravityCompat.START));

        binding.drawerLayout.addDrawerListener(drawerToggle);
        binding.drawerLayout.post(drawerToggle::syncState);

    }

    private void initNavigationView() {

        if (binding.navView != null) {

            // Updating Custom Fonts
            Menu m = binding.navView.getMenu();
            try {
                if (m != null) {

                    for (int i = 0; i < m.size(); i++) {
                        MenuItem mi = m.getItem(i);

                        //for applying a font to subMenu ...
                        SubMenu subMenu = mi.getSubMenu();
                        if (subMenu != null && subMenu.size() > 0) {
                            for (int j = 0; j < subMenu.size(); j++) {
                                MenuItem subMenuItem = subMenu.getItem(j);

                                subMenuItem.setTitle(subMenuItem.getTitle());
                                // update font

                                subMenuItem.setTitle(Utils.getSpannableString(getBaseContext(), subMenuItem.getTitle().toString(), Utils.Fonts.ROBOTO));

                            }
                        }

                        mi.setTitle(mi.getTitle());
                        // update font

                        mi.setTitle(Utils.getSpannableString(getBaseContext(), mi.getTitle().toString(), Utils.Fonts.ROBOTO));
                    }
                }
            } catch (Exception e) {
                Utils.psErrorLog("Error in Setting Custom Font", e);
            }

            binding.navView.setNavigationItemSelectedListener(menuItem -> {
                navigationMenuChanged(menuItem);
                return true;
            });

        }

        if (binding.bottomNavigationView != null) {

            // Updating Custom Fonts
            Menu m = binding.bottomNavigationView.getMenu();
            try {

                for (int i = 0; i < m.size(); i++) {
                    MenuItem mi = m.getItem(i);

                    //for applying a font to subMenu ...
                    SubMenu subMenu = mi.getSubMenu();
                    if (subMenu != null && subMenu.size() > 0) {
                        for (int j = 0; j < subMenu.size(); j++) {
                            MenuItem subMenuItem = subMenu.getItem(j);

                            subMenuItem.setTitle(subMenuItem.getTitle());
                            // update font

                            subMenuItem.setTitle(Utils.getSpannableString(getBaseContext(), subMenuItem.getTitle().toString(), Utils.Fonts.ROBOTO));

                        }
                    }

                    mi.setTitle(mi.getTitle());
                    // update font

                    mi.setTitle(Utils.getSpannableString(getBaseContext(), mi.getTitle().toString(), Utils.Fonts.ROBOTO));
                }
            } catch (Exception e) {
                Utils.psErrorLog("Error in Setting Custom Font", e);
            }

            binding.navView.setNavigationItemSelectedListener(menuItem -> {
                navigationMenuChanged(menuItem);
                return true;
            });

        }

    }


    public void hideBottomNavigation() {
        binding.bottomNavigationView.setVisibility(View.GONE);
        binding.addItemButton.setVisibility(View.GONE);

        Utils.removeToolbarScrollFlag(binding.toolbar);

    }

    private void showBottomNavigation() {
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
//        binding.addItemButton.setVisibility(View.VISIBLE);

        Utils.addToolbarScrollFlag(binding.toolbar);

    }

    private void navigationMenuChanged(MenuItem menuItem) {
        openFragment(menuItem.getItemId());

        if (menuItem.getItemId() != R.id.nav_logout_login) {
            menuItem.setChecked(true);
            binding.drawerLayout.closeDrawers();
        }
    }

    public void setSelectMenu(int id) {
        binding.navView.setCheckedItem(id);
    }

    private int menuId = 0;

    /**
     * Open Fragment
     *
     * @param menuId To know which fragment to open.
     */
    private void openFragment(int menuId) {

        this.menuId = menuId;
        switch (menuId) {
            case R.id.nav_home:
            case R.id.nav_home_login:

                setToolbarText(binding.toolbar, Constants.EMPTY_STRING);
                navigationController.navigateToHome(this, false, selectedLocationId, selectedLocationName,false);
                showBottomNavigation();
                break;

            case R.id.nav_category:
            case R.id.nav_category_login:
                setToolbarText(binding.toolbar, getString(R.string.menu__category));
                navigationController.navigateToCategory(this);
                hideBottomNavigation();
                break;

            case R.id.nav_latest:
            case R.id.nav_latest_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__latest_item));
                navigationController.navigateToHomeLatestFiltering(MainActivity.this, new ItemParameterHolder().getRecentItem());
                hideBottomNavigation();
                break;

            case R.id.nav_popular:
            case R.id.nav_popular_login:
                setToolbarText(binding.toolbar, getString(R.string.menu__trending_item));
                navigationController.navigateToHomePopularFiltering(MainActivity.this, new ItemParameterHolder().getPopularItem());
                hideBottomNavigation();
                break;

            case R.id.nav_profile:
            case R.id.nav_profile_login:

                Utils.navigateOnUserVerificationFragment(pref,user,navigationController,this);

                Utils.psLog("nav_profile");

                hideBottomNavigation();

                break;
            case R.id.nav_favourite_news_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__favourite_items));
                navigationController.navigateToFavourite(this);
                Utils.psLog("nav_favourite_news");

                hideBottomNavigation();
                break;

            case R.id.nav_offer_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__offer_item));
                navigationController.navigateToOfferMessage(this);
                Utils.psLog("nav_offer_list");

                hideBottomNavigation();
                break;

            case R.id.nav_message_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__message));
                navigationController.navigateToMessage(this);
                Utils.psLog("nav_message_list");

                hideBottomNavigation();
                break;

            case R.id.nav_transaction_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__paid_ad_transaction));
                navigationController.navigateToTransactions(this);
                Utils.psLog("nav_transactions_news");

                hideBottomNavigation();
                break;

            case R.id.nav_user_history_login:
                setToolbarText(binding.toolbar, getString(R.string.menu__user_history));
                navigationController.navigateToHistory(this);
                Utils.psLog("nav_history");

                hideBottomNavigation();
                break;

            case R.id.nav_logout_login:

                psDialogMsg.showConfirmDialog(getString(R.string.edit_setting__logout_question), getString(R.string.app__ok), getString(R.string.app__cancel));

                psDialogMsg.show();

                psDialogMsg.okButton.setOnClickListener(view -> {

                    psDialogMsg.cancel();

                    hideBottomNavigation();

                    userViewModel.deleteUserLogin(user).observe(this, status -> {
                        if (status != null) {
                            this.menuId = 0;

                            setToolbarText(binding.toolbar, getString(R.string.app__app_name));

                            isLogout = true;

                            LoginManager.getInstance().logOut();
                        }
                    });

                    Utils.psLog("nav_logout_login");
                });

                psDialogMsg.cancelButton.setOnClickListener(view -> psDialogMsg.cancel());

                break;

            case R.id.nav_setting:
            case R.id.nav_setting_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__setting));
                navigationController.navigateToSetting(this);
                Utils.psLog("nav_setting");

                hideBottomNavigation();
                break;

            case R.id.nav_language:
            case R.id.nav_language_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__language));
                navigationController.navigateToLanguageSetting(this);
                Utils.psLog("nav_language");
                hideBottomNavigation();

                break;
            case R.id.nav_rate_this_app:
            case R.id.nav_rate_this_app_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__rate));
                navigationController.navigateToPlayStore(this);
                hideBottomNavigation();

                break;

            case R.id.nav_contact_us:
            case R.id.nav_contact_us_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__contact_us));
                navigationController.navigateToContactUs(this);
                hideBottomNavigation();

                break;

            case R.id.nav_privacy_policy:
            case R.id.nav_privacy_policy_login:

                setToolbarText(binding.toolbar, getString(R.string.menu__privacy_policy));
                navigationController.navigateToPrivacyPolicy(this);
                hideBottomNavigation();

                break;

        }

    }



    /**
     * Initialize Data
     */
    private void initData() {

        try {
            notificationSetting = pref.getBoolean(Constants.NOTI_SETTING, false);
            token = pref.getString(Constants.NOTI_TOKEN, "");

        } catch (NullPointerException ne) {
            Utils.psErrorLog("Null Pointer Exception.", ne);
        } catch (Exception e) {
            Utils.psErrorLog("Error in getting notification flag data.", e);
        }

        try {
            loginUserId = pref.getString(Constants.USER_ID, Constants.EMPTY_STRING);
        } catch (Exception e) {
            Utils.psErrorLog("", e);
        }

        userViewModel.getLoginUser().observe(this, data -> {

            if (data != null) {

                if (data.size() > 0) {
                    user = data.get(0).user;

                    pref.edit().putString(Constants.USER_ID, user.userId).apply();
                    pref.edit().putString(Constants.USER_NAME, user.userName).apply();
                    pref.edit().putString(Constants.USER_EMAIL, user.userEmail).apply();
                    pref.edit().putString(Constants.USER_PASSWORD, user.userEmail).apply();

                } else {
                    user = null;

                    pref.edit().remove(Constants.USER_ID).apply();
                    pref.edit().remove(Constants.USER_NAME).apply();
                    pref.edit().remove(Constants.USER_EMAIL).apply();
                    pref.edit().remove(Constants.USER_PASSWORD).apply();
                }

            } else {

                user = null;
                pref.edit().remove(Constants.USER_ID).apply();
                pref.edit().remove(Constants.USER_NAME).apply();
                pref.edit().remove(Constants.USER_EMAIL).apply();
                pref.edit().remove(Constants.USER_PASSWORD).apply();

            }
            updateMenu();

            if (isLogout) {
                navigationController.navigateToHome(MainActivity.this, false, selectedLocationId, selectedLocationName,true);
                showBottomNavigation();
                refreshUserData();
                isLogout = false;
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.revokeAccess()
                        .addOnCompleteListener(this, task -> {
                            // ...
                        });

                LoginManager.getInstance().logOut();

            }

        });


        registerNotificationToken(); // Just send "" because don't have token to sent. It will get token itself.

        psCountViewModel = new ViewModelProvider(this, viewModelFactory).get(PSCountViewModel.class);

        LiveData<Resource<PSCount>> chatHistoryListData = psCountViewModel.getPSCount();

        if (chatHistoryListData != null) {

            chatHistoryListData.observe(this, psCountResource -> {
                if (psCountResource != null) {

                    Utils.psLog("Got Data" + psCountResource.message + psCountResource.toString());

                    switch (psCountResource.status) {
                        case LOADING:
                            // Loading State
                            // Data are from Local DB

                            if (psCountResource.data != null) {
                                // Update the data
                                // Notification
                                notificationCount = psCountResource.data.blogNotiUnreadCount;
                                if (notificationTextView != null) {
                                    if (notificationCount.equals("0")) {
                                        notificationTextView.setVisibility(View.GONE);
                                    } else {
                                        notificationTextView.setVisibility(View.VISIBLE);

                                        int count = Integer.valueOf(notificationCount);
                                        if (count > 9) {
                                            notificationTextView.setText("9+");
                                        } else {
                                            notificationTextView.setText(String.valueOf(count));
                                        }
                                    }
                                }

                                Intent notificationIntent = new Intent(getApplicationContext(), ChatFragment.class);

                                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                                // Message
                                int sellerCount = Integer.valueOf(psCountResource.data.sellerUnreadCount);
                                int buyerCount = Integer.valueOf(psCountResource.data.buyerUnreadCount);
//                                Setting Notifications for buyers
//                                if(buyerCount > 0) {
//
//
//                                    // Resources r = getResources();
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                        CharSequence name = "Message";
//                                        String description = "Buyer sent you a message";
//                                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
//                                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID1, name, importance);
//                                        channel.setDescription(description);
//                                        AudioAttributes audioAttrib = new AudioAttributes.Builder()
//                                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                                                .build();
//                                        channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttrib);
//                                        // Register the channel with the system; you can't change the importance
//                                        // or other notification behaviors after this
//                                        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
//
//                                        notificationManager.createNotificationChannel(channel);
//                                    }
//
//
//                                    NotificationCompat.Builder buyernotification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID1)
//                                            .setSmallIcon(R.drawable.app_icon)
//                                            .setContentTitle(getString(R.string.chatNotificationTitle))
//                                            .setContentText(getString(R.string.chatNotificationBuyerDis))
//                                            .setContentIntent(pi)
//                                            .setAutoCancel(true);
//                                    ;
//
//                                    buyernotification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//
//                                    NotificationManager notificationManager1 = (NotificationManager) getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
//                                    notificationManager1.notify(1, buyernotification.build());
//
//                                }
////                                --------------------------------------
//
//                                //                                Setting Notifications for buyers
//
//                                if(sellerCount > 0) {
//
//
//                                    // Resources r = getResources();
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                        CharSequence name = "Message";
//                                        String description = "Buyer sent you a message";
//                                        int importance = NotificationManager.IMPORTANCE_DEFAULT;
//                                        NotificationChannel channel = new NotificationChannel(CHANNEL_ID2, name, importance);
//                                        channel.setDescription(description);
//                                        AudioAttributes audioAttrib = new AudioAttributes.Builder()
//                                                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
//                                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                                                .build();
//                                        channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttrib);
//                                        // Register the channel with the system; you can't change the importance
//                                        // or other notification behaviors after this
//                                        NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
//
//                                        notificationManager.createNotificationChannel(channel);
//                                    }
//
//
//                                    NotificationCompat.Builder sellernotification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID2)
//                                            .setSmallIcon(R.drawable.app_icon)
//                                            .setContentTitle(getString(R.string.chatNotificationTitle))
//                                            .setContentText(getString(R.string.chatNotificationSellerDis))
//                                            .setContentIntent(pi)
//                                            .setAutoCancel(true);
//                                    ;
//
//                                    sellernotification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//
//                                    NotificationManager notificationManager2 = (NotificationManager) getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
//                                    notificationManager2.notify(2, sellernotification.build());
//
//                                }

//                                ---------------------------------------------------------



                                int totalCount = sellerCount + buyerCount;
                                if (totalCount == 0) {
                                    messageNotificationTextView.setVisibility(View.GONE);
                                } else {
                                    messageNotificationTextView.setVisibility(View.VISIBLE);

                                    if (totalCount > 9) {
                                        messageNotificationTextView.setText("9+");
                                    } else {
                                        messageNotificationTextView.setText(String.valueOf(totalCount));
                                    }
                                }

                            } else {
                                messageNotificationTextView.setVisibility(View.GONE);
                                if (notificationTextView != null) {
                                    notificationTextView.setVisibility(View.GONE);
                                }
                            }

                            break;

                        case SUCCESS:
                            // Success State
                            // Data are from Server

                            if (psCountResource.data != null) {
                                // Update the data
                                // Notification
                                notificationCount = psCountResource.data.blogNotiUnreadCount;
                                if (notificationTextView != null) {
                                    if (notificationCount.equals("0")) {
                                        notificationTextView.setVisibility(View.GONE);
                                    } else {
                                        notificationTextView.setVisibility(View.VISIBLE);

                                        int count = Integer.valueOf(notificationCount);
                                        if (count > 9) {
                                            notificationTextView.setText("9+");
                                        } else {
                                            notificationTextView.setText(String.valueOf(count));
                                        }
                                    }
                                }


                                // Message
                                int sellerCount = Integer.valueOf(psCountResource.data.sellerUnreadCount);
                                int buyerCount = Integer.valueOf(psCountResource.data.buyerUnreadCount);
                                int totalCount = sellerCount + buyerCount;
                                if (totalCount == 0) {
                                    messageNotificationTextView.setVisibility(View.GONE);
                                } else {
                                    messageNotificationTextView.setVisibility(View.VISIBLE);

                                    if (totalCount > 9) {
                                        messageNotificationTextView.setText("9+");
                                    } else {
                                        messageNotificationTextView.setText(String.valueOf(totalCount));
                                    }
                                }

                            } else {
                                messageNotificationTextView.setVisibility(View.GONE);
                                if (notificationTextView != null) {
                                    notificationTextView.setVisibility(View.GONE);
                                }
                            }

                            psCountViewModel.setLoadingState(false);

                            break;

                        case ERROR:
                            // Error State
                            messageNotificationTextView.setVisibility(View.GONE);
                            if (notificationTextView != null) {
                                notificationTextView.setVisibility(View.GONE);
                            }

                            psCountViewModel.setLoadingState(false);

                            break;
                        default:
                            // Default

                            break;
                    }

                } else {

                    // Init Object or Empty Data
                    Utils.psLog("Empty Data");

                    if (psCountViewModel.offset > 1) {
                        // No more data for this list
                        // So, Block all future loading
                        psCountViewModel.forceEndLoading = true;
                    }

                }

            });
        }
    }

    /**
     * This function will change the menu based on the user is logged in or not.
     */
    private void updateMenu() {

        if (user == null) {

            binding.navView.getMenu().setGroupVisible(R.id.group_before_login, true);
            binding.navView.getMenu().setGroupVisible(R.id.group_after_login, false);

            setSelectMenu(R.id.nav_home);

        } else {
            binding.navView.getMenu().setGroupVisible(R.id.group_after_login, true);
            binding.navView.getMenu().setGroupVisible(R.id.group_before_login, false);

            if (menuId == R.id.nav_profile) {
                setSelectMenu(R.id.nav_profile_login);
            } else if (menuId == R.id.nav_profile_login) {
                setSelectMenu(R.id.nav_profile_login);
            } else {
                setSelectMenu(R.id.nav_home_login);
            }

        }


    }

    private void registerNotificationToken() {
        /*
         * Register Notification
         */

        // Check already submit or not
        // If haven't, submit to server
        if (!notificationSetting) {

            if (this.token.equals("")) {

                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {

                                return;
                            }

                            // Get new Instance ID token
                            if (task.getResult() != null) {
                                token = task.getResult().getToken();
                            }

                            notificationViewModel.registerNotification(getBaseContext(), Constants.PLATFORM, token,loginUserId);
                        });


            }
        } else {
            Utils.psLog("Notification Token is already registered. Notification Setting : true.");
        }
    }

    //endregion

    Menu menu = null;
    public void updateToolbarIconColor(int color) {
        toolbarIconColor = color;
        updateMenuIconColor(menu, color);
    }

    private void updateMenuIconColor(Menu menu, int color) {
        if(menu != null) {
            for (int i = 0; i < menu.size(); i++) {
                ImageView notiImageView = menu.getItem(i).getActionView().findViewById(R.id.notiImageView);
                if (notiImageView != null) {
                    notiImageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    public void updateMenuIconWhite() {
        drawerToggle.setHomeAsUpIndicator(R.drawable.baseline_menu_white_24);
    }

    public void updateMenuIconGrey() {
        drawerToggle.setHomeAsUpIndicator(R.drawable.baseline_menu_grey_24);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notification_menu, menu);

        this.menu = menu;
        updateMenuIconColor(menu, toolbarIconColor);

        for(int i = 0; i< menu.size(); i++) {
            notificationIconDrawable = menu.getItem(i).getIcon();
            notificationIconDrawable.setColorFilter(toolbarIconColor, PorterDuff.Mode.SRC_ATOP);
        }
        View itemView = menu.getItem(0).getActionView();

        notificationTextView = itemView.findViewById(R.id.txtCount);
        notificationTextView.setText(notificationCount);

        if (notificationCount.equals("0")) {
            notificationTextView.setVisibility(View.GONE);
        } else {
            notificationTextView.setVisibility(View.VISIBLE);

            int count = Integer.valueOf(notificationCount);
            if (count > 9) {
                notificationTextView.setText("9+");
            }
        }
        notificationIconImageView = itemView.findViewById(R.id.notiImageView);
        notificationIconImageView.setOnClickListener(
                v -> navigationController.navigateToNotificationList(this)
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_notification) {

            navigationController.navigateToNotificationList(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void checkConsentStatus() {

        // For Testing Open this
        ConsentInformation.getInstance(this).
                setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);

        ConsentInformation consentInformation = ConsentInformation.getInstance(this);
        String[] publisherIds = {getString(R.string.adview_publisher_key)};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.

                Utils.psLog(consentStatus.name());

                if (!consentStatus.name().equals(pref.getString(Config.CONSENTSTATUS_CURRENT_STATUS, Config.CONSENTSTATUS_CURRENT_STATUS)) || consentStatus.name().equals(Config.CONSENTSTATUS_UNKNOWN)) {
                    collectConsent();
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.

                Utils.psLog("Failed to update");
            }
        });
    }

    private void collectConsent() {
        URL privacyUrl = null;
        try {
            privacyUrl = new URL(Config.POLICY_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }

        form = new ConsentForm.Builder(this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.

                        Utils.psLog("Form loaded");

                        if (form != null) {
                            form.show();
                        }
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.

                        Utils.psLog("Form Opened");
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        // Consent form was closed.

                        pref.edit().putString(Config.CONSENTSTATUS_CURRENT_STATUS, consentStatus.name()).apply();
                        pref.edit().putBoolean(Config.CONSENTSTATUS_IS_READY_KEY, true).apply();
                        Utils.psLog("Form Closed");
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        // Consent form error.

                        pref.edit().putBoolean(Config.CONSENTSTATUS_IS_READY_KEY, false).apply();
                        Utils.psLog("Form Error " + errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .withAdFreeOption()
                .build();

        form.load();

    }
    //endregion

}

package com.yashbuysell.psbuyandsell.ui.item.detail;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.ads.AdRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.yashbuysell.psbuyandsell.Config;
import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.binding.FragmentDataBindingComponent;
import com.yashbuysell.psbuyandsell.databinding.FragmentItemBinding;
import com.yashbuysell.psbuyandsell.ui.common.PSFragment;
import com.yashbuysell.psbuyandsell.utils.AutoClearedValue;
import com.yashbuysell.psbuyandsell.utils.Constants;
import com.yashbuysell.psbuyandsell.utils.PSDialogMsg;
import com.yashbuysell.psbuyandsell.utils.Utils;
import com.yashbuysell.psbuyandsell.utils.ViewAnimationUtil;
import com.yashbuysell.psbuyandsell.viewmodel.aboutus.AboutUsViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.item.FavouriteViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.item.ItemViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.item.SpecsViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.item.TouchCountViewModel;
import com.yashbuysell.psbuyandsell.viewmodel.rating.RatingViewModel;
import com.yashbuysell.psbuyandsell.viewobject.Item;
import com.yashbuysell.psbuyandsell.viewobject.common.Resource;
import com.yashbuysell.psbuyandsell.viewobject.common.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemFragment extends PSFragment {

    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    private ItemViewModel itemViewModel;
    private TouchCountViewModel touchCountViewModel;
    private AboutUsViewModel aboutUsViewModel;
    private FavouriteViewModel favouriteViewModel;
    private SpecsViewModel specsViewModel;
    private RatingViewModel ratingViewModel;
    private PSDialogMsg psDialogMsg;
    private ImageView imageView;
    private String buyerPinCode ;
    private String sellerPinCode ;
    private String courierWeight ;
    public static String itemName ;
    public static String courierDetails = "courierDetails" ;
    private String token ;
    private String cAuthURL ;
    private String url ;
    private ArrayList<Float> rateList
            = new ArrayList<Float>() ;
    private AutoClearedValue<FragmentItemBinding> binding;
    private AutoClearedValue<ProgressDialog> prgDialog;

//    firebase
    DatabaseReference mDatabase , mData;


    //endregion

    //region Override Methods
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FragmentItemBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_item, container, false, dataBindingComponent);

        binding = new AutoClearedValue<>(this, dataBinding);
        AndroidNetworking.initialize(getApplicationContext());
        imageView = binding.get().coverUserImageView;


        return binding.get().getRoot();
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initUIAndActions() {

        psDialogMsg = new PSDialogMsg(getActivity(), false);
        psDialogMsg.showInfoDialog(getString(R.string.error_message__login_first), getString(R.string.app__ok));

        prgDialog = new AutoClearedValue<>(this, new ProgressDialog(getActivity()));
        prgDialog.get().setMessage((Utils.getSpannableString(getContext(), getString(R.string.message__please_wait), Utils.Fonts.MM_FONT)));
        prgDialog.get().setCancelable(false);

        if (Config.SHOW_ADMOB && connectivity.isConnected()) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            binding.get().adView.loadAd(adRequest);
        } else {
            binding.get().adView.setVisibility(View.GONE);
        }
        binding.get().courierPriceTextView.setVisibility(View.INVISIBLE);
        binding.get().phoneTextView.setOnClickListener(v -> {
            String number = binding.get().phoneTextView.getText().toString();
            if (!(number.trim().isEmpty() || number.trim().equals("-"))) {
                Utils.callPhone(this, number);
            }
        });

        binding.get().callButton.setOnClickListener(v -> {
            String number = binding.get().phoneTextView.getText().toString();
            if (!(number.trim().isEmpty() || number.trim().equals("-"))) {
                Utils.callPhone(this, number);
            }
        });

        binding.get().viewOnMapTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationController.navigateToMapActivity(getActivity(), itemViewModel.itemContainer.lng, itemViewModel.itemContainer.lat, Constants.MAP);
            }
        });

        binding.get().safetyTipButton.setOnClickListener(v -> navigationController.navigateToSafetyTipsActivity(getActivity()));

        binding.get().userCardView.setOnClickListener(v -> navigationController.navigateToUserDetail(getActivity(), itemViewModel.otherUserId, itemViewModel.otherUserName));

        binding.get().userNameActiveHourTextView.setOnClickListener(v -> navigationController.navigateToUserDetail(getActivity(), itemViewModel.otherUserId, itemViewModel.otherUserName));


        binding.get().checkAvailibilityButton.setOnClickListener(v -> {
            this.itemName = binding.get().itemNameTextView.getText().toString();
            this.mDatabase = FirebaseDatabase.getInstance().getReference(itemViewModel.itemId) ;

            this.mData = mDatabase.child(courierDetails);
           this.buyerPinCode = binding.get().checkAvailibilityEditText.getText().toString() ;
            Log.d("itemId" , "itemId at ItemFragment : " + itemViewModel.itemId ) ;
           mData.addValueEventListener(new ValueEventListener() {


               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
try{
    sellerPinCode = snapshot.child("seller_pincode").getValue().toString();

    courierWeight = snapshot.child("weight").getValue().toString() ;

    url = getString(R.string.courierCheck_serviceability);
    cAuthURL = getString(R.string.courierUser_auth) ;
    if(cAuthURL != Constants.EMPTY_STRING && cAuthURL != null) {
        AndroidNetworking.post(cAuthURL)
                .addHeaders("Content-Type", "application/json")
                .addBodyParameter("email", getString(R.string.courierUser_auth_email))
                .addBodyParameter("password", getString(R.string.courierUser_auth_pass))
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            token = response.get("token").toString();
                            Log.d("courierToken", response.get("token").toString());
                            if(token != null && token!="") {


                                AndroidNetworking.get(url+"?pickup_postcode="+sellerPinCode+"&delivery_postcode="+buyerPinCode+"&weight="+courierWeight+"&cod=0")
                                        .addHeaders("Content-Type", "application/json")
                                        .addHeaders("Authorization" ,"Bearer " + token)
                                        .setTag("test")
                                        .setPriority(Priority.MEDIUM)
                                        .build()
                                        .getAsJSONObject(new JSONObjectRequestListener() {
                                            @Override
                                            public void onResponse(JSONObject response) {


                                                Log.d("courierResponse", response.toString());
                                                try {

                                                    JSONObject data = (JSONObject) response.getJSONObject("data");
                                                    JSONArray availableCourierCompanies = (JSONArray)  data.get("available_courier_companies");

                                                    Log.d("courierResponse" , availableCourierCompanies.toString()) ;

                                                    for(int i = 0; i < availableCourierCompanies.length(); i++) {
                                                        JSONObject obj = (JSONObject) availableCourierCompanies.get(i);
                                                        String rate = obj.getString("rate");

                                                        Log.d("courierRate" , rate) ;
                                                        if(rate != null )
                                                            rateList.add(Float.parseFloat(rate));


                                                    }
                                                    if(rateList != null){
                                                        Collections.sort(rateList);

                                                        try {
                                                            mData.child("lowestRate").setValue(rateList.get(0).toString()) ;
                                                            binding.get().courierPriceTextView.setText("Delivery Charge : "  + "₹ " + rateList.get(0).toString() );

                                                            binding.get().courierPriceTextView.setVisibility(View.VISIBLE);

                                                        }
                                                        catch (Exception e){
                                                            Toast.makeText(getApplicationContext(), " Error Getting Courier Charge ", Toast.LENGTH_LONG ).show();

                                                            e.printStackTrace();
                                                        }

                                                    }

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }

                                            @Override
                                            public void onError(ANError error) {
                                                Log.d("courierError" , error.toString() ) ;
                                            }
                                        });

                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Try Again" , Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });


    }
    else {
        Toast.makeText(getApplicationContext(), "Try Again" , Toast.LENGTH_SHORT).show();
    }
}
catch (Exception e){

}




               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });


//                   HttpResponse<String> response = Unirest.get("https://apiv2.shiprocket.in/v1/external/courier/serviceability/")
//                           .header("Content-Type", "application/json")
//                           .header("Authorization", "Bearer " + token)
//                           .body("{\r\n    \"pickup_postcode\" : \"110030\",\r\n    \"delivery_postcode\" : \"122002\",\r\n    \"weight\" : \"2\", \r\n    \"cod\" : false \r\n     \r\n}")
//                           .asString();

            Log.d("courierDetails" , sellerPinCode + " " + buyerPinCode + " " + courierWeight);

//if((sellerPinCode != Constants.EMPTY_STRING && sellerPinCode != null) && (buyerPinCode != Constants.EMPTY_STRING && buyerPinCode != null) && (courierWeight != Constants.EMPTY_STRING && courierWeight != null) &&  (token != Constants.EMPTY_STRING   && token != null) ) {
//
//
//
//
//            List<String> jsonResponses = new ArrayList<>();
//            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//
//            JSONObject postData = new JSONObject();
//            if(buyerPinCode != null && sellerPinCode != null) {
//                Log.d("courierSellerPinCode", "sellerPinCode" + "  " + Integer.parseInt(sellerPinCode));
//                Log.d("courierSellerPinCode", "buyerPinCode" + "  " + Integer.parseInt(buyerPinCode));
//
//            try {
//
//                postData.put("pickup_postcode", Integer.parseInt(sellerPinCode));
//                postData.put("delivery_postcode", Integer.parseInt(buyerPinCode));
//                postData.put("weight" , courierWeight) ;
//                postData.put("cod" , false) ;
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//                Log.d("courierCrash" , e.toString());
//            }
//            }
//
//
//            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, postData, new Response.Listener<JSONObject>() {
//
//                @Override
//                public void onResponse(JSONObject response) {
//                    try {
//                        Log.d("courierResponse" , String.valueOf(response)) ;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Log.d("courierResponse" , e.toString()) ;
//                    }
//                }
//            }, new Response.ErrorListener() {
//
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    error.printStackTrace();
//                    Log.d("courierError" , error.toString()) ;
//                }
//
//            }){
//                @Override
//                public Map<String, String> getHeaders() {
//                    Map<String, String> params = new HashMap<String, String>();
//                    try {
//                        params.put("Content-Type", "application/json");
//                        params.put("Authorization", "Bearer " + token);
//                    }
//                    catch (Exception e) {
//                        Log.d("courierError" , e.toString()) ;
//                    }
//
//                    return params;
//                }
//
//                       @Override
//                       protected Map<String, String> getParams() {
//                           Map<String, String> params = new HashMap<String, String>();
//
//
//                            if (sellerPinCode != null && buyerPinCode != null){
//                                params.put("pickup_postcode", sellerPinCode);
//                                params.put("delivery_postcode", buyerPinCode);
//                                params.put("weight", courierWeight);
//                                params.put("cod" ,"false") ;
//                            }
//
//
//
//                           return params;
//                       }
//            } ;
//    requestQueue.add(jsonObjectRequest);
//}
//else {
//    Toast.makeText(getApplicationContext() , "Try Again" , Toast.LENGTH_SHORT).show();
//}
//////
////
////
////
//
////
//
//






//            mData = mDatabase.child(itemName) ;
        });
        binding.get().menuImageView.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(getActivity(), binding.get().menuImageView);
            popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {

                if (item.getTitle().toString().equals(getString(R.string.menu__report_item))) {

                    itemViewModel.setReportItemStatusObj(itemViewModel.itemId, loginUserId);
                } else {//share

                    Bitmap bitmap = getBitmapFromView(getCurrentImageView());
                    shareImageUri(saveImageExternal(bitmap), binding.get().itemNameTextView.getText().toString());
                }
                return false;
            });

            popupMenu.show();

        });


        binding.get().countPhotoConstraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationController.navigateToGalleryActivity(ItemFragment.this.getActivity(), Constants.IMAGE_TYPE_PRODUCT, itemViewModel.itemId);
            }
        });

        binding.get().coverUserImageView.setOnClickListener(v -> navigationController.navigateToGalleryActivity(ItemFragment.this.getActivity(), Constants.IMAGE_TYPE_PRODUCT, itemViewModel.itemId));

        binding.get().editButton.setOnClickListener(v -> navigationController.navigateToItemEntryActivity(getActivity(), itemViewModel.itemId, selected_location_id, selected_location_name, selectedLat , selectedLng));

        binding.get().soldTextView.setOnClickListener(v -> {
            if (binding.get().soldTextView.getText().equals(getResources().getString(R.string.item_detail__mark_sold))) {
                psDialogMsg.showConfirmDialog(getString(R.string.item_detail__confirm_sold_out), getString(R.string.app__ok), getString(R.string.message__cancel_close));
                psDialogMsg.show();

                psDialogMsg.okButton.setOnClickListener(v12 -> {
                    itemViewModel.setMarkAsSoldOutItemObj(itemViewModel.itemId, loginUserId);

                    psDialogMsg.cancel();
                });

                psDialogMsg.cancelButton.setOnClickListener(v1 -> psDialogMsg.cancel());

            }
        });

        binding.get().deleteButton.setOnClickListener(v -> {
            psDialogMsg.showConfirmDialog(getString(R.string.item_detail__confirm_delete), getString(R.string.app__ok), getString(R.string.message__cancel_close));
            psDialogMsg.show();

            psDialogMsg.okButton.setOnClickListener(v12 -> {
                itemViewModel.setDeleteItemObj(itemViewModel.itemId, loginUserId);

                psDialogMsg.cancel();
            });

            psDialogMsg.cancelButton.setOnClickListener(v1 -> psDialogMsg.cancel());

        });

        binding.get().ratingBarInformation.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                navigationController.navigateToRatingList(ItemFragment.this.getActivity(), binding.get().getItem().user.userId);
            }
            return true;
        });

        binding.get().backImageView.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        binding.get().statisticDownImageView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(v);
            if (show) {
                ViewAnimationUtil.expand(binding.get().viewConstraintLayout);
                ViewAnimationUtil.expand(binding.get().reviewConstraintLayout);
            } else {
                ViewAnimationUtil.collapse(binding.get().viewConstraintLayout);
                ViewAnimationUtil.collapse(binding.get().reviewConstraintLayout);
            }
        });

        binding.get().statisticTextView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(binding.get().statisticDownImageView);
            if (show) {
                ViewAnimationUtil.expand(binding.get().viewConstraintLayout);
                ViewAnimationUtil.expand(binding.get().reviewConstraintLayout);
            } else {
                ViewAnimationUtil.collapse(binding.get().viewConstraintLayout);
                ViewAnimationUtil.collapse(binding.get().reviewConstraintLayout);
            }
        });

        binding.get().locationTitleDownImageView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(v);
            if (show) {
                //if add more field ,wrap with constraint
                ViewAnimationUtil.expand(binding.get().viewOnMapTextView);
            } else {
                ViewAnimationUtil.collapse(binding.get().viewOnMapTextView);

            }
        });

        binding.get().locationTitleTextView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(binding.get().locationTitleDownImageView);
            if (show) {
                ViewAnimationUtil.expand(binding.get().viewOnMapTextView);
            } else {
                ViewAnimationUtil.collapse(binding.get().viewOnMapTextView);
            }
        });

        binding.get().meetTheSellerDownImageView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(v);
            if (show) {
                ViewAnimationUtil.expand(binding.get().meetTheSellerConstraintLayout);
            } else {
                ViewAnimationUtil.collapse(binding.get().meetTheSellerConstraintLayout);
            }
        });

        binding.get().meetTheSellerTextView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(binding.get().meetTheSellerDownImageView);
            if (show) {
                ViewAnimationUtil.expand(binding.get().meetTheSellerConstraintLayout);
            } else {
                ViewAnimationUtil.collapse(binding.get().meetTheSellerConstraintLayout);
            }
        });

        binding.get().promoteDownImageView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(v);
            if (show) {
                ViewAnimationUtil.expand(binding.get().itemPromoteConstraintLayout);
            } else {
                ViewAnimationUtil.collapse(binding.get().itemPromoteConstraintLayout);
            }
        });

        binding.get().promoteTitleTextView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(binding.get().promoteDownImageView);
            if (show) {
                ViewAnimationUtil.expand(binding.get().itemPromoteConstraintLayout);
            } else {
                ViewAnimationUtil.collapse(binding.get().itemPromoteConstraintLayout);
            }
        });

        binding.get().gettingThisDownImageView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(v);
            if (show) {
                ViewAnimationUtil.expand(binding.get().meetTextView);
                ViewAnimationUtil.expand(binding.get().addressTextView);
                ViewAnimationUtil.expand(binding.get().imageView25);
            } else {
                ViewAnimationUtil.collapse(binding.get().meetTextView);
                ViewAnimationUtil.collapse(binding.get().addressTextView);
                ViewAnimationUtil.collapse(binding.get().imageView25);
            }
        });

        binding.get().safetyTipsDownImageView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(v);
            if (show) {
                ViewAnimationUtil.expand(binding.get().safetyTipButton);
                ViewAnimationUtil.expand(binding.get().safetyTextView);

            } else {
                ViewAnimationUtil.collapse(binding.get().safetyTipButton);
                ViewAnimationUtil.collapse(binding.get().safetyTextView);

            }
        });

        binding.get().gettingThisTextView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(binding.get().gettingThisDownImageView);
            if (show) {
                ViewAnimationUtil.expand(binding.get().meetTextView);
                ViewAnimationUtil.expand(binding.get().addressTextView);
                ViewAnimationUtil.expand(binding.get().imageView25);
            } else {
                ViewAnimationUtil.collapse(binding.get().meetTextView);
                ViewAnimationUtil.collapse(binding.get().addressTextView);
                ViewAnimationUtil.collapse(binding.get().imageView25);
            }
        });

        binding.get().safetyTitleTextView.setOnClickListener(v -> {
            boolean show = Utils.toggleUpDownWithAnimation(binding.get().safetyTipsDownImageView);
            if (show) {
                ViewAnimationUtil.expand(binding.get().safetyTipButton);
                ViewAnimationUtil.expand(binding.get().safetyTextView);

            } else {
                ViewAnimationUtil.collapse(binding.get().safetyTipButton);
                ViewAnimationUtil.collapse(binding.get().safetyTextView);

            }
        });

        binding.get().favouriteImageView.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {

                Item item = binding.get().getItem();
                if (item != null) {
                    favFunction(item, likeButton);
                }

            }

            @Override
            public void unLiked(LikeButton likeButton) {

                Item item = binding.get().getItem();
                if (item != null) {
                    unFavFunction(item, likeButton);
                }
            }
        });

        binding.get().chatButton.setOnClickListener(v -> {

            Utils.navigateOnUserVerificationActivity(userIdToVerify, loginUserId, psDialogMsg, ItemFragment.this.getActivity(), navigationController, new Utils.NavigateOnUserVerificationActivityCallback() {
                @Override
                public void onSuccess() {

                    if (itemViewModel.currentItem.user.userId.isEmpty()) {
                        psDialogMsg.showWarningDialog(getString(R.string.item_entry_user_not_exit), getString(R.string.app__ok));
                        psDialogMsg.show();
                    } else {
                        navigationController.navigateToChatActivity(getActivity(),
                                itemViewModel.currentItem.id,
                                itemViewModel.currentItem.user.userId,
                                itemViewModel.currentItem.user.userName,
                                itemViewModel.currentItem.defaultPhoto.imgPath,
                                itemViewModel.currentItem.title,
                                itemViewModel.currentItem.itemCurrency.currencySymbol,
                                itemViewModel.currentItem.price,
                                itemViewModel.currentItem.itemCondition.name,
                                Constants.CHAT_FROM_SELLER,
                                itemViewModel.currentItem.user.userProfilePhoto,
                                0
                        );
                    }
                }
            });

//            if (userIdToVerify.isEmpty()) {
//                if (loginUserId.equals("")) {
//                    navigationController.navigateToUserLoginActivity(getActivity());
//                } else if (itemViewModel.currentItem.user.userId.isEmpty()) {
//                    psDialogMsg.showWarningDialog(getString(R.string.item_entry_user_not_exit), getString(R.string.app__ok));
////                    psDialogMsg.show();
//                } else {
//                    navigationController.navigateToChatActivity(getActivity(),
//                            itemViewModel.currentItem.id,
//                            itemViewModel.currentItem.user.userId,
//                            itemViewModel.currentItem.user.userName,
//                            itemViewModel.currentItem.defaultPhoto.imgPath,
//                            itemViewModel.currentItem.title,
//                            itemViewModel.currentItem.itemCurrency.currencySymbol,
//                            itemViewModel.currentItem.price,
//                            itemViewModel.currentItem.itemCondition.name,
//                            Constants.CHAT_FROM_SELLER,
//                            itemViewModel.currentItem.user.userProfilePhoto,
//                            0
//                    );
//                }
//            } else {
//
//                navigationController.navigateToVerifyEmailActivity(getActivity());
//            }

        });

        binding.get().ratingBarInformation.setOnClickListener(v -> navigationController.navigateToRatingList(ItemFragment.this.getActivity(), binding.get().getItem().user.userId));

//        binding.get().promoteButton.setOnClickListener(v -> {
//
//            Utils.navigateOnUserVerificationActivity(userIdToVerify, loginUserId, psDialogMsg, getActivity(), navigationController, new Utils.NavigateOnUserVerificationActivityCallback() {
//                @Override
//                public void onSuccess() {
//                    navigationController.navigateToItemPromoteActivity(ItemFragment.this.getActivity(), Constants.ADD_NEW_ITEM);
//                }
//            });
//
//        });

//        binding.get().promoteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navigationController.navigateToItemPromoteActivity(ItemFragment.this.getActivity(), itemViewModel.itemId);
//            }
//        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        String number = binding.get().phoneTextView.getText().toString();
        if (!(number.trim().isEmpty() || number.trim().equals("-"))) {
            Utils.phoneCallPermissionResult(requestCode, grantResults, this, number);
        }
    }

    @Override
    protected void initViewModels() {

        itemViewModel = new ViewModelProvider(this, viewModelFactory).get(ItemViewModel.class);
        ratingViewModel = new ViewModelProvider(this, viewModelFactory).get(RatingViewModel.class);
        specsViewModel = new ViewModelProvider(this, viewModelFactory).get(SpecsViewModel.class);
        favouriteViewModel = new ViewModelProvider(this, viewModelFactory).get(FavouriteViewModel.class);
        touchCountViewModel = new ViewModelProvider(this, viewModelFactory).get(TouchCountViewModel.class);
        aboutUsViewModel = new ViewModelProvider(this, viewModelFactory).get(AboutUsViewModel.class);

    }

    @Override
    protected void initAdapters() {

    }

    @Override
    protected void initData() {

        getIntentData();

        getItemDetail();


        getMarkAsSoldOutData();
        callTouchCount();
        getTouchCount();
        getFavData();
//

//
        getFavData();

        getReportItemStatus();

        getDeleteItemStatus();

        getAboutUsData();
    }

    private void getAboutUsData() {
        aboutUsViewModel.setAboutUsObj("about us");
        aboutUsViewModel.getAboutUsData().observe(this, resource -> {

            if (resource != null) {

                switch (resource.status) {
                    case LOADING:
                        // Loading State
                        // Data are from Local DB

                        if (resource.data != null) {

                            fadeIn(binding.get().getRoot());

                        }
                        break;
                    case SUCCESS:
                        // Success State
                        // Data are from Server

                        if (resource.data != null) {

                            binding.get().safetyTextView.setText(resource.data.safetyTips);
                        }

                        break;
                    case ERROR:
                        // Error State

                        break;
                    default:
                        // Default

                        break;
                }

            } else {

                // Init Object or Empty Data
                Utils.psLog("Empty Data");

            }


            // we don't need any null checks here for the adapter since LiveData guarantees that
            // it won't call us if fragment is stopped or not started.
            if (resource != null) {
                Utils.psLog("Got Data Of About Us.");


            } else {
                //noinspection Constant Conditions
                Utils.psLog("No Data of About Us.");
            }
        });
    }

    private void getDeleteItemStatus() {
        itemViewModel.getDeleteItemStatus().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:

                        //add offer text
                        Toast.makeText(getContext(), "Success Delete this Item", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().finish();
                        }

                        break;

                    case ERROR:
                        Toast.makeText(getContext(), "Fail Delete this item", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void getMarkAsSoldOutData() {
        LiveData<Resource<Item>> itemDetail = itemViewModel.getMarkAsSoldOutItemData();
        if (itemDetail != null) {
            itemDetail.observe(this, listResource -> {
                if (listResource != null) {

                    switch (listResource.status) {
                        case LOADING:
                            // Loading State
                            // Data are from Local DB

                            if (listResource.data != null) {
                                //fadeIn Animation

                                fadeIn(binding.get().getRoot());

                            }

                            break;

                        case SUCCESS:
                            // Success State
                            // Data are from Server

                            if (listResource.data != null) {

                                Toast.makeText(getContext(), "success make sold out", Toast.LENGTH_SHORT).show();

                            }

                            itemViewModel.setLoadingState(false);

                            break;

                        case ERROR:

                            // Error State
                            itemViewModel.setLoadingState(false);
//                            binding.get().markAsSoldButton.setVisibility(View.VISIBLE);

                            break;

                        default:
                            // Default

                            break;
                    }

                } else {

                    itemViewModel.setLoadingState(false);

                }
            });
        }
    }

    private void getReportItemStatus() {

        itemViewModel.getReportItemStatusData().observe(this, result -> {

            if (result != null) {
                switch (result.status) {
                    case SUCCESS:

                        //add offer text
                        Toast.makeText(getContext(), "Success Report this Item", Toast.LENGTH_SHORT).show();

                        break;

                    case ERROR:
                        Toast.makeText(getContext(), "Fail Report this item", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private void getTouchCount() {

        //get touch count post method
        touchCountViewModel.getTouchCountPostData().observe(this, result -> {
            if (result != null) {
                if (result.status == Status.SUCCESS) {
                    if (ItemFragment.this.getActivity() != null) {
                        Utils.psLog(result.status.toString());
                    }

                } else if (result.status == Status.ERROR) {
                    if (ItemFragment.this.getActivity() != null) {
                        Utils.psLog(result.status.toString());
                    }
                }
            }
        });
    }

    private void getFavData() {
        //get favourite post method
        favouriteViewModel.getFavouritePostData().observe(this, result -> {
            if (result != null) {
                if (result.status == Status.SUCCESS) {
                    if (this.getActivity() != null) {
                        Utils.psLog(result.status.toString());
                        favouriteViewModel.setLoadingState(false);
                        itemViewModel.setItemDetailObj(itemViewModel.itemId, itemViewModel.historyFlag, loginUserId);
                    }

                } else if (result.status == Status.ERROR) {
                    if (this.getActivity() != null) {
                        Utils.psLog(result.status.toString());
                        favouriteViewModel.setLoadingState(false);
                    }
                }
            }
        });
    }

    private void getIntentData() {
        try {
            if (getActivity() != null) {
                if (getActivity().getIntent().getExtras() != null) {
                    itemViewModel.itemId = getActivity().getIntent().getExtras().getString(Constants.ITEM_ID);
                    itemViewModel.historyFlag = getActivity().getIntent().getExtras().getString(Constants.HISTORY_FLAG);

                }
            }
        } catch (Exception e) {
            Utils.psErrorLog("", e);
        }
    }

    private void shareImageUri(Uri uri, String titleName) {

        new Thread(() -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_TEXT, titleName);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                Objects.requireNonNull(getContext()).startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Bitmap getBitmapFromView(ImageView view) {
        Drawable drawable = view.getDrawable();

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private ImageView getCurrentImageView() {
        return imageView;
    }

    private Uri saveImageExternal(Bitmap image) {
        Uri uri = null;
        try {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            File file = new File(Objects.requireNonNull(getContext()).getExternalFilesDir(Environment.DIRECTORY_PICTURES), "to-share.png");
            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.close();
            uri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    private void getItemDetail() {

        itemViewModel.setItemDetailObj(itemViewModel.itemId, itemViewModel.historyFlag, loginUserId);

        LiveData<Resource<Item>> itemDetail = itemViewModel.getItemDetailData();
        if (itemDetail != null) {
            itemDetail.observe(this, listResource -> {
                if (listResource != null) {

                    switch (listResource.status) {
                        case LOADING:
                            // Loading State
                            // Data are from Local DB

                            if (listResource.data != null) {
                                //fadeIn Animation
                                fadeIn(binding.get().getRoot());

                                itemViewModel.itemContainer = listResource.data;

                                specsViewModel.setSpecsListObj(itemViewModel.itemId);
                                itemViewModel.userId = listResource.data.user.userId;
                                replaceItemData(listResource.data);
                                showOrHide(listResource.data);
                                bindingRatingData(listResource.data);
                                bindingCountData(listResource.data);
                                bindingFavoriteData(listResource.data);
                                bindingCategoryNameAndSubCategoryName(listResource.data);
                                bindingPriceWithCurrencySymbol(listResource.data);
                                bindingPhoneNo(listResource.data);
                                bindingFavouriteCount(listResource.data);
                                bindingSoldData(listResource.data);
                                bindindAddedDateUserName(listResource.data);
                                bindingBusinessMode(listResource.data);
                                bindingBottomConstraintLayout(listResource.data);
                                bindingPhotoCount(listResource.data);
                                bindingVerifiedData(listResource.data);
                                bindingItemDialOption(listResource.data);
                                bindingPromoteConstraintLayout(listResource.data);
                                bindingPaidStatus(listResource.data);
                            }

                            break;

                        case SUCCESS:
                            // Success State
                            // Data are from Server

                            if (listResource.data != null) {

                                specsViewModel.setSpecsListObj(itemViewModel.itemId);

                                itemViewModel.itemContainer = listResource.data;

                                // Update the data
                                replaceItemData(listResource.data);
                                showOrHide(listResource.data);
                                itemViewModel.userId = listResource.data.user.userId;
//                                if (itemViewModel.userId != null){
//                                callTouchCount();
//                                }
                                bindingRatingData(listResource.data);
                                bindingCountData(listResource.data);
                                bindingFavoriteData(listResource.data);
                                bindingCategoryNameAndSubCategoryName(listResource.data);
                                bindingPriceWithCurrencySymbol(listResource.data);
                                bindingPhoneNo(listResource.data);
                                bindingFavouriteCount(listResource.data);
                                bindingSoldData(listResource.data);
                                bindindAddedDateUserName(listResource.data);
                                bindingBusinessMode(listResource.data);
                                bindingBottomConstraintLayout(listResource.data);
                                bindingPhotoCount(listResource.data);
                                bindingVerifiedData(listResource.data);
                                bindingItemDialOption(listResource.data);
                                itemViewModel.locationId = listResource.data.itemLocation.id;
                                itemViewModel.locationName = listResource.data.itemLocation.name;
                                itemViewModel.otherUserId = listResource.data.user.userId;
                                itemViewModel.otherUserName = listResource.data.user.userName;
                                bindingPromoteConstraintLayout(listResource.data);
                                bindingPaidStatus(listResource.data);
//                                checkText(listResource.data);

                            }

                            itemViewModel.setLoadingState(false);

                            break;

                        case ERROR:

                            // Error State
                            itemViewModel.setLoadingState(false);

                            break;

                        default:
                            // Default

                            break;
                    }

                } else {

                    itemViewModel.setLoadingState(false);

                }
            });
        }


        //get rating post method
        ratingViewModel.getRatingPostData().observe(this, result -> {
            if (result != null) {
                if (result.status == Status.SUCCESS) {
                    if (ItemFragment.this.getActivity() != null) {
                        Utils.psLog(result.status.toString());
                        ratingViewModel.setLoadingState(false);
                        prgDialog.get().dismiss();
                        prgDialog.get().cancel();
                        navigationController.navigateToRatingList(ItemFragment.this.getActivity(), binding.get().getItem().user.userId);
                    }

                } else if (result.status == Status.ERROR) {
                    if (ItemFragment.this.getActivity() != null) {
                        Utils.psLog(result.status.toString());
                        ratingViewModel.setLoadingState(false);
                        prgDialog.get().dismiss();
                        prgDialog.get().cancel();
                    }
                }
            }
        });


        //load product specs

//        LiveData<List<ItemSpecs>> itemSpecs = specsViewModel.getSpecsListData();
//        if(itemSpecs != null) {
//            itemSpecs.observe(this, listResource -> {
//                if (listResource != null && listResource.size() > 0) {
//
//                    ItemFragment.this.replaceItemSpecsData(listResource);
//                    specsViewModel.isSpecsData = true;
//
//                } else {
//                    specsViewModel.isSpecsData = false;
//                    binding.get().fiveCardView.setVisibility(View.GONE);
//
//                }
//                showOrHideSpecs();
//            });
//        }
//    }

//    private void bindingMapData(Item item) {
//        itemViewModel.latValue = item.lat;
//        itemViewModel.lngValue = item.lng;

//        //load product specs
//
//        LiveData<List<ItemSpecs>> itemSpecs = specsViewModel.getSpecsListData();
//        if(itemSpecs != null) {
//            itemSpecs.observe(this, listResource -> {
//                if (listResource != null && listResource.size() > 0) {
//
//                    ItemFragment.this.replaceItemSpecsData(listResource);
//                    specsViewModel.isSpecsData = true;
//
//                } else {
//                    specsViewModel.isSpecsData = false;
//                    binding.get().fiveCardView.setVisibility(View.GONE);
//
//                }
//                showOrHideSpecs();
//            });
//        }

    }

    private void callTouchCount() {
        if (!loginUserId.equals(itemViewModel.userId)) {
            if (connectivity.isConnected()) {
                touchCountViewModel.setTouchCountPostDataObj(loginUserId, itemViewModel.itemId);
            }
        }
    }

    private void replaceItemData(Item item) {
        itemViewModel.currentItem = item;
        binding.get().setItem(item);

    }

    private void bindingCountData(Item item) {
        binding.get().favouriteCountTextView.setText(getString(R.string.item_detail__fav_count, item.favouriteCount));
        binding.get().viewCountTextView.setText(getString(R.string.item_detail__view_count, item.touchCount));
    }

    private void bindingCategoryNameAndSubCategoryName(Item item) {
        String categoryName = item.category.name;
        String subCategoryName = item.subCategory.name;

        if (categoryName.equals("")) {
            binding.get().categoryAndSubCategoryTextView.setText(subCategoryName);
        } else if (subCategoryName.equals("")) {
            binding.get().categoryAndSubCategoryTextView.setText(categoryName);
        } else {
            String name = categoryName + " / " + subCategoryName;
            binding.get().categoryAndSubCategoryTextView.setText(name);
        }

    }

    private void bindingPriceWithCurrencySymbol(Item item) {
        String currencySymbol = item.itemCurrency.currencySymbol;
        String price;
        try {
            price = Utils.format(Double.parseDouble(item.price));
        } catch (Exception e) {
            price = item.price;
        }

        String currencyPrice;
        if (Config.SYMBOL_SHOW_FRONT) {
            currencyPrice = currencySymbol + " " + price;
        } else {
            currencyPrice = price + " " + currencySymbol;
        }
        binding.get().priceTextView.setText(currencyPrice);
    }

    private void bindingPhoneNo(Item item) {
        if (item.user.userPhone.trim().isEmpty()) {
            binding.get().callButton.setVisibility(View.GONE);
        } else {
            binding.get().callButton.setVisibility(View.VISIBLE);
        }
    }

    private void bindingRatingData(Item item) {

        if (item.user.overallRating.isEmpty()) {
            binding.get().ratingCountTextView.setText(getString(R.string.item_detail__rating));
        } else {
            binding.get().ratingCountTextView.setText(item.user.overallRating);
        }

        if (!item.user.overallRating.isEmpty()) {
            binding.get().ratingBarInformation.setRating(item.user.ratingDetails.totalRatingValue);
        }

        String ratingCount = "( " + item.user.ratingCount + " )";

        binding.get().ratingInfoTextView.setText(ratingCount);

    }

    private void bindingVerifiedData(Item item) {

        if (item.user.emailVerify.equals("1")) {
            binding.get().mailImageView.setVisibility(View.VISIBLE);
        } else {
            binding.get().mailImageView.setVisibility(View.GONE);
        }

        if (item.user.facebookVerify.equals("1")) {
            binding.get().facebookImageView.setVisibility(View.VISIBLE);
        } else {
            binding.get().facebookImageView.setVisibility(View.GONE);
        }

        if (item.user.phoneVerify.equals("1")) {
            binding.get().phoneImage.setVisibility(View.VISIBLE);
        } else {
            binding.get().phoneImage.setVisibility(View.GONE);
        }

        if (item.user.googleVerify.equals("1")) {
            binding.get().googleImage.setVisibility(View.VISIBLE);
        } else {
            binding.get().googleImage.setVisibility(View.GONE);
        }
    }

    private void bindingFavouriteCount(Item item) {
        String favouriteCount = item.favouriteCount + " " + getString(R.string.item_detail__like);
        binding.get().likesTextView.setText(favouriteCount);

    }

    private void bindingFavoriteData(Item item) {
        if (item.isFavourited.equals(Constants.ONE)) {
            binding.get().favouriteImageView.setLiked(true);
        } else {
            binding.get().favouriteImageView.setLiked(false);
        }
    }

    private void bindingItemDialOption(Item item) {
        if (item.dealOptionId.equals("1")) {
            binding.get().meetTextView.setText(getString(R.string.item_detail__meetup));
        } else {
            binding.get().meetTextView.setText(getString(R.string.item_detail__mailing_on_delivery));
        }
    }

    private void bindingSoldData(Item item) {
        if (item.isSoldOut.equals(Constants.ONE)) {
            binding.get().chatButton.setVisibility(View.GONE);
            binding.get().soldTextView.setText(getString(R.string.item_detail__sold));
        } else {
            if (item.addedUserId.equals(loginUserId)) {
                binding.get().soldTextView.setText(R.string.item_detail__mark_sold);
            } else {
                binding.get().soldTextView.setVisibility(View.GONE);
            }
        }
    }

    private void bindingPaidStatus(Item item) {
        switch (item.paidStatus) {
            case Constants.ADSPROGRESS:
                binding.get().adsCheckingTextView.setText(getString(R.string.paid__ads_in_progress));
                binding.get().adsCheckingTextView.setBackgroundColor(getResources().getColor(R.color.paid_ad));
                break;
            case Constants.ADSFINISHED:
                binding.get().adsCheckingTextView.setText(getString(R.string.paid__ads_in_completed));
                binding.get().adsCheckingTextView.setBackgroundColor(getResources().getColor(R.color.paid_ad_completed));
                break;
            case Constants.ADSNOTYETSTART:
                binding.get().adsCheckingTextView.setText(getString(R.string.paid__ads_is_not_yet_start));
                binding.get().adsCheckingTextView.setBackgroundColor(getResources().getColor(R.color.paid_ad_is_not_start));
                break;
            default:
                binding.get().adsCheckingTextView.setVisibility(View.GONE);
                break;
        }
    }

    private void bindingPhotoCount(Item item) {
        if (item.photoCount.equals("1")) {
            String photoCount = item.photoCount + " " + getString(R.string.item_detail__photo);
            binding.get().photoCountTextView.setText(photoCount);
        } else {
            String photoCount = item.photoCount + " " + getString(R.string.item_detail__photos);
            binding.get().photoCountTextView.setText(photoCount);
        }
    }

    private void bindingBusinessMode(Item item) {
        if (item.businessMode.equals("0")) {
            binding.get().orderTextView.setText(getString(R.string.item_detail__order_not_more_than_one));
        }
        if (item.businessMode.equals("1")) {
            binding.get().orderTextView.setText(getString(R.string.item_detail__order_more_than_one));
        }
    }

    private void bindindAddedDateUserName(Item item) {
        binding.get().activeHourTextView.setText(item.addedDateStr);
        binding.get().userNameActiveHourTextView.setText(item.user.userName);
    }

    private void bindingBottomConstraintLayout(Item item) {
        if (item.isOwner.equals(Constants.ONE)) {
            binding.get().itemOwnerConstraintLayout.setVisibility(View.VISIBLE);
            binding.get().itemSupplierConstraintLayout.setVisibility(View.GONE);
        } else {
            binding.get().itemSupplierConstraintLayout.setVisibility(View.VISIBLE);
            binding.get().itemOwnerConstraintLayout.setVisibility(View.GONE);
        }
    }

    private void bindingPromoteConstraintLayout(Item item) {
        if (item.isOwner.equals(Constants.ONE) && item.status.equals(Constants.ONE) &&
                (item.paidStatus.equals(Constants.ADSFINISHED) || item.paidStatus.equals(Constants.ADSNOTAVAILABLE))) {
            binding.get().itemPromoteCardView.setVisibility(View.VISIBLE);
        } else {
            binding.get().itemPromoteCardView.setVisibility(View.GONE);
        }
    }


    private void unFavFunction(Item item, LikeButton likeButton) {

        Utils.navigateOnUserVerificationActivityFromFav(userIdToVerify, loginUserId, psDialogMsg, getActivity(), navigationController, likeButton, () -> {

            if (!favouriteViewModel.isLoading) {
                favouriteViewModel.setFavouritePostDataObj(item.id, loginUserId);
                likeButton.setLikeDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.heart_off, null));
            }

        });

    }

    private void favFunction(Item item, LikeButton likeButton) {

        Utils.navigateOnUserVerificationActivityFromFav(userIdToVerify, loginUserId, psDialogMsg, getActivity(), navigationController, likeButton, () -> {

            if (!favouriteViewModel.isLoading) {
                favouriteViewModel.setFavouritePostDataObj(item.id, loginUserId);
                likeButton.setLikeDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.heart_on, null));
            }

        });

    }

    private void showOrHide(Item item) {

        if (item != null && item.itemPriceType != null && item.itemPriceType.name.equals("")) {
            binding.get().priceTypeTextView.setVisibility(View.GONE);
        } else {
            binding.get().priceTypeTextView.setVisibility(View.VISIBLE);
        }
        if (item != null && item.addedDateStr != null && item.addedDateStr.equals("")) {
            binding.get().activeHourTextView.setVisibility(View.GONE);
            binding.get().imageView16.setVisibility(View.GONE);
        } else {
            binding.get().activeHourTextView.setVisibility(View.VISIBLE);
            binding.get().imageView16.setVisibility(View.VISIBLE);
        }
        if (item != null && item.price != null && item.price.equals("")) {
            binding.get().priceTextView.setVisibility(View.GONE);
            binding.get().imageView17.setVisibility(View.GONE);
        } else {
            binding.get().priceTextView.setVisibility(View.VISIBLE);
            binding.get().imageView17.setVisibility(View.VISIBLE);
        }
        if (item != null && item.favouriteCount != null && item.favouriteCount.equals("")) {
            binding.get().likesTextView.setVisibility(View.GONE);
            binding.get().imageView22.setVisibility(View.GONE);
        } else {
            binding.get().likesTextView.setVisibility(View.VISIBLE);
            binding.get().imageView22.setVisibility(View.VISIBLE);
        }
        if (item != null && item.itemCondition.name != null && item.itemCondition.name.equals("")) {
            binding.get().newTextView.setVisibility(View.GONE);
            binding.get().imageView18.setVisibility(View.GONE);
        } else {
            binding.get().newTextView.setVisibility(View.VISIBLE);
            binding.get().imageView18.setVisibility(View.VISIBLE);
        }

        if (item != null && item.category.name != null && item.subCategory.name != null && item.category.name.equals("") && item.subCategory.name.equals("")) {
            binding.get().categoryAndSubCategoryTextView.setVisibility(View.GONE);
            binding.get().imageView23.setVisibility(View.GONE);
        } else {
            binding.get().categoryAndSubCategoryTextView.setVisibility(View.VISIBLE);
            binding.get().imageView23.setVisibility(View.VISIBLE);
        }
        if (item != null && item.highlightInfo != null && item.highlightInfo.equals("")) {
            binding.get().highlightInfoTextView.setVisibility(View.GONE);
            binding.get().imageView26.setVisibility(View.GONE);
        } else {
            binding.get().highlightInfoTextView.setVisibility(View.VISIBLE);
            binding.get().imageView26.setVisibility(View.VISIBLE);
        }

        if (item != null && item.brand != null && item.brand.equals("")) {
            binding.get().brandTextView.setVisibility(View.GONE);
            binding.get().imageView24.setVisibility(View.GONE);
        } else {
            binding.get().brandTextView.setVisibility(View.VISIBLE);
            binding.get().imageView24.setVisibility(View.VISIBLE);
        }

        if (item != null && item.itemType.name != null && item.itemType.name.equals("")) {
            binding.get().saleBuyTextView.setVisibility(View.GONE);
            binding.get().imageView27.setVisibility(View.GONE);
        } else {
            binding.get().saleBuyTextView.setVisibility(View.VISIBLE);
            binding.get().imageView27.setVisibility(View.VISIBLE);
        }

        if (item != null && item.businessMode != null && item.businessMode.equals("1") || item != null && item.businessMode != null && item.businessMode.equals("0")) {
            binding.get().orderTextView.setVisibility(View.VISIBLE);
            binding.get().imageView29.setVisibility(View.VISIBLE);
        } else {
            binding.get().orderTextView.setVisibility(View.GONE);
            binding.get().imageView29.setVisibility(View.GONE);
        }

        if (item != null && item.description != null && item.description.equals("")) {
            binding.get().informationTextView.setVisibility(View.GONE);
            binding.get().imageView30.setVisibility(View.GONE);
        } else {
            binding.get().informationTextView.setVisibility(View.VISIBLE);
            binding.get().imageView30.setVisibility(View.VISIBLE);
        }

        if (item != null && item.addedUserId != null && item.addedUserId.equals(loginUserId)) {
//            if(item.isSoldOut.equals(Constants.ONE)){
//                binding.get().markAsSoldButton.setVisibility(View.GONE);
//            }else {
//                binding.get().markAsSoldButton.setVisibility(View.VISIBLE);
//            }
            binding.get().editButton.setVisibility(View.VISIBLE);
            binding.get().deleteButton.setVisibility(View.VISIBLE);
            binding.get().itemSupplierConstraintLayout.setVisibility(View.GONE);
            binding.get().adsCheckingTextView.setVisibility(View.VISIBLE);

        } else {
            binding.get().editButton.setVisibility(View.GONE);
            binding.get().deleteButton.setVisibility(View.GONE);
//            binding.get().markAsSoldButton.setVisibility(View.GONE);
            binding.get().itemSupplierConstraintLayout.setVisibility(View.VISIBLE);
            binding.get().adsCheckingTextView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        loadLoginUserId();
        if (loginUserId != null) {
            itemViewModel.setItemDetailObj(itemViewModel.itemId, itemViewModel.historyFlag, loginUserId);
        }
        psDialogMsg.cancel();
//        binding.get().rating.setRating(0);
    }


}

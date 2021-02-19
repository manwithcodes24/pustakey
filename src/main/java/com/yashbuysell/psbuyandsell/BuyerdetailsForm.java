package com.yashbuysell.psbuyandsell;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonParser;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.stripe.android.EphemeralKey;
import com.stripe.android.EphemeralKeyUpdateListener;
import com.stripe.android.PaymentConfiguration;
import com.yashbuysell.psbuyandsell.ui.chat.chat.ChatActivity;
import com.yashbuysell.psbuyandsell.utils.Constants;
import com.yashbuysell.psbuyandsell.utils.PSDialogMsg;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.facebook.FacebookSdk.getApplicationContext;

public class BuyerdetailsForm extends AppCompatActivity  implements PaymentResultListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText name ;
    private EditText address ;
    private EditText phoneno ;
    private EditText alternatephone ;
    private EditText email ;
    private EditText pincode ;
    private EditText state ;
    private EditText city ;
    private EditText country ;
    private static DecimalFormat df = new DecimalFormat("0.00");
    private String pickup_location ;

    private Button submitBtn ;
    public String product_title = Constants.EMPTY_STRING;
    private DatabaseReference mDatabase, buyerInfoDB ,sellerInfoDB ,courierInfoDB;
    double convertedAmount ;
    String appKeyId ;
    String appSecretKey ;
    private String itemId, receiverId ;
    private String token ;
    private String seller_name ;
    private String seller_email ;
    private String seller_phone ;
    private String seller_city ;
    private String seller_country ;
    private String seller_state ;
    private String seller_pincode;
    private String seller_address;
    private String courier_weight;
    private String courier_title;
    private String courier_length;
    private String courier_breadth;
    private String courier_height;
    private String itemPrice;
    private String currentDateandTime ;
    private String finalAmount ;
    private String order_id ;
    private String seller_nameinBank, seller_ifsc, seller_accnum ;
    private ArrayList orderIdList
            = new ArrayList<Float>() ;
    private ArrayList<Double> rateList
            = new ArrayList<Double>() ;
    private String courierCompanyId;
    private String leastCourierPrice;
    private String leastPriceCompanyId ;
        private JSONObject priceAndIdObject;
    final String CHANNEL_ID = "1" ;

    //    warning box
    private PSDialogMsg psDialogMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyerdetails_form);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        psDialogMsg = new PSDialogMsg(this, false);
        this.name = findViewById(R.id.buyerNameEditText) ;
        this.address = findViewById(R.id.buyerAddressEditText) ;
        this.phoneno = findViewById(R.id.buyerphoneEditText) ;
        this.alternatephone = findViewById(R.id.buyerAlternatePhone) ;
        this.pincode = findViewById(R.id.buyerpincodeEditText) ;
        this.city = findViewById(R.id.buyercityEditText) ;
        this.state = findViewById(R.id.buyerStateEditText) ;
        this.email = findViewById(R.id.buyerEmailEditText) ;
        priceAndIdObject = new JSONObject();
        this.country = findViewById(R.id.buyerCountryEditText) ;
        this.submitBtn = findViewById(R.id.submitButton) ;
        this.itemId = getIntent().getStringExtra("itemId");
        this.receiverId = getIntent().getStringExtra("receiverId");
        Checkout.preload(getApplicationContext());
        pickup_location = UUID.randomUUID().toString().substring(0,5)  ;

        AndroidNetworking.initialize(getApplicationContext());
        PaymentConfiguration.init(getApplicationContext(), getString(R.string.stripe_publickey));







        if(itemId != null && itemId !=""){
            this.mDatabase = FirebaseDatabase.getInstance().getReference(itemId);
            this.buyerInfoDB = mDatabase.child("buyerDetails");
            this.courierInfoDB = mDatabase.child("courierDetails") ;
            this.sellerInfoDB = mDatabase.child("sellerDetails") ;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.currentDateandTime = sdf.format(new Date());
        Log.d("currentDateandTime" , currentDateandTime) ;









        if(courierInfoDB != null ) {


        try {


        courierInfoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {

                    courier_title = snapshot.child("name").getValue().toString() ;
                    courier_weight = snapshot.child("weight").getValue().toString() ;
                    Log.d("courier_Weight" , "at Buyer  " + courier_weight);
                    courier_length = snapshot.child("length").getValue().toString() ;
                    courier_breadth = snapshot.child("breadth").getValue().toString() ;
                    courier_height = snapshot.child("height").getValue().toString() ;
                    itemPrice = snapshot.child("price").getValue().toString() ;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;
        }
        catch (Exception e) {
            Log.d("Error at cInfo" , "Error at saving courier info " + e.toString());
        }
        }

        if(sellerInfoDB != null){


        try{


        sellerInfoDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                seller_address = snapshot.child("address").getValue().toString() ;
                seller_pincode = snapshot.child("pincode").getValue().toString() ;
                seller_name = snapshot.child("name").getValue().toString() ;
                seller_email = snapshot.child("email").getValue().toString() ;
                seller_phone = snapshot.child("phone").getValue().toString() ;
                seller_city = snapshot.child("city").getValue().toString() ;
                seller_country = snapshot.child("country").getValue().toString() ;
                seller_state = snapshot.child("state").getValue().toString() ;
                seller_nameinBank = snapshot.child("nameinbank").getValue().toString() ;
                seller_ifsc = snapshot.child("ifsc").getValue().toString() ;
                seller_accnum = snapshot.child("accnum").getValue().toString() ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        }
        catch (Exception e) {
            Log.d("Error at cInfo" , "Error at saving seller info " + e.toString());

        }
        }




        pincode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String postelcode_checkingURL = getString(R.string.postalcode_checker) ;
                AndroidNetworking.get(postelcode_checkingURL + pincode.getText().toString())
                        .setTag("postalcodeDetails")
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONArray(new JSONArrayRequestListener() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d("postal_Code" , response.toString()) ;
                                JSONArray data = response ;
                                try {
                                    JSONObject objData = (JSONObject) response.get(0);
                                    String status = objData.getString("Status");
                                    JSONObject PostalOffices = (JSONObject) objData.getJSONArray("PostOffice").get(0) ;
                                    Log.d("postalcode_object" , PostalOffices.toString()) ;
                                    String postalcode_city = PostalOffices.getString("District");
                                    String postalcode_country = PostalOffices.getString("Country");
                                    String postalcode_state = PostalOffices.getString("State");
                                    city.setText(postalcode_city);
                                    state.setText(postalcode_state);
                                    country.setText(postalcode_country);


                                    Log.d("postalcode_status" , status) ;




                                } catch (JSONException e) {
                                    Log.d("postalcode_status" , e.getMessage().toString()) ;

                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onError(ANError error) {


                                Log.d("postalcode_status_error" , error.getErrorBody().toString()) ;

                            }
                        });
//change this for pin code
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        submitBtn.setOnClickListener(view -> {

            if((name.getText().toString() != ""
                    && name.getText().toString() != null)&&
                    (address.getText().toString() != ""
                            && address.getText().toString() != null) &&
                    (phoneno.getText().toString() != ""
                            && phoneno.getText().toString() != null) && (alternatephone.getText().toString().length() == 10) &&
                    (alternatephone.getText().toString() != ""
                            && alternatephone.getText().toString() != null) && (alternatephone.getText().toString().length() == 10) &&
                    (pincode.getText().toString() != ""
                            && pincode.getText().toString() != null) &&
                    (city.getText().toString() != ""
                            && city.getText().toString() != null) &&
                    (country.getText().toString() != ""
                            && country.getText().toString() != null)


            ){
                buyerInfoDB.child("name").setValue(name.getText().toString());
                buyerInfoDB.child("address").setValue(address.getText().toString());
                buyerInfoDB.child("phoneno").setValue(phoneno.getText().toString());
                buyerInfoDB.child("alternatephone").setValue(alternatephone.getText().toString());
                buyerInfoDB.child("pincode").setValue(pincode.getText().toString());
                buyerInfoDB.child("city").setValue(city.getText().toString());
                buyerInfoDB.child("country").setValue(country.getText().toString());
                buyerInfoDB.child("payment").setValue(getString(R.string.paymentPending));
                buyerInfoDB.child("order").setValue(getString(R.string.paymentPending));
                String cAuthURL = getString(R.string.courierUser_auth) ;

                String serviceUrl = getString(R.string.courierCheck_serviceability);

                if(cAuthURL != null) {
                    AndroidNetworking.post(cAuthURL)
                            .addHeaders("Content-Type", "application/json")
                            .addBodyParameter("email", getString(R.string.courierUser_auth_email))
                            .addBodyParameter("password", getString(R.string.courierUser_auth_pass))
                            .setTag("token")
                            .setPriority(Priority.IMMEDIATE)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        token = response.get("token").toString();
                                        Log.d("courierToken " , token) ;

                                        if(token != "" && token != null){
                                            try {
                                                AndroidNetworking.get(serviceUrl+"?pickup_postcode="+seller_pincode+"&delivery_postcode="+pincode.getText().toString()+"&weight="+courier_weight+"&cod=0")
                                                        .addHeaders("Content-Type", "application/json")
                                                        .addHeaders("Authorization" ,"Bearer " + token)
                                                        .setTag("test")
                                                        .setPriority(Priority.IMMEDIATE)
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
                                                                        Log.d("courierRate" , rate);

                                                                         courierCompanyId = obj.getString("courier_company_id");

                                                                        if(rate != null){
                                                                            rateList.add(Double.parseDouble(rate));
                                                                        priceAndIdObject.put(String.valueOf(Float.parseFloat(rate)),  courierCompanyId) ;

                                                                        }


                                                                    }
                                                                    Collections.sort(rateList);
                                                                    if(rateList != null) {
                                                                        Toast.makeText(getApplicationContext(), "Delivery Charge : " + rateList.get(0).toString(), Toast.LENGTH_LONG ).show();

//                                                                        leastCourierPrice = rateList.get(0).toString() ;
                                                                        leastCourierPrice = String.valueOf(Float.parseFloat( rateList.get(0).toString() ) );
                                                                        finalAmount = String.valueOf(Float.parseFloat(itemPrice) + Float.parseFloat( rateList.get(0).toString() ));
                                                                        startPayment();
                                                                    }


                                                                }
                                                                catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }

                                                            }

                                                            @Override
                                                            public void onError(ANError error) {
                                                                Log.d("courierError" , error.toString() ) ;
                                                            }
                                                        });
                                            }

                                            catch (Exception e) {
                                                e.printStackTrace();
                                            }



////                                                order parameter
//                                                JSONObject jsonObject = new JSONObject();
//                                                JSONArray childArr =  new JSONArray() ;
//                                                JSONObject childObj =  new JSONObject() ;
//                                                childObj.put("name" , courier_title) ;
//                                                childObj.put("sku" , itemId.substring(0,6)) ;
//                                                childObj.put("units" , "1") ;
//                                                Log.d("courierOrder" , "final amount = " + finalAmount) ;
//                                                childObj.put("selling_price" , finalAmount) ;
//
//                                                childArr.put(childObj) ;
//                                                try {
//                                                    jsonObject.put("mode" , getString(R.string.courierOrder_mode)) ;
//                                                    jsonObject.put("order_id", itemId.substring(0,6));
//                                                    jsonObject.put("request_pickup", '1');
//                                                    jsonObject.put("print_label", '1');
//                                                    jsonObject.put("generate_manifest", '1');
//                                                    jsonObject.put("courier_id", courierCompanyId);
//                                                    jsonObject.put("order_date", currentDateandTime);
//                                                    jsonObject.put("pickup_location", itemId.substring(0,6));
//                                                    jsonObject.put("billing_customer_name", name.getText().toString());
//                                                    jsonObject.put("billing_last_name", name.getText().toString());
//                                                    jsonObject.put("billing_address", address.getText().toString());
//                                                    jsonObject.put("billing_city", city.getText().toString());
//                                                    jsonObject.put("billing_pincode", pincode.getText().toString());
//                                                    jsonObject.put("billing_state", state.getText().toString());
//                                                    jsonObject.put("billing_country", country.getText().toString());
//                                                    jsonObject.put("billing_email", email.getText().toString());
//                                                    jsonObject.put("billing_phone", phoneno.getText().toString());
//                                                    jsonObject.put("billing_alternate_phone", alternatephone.getText().toString());
//                                                    jsonObject.put("shipping_is_billing", "1");
//
//                                                    jsonObject.put("order_items", childArr);
//                                                    jsonObject.put("payment_method", "Prepaid");
//
//                                                    jsonObject.put("sub_total", finalAmount);
//                                                    jsonObject.put("length", courier_length);
//                                                    jsonObject.put("breadth", courier_breadth);
//                                                    jsonObject.put("height", courier_height);
//                                                    jsonObject.put("weight", courier_weight);
//
//
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                                try {
//
//                                                AndroidNetworking.post(createOrderURL)
//                                                        .addHeaders("Content-Type" , "application/json")
//                                                        .addHeaders("Authorization" , "Bearer " + token)
//                                                        .addJSONObjectBody(jsonObject)
//                                                        .setTag("createOrder")
//                                                        .setPriority(Priority.HIGH)
//                                                        .build()
//                                                        .getAsJSONObject(new JSONObjectRequestListener() {
//                                                            @Override
//                                                            public void onResponse(JSONObject response) {
//                                                                Log.d("courierOrder" , "creating Order response" + response.toString()) ;
//                                                            }
//                                                            @Override
//                                                            public void onError(ANError error) {
//                                                                Log.d("courierOrderError" , "Error creating Order " + error.getErrorBody()) ;
//                                                            }
//                                                        });
//
//
//
//                                                }
//                                                catch (Exception e) {
//                                                    Log.d("responseError" , e.toString()) ;
//
//                                                }
                                        }

                                    }
                                    catch (JSONException e) {
                                        Log.d("courierError" , e.toString());

                                    }


                                }
                                @Override
                                public void onError(ANError error) {
                                    // handle
                                    Log.d("courierError" , error.toString()) ;
                                }
                            });

                }

            }
            else {
                psDialogMsg.showErrorDialog(getApplicationContext().getString(R.string.item_entry_need_details), getApplicationContext().getString(R.string.app__ok));
                psDialogMsg.show();
            }








        }) ;



    }
    public void startPayment() throws JSONException {
        convertedAmount = Math.round((Double.parseDouble(finalAmount) * 100.0)  * 100.0) / 100.0;
        leastPriceCompanyId = priceAndIdObject.getString(leastCourierPrice).toString();
        Log.d("PaymentError" , "convertedAmount " + convertedAmount) ;

        try {





        }
        catch (Exception e){
            e.printStackTrace();
        }



        /**
         * Instantiate Checkout
         */
        Checkout checkout = new Checkout();
        checkout.setKeyID(getApplicationContext().getString(R.string.razor_key));
        /**
         * Set your logo here
         */
        checkout.setImage(R.drawable.app_icon);

        /**
         * Reference to current activity
         */
        final Activity activity = this;

        /**
         * Pass your payment options to the Razorpay Checkout as a JSONObject
         */
        try {
            JSONObject options = new JSONObject();

            options.put("name", seller_name);
            options.put("description", "payment for " + product_title);
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
            Log.d("orderId" , " RazorPayId = " + itemId) ;
//            options.put("order_id", itemId);//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", String.valueOf(convertedAmount));//pass amount in currency subunits
            options.put("prefill.email", email.getText().toString());
            options.put("prefill.contact",phoneno.getText().toString());
            checkout.open(activity, options);
        } catch(Exception e) {
            e.printStackTrace();
            psDialogMsg.showErrorDialog( "Error in payment! Check your data" , getString(R.string.app__ok));
            Log.e(TAG, "Error in starting Razorpay Checkout"+ e);
        }
        /**
         * Instantiate Checkout
         */


    }



    @Override
    public void onPaymentSuccess(String s) {

        try {

                mDatabase.child("payment").setValue(getString(R.string.paymentDone));





//                creating payout
//                String payoutUrl = getString(R.string.razor_createPayout);
//
//                String cred = appKeyId +":" +appSecretKey ;
//                String encoding =  android.util.Base64.encodeToString(cred.getBytes(), Base64.DEFAULT) ;
//                AndroidNetworking.post(payoutUrl)
//                        .addHeaders("Content-Type" , "application/json")
//                        .addHeaders("Authorization" , "Basic " + encoding)
//                        .addJSONObjectBody(orderRequest)
//                        .setTag("createRazorOrder")
//                        .setPriority(Priority.HIGH)
//                        .build()
//                        .getAsJSONObject(new JSONObjectRequestListener() {
//
//
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                try {
//                                    order_id = response.getString("id") ;
//                                    JSONObject orderRequest = new JSONObject();
//                                    orderRequest.put(itemId,order_id) ;
//                                    orderIdList.add(orderRequest);
//                                    mDatabase.child("order_id").setValue(order_id) ;
//                                    mDatabase.child("order").setValue(order_id) ;
//
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            @Override
//                            public void onError(ANError anError) {
//
//                            }
//                        });





                if(finalAmount != null){
                    if(seller_nameinBank != Constants.EMPTY_STRING && seller_ifsc != Constants.EMPTY_STRING && seller_accnum != Constants.EMPTY_STRING){


                    String payoutString = "{\n    \"account_number\": \"2323230008656323\",\n    \"amount\": "+convertedAmount+",\n    \"currency\": \"INR\",\n    \"mode\": \"IMPS\",\n    \"purpose\": \"payout\",\n    \"fund_account\": {\n        \"account_type\": \"bank_account\",\n        \"bank_account\": {\n            \"name\": \"" + seller_nameinBank + "\",\n            \"ifsc\": \""+seller_ifsc+"\",\n            \"account_number\": \""+seller_accnum+"\"\n        },\n        \"contact\": {\n            \"name\": \""+ seller_name+"\",\n            \"email\": \""+ seller_email +"\",\n            \"contact\": \""+seller_phone+"\",\n            \"type\": \"vendor\",\n            \"reference_id\": \""+itemId+"\",\n            \"notes\": {\n                \"notes_key_1\": \"payout to vendor\",\n                \"notes_key_2\": \"payout to vendor\"\n            }\n        }\n    },\n    \"queue_if_low_balance\": true,\n    \"reference_id\": \""+itemId+"\",\n    \"narration\": \"payout to vendor\",\n    \"notes\": {\n        \"notes_key_1\": \"payout to vendor\",\n        \"notes_key_2\": \"Engage\"\n    }\n}" ;
                    //
                    JSONObject payoutobj = new JSONObject(payoutString) ;

                    String createPayout  = getString(R.string.razor_createPayout) ;

                    AndroidNetworking.post(createPayout)
                            .addHeaders("Content-Type" , "application/json")
                            .addJSONObjectBody(payoutobj)
                            .setTag("createPayout")
                            .setPriority(Priority.IMMEDIATE)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {


                                @Override
                                public void onResponse(JSONObject response) {

                                }

                                @Override
                                public void onError(ANError anError) {

                                }
                            });

                    }
                    String createOrderURL  = getString(R.string.courierOrder_createOrder) ;

                    JSONObject jsonObject = new JSONObject();
                    JSONArray childArr =  new JSONArray() ;
                    JSONObject childObj =  new JSONObject() ;
                    JSONObject vendorDetailsObj =  new JSONObject() ;
                    vendorDetailsObj.put("email" , seller_email) ;
                    vendorDetailsObj.put("phone" , seller_phone) ;
                    vendorDetailsObj.put("name" , seller_name) ;
                    vendorDetailsObj.put("address" , seller_address) ;
                    vendorDetailsObj.put("address_2" , "") ;
                    vendorDetailsObj.put("city" , seller_city) ;
                    vendorDetailsObj.put("state" , seller_state) ;
                    vendorDetailsObj.put("country" , seller_country) ;
                    vendorDetailsObj.put("pin_code" , seller_pincode) ;
                    vendorDetailsObj.put("pickup_location" , pickup_location) ;


                    childObj.put("name" , courier_title) ;
                    childObj.put("sku" , "ch123") ;
                    childObj.put("units" , "1") ;
                    childObj.put("selling_price" , finalAmount) ;

                    childArr.put(childObj) ;
                    try {
                        jsonObject.put("mode" , getString(R.string.courierOrder_mode)) ;
                        jsonObject.put("request_pickup", '1');
                        jsonObject.put("print_label", '1');
                        jsonObject.put("courier_id",leastPriceCompanyId );
                        jsonObject.put("order_id", pickup_location);

                        jsonObject.put("order_date", currentDateandTime);
                        jsonObject.put("billing_customer_name", name.getText().toString());
                        jsonObject.put("billing_last_name", name.getText().toString());
                        jsonObject.put("billing_address", address.getText().toString());
                        jsonObject.put("billing_address_2", "");
                        jsonObject.put("billing_city", city.getText().toString());
                        jsonObject.put("billing_state", state.getText().toString());
                        jsonObject.put("billing_country", country.getText().toString());
                        jsonObject.put("billing_pincode", pincode.getText().toString());

                        jsonObject.put("billing_email", email.getText().toString());
                        jsonObject.put("billing_phone", phoneno.getText().toString());
                        jsonObject.put("billing_alternate_phone", alternatephone.getText().toString());
                        jsonObject.put("shipping_is_billing", "1");

                        jsonObject.put("order_items", childArr);
                        jsonObject.put("payment_method", "Prepaid");


                        jsonObject.put("sub_total", finalAmount);
                        jsonObject.put("weight", courier_weight);

                        jsonObject.put("length", courier_length);
                        jsonObject.put("breadth", courier_breadth);
                        jsonObject.put("height", courier_height);
                        jsonObject.put("pickup_location", pickup_location );

                        jsonObject.put("vendor_details", vendorDetailsObj);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {

                        AndroidNetworking.post(createOrderURL)
                                .addHeaders("Content-Type" , "application/json")
                                .addHeaders("Authorization" , "Bearer " + token)
                                .addJSONObjectBody(jsonObject)
                                .setTag("createOrder")
                                .setPriority(Priority.IMMEDIATE)
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.M)
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("courierOrder" , "creating Order response" + response.toString()) ;

                                        try {
                                            JSONObject payload = (JSONObject) response.get("payload");
                                            String labelUrl = payload.getString("label_url")  ;
                                            Log.d("courierOrder" , "label URL = " + labelUrl) ;

                                            if(labelUrl !=  null && labelUrl != "" && labelUrl != "null") {
                                                mDatabase.child("labelUrl").setValue(labelUrl);
                                                mDatabase.child("order").setValue(getString(R.string.paymentDone));



                                                Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                                                notificationIntent.setData(Uri.parse(labelUrl));
                                                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
                                                // Resources r = getResources();
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    CharSequence name = "Pustakey";
                                                    String description = "Pustakey Notificaton";
                                                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                                                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                                                    channel.setDescription(description);

                                                    // Register the channel with the system; you can't change the importance
                                                    // or other notification behaviors after this
                                                    NotificationManager notificationManager = getSystemService(NotificationManager.class);

                                                    notificationManager.createNotificationChannel(channel);
                                                }


                                                NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                                        .setSmallIcon(R.drawable.app_icon)
                                                        .setContentTitle(getString(R.string.labelNotificationTitle))
                                                        .setContentText(getString(R.string.labelNotificationDis))
                                                        .setContentIntent(pi)
                                                        .setAutoCancel(true);;

                                                notification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) ;

                                                NotificationManager notificationManager2 =  (NotificationManager) getApplicationContext().getSystemService(Service.NOTIFICATION_SERVICE);
                                                notificationManager2.notify(1, notification.build());

                                                DatabaseReference fcmRef = FirebaseDatabase.getInstance().getReference("users") ;
                                                fcmRef.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        try {
                                                            String fcmtoken = snapshot.child(receiverId).child("fcmtoken").getValue().toString() ;

                                                            final String postUrl = "https://fcm.googleapis.com/fcm/send";

                                                            JSONObject jsonObject = new JSONObject() ;
                                                            jsonObject.put("registration_id" , fcmtoken);
                                                            jsonObject.put("to" , fcmtoken);
                                                            jsonObject.put("collapse_key" , "type_a");

                                                            JSONObject notiObj = new JSONObject();
                                                            notiObj.put("title", "Label Generated") ;
                                                            notiObj.put("body", getString(R.string.labelNotificationDis)) ;

                                                            jsonObject.put("notification" , notiObj);


                                                            AndroidNetworking.post(postUrl)
                                                                    .addHeaders("Content-Type", "application/json")
                                                                    .addHeaders("Authorization", "key=" + getApplicationContext().getString(R.string.firebase_serverkey))
                                                                    .addJSONObjectBody(jsonObject)
                                                                    .setTag("notification")
                                                                    .setPriority(Priority.IMMEDIATE)
                                                                    .build()
                                                                    .getAsJSONObject(new JSONObjectRequestListener() {
                                                                        @Override
                                                                        public void onResponse(JSONObject response) {
                                                                            finish();

                                                                        }

                                                                        @Override
                                                                        public void onError(ANError error) {

                                                                        }
                                                                    });

                                                        }
                                                        catch (Exception e) {
                                                            Toast.makeText(getApplicationContext() , "Error occured", Toast.LENGTH_SHORT) ;
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
//                                                Intent goToNextHome = new Intent(getApplicationContext(), MainActivity.class);
//                                                startActivity(goToNextHome);
//                                                Intent broadcastIntent = new Intent("com.yashbuysell.psbuyandsell_broadcast-Label");
//                                                broadcastIntent.putExtra("title" , "Label") ;
//                                                broadcastIntent.putExtra("label" , labelUrl) ;
////                            LocalBroadcastManager localBroadcastManager = new LocalBroadcastManager().getInstance(context) ;
//                                                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcastIntent);
//                                                PendingIntent broadcastPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, broadcastIntent, 0);
//                                                Intent toChat  = new Intent(getApplicationContext() , ChatActivity.class) ;
//                                                getApplicationContext().startService(toChat) ;








                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    @Override
                                    public void onError(ANError error) {
                                        psDialogMsg.showErrorDialog( "Error creating order!" , getString(R.string.app__ok));

                                        Log.d("courierOrderError" , "Error creating Order " + error.getErrorBody()) ;
                                    }
                                });



                    }

                    catch (Exception e) {
                        Log.d("responseError" , e.toString()) ;

                    }
                }


        }
        catch (Exception e){
            Log.d("courierError" , e.toString()) ;
        }
    }

    @Override
    public void onPaymentError(int i, String s) {
        mDatabase.child("payment").setValue(getString(R.string.paymentError));
        Log.e("PaymentError" , s);
        Toast.makeText(getApplicationContext() , "Payment Error" , Toast.LENGTH_SHORT).show();
    }
}


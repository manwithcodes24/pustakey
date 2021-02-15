package com.yashbuysell.psbuyandsell.ui.courier;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.yashbuysell.psbuyandsell.R;
import com.yashbuysell.psbuyandsell.binding.FragmentDataBindingComponent;
import com.yashbuysell.psbuyandsell.databinding.FragmentCourierTrackingBinding;
import com.yashbuysell.psbuyandsell.databinding.FragmentItemBinding;
import com.yashbuysell.psbuyandsell.utils.AutoClearedValue;
import com.yashbuysell.psbuyandsell.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link courierTrackingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class courierTrackingFragment extends Fragment {
    private final androidx.databinding.DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);
    private AutoClearedValue<FragmentCourierTrackingBinding> binding;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String token ;
    EditText awbNumberEditText ;
    Button awbSubmit ;
    private String awbNumber ;
    private String destination = Constants.EMPTY_STRING;



    public courierTrackingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment courierTrackingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static courierTrackingFragment newInstance(String param1, String param2) {
        courierTrackingFragment fragment = new courierTrackingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
//        AndroidNetworking.initialize(getApplicationContext());
//        awbNumberEditText = (EditText) getActivity().findViewById(R.id.awbNumber) ;
//        awbSubmit = (Button)  getActivity().findViewById(R.id.awbSubmitBtn) ;
//
//        String trackUrl = getApplicationContext().getString(R.string.courierOrder_trackShipment) ;
//        awbSubmit.setOnClickListener(view -> {
//            if(awbNumberEditText.getText().toString() != null && awbNumberEditText.getText().toString() != ""){
//                AndroidNetworking.get(trackUrl + awbNumberEditText.getText().toString())
//                        .addHeaders("Content-Type", "application/json")
//                        .setTag("courierTracking")
//                        .setPriority(Priority.MEDIUM)
//                        .build()
//                        .getAsJSONObject(new JSONObjectRequestListener() {
//
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                try {
//                                    JSONArray shipmentDataArray = (JSONArray) response.getJSONArray("shipment_track");
//                                    JSONObject shipmentObj = (JSONObject) shipmentDataArray.get(0);
//                                    String destination = shipmentObj.getString("destination") ;
//                                    Toast.makeText(getApplicationContext(), "Destination : " + destination , Toast.LENGTH_LONG).show();
//                                } catch (Exception e) {
//                                    Toast.makeText(getApplicationContext(), getString(R.string.trackError) , Toast.LENGTH_LONG).show();
//
//                                    e.printStackTrace();
//                                }
//
//                            }
//
//                            @Override
//                            public void onError(ANError anError) {
//
//                            }
//                        }) ;
//            }
//
//
//
//
//
//        });




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentCourierTrackingBinding dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_courier_tracking, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        AndroidNetworking.initialize(getApplicationContext());
        binding.get().showDestination.setVisibility(View.INVISIBLE);

        awbNumber = binding.get().awbNumber.getText().toString() ;
        String trackUrl = getApplicationContext().getString(R.string.courierOrder_trackShipment) ;
        String cAuthURL = getString(R.string.courierUser_auth) ;

        binding.get().awbSubmitBtn.setOnClickListener(view -> {
            if(awbNumber != null && awbNumber != ""){
                AndroidNetworking.post(cAuthURL)
                        .addHeaders("Content-Type", "application/json")
                        .addBodyParameter("email", getString(R.string.courierUser_auth_email))
                        .addBodyParameter("password", getString(R.string.courierUser_auth_pass))
                        .setTag("token")
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {


                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    token = response.get("token").toString();
                                    if(token != "" && token !=null ) {
                                        Log.d("awb" , "awbNumber = " +  binding.get().awbNumber.getText().toString() ) ;
                                        AndroidNetworking.get(trackUrl+binding.get().awbNumber.getText().toString())
                                                .addHeaders("Content-Type" , "application/json")
                                                .addHeaders("Authorization" , "Bearer " + token)
                                                .setTag("courierTracking")
                                                .setPriority(Priority.MEDIUM)
                                                .build()
                                                .getAsJSONObject(new JSONObjectRequestListener() {

                                                    @Override
                                                    public void onResponse(JSONObject response) {
                                                        try {
                                                            JSONObject trackingData = (JSONObject)  response.get("tracking_data");
                                                            JSONArray shipmentDataArray = (JSONArray) trackingData.getJSONArray("shipment_track_activities");
                                                            if(shipmentDataArray != null) {
                                                                JSONObject shipmentObj = (JSONObject) shipmentDataArray.get(0);
                                                                destination = shipmentObj.getString("activity") ;
                                                                Toast.makeText(getApplicationContext(), "Destination : " + destination , Toast.LENGTH_LONG).show();
                                                                    binding.get().showDestination.setText("Latest activity : " + destination);
                                                                    binding.get().showDestination.setVisibility(View.VISIBLE);
                                                            }
                                                        } catch (Exception e) {
                                                            Toast.makeText(getApplicationContext(), getString(R.string.trackError) , Toast.LENGTH_LONG).show();

                                                            e.printStackTrace();
                                                        }

                                                    }

                                                    @Override
                                                    public void onError(ANError anError) {
                                                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                                                        Log.d("error" , anError.getErrorBody().toString());
                                                    }
                                                }) ;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }

                            @Override
                            public void onError(ANError anError) {

                            }
                        }) ;



            }





        });
        return binding.get().getRoot() ;
    }
}
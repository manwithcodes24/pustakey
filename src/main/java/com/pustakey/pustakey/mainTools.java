package com.pustakey.pustakey;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pustakey.pustakey.ui.courier.courierTrackingFragment;
import com.pustakey.pustakey.ui.item.favourite.FavouriteListFragment;
import com.pustakey.pustakey.utils.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class mainTools extends Fragment {
Button trackBtn , cartBtn, instructionBtn ;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_main_tools, container, false);

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        trackBtn = view.findViewById(R.id.trackBtn) ;
        cartBtn = view.findViewById(R.id.cartBtn) ;
        instructionBtn = view.findViewById(R.id.instructionBtn) ;

        trackBtn.setOnClickListener(view1 -> {
            try {

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new courierTrackingFragment())
                        .commitAllowingStateLoss();
            } catch (Exception e) {
                Utils.psErrorLog("Error! Can't replace fragment.", e);
            }
        });
        cartBtn.setOnClickListener(v -> {
            try {

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new FavouriteListFragment())
                        .commitAllowingStateLoss();
            } catch (Exception e) {
                Utils.psErrorLog("Error! Can't replace fragment.", e);
            }
        });
        instructionBtn.setOnClickListener(v -> {
            try {

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new instruction())
                        .commitAllowingStateLoss();
            } catch (Exception e) {
                Utils.psErrorLog("Error! Can't replace fragment.", e);
            }
        });


    }
}
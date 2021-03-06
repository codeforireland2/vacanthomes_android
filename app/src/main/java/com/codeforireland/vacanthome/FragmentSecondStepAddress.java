package com.codeforireland.vacanthome;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.codeforireland.vacanthome.model.HomeData;
import com.codeforireland.vacanthome.utils.LocationUtils;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;


/**
 * Created by Nikodem Walicki on 2018-06-10.
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 * Use the {@link FragmentSecondStepAddress#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSecondStepAddress extends Fragment implements Step{

    private static final String TAG = FragmentSecondStepAddress.class.getSimpleName();
    private static final String ARG_POSITION = "current_step_position_key";

    private int stepPosition;
    private TextView tvInfo; // I can use it to display some extra message
    private Switch tbGrass, tbWindows, tbActivity; //ToggleButton
    private Button btnAutoLocation, btnMapLocation;
    private Spinner spinnerHomeType;
    private EditText edComment;
    private LinearLayout layoutClickGrass, layoutClickWindows, layoutClickActivity;
    public  static final int PERMISSIONS_REQUEST = 123;
    public static final int MAP_REQUEST = 234;

    private FragmentStepsInterfaces.SecondStepInterface mListener;

    public FragmentSecondStepAddress() {
    }

    public static FragmentSecondStepAddress newInstance() {
        return new FragmentSecondStepAddress();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stepPosition = getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_fragment_second_step_address, container, false);
        layoutClickActivity = v.findViewById(R.id.activity_click_area);
        layoutClickGrass = v.findViewById(R.id.grass_click_area);
        layoutClickWindows = v.findViewById(R.id.windows_click_area);
        tvInfo = v.findViewById(R.id.fragment_step_second_text_info);
        edComment = v.findViewById(R.id.fragment_step_second_comments_edittext);
        tbGrass = v.findViewById(R.id.fragment_step_second_grass_toggleButton);
        tbWindows = v.findViewById(R.id.fragment_step_second_windows_toggleButton);
        tbActivity = v.findViewById(R.id.fragment_step_second_activity_toggleButton);
        spinnerHomeType = v.findViewById(R.id.fragment_step_second_typeHome_picker);
        btnAutoLocation = v.findViewById(R.id.fragment_step_second_location_gps_button);
        btnMapLocation = v.findViewById(R.id.fragment_step_second_location_map_button);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyButton buttons = new MyButton();
        btnMapLocation.setOnClickListener(buttons);
        btnAutoLocation.setOnClickListener(buttons);
        layoutClickWindows.setOnClickListener(buttons);
        layoutClickGrass.setOnClickListener(buttons);
        layoutClickActivity.setOnClickListener(buttons);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentStepsInterfaces.SecondStepInterface) {
            mListener = (FragmentStepsInterfaces.SecondStepInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ThirdStepInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "Acc Result:::: request code: "+requestCode+" result code: "+requestCode);
        if(requestCode==MAP_REQUEST){
            if(data!=null){
                double lat = data.getDoubleExtra("lat", 0);
                double lng = data.getDoubleExtra("lng", 0);
                Log.d(TAG, "latitude: "+lat+" longitude: "+lng);
                HomeData.getHomeDataInstance().setLatitude(lat);
                HomeData.getHomeDataInstance().setLongitude(lng);
            }
        }
    }
    /**
     * if lat or lng == 0 not allow to send this data, the other are optional
     * @return null if  location is captured
     */
    @Nullable
    @Override
    public VerificationError verifyStep() {
        boolean grass = tbGrass.isChecked();
        boolean windows = tbWindows.isChecked();
        boolean activity = tbActivity.isChecked();
        String homeType = spinnerHomeType.getSelectedItem().toString();
        HomeData.getHomeDataInstance().setGrassOverGrown(grass);
        HomeData.getHomeDataInstance().setWindowsBoarded(windows);
        HomeData.getHomeDataInstance().setVisibleActivity(activity);
        HomeData.getHomeDataInstance().setHomeType(homeType);
        String comment = edComment.getText().toString();
        if(!comment.isEmpty()) HomeData.getHomeDataInstance().setComment(comment);

        Log.d(TAG, "spinner position: "+spinnerHomeType.getSelectedItemPosition());
        if(spinnerHomeType.getSelectedItemPosition()==0) return new VerificationError("please select type of home");
        if(HomeData.getHomeDataInstance().getLatitude()==0) return new VerificationError("please set location");
        return null;
    }

    @Override
    public void onSelected() {
    }

    @Override
    public void onError(@NonNull VerificationError error) {
    }

    private void getLocation(boolean locattionFromMap){
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
            Location location = LocationUtils.getLatLng(getActivity());
            Toast.makeText(getContext(), "Map needs a developer key", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), MyLocationMapActivity.class);
            if(locattionFromMap){
                if(location!=null){
                    intent.putExtra("lat", location.getLatitude());
                    intent.putExtra("lng", location.getLongitude());
                }
                startActivityForResult(intent, MAP_REQUEST);
            }else {
                if(location!=null){
                    Log.d(TAG, "latitude: "+location.getLatitude());
                    HomeData.getHomeDataInstance().setLatitude(location.getLatitude());
                    HomeData.getHomeDataInstance().setLongitude(location.getLongitude());
                }else {
                    Log.d(TAG, "current location not available");
                }
            }
        }else{
            requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSIONS_REQUEST);
        }
    }

    private class MyButton implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.fragment_step_second_location_gps_button:
                    getLocation(false);
                    break;
                case R.id.fragment_step_second_location_map_button:
                    getLocation(true);
                    break;
                case R.id.activity_click_area:
                    if(tbActivity.isChecked()) tbActivity.setChecked(false);
                    else tbActivity.setChecked(true);
                    break;
                case R.id.grass_click_area:
                    if(tbGrass.isChecked()) tbGrass.setChecked(false);
                    else tbGrass.setChecked(true);
                    break;
                case R.id.windows_click_area:
                    if(tbWindows.isChecked()) tbWindows.setChecked(false);
                    else tbWindows.setChecked(true);
                    break;
            }
        }
    }
}
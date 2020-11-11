package com.hien.latecoffeeshipper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hien.latecoffeeshipper.common.Common;
import com.hien.latecoffeeshipper.common.LatLngInterpolator;
import com.hien.latecoffeeshipper.common.MarkerAnimation;
import com.hien.latecoffeeshipper.model.ShippingOderModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.paperdb.Paper;

public class ShippingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker ShipperMarker;
    private ShippingOderModel shippingOderModel;

    @BindView(R.id.txt_name)
    TextView txt_name;
    @BindView(R.id.txt_oder_number)
    TextView txt_order_number;
    @BindView(R.id.txt_address)
    TextView txt_address;
    @BindView(R.id.txt_date)
    TextView txt_date;
    @BindView(R.id.btn_start_trip)
    MaterialButton btn_start_trip;
    @BindView(R.id.btn_call)
    MaterialButton btn_call;
    @BindView(R.id.btn_done)
    MaterialButton btn_done;
    @BindView(R.id.img_food_image)
    ImageView img_food_image;

    private boolean isInit = false;
    private Location previousLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping);

        ButterKnife.bind(this);

        buildLocationRequest();

        buildLocationCallBack();

        setShippingOrder();

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(ShippingActivity.this::onMapReady);
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ShippingActivity.this);
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ShippingActivity.this, "You must enable location permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


    }

    private void setShippingOrder() {
        Paper.init(this);
        String data = Paper.book().read(Common.SHIPPING_ORDER_DATA);
        if (!TextUtils.isEmpty(data)) {
            shippingOderModel = new Gson()
                    .fromJson(data, new TypeToken<ShippingOderModel>() {
                    }.getType());
            if (shippingOderModel != null) {
                Common.setSpanStringColor("Name: ",
                        shippingOderModel.getOrderModel().getUserName(),
                        txt_name,
                        Color.parseColor("#333639"));
                txt_date.setText(new StringBuilder()
                        .append(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                .format(shippingOderModel.getOrderModel().getCreateDate())));
                Common.setSpanStringColor("No: ",
                        shippingOderModel.getOrderModel().getKey(),
                        txt_order_number,
                        Color.parseColor("#673ab7"));
                Common.setSpanStringColor("Address: ",
                        shippingOderModel.getOrderModel().getShippingAddress(),
                        txt_address,
                        Color.parseColor("#795548"));
                Glide.with(this)
                        .load(shippingOderModel.getOrderModel().getCartItemList().get(0)
                                .getFoodImage())
                        .into(img_food_image);

            }
        } else {
            Toast.makeText(this, "Shipping Order Is NUll", Toast.LENGTH_SHORT).show();
        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // Add a marker in Sydney and move the camera
                LatLng locationShipper = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                if (ShipperMarker == null) {
                    int hieght, width;

                    hieght = width = 80;
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                            .getDrawable(ShippingActivity.this, R.drawable.shipper);
                    Bitmap resized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, hieght, false);
                    ShipperMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(resized))
                            .position(locationShipper).title("You"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 15));
                } else {
                    ShipperMarker.setPosition(locationShipper);
                }

                if (isInit && previousLocation != null) {
                    LatLng previousLocationLatLng = new LatLng(previousLocation.getLatitude(),
                            previousLocation.getLongitude());
                    MarkerAnimation.animateMarkerToGB(ShipperMarker, locationShipper, new LatLngInterpolator.Spherical());
                    ShipperMarker.setRotation(Common.setBearing(locationShipper, previousLocationLatLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(locationShipper));
                    previousLocation=locationResult.getLastLocation();
                }
                if (!isInit){
                    isInit=true;
                    previousLocation=locationResult.getLastLocation();
                }
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(15000); //15s
        locationRequest.setFastestInterval(10000);//10s
        locationRequest.setSmallestDisplacement(20f);//20m
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                    R.raw.uber_light_with_label));
            if (!success)
                Log.e("err", "Style parsing Failed");
        } catch (Resources.NotFoundException ex) {
            Log.e("err", "Style not Found");
        }

    }

    @Override
    protected void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }
}
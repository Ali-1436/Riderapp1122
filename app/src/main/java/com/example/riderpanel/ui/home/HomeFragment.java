package com.example.riderpanel.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.riderpanel.Callback.lFirebaseDriverInfoListener;
import com.example.riderpanel.Callback.lFirebaseFailedListener;
import com.example.riderpanel.Common.Common;
import com.example.riderpanel.Model.DriverGeoModel;
import com.example.riderpanel.Model.DriverInfoModel;
import com.example.riderpanel.Model.GeoQueryModel;
import com.example.riderpanel.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment implements OnMapReadyCallback, lFirebaseFailedListener, lFirebaseDriverInfoListener {

    private GoogleMap mMap;
    private HomeViewModel homeViewModel;
    private SupportMapFragment mapFragment;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private boolean firstTime = true;
    FirebaseDatabase database;
    DatabaseReference databaseReference,current;
    //Location
    private double distance = 1.0;  //default in kms
    private static final double LIMIT_RANGE = 10.0;//km
    private Location previousLocaion, currentLocation; //use to calculate

    //Listener
    lFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    lFirebaseFailedListener iFirebaseFailedListener;
    private String cityName;


    @Override
    public void onDestroy() {

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);


        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);


        View root = inflater.inflate(R.layout.fragment_home, container, false);
        inti();
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        return root;
    }

    private void inti() {
        iFirebaseFailedListener = this;
        iFirebaseDriverInfoListener = this;

        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                LatLng newposition = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newposition, 18f));


                    //if user has change location, calculate and load driver again
                if (firstTime) {


                    previousLocaion = currentLocation = locationResult.getLastLocation();
                    firstTime = false;
                    Snackbar.make(getView(), "code chal rah hy    first time", Snackbar.LENGTH_SHORT).show();
                } else {
                    previousLocaion = currentLocation;
                    currentLocation = locationResult.getLastLocation();

                }

                if (previousLocaion.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE) // Not Over Range
                { Snackbar.make(getView(), "code chal rah hy    loadavaiabledrivers", Snackbar.LENGTH_SHORT).show();
                    loadavaiabledrivers();
                } else {//do nothing
                }

            }

        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        loadavaiabledrivers();

    }

    private void loadavaiabledrivers() {


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


//        database= FirebaseDatabase.getInstance();
//       databaseReference=database.getReference(Common.DRIVERS_LOCATION_REFERENCES);


        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Location>() {

            public void onSuccess(Location location) {

                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addressList;


                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    cityName = addressList.get(0).getLocality();


                    //
                    DatabaseReference driver_location_ref= FirebaseDatabase.getInstance()
                            .getReference(Common.DRIVERS_LOCATION_REFERENCES)
                            .child(cityName);
                    Snackbar.make(getView(), driver_location_ref +"", Snackbar.LENGTH_LONG).show();




                    GeoFire gf=new GeoFire(driver_location_ref);


                    GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(location.getLatitude(),location.getLongitude()),1.0);
                    geoQuery.removeAllListeners();


//                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//                        @Override
//                        public void onKeyEntered(String key, GeoLocation location) {
//                            Snackbar.make(getView(), "laction wala b chal raha hy  4", Snackbar.LENGTH_SHORT).show();
////                        }
//                        }
//
//                        @Override
//                        public void onKeyExited(String key) {
//                            Snackbar.make(getView(), "laction wala b chal raha hy    1", Snackbar.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onKeyMoved(String key, GeoLocation location) {
//                            Snackbar.make(getView(), "laction wala b chal raha hy    2", Snackbar.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onGeoQueryReady() {
//                            Snackbar.make(getView(), "laction wala b chal raha hy    3", Snackbar.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onGeoQueryError(DatabaseError error) {
//                            Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
//
//
//                        }
//                    });
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {

                            Common.driverFound.add(new DriverGeoModel(key,location));
                            Snackbar.make(getView(), "laction wala b chal raha hy  4", Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onKeyExited(String key) {
                            Snackbar.make(getView(), "laction wala b chal raha hy    1", Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                            Snackbar.make(getView(), "laction wala b chal raha hy", Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onGeoQueryReady() {


                            Snackbar.make(getView(), "onGeoQueryReady  wala b chal raha hy", Snackbar.LENGTH_SHORT).show();
                            if(distance <= LIMIT_RANGE){

                                Snackbar.make(getView(), "code chal rah hy continue search", Snackbar.LENGTH_SHORT).show();
                                distance++;
                                loadavaiabledrivers();//Continue search in new distance
                            }else {
                                distance=1.0;//Rest it

                                Snackbar.make(getView(), "code chal rah hy Driver wala chal rahy", Snackbar.LENGTH_SHORT).show();
                                addDiverMaker();
                            }
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                            Snackbar.make(getView(), "DatabaseError chal rah hy Driver wala chal rahy", Snackbar.LENGTH_SHORT).show();
                            Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });

                //listen to new driver in city and range
                    driver_location_ref.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Snackbar.make(getView(), "code chal rah hy 6", Snackbar.LENGTH_SHORT).show();
                        GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                        GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                geoQueryModel.getL().get(1));
                        DriverGeoModel driverGeoModel = new DriverGeoModel(snapshot.getKey(), geoLocation);
                        Location newDriverLoaction = new Location("");
                        newDriverLoaction.setLatitude(geoLocation.latitude);
                        newDriverLoaction.setLongitude(geoLocation.longitude);
                        Float newDistnace = location.distanceTo(newDriverLoaction) / 1000;
                        if (newDistnace <= LIMIT_RANGE)
                            findDriverByKey(driverGeoModel);//if driver in range, add to map


                    }


                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                }catch(IOException e)
                {
                    e.printStackTrace();
                    Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();

                }


            }
        });

    }

    private void addDiverMaker() {
        if(Common.driverFound.size() > 0){
            Snackbar.make(getView(), "code chal rah hy 5", Snackbar.LENGTH_SHORT).show();

            Observable.fromIterable(Common.driverFound).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(driverGeoModel -> {
                        findDriverByKey(driverGeoModel);
                    },throwable -> {
                        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                    },()->{});


        }else {
            Snackbar.make(getView(),"driver not found",Snackbar.LENGTH_SHORT).show();
        }
    }

    private void findDriverByKey(DriverGeoModel driverGeoModel) {
        Snackbar.make(getView(), "code chal rah hy  1", Snackbar.LENGTH_SHORT).show();
        FirebaseDatabase.getInstance().getReference(Common.DRIVER_INFO_REFERENCE)
                .child(driverGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren())
                        { Snackbar.make(getView(), "code chal rah hy   2", Snackbar.LENGTH_SHORT).show();
                            driverGeoModel.setDriverInfoModel(snapshot.getValue(DriverInfoModel.class));
                            iFirebaseDriverInfoListener.OnDriverInfoLoadSuccess(driverGeoModel);
                        }else{

                            iFirebaseFailedListener.OnFirebasedLoadFaild("Not found key"+driverGeoModel.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iFirebaseFailedListener.OnFirebasedLoadFaild(error.getMessage());
                    }

                });
    }



    @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                Dexter.withContext(getContext())
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                mMap.setMyLocationEnabled(true);
                                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                                    @Override
                                    public boolean onMyLocationButtonClick() {
                                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return false;
                                        }
                                        fusedLocationProviderClient.getLastLocation()
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnSuccessListener(new OnSuccessListener<Location>() {
                                                    @Override
                                                    public void onSuccess(Location location) {
                                                        LatLng userLatling = new LatLng(location.getLatitude(), location.getLongitude());
                                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatling, 18f));
                                                    }
                                                });
                                        return true;
                                    }
                                });

                                View loctionbutton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                        .getParent()).findViewById(Integer.parseInt("2"));
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) loctionbutton.getLayoutParams();
                                //Right button
                                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                                params.setMargins(0, 0, 0, 250);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                                Snackbar.make(getView(), "code chal rah hy yaha denied hori hay", Snackbar.LENGTH_SHORT).show();
                                Snackbar.make(getView(), permissionDeniedResponse.getPermissionName() + " need enale", Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                            }
                        }).check();
                        mMap.getUiSettings().setZoomControlsEnabled(true);
                try {
                    boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.uber_maps_style));
                    if (!success)
                        Snackbar.make(getView(), " Load map style failed ", Snackbar.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();

                }

            }

    @Override
    public void OnFirebasedLoadFaild(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void OnDriverInfoLoadSuccess(DriverGeoModel driverGeoModel) {
        //if already have maker with this key,do`nt set agin

        DriverInfoModel driverInfoModel=new DriverInfoModel();
        if (!Common.makerList.containsKey(driverGeoModel.getKey()))
            Snackbar.make(getView(), "code chal rah hy    3", Snackbar.LENGTH_SHORT).show();
            Common.makerList.put(driverGeoModel.getKey(),mMap.addMarker(new MarkerOptions()
            .position(new LatLng(driverGeoModel.getGeolocation().latitude,
                    driverGeoModel.getGeolocation().longitude)).flat(true)
            .title(Common.buildName(driverInfoModel.getFisrtName(),
                    driverInfoModel.getLastname())).snippet(driverInfoModel.getPhone())
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.sebus))));
        if(!TextUtils.isEmpty(cityName))
        {
            DatabaseReference driverLocation =FirebaseDatabase.getInstance()
                    .getReference(Common.DRIVERS_LOCATION_REFERENCES)
                    .child(cityName)
                    .child(driverGeoModel.getKey());
            driverLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(Common.makerList.get(driverGeoModel.getKey()) != null)
                        Common.makerList.get(driverGeoModel.getKey()).remove();//remove maker
                    Common.makerList.remove(driverGeoModel.getKey());//remove maker info form hash map
                    driverLocation.removeEventListener(this);//remove event listener
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
                }
        );


        }




    }
            }




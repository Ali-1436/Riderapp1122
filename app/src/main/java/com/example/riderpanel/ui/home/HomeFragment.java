package com.example.riderpanel.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.example.riderpanel.Callback.lFirebaseDriverInfoListener;
import com.example.riderpanel.Callback.lFirebaseFailedListener;
import com.example.riderpanel.Common.Common;
import com.example.riderpanel.Model.AnimationModel;
import com.example.riderpanel.Model.DriverGeoModel;
import com.example.riderpanel.Model.DriverInfoModel;
import com.example.riderpanel.Model.GeoQueryModel;
import com.example.riderpanel.R;
import com.example.riderpanel.Remote.IGoogleAPI;
import com.example.riderpanel.Remote.RerofitClient;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
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

    private List<LatLng> polylinelist;
    private Handler handler;
    private int index,next;
    private LatLng start,end;
    private float v;
    private double lat,lng;
        private FirebaseDatabase databaseref;


    //Listener
    lFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    lFirebaseFailedListener iFirebaseFailedListener;
    private String cityName;

    
    //
    private CompositeDisposable compositeDisposable =new CompositeDisposable();
    private IGoogleAPI iGoogleApI;

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

        iGoogleApI = RerofitClient.getInstance().create(IGoogleAPI.class);
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

                    DatabaseReference databaseReference;
                    //
               databaseref = FirebaseDatabase.getInstance();
                           databaseReference =databaseref.getReference(Common.DRIVERS_LOCATION_REFERENCES)
                            .child(cityName);
                    Snackbar.make(getView(), databaseReference +"", Snackbar.LENGTH_LONG).show();




                    GeoFire gf=new GeoFire(databaseReference);


                    GeoQuery geoQuery=gf.queryAtLocation(new GeoLocation(location.getLatitude(),location.getLongitude()),distance);
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
                            Snackbar.make(getView(), "onKeyEntered  4", Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onKeyExited(String key) {
                            Snackbar.make(getView(), " onKeyExitedwala b chal raha hy    1", Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                            Snackbar.make(getView(), "onKeyMoved wala b chal raha hy", Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onGeoQueryReady() {


                            Snackbar.make(getView(), "onGeoQueryReady  wala b chal raha hy", Snackbar.LENGTH_LONG).show();
                            if(distance <= LIMIT_RANGE){


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

//            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    Toast.makeText(getContext(),"onChildAdded", Toast.LENGTH_LONG).show();
//                    GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
////                    Toast.makeText(getContext(),"onChildAdded = "+geoQueryModel.getL().get(0)+" one more ="+geoQueryModel.getL().get(1), Toast.LENGTH_LONG).show();
//                        GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
//                                geoQueryModel.getL().get(1));
//                        DriverGeoModel driverGeoModel = new DriverGeoModel(snapshot.getKey(), geoLocation);
//                        Location newDriverLoaction = new Location("");
//                        newDriverLoaction.setLatitude(geoLocation.latitude);
//                        newDriverLoaction.setLongitude(geoLocation.longitude);
//                        Float newDistnace = location.distanceTo(newDriverLoaction) / 1000;
//                        if (newDistnace <= LIMIT_RANGE)
//                            findDriverByKey(driverGeoModel);//if driver in range, add to map
//
//
//
//                }
    //
    //                @Override
    //                public void onCancelled(@NonNull DatabaseError error) {
    //                    Toast.makeText(getContext()," onCancelled",Toast.LENGTH_LONG).show();
    //                }
    //            });

//                    databaseReference.addChildEventListener(new ChildEventListener() {
//                        @Override
//                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                            Toast.makeText(getContext(),"onChildAdded",Toast.LENGTH_LONG).show();
//                        }
//
//                        @Override
//                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                            Toast.makeText(getContext(),"onChildChanged",Toast.LENGTH_LONG).show();
//                        }
//
//                        @Override
//                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//                        }
//
//                        @Override
//                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                            Toast.makeText(getContext()," onChildMoved",Toast.LENGTH_LONG).show();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Toast.makeText(getContext()," onCancelled",Toast.LENGTH_LONG).show();
//                        }
//                    });
//                    databaseReference.addChildEventListener(new ChildEventListener() {
//                        @Override
//                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//                            Snackbar.make(getView(), " DataSnapshot code chal rAHA HY", Snackbar.LENGTH_SHORT).show();
//                        GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
//                        GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
//                                geoQueryModel.getL().get(1));
//                        DriverGeoModel driverGeoModel = new DriverGeoModel(snapshot.getKey(), geoLocation);
//                        Location newDriverLoaction = new Location("");
//                        newDriverLoaction.setLatitude(geoLocation.latitude);
//                        newDriverLoaction.setLongitude(geoLocation.longitude);
//                        Float newDistnace = location.distanceTo(newDriverLoaction) / 1000;
//                        if (newDistnace <= LIMIT_RANGE)
//                            findDriverByKey(driverGeoModel);//if driver in range, add to map
//
//
//                        }
//
//
//                        @Override
//                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                            Snackbar.make(getView(), "onChildChanged code chal rah hy 6", Snackbar.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                            Snackbar.make(getView(), "onChildRemovecode chal rah hy 6", Snackbar.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                            Snackbar.make(getView(), " onChildMoved chal rah hy 6", Snackbar.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
//                        }
//                    });

                //listen to new driver in city and range

                }catch(IOException e)
                {
                    e.printStackTrace();
                    Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();

                }


            }
        });

    }

    private void addDiverMaker() {
//        if(Common.driverFound.size() > 0){
            Snackbar.make(getView(), "code chal rah hy 5", Snackbar.LENGTH_SHORT).show();

            final Disposable subscribe = Observable.fromIterable(Common.driverFound).subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(driverGeoModel -> {
                        findDriverByKey(driverGeoModel);
                    }, throwable -> {
                        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                    }, () -> {
                    });


//        }else {
//            Snackbar.make(getView(),"driver not found",Snackbar.LENGTH_SHORT).show();
//        }
    }

    private void findDriverByKey(DriverGeoModel driverGeoModel) {
        Snackbar.make(getView(),"findDriverByKey",Snackbar.LENGTH_SHORT).show();
//        }
        FirebaseDatabase.getInstance().getReference(Common.DRIVER_INFO_REFERENCE)
                .child(driverGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren())
                        { Snackbar.make(getView(),"onDataChange",Snackbar.LENGTH_SHORT).show();
//        }
                            driverGeoModel.setDriverInfoModel(snapshot.getValue(DriverInfoModel.class));
                            iFirebaseDriverInfoListener.OnDriverInfoLoadSuccess(driverGeoModel);
                        }else{
                            Snackbar.make(getView(),"Not found key",Snackbar.LENGTH_SHORT).show();
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
                    if(!snapshot.hasChildren()) {
                        if (Common.makerList.get(driverGeoModel.getKey()) != null)
                            Common.makerList.get(driverGeoModel.getKey()).remove();//remove maker
                        Common.makerList.remove(driverGeoModel.getKey());//remove maker info form hash map
                       Common.driverlocationsubcribe.remove(driverGeoModel.getKey());
                        driverLocation.removeEventListener(this);//remove event listener
                    }else
                    {
                        if (Common.makerList.get(driverGeoModel.getKey()) !=null){
                            GeoQueryModel geoQueryModel=snapshot.getValue(GeoQueryModel.class);
                            AnimationModel animationModel=new AnimationModel(false,geoQueryModel);
                            if(Common.driverlocationsubcribe.get(driverGeoModel.getKey()) != null){


                                Marker currentmarker = Common.makerList.get(driverGeoModel.getKey());
                                AnimationModel oldposition = Common.driverlocationsubcribe.get(driverGeoModel.getKey());


                                String from = new StringBuilder()
                                        .append(oldposition.getGeoQueryModel().getL().get(0))
                                        .append(",")
                                        .append(oldposition.getGeoQueryModel().getL().get(1))
                                        .toString();

                                String to = new StringBuilder()
                                        .append(animationModel.getGeoQueryModel().getL().get(0))
                                        .append(",")
                                        .append(animationModel.getGeoQueryModel().getL().get(1))
                                        .toString();

                                movemarkerAnimation (driverGeoModel.getKey(),animationModel,currentmarker,from,to);

                            }else{
                                Common.driverlocationsubcribe.put(driverGeoModel.getKey(),animationModel);
                            }
                        }

                    }

                    }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
                }
        );


        }




    }

    private void movemarkerAnimation(String key, AnimationModel animationModel, Marker currentmarker, String from, String to) {
        if (!animationModel.isRun())
        {
//            //Request API
            compositeDisposable.add(iGoogleApI.getDirection("driving",
                    "less_driving",
                    from,to,
                    getString(R.string.google_api_key))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(returnResult -> {
                        Log.d("API_RETURN", returnResult);



                                                try {
                            JSONObject jsonObject = new JSONObject(returnResult);
                            JSONArray jsonArray = jsonObject.getJSONArray("routes");

                            for (int i = 0; i <= jsonArray.length(); i++) {
                                JSONObject route = jsonArray.getJSONObject(i);
                                JSONObject poly = route.getJSONObject("Overview_polyline");
                                String polyline = poly.getString("points");
                                polylinelist = Common.decodepoly(polyline);


                            }


                            handler = new Handler();
                            index = -1;
                            next = 1;

                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (polylinelist.size() > 1) {
                                        if (index < polylinelist.size() - 2) {
                                            index++;
                                            next = index + 1;
                                            start = polylinelist.get(index);
                                            end = polylinelist.get(next);
                                        }

                                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
                                        valueAnimator.setDuration(3000);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(value -> {
                                            v = value.getAnimatedFraction();
                                            lat = v * end.latitude + (1 - v) * start.latitude;
                                            lng = v * end.longitude + (1 - v) * start.longitude;
                                            LatLng newPos = new LatLng(lat, lng);
                                            currentmarker.setPosition(newPos);
                                            currentmarker.setAnchor(0.5f, 0.5f);
                                            currentmarker.setRotation(Common.grtBearing(start, newPos));


                                        });

                                        valueAnimator.start();
                                        if (index < polylinelist.size() - 2) //destination
                                            handler.postDelayed(this,1500);
                                        else if(index < polylinelist.size() - 1) //done
                                        {
                                            animationModel.setRun(false);
                                           Common.driverlocationsubcribe.put(key,animationModel); //Update dots
                                        }


                                    }
                                }

                            };
                            //Run Handler
                            handler.postDelayed(runnable,1500);


                        }catch (Exception e){
                          Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();

                        }
//
                    })
            );
//
    }
}
}


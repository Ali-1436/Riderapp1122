package com.example.riderpanel.Common;

import com.example.riderpanel.Model.DriverGeoModel;
import com.example.riderpanel.Model.Model;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Common {
    public static final String RIDER_INFO_REFERENCE="Riders";
    public static final String DRIVERS_LOCATION_REFERENCES ="DriversLocation" ;
    public static final String DRIVER_INFO_REFERENCE ="DriverInfo" ;
    public static Model currentRider;
    public static Set<DriverGeoModel> driverFound =new HashSet<DriverGeoModel>();
    public static HashMap<String, Marker> makerList=new HashMap<>();

    public static StringBuilder buildWelcomeMessage() {

        if(Common.currentRider !=null)
        {
            return  new StringBuilder("welcom ")
                    .append(Common.currentRider.getFirstname())
                    .append(" ")
                    .append(Common.currentRider.getLastname().toString()) ;

        }else

            return null;
    }


    public  static  String buildName(String firstname ,String lastname){
return new StringBuilder(firstname).append("").append(lastname).toString();

    }
}

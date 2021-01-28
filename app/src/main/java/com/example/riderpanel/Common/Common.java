package com.example.riderpanel.Common;

import com.example.riderpanel.Model.AnimationModel;
import com.example.riderpanel.Model.DriverGeoModel;
import com.example.riderpanel.Model.Model;
import com.example.riderpanel.Model.RiderInfoModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {
    public static final String RIDER_INFO_REFERENCE="RidersInFo";
    public static final String DRIVERS_LOCATION_REFERENCES ="DriversLocation" ;
    public static final String DRIVER_INFO_REFERENCE ="DriverInfo" ;
    public static Model currentRider;
    public static Set<DriverGeoModel> driverFound =new HashSet<DriverGeoModel>();
    public static HashMap<String, Marker> makerList=new HashMap<>();
    public static HashMap<String, AnimationModel> driverlocationsubcribe = new HashMap<String, AnimationModel>();
    public static RiderInfoModel currentUser;

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

    public static List<LatLng> decodepoly(String encoded) {
        List poly = new ArrayList();
        int index=0,len=encoded.length();
        int lat=0,lng=0;
        while(index < len)
        {
            int b,shift=0,result=0;
            do{
                b=encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;

            }while(b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do{
                b = encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift +=5;
            }while(b >= 0x20);
            int dlng = ((result & 1)!=0 ? ~(result >> 1): (result >> 1));
            lng +=dlng;

            LatLng p = new LatLng((((double)lat / 1E5)),
                    (((double)lng/1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static float grtBearing(LatLng begin, LatLng end) {
        //You can copy this function by link at description
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }
}

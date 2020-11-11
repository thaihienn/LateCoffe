package com.hien.latecoffeeshipper.common;

import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public interface LatLngInterpolator {
    LatLng interpolate(float fraction, LatLng a, LatLng b);

    class Linear implements LatLngInterpolator {
        @Override
        public LatLng interpolate(float fraction, LatLng a, LatLng b) {
            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
            double lng = (b.longitude - a.longitude) * fraction + b.longitude;
            return new LatLng(lat, lng);
        }
    }

    class LinearFixed implements LatLngInterpolator {
        @Override
        public LatLng interpolate(float fraction, LatLng a, LatLng b) {
            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
            double lngDelta = b.longitude - a.longitude;
            if (Math.abs(lngDelta) > 180) {
                lngDelta = lngDelta - Math.signum(lngDelta) * 360;
            }

            return new LatLng(lat, lngDelta);
        }
    }

    class Spherical implements LatLngInterpolator {
        @Override
        public LatLng interpolate(float fraction, LatLng form, LatLng to) {

            double formLat = toRadians(form.latitude);
            double formLng = toRadians(form.longitude);
            double toLat = toRadians(to.latitude);
            double toLng = toRadians(to.longitude);

            //compute spherical interpolation cofficients
            double angle = computeAngleBetween(formLat, formLng, toLat, toLng);
            double sinAngle = sin(angle);
            if (sinAngle < 1E-6)
                return form;
            double a = sin((1 - fraction) * angle) / sinAngle;
            double b = sin(fraction * angle) / sinAngle;

            //converts form polar to vector and interpolate
            double x = a * cos(formLat) * cos(formLng) + b * cos(toLat) * cos(toLng);
            double y = a * cos(formLat) * sin(formLng) + b * cos(toLat) * sin(toLng);
            double z = a * sin(formLat) + b * sin(toLat);

            //converts interpolated vector back to polar
            double lat= atan2(z,sqrt(x*x+y*y));
            double lng=atan2(x,y);
            return new LatLng(toDegrees(lat),toDegrees(lng));
        }

        private double computeAngleBetween(double formLat, double formLng, double toLat, double toLng) {
            double dLat = formLat = toLat;
            double dLng = formLng - toLng;
            return 2 * asin(sqrt(pow(sin(dLat / 2), 2) + cos(formLat) * cos(toLat) * pow(sin(dLng / 2), 2)));

        }
    }
}

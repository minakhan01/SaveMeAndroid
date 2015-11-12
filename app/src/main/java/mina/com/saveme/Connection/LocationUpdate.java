package mina.com.saveme.Connection;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import mina.com.saveme.Interfaces.ConnectionListener;
import mina.com.saveme.Interfaces.UpdateListener;
import mina.com.saveme.Services.FetchAddressIntentService;
import mina.com.saveme.StaticHelpers.Constants;

/**
 * Created by khan32m on 8/19/15.
 */
public class LocationUpdate implements ConnectionListener, LocationListener {

  private GoogleApiClient mGoogleApiClient;
  private Location mLastLocation;
  private LocationRequest mLocationRequest;
  private static final String TAG = "LocationUpdate";
  private UpdateListener updateListener;

  @Override
  public void connectionChanged() {
    updateLocation();
  }

  public LocationUpdate(GoogleApiClient mGoogleApiClient, UpdateListener updateListener){
    this.mGoogleApiClient = mGoogleApiClient;
    this.updateListener = updateListener;
  }

  public void updateLocation(){
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    if (mLastLocation != null) {
      // Determine whether a Geocoder is available.
      updateListener.updateLocation(mLastLocation);
    }
}

  @Override
  public void onLocationChanged(Location location) {
    Log.i(TAG, location.toString());
  }
}

package mina.com.saveme.Connection;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import mina.com.saveme.Interfaces.ConnectionListener;

public class GoogleConnection implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private GoogleApiClient mGoogleApiClient;
  private WeakReference<Activity> activityWeakReference;
  private List<ConnectionListener> connectionListeners;
  private static GoogleConnection sGoogleConnection;
  private static final String TAG = "GoogleConnection";

  @Override
  public void onConnected(Bundle bundle) {
    Log.d(TAG, "onConnected");
    connectionUpdated();
  }

  @Override
  public void onConnectionSuspended(int cause) {
    mGoogleApiClient.connect();
  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {
    Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
  }

  public void addConnectionListener(ConnectionListener connectionListener){
    connectionListeners.add(connectionListener);
    Log.d(TAG, "addConnectionListener");
  }

  public GoogleApiClient getGoogleApiClient(){
    Log.d(TAG, "getGoogleApiClient");
    return mGoogleApiClient;
  }

  public static GoogleConnection getInstance(Activity activity) {
    if (null == sGoogleConnection) {
      sGoogleConnection = new GoogleConnection(activity);
    }

    return sGoogleConnection;
  }

  private GoogleConnection(Activity activity) {
    activityWeakReference = new WeakReference<>(activity);
    connectionListeners = new ArrayList<ConnectionListener>();
    mGoogleApiClient = new GoogleApiClient.Builder(activityWeakReference.get().getApplicationContext())
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
    mGoogleApiClient.connect();
    Log.d(TAG, "constructing GoogleConnection");
  }

  private void connectionUpdated() {
    Log.d(TAG, "connectionUpdated");
    notifyObservers();
  }

  private void notifyObservers(){
    for (ConnectionListener connectionListener: connectionListeners){
      connectionListener.connectionChanged();
    }
  }

}

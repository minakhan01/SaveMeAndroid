package mina.com.saveme.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import mina.com.saveme.R;
import mina.com.saveme.StaticHelpers.Constants;

public class SignupActivity extends ActionBarActivity {

  private SharedPreferences prefs;
  private static String TAG = "SignupActivity";
  private boolean validResponses;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    validResponses = false;
    setContentView(R.layout.activity_signup);
    checkAppDataAndIfRedirect();
  }

  private void checkAppDataAndIfRedirect(){
    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    boolean userExists = prefs.getBoolean(Constants.USER_EXISTS_PREF_KEY, false); // get value of last login status
    if (userExists){
      startSaveMeActivity();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void signUpNext(View view){
    checkResponses();
    if (validResponses){
    prefs.edit().putBoolean(Constants.USER_EXISTS_PREF_KEY, true).commit();
    startSaveMeActivity();}
    else {
      Log.d(TAG, "Invalid Responses");
    }
  }

  private void startSaveMeActivity(){
    Intent intent = new Intent(this, SaveMeActivity.class);
    startActivity(intent);
  }

  private void checkResponses(){
    //TODO
    validResponses = true;
  }
}

package mina.com.saveme.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

import mina.com.saveme.Connection.GoogleConnection;
import mina.com.saveme.Connection.LocationUpdate;
import mina.com.saveme.Database.SaveMeSQLiteHelper;
import mina.com.saveme.Database.SaviorContactsContract;
import mina.com.saveme.Interfaces.ConnectionListener;
import mina.com.saveme.Interfaces.UpdateListener;
import mina.com.saveme.Models.SaviorContact;
import mina.com.saveme.R;
import mina.com.saveme.Services.FetchAddressIntentService;
import mina.com.saveme.StaticHelpers.Constants;
import mina.com.saveme.Utils.SMSsender;

/**
 * Created by khan32m on 8/3/15.
 */
public class SaveMeActivity extends Activity implements UpdateListener {

  private GoogleConnection sGoogleConnection;
  private Location mLastLocation;
  private String mAddressOutput;
  private TextView mLocationAddressTextView;
  private AddressResultReceiver mResultReceiver;
  private SharedPreferences prefs;
  private List<SaviorContact> saviorContacts;
  private static String TAG = "SaveMeActivity";
  private static SMSsender saveMeMessenger;
  private ArrayAdapter<String> arrayAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_saveme);
    prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    boolean saviorsExist = prefs.getBoolean(Constants.SAVIORS_EXIST_PREF_KEY, false);
    saveMeMessenger = SMSsender.getInstance();
      saviorContacts = readDBContacts();
      ListView listView = (ListView)findViewById(R.id.list_item);
      List<String> saviorNames = getSaviorName(saviorContacts);
      arrayAdapter =
          new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, saviorNames);
      // Set The Adapter
      listView.setAdapter(arrayAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> listView, View itemView, int itemPosition, long itemId) {
        Log.d(TAG, saviorContacts.get(itemPosition).toString());
        final int iPosition = itemPosition;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            switch (which){
              case DialogInterface.BUTTON_POSITIVE:
                deleteContactInDB(saviorContacts.get(iPosition));
                break;

              case DialogInterface.BUTTON_NEGATIVE:
                break;
            }
          }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
        builder.setMessage("Remove "+saviorContacts.get(itemPosition).getDisplayName()+"?").setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show();
      }
    });
      startLocationUpdates();
  }

  private List<String> getSaviorName(List<SaviorContact> saviorContacts){
    List<String> saviorNames = new ArrayList<String>();
    for (SaviorContact saviorContact: saviorContacts){
      saviorNames.add(saviorContact.getDisplayName());
    }
    return saviorNames;
  }

  private void addSaviors() {
    Intent intent= new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    startActivityForResult(intent, Constants.PICK_CONTACTS);
  }

  @Override
  public void onActivityResult(int reqCode, int resultCode, Intent data) {
    super.onActivityResult(reqCode, resultCode, data);
    Log.d(TAG, "onActivityResult");
    switch (reqCode) {
      case (Constants.PICK_CONTACTS):
        if (resultCode == Activity.RESULT_OK) {
          Log.d(TAG, "case (Constants.PICK_CONTACTS)");
          Uri contactData = data.getData();
          String id, name, phone, hasPhone;
          int idx;
          Cursor cursor =  getContentResolver().query(contactData, null, null, null, null);
          if (cursor.moveToFirst()) {
            idx = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            id = cursor.getString(idx);

            idx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            name = cursor.getString(idx);

            idx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
            hasPhone = cursor.getString(idx);
            Log.d(TAG, "hasPhone: "+hasPhone);
            if (hasPhone.equals("1")) {
              Cursor phones = getContentResolver().query(
                  ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                  ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                  null, null);
              phones.moveToFirst();
              phone = phones.getString(phones.getColumnIndex("data1"));
              System.out.println("number is:"+phone);
              SaviorContact contact = new SaviorContact(id, name, phone);
              addContactInDB(contact);
              Log.d(TAG, contact.toString());
              prefs.edit().putBoolean(Constants.SAVIORS_EXIST_PREF_KEY, true).commit();
            }
          }
        }
        break;
    }
  }

  public void addContactInDB(SaviorContact contact){
    // Gets the data repository in write mode
    SQLiteDatabase db = SaveMeSQLiteHelper.getInstance(this).getWritableDatabase();

    // Create a new map of values, where column names are the keys
    ContentValues values = new ContentValues();
    values.put(SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_LOOKUPKEY, contact.getLookUpKey());
    values.put(SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_USERNAME, contact.getDisplayName());
    values.put(SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_PHONE, contact.getPhoneNumber());

    // Insert the new row, returning the primary key value of the new row
    long newRowId;
    newRowId = db.insert(
        Constants.SAVIOR_CONTACTS_TABLE,
        null,
        values);
    updateContacts();
  }

  public void deleteContactInDB(SaviorContact contact){
    // Gets the data repository in write mode
    SQLiteDatabase db = SaveMeSQLiteHelper.getInstance(this).getWritableDatabase();

    // Define 'where' part of query.
    String selection = SaviorContactsContract.SaviorContactsEntry._ID + " LIKE ?";
// Specify arguments in placeholder order.
    String[] selectionArgs = { String.valueOf(contact.getId()) };
// Issue SQL statement.
    db.delete(Constants.SAVIOR_CONTACTS_TABLE, selection, selectionArgs);

    updateContacts();
  }

  private void updateContacts() {
    saviorContacts = readDBContacts();
    List<String> saviorNames = getSaviorName(saviorContacts);
    arrayAdapter.clear();
    arrayAdapter.addAll(saviorNames);
    arrayAdapter.notifyDataSetChanged();
  }

  public List<SaviorContact> readDBContacts(){
    SQLiteDatabase db = SaveMeSQLiteHelper.getInstance(this).getWritableDatabase();
    List<SaviorContact> saviorContacts = new ArrayList<SaviorContact>();
// Define a projection that specifies which columns from the database
// you will actually use after this query.
    String[] projection = {
        SaviorContactsContract.SaviorContactsEntry._ID,
        SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_LOOKUPKEY,
        SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_USERNAME,
        SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_PHONE
    };

// How you want the results sorted in the resulting Cursor
    String sortOrder =
        SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_USERNAME + " DESC";

    Cursor cursor = db.query(
        Constants.SAVIOR_CONTACTS_TABLE,  // The table to query
        projection,                               // The columns to return
        null,                                // The columns for the WHERE clause
        null,                            // The values for the WHERE clause
        null,                                     // don't group the rows
        null,                                     // don't filter by row groups
        sortOrder                                 // The sort order
    );

    cursor.moveToFirst();
    // looping through all rows and adding to list
    if (cursor.moveToFirst()) {
      do {
        int id = Integer.parseInt(cursor.getString(0));
        String lookupKey = cursor.getString(1);
        String name = cursor.getString(2);
        String phone = cursor.getString(3);
        SaviorContact contact = new SaviorContact(id, lookupKey, name, phone);
        Log.d(TAG, contact.toString());
        // Adding contact to list
        saviorContacts.add(contact);
      } while (cursor.moveToNext());
    }

    //TODO return contacts
    return saviorContacts;
  }

  private void startLocationUpdates(){
    sGoogleConnection = GoogleConnection.getInstance(this);
    addConnectionListeners();
    mLocationAddressTextView = (TextView) findViewById(R.id.location_text);
    mResultReceiver = new AddressResultReceiver(new Handler());
    mLocationAddressTextView.setText("detected location appears here");
  }

  public void updateLocation(Location location){
    this.mLastLocation = location;
    Log.d(TAG, "updateLocation: " + location.toString());
    startIntentService();
  }

  protected void startIntentService() {
    Intent intent = new Intent(this, FetchAddressIntentService.class);
    intent.putExtra(Constants.RECEIVER, mResultReceiver);
    intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
    startService(intent);
    Log.d(TAG, "startIntentService");
  }

  public void addConnectionListeners(){
    GoogleApiClient googleApiClient = sGoogleConnection.getGoogleApiClient();
    LocationUpdate locationUpdate = new LocationUpdate(googleApiClient, this);
    sGoogleConnection.addConnectionListener(locationUpdate);
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

  public void addContacts(View view){
    addSaviors();
  }

  public void saveMe(View view){
    sendSaveMeSMS();
  }

  private void sendSaveMeSMS(){
  EditText editText = (EditText) findViewById(R.id.optional_message);
  saveMeMessenger.sendSMS(saviorContacts, mAddressOutput, editText.getText().toString());}

  protected void displayAddressOutput() {
    mLocationAddressTextView.setText(mAddressOutput);
  }

  class AddressResultReceiver extends ResultReceiver {
    public AddressResultReceiver(Handler handler) {
      super(handler);
    }

    /**
     *  Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

      // Display the address string or an error message sent from the intent service.
      mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
      Log.d(TAG, mAddressOutput);
      displayAddressOutput();
    }
  }
}
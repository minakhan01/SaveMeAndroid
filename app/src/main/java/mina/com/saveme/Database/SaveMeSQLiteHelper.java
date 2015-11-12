package mina.com.saveme.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import mina.com.saveme.StaticHelpers.Constants;

public class SaveMeSQLiteHelper extends SQLiteOpenHelper {

  private static SaveMeSQLiteHelper sInstance;

  private static final String DATABASE_NAME = "SaveME.db";
  private static final int DATABASE_VERSION = 1;
  private static Context context;
  private SQLiteDatabase sqLiteDatabase;
  private static final int BUFFER_SIZE =      2048;
  private static final String TAG = "SaveMeSQLiteHelper";

  private static final String TEXT_TYPE = " TEXT";
  private static final String COMMA_SEP = ",";
  private static final String SQL_CREATE_SAVIOR_ENTRIES =
      "CREATE TABLE " + Constants.SAVIOR_CONTACTS_TABLE + " (" +
          SaviorContactsContract.SaviorContactsEntry._ID + " INTEGER PRIMARY KEY," +
          SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_LOOKUPKEY + TEXT_TYPE + COMMA_SEP +
          SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_USERNAME + TEXT_TYPE + COMMA_SEP +
          SaviorContactsContract.SaviorContactsEntry.COLUMN_NAME_PHONE + TEXT_TYPE +
          " )";
  private static final String SQL_DELETE_SAVIOR_ENTRIES =
      "DROP TABLE IF EXISTS " + Constants.SAVIOR_CONTACTS_TABLE;

  public static synchronized SaveMeSQLiteHelper getInstance(Context context) {

    // Use the application context, which will ensure that you
    // don't accidentally leak an Activity's context.
    // See this article for more information: http://bit.ly/6LRzfx
    if (sInstance == null) {
      sInstance = new SaveMeSQLiteHelper(context.getApplicationContext());
    }
    return sInstance;
  }

  /**
   * Constructor should be private to prevent direct instantiation.
   * make call to static method "getInstance()" instead.
   */
  private SaveMeSQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    this.sqLiteDatabase = sqLiteDatabase;
    sqLiteDatabase.execSQL(SQL_CREATE_SAVIOR_ENTRIES);
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
    sqLiteDatabase.execSQL(SQL_DELETE_SAVIOR_ENTRIES);
    onCreate(sqLiteDatabase);
  }

  @Override
  public synchronized void close() {
    if (sInstance != null)
      getReadableDatabase().close();
  }

}

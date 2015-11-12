package mina.com.saveme.Database;

import android.provider.BaseColumns;

import mina.com.saveme.StaticHelpers.Constants;

public final class SaviorContactsContract {
  // To prevent someone from accidentally instantiating the contract class,
  // give it an empty constructor.
  public SaviorContactsContract() {}

  /* Inner class that defines the table contents */
  public static abstract class SaviorContactsEntry implements BaseColumns {
    public static final String COLUMN_NAME_LOOKUPKEY = "lookupkey";
    public static final String COLUMN_NAME_ENTRY_ID = "entryid";
    public static final String COLUMN_NAME_USERNAME = "name";
    public static final String COLUMN_NAME_PHONE = "phone";
  }
}

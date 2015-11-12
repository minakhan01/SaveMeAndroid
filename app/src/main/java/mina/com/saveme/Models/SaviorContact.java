package mina.com.saveme.Models;

/**
 * Created by khan32m on 8/3/15.
 */
public class SaviorContact {
  private int id;
  private String displayName;
  private String lookUpKey;
  private String phoneNumber;

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public String getLookUpKey() {
    return lookUpKey;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getId() {
    return id;
  }

  public SaviorContact(String lookUpKey, String displayName, String phoneNumber){
    this.lookUpKey = lookUpKey;
    this.displayName = displayName;
    this.phoneNumber = phoneNumber;
  }

  public SaviorContact(int id, String lookUpKey, String displayName, String phoneNumber){
    this.id= id;
    this.lookUpKey = lookUpKey;
    this.displayName = displayName;
    this.phoneNumber = phoneNumber;
  }

  @Override
  public String toString(){
    return "Name: "+displayName+", Phone: "+phoneNumber;
  }
}

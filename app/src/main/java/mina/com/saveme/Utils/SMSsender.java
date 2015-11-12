package mina.com.saveme.Utils;

import android.telephony.SmsManager;

import java.util.List;

import mina.com.saveme.Models.SaviorContact;

public class SMSsender {
  private static SMSsender instance = null;

  private SMSsender(){}

  public static SMSsender getInstance(){
    if (instance == null)
      instance = new SMSsender();
    return instance;
  }

  public void sendSMS(List<SaviorContact> saviors, String location, String optionalMessage){
    SmsManager smsManager = SmsManager.getDefault();
    String smsMessageText = "SAVE ME! I am near "+ location+". "+optionalMessage;
    for (SaviorContact savior: saviors){
    smsManager.sendTextMessage(savior.getPhoneNumber(), null, smsMessageText, null, null);}
  }
}

/*
* 2016.5.12 Hong Gi Wook
* Assignment #3 –Messaging via Hybrid App
*
* Android activity, HTML, JavaScript, CSS
*
* Hybrid Android App using WebView and JavaScript
* User can send mobile massage using this app
* User input phone number(click button) and enter massage then push submit button
*
* Design
* - Hybrid Android App using WebView and JavaScript
* - Using webView, show HTML and javascript to user interface
* - If HTML number button clicked, clicked number send to Android using javascript function
* - Android concatenate number and return to javascript function
* - Using addJavascriptInterface that allow the Android app and the HTML page to ‘talk’ to each other
* - If submit button clicked, check phone number and massage (length > 0)
* - Then send mobile massage using sms manager and initialize phone number and message text
*
* Exception
* - Check Phone number or Message empty
* - Check Phone number is Numeric
*
* */


package com.example.hong.messaginghybridapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	WebView browser;
	Context mContext;

	String smsNum = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mContext = getApplicationContext();

		browser = (WebView) findViewById(R.id.webView);
		// Enable the javaScript code of visited pages to operate
		browser.getSettings().setJavaScriptEnabled(true);
		// Allow the Android app and the HTML page to ‘talk’ to each other using "Android"
		browser.addJavascriptInterface(new JavaScriptInterface(this), "Android");
		// if the html file is in the app's memory space use
		browser.loadUrl("file:///android_asset/local_web.html");

	}

	public class JavaScriptInterface {
		Context mContext;
		/** Instantiate the interface and set the context */
		JavaScriptInterface(Context c) {
			mContext = c;
		}
		/** Show a toast from the web page */
		// Any method that you want available to your javascript
		@JavascriptInterface
		public String writeNumber(String msg) { // Attach msg to MobileNumber string and return
			smsNum += msg;
			return smsNum;
		}
		@JavascriptInterface
		public void submit(String phone, String msg) { // HTML's submit button clicked

			if (checkException(phone, msg)){ // If phone number and message exist
				smsNum = "";
				Toast.makeText(getApplicationContext(), "submit", Toast.LENGTH_SHORT).show();
				sendSMS(phone, msg); // Send SMS to phone number
			} else {
				Toast.makeText(getApplicationContext(), "Empty!!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	// exception handling
	public boolean checkException(String phone, String msg) {
		if (phone.isEmpty() || msg.isEmpty() || !isNumeric(phone)) { // empty or isn't numeric
			Toast.makeText(getApplicationContext(), "Exception : " + "Enter number", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	// Whether phone number is number or not
	public boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		}
		catch(NumberFormatException nfe) {
			return false;
		}
		return true;
	}


	// Send smsText(message) to smsNumber(phone number)
	public void sendSMS(String smsNumber, String smsText) {
		// sms send
		PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
		// sms receive
		PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

		// The receiver will be called with any broadcast Intent that matches filter
		registerReceiver(new BroadcastReceiver() {
			// Called when the BroadcastReceiver is receiving an Intent broadcast
			@Override
			public void onReceive(Context context, Intent intent) {
				switch(getResultCode()){
					case Activity.RESULT_OK:
						// sms send
						Toast.makeText(mContext, "Submit ok", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						// sms send fail
						Toast.makeText(mContext, "Submit fail", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						// no service
						Toast.makeText(mContext, "No service locaton", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						// radio off
						Toast.makeText(mContext, "Radio is off", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						// PDU fail
						Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}, new IntentFilter("SMS_SENT_ACTION"));

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()){
					case Activity.RESULT_OK:
						// arrive
						Toast.makeText(mContext, "SMS Submit ok", Toast.LENGTH_SHORT).show();
						break;
					case Activity.RESULT_CANCELED:
						// not arrive
						Toast.makeText(mContext, "SMS Submit fail", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		}, new IntentFilter("SMS_DELIVERED_ACTION"));
		// declare sms manager
		SmsManager mSmsManager = SmsManager.getDefault();
		// using sms manager, send text massage
		mSmsManager.sendTextMessage(smsNumber, null, smsText, sentIntent, deliveredIntent);
	}
}
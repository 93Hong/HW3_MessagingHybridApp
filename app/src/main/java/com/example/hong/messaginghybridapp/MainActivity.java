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
* - Then send mobile massage using sms manager
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

	String smsNum = "", smsText = "";

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
		public void submit(String a, String b) { // HTML's submit button clicked
			Toast.makeText(getApplicationContext(), "submit", Toast.LENGTH_SHORT).show();
			if (a.length()>0 && b.length()>0){ // If phone number and message exist
				smsNum = a; smsText = b;
				sendSMS(smsNum, smsText); // Send SMS to phone number
			} else {
				Toast.makeText(getApplicationContext(), "모두 입력해 주세요", Toast.LENGTH_SHORT).show();
			}
		}
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
						Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						// sms send fail
						Toast.makeText(mContext, "전송 실패", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						// no service
						Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						// radio off
						Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
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
						Toast.makeText(mContext, "SMS 도착 완료", Toast.LENGTH_SHORT).show();
						break;
					case Activity.RESULT_CANCELED:
						// not arrive
						Toast.makeText(mContext, "SMS 도착 실패", Toast.LENGTH_SHORT).show();
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
package com.application.safety_alarm;



import java.util.Calendar;
import java.util.List;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class NewAppointmentActivity extends FragmentActivity{
	// Added by sverre---------------
	String debug = "debug";
	String chosenAP;
	TextView chosenWifi;
	TimePickerDialog timePickerDialog;
	final static int RQS_1 = 1;
	//--------------------------------
	
	
	private AppointmentsDataSource datasource;
	private Appointment newApp;
	private TextView dateView;
	private TextView timeView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_appointment);
	
		// Register the receiver
        registerReceiver(alarmReceiver,new IntentFilter("intent_filter"));
		
		newApp = new Appointment();
		Button changeStateButton = (Button)findViewById(R.id.change_state);
		changeStateButton.setText("Guardian");
		dateView = (TextView) findViewById(R.id.dateView);
		timeView = (TextView) findViewById(R.id.timeView);
		chosenWifi = (TextView)findViewById(R.id.wifiView);
		timeView.setText(newApp.getTime());
		dateView.setText(newApp.getDate());
		chosenWifi.setText("No access point choosen");
		

		
	}
	
	private BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.i("debug", "onReceive");
			Toast.makeText(arg0, "Alarm received!", Toast.LENGTH_SHORT).show();
			checkIfSelectedWifiIsInRange(arg0);	
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
	        String string = data.getStringExtra("RESULT_STRING");
	        chosenAP = string;
	        chosenWifi.setText(string);
	        Log.i("debug", ".onActivityResult -> " + chosenAP);  
	    }	
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_new_appointment, menu);
		return true;
	}
	
	
//	public void setTime(View v) {
//	    DialogFragment newFragment = new TimePickerFragment();
//	    newFragment.show(getSupportFragmentManager(), "timePicker");
//	}
//	public void setDate(View v) {
//	    DialogFragment newFragment = new DatePickerFragment();
//	    newFragment.show(getSupportFragmentManager(), "datePicker");
//	}
	
	//*************************** New timer****************************************
	public void openTimePickerDialog(View v){
		Log.i("debug", "db2");
		Calendar calendar = Calendar.getInstance();
		
		timePickerDialog = new TimePickerDialog(
				NewAppointmentActivity.this, 
				onTimeSetListener, 
				calendar.get(Calendar.HOUR_OF_DAY), 
				calendar.get(Calendar.MINUTE), 
				true);
		timePickerDialog.setTitle("Set Alarm Time");  
        
		timePickerDialog.show();

	}
    
    OnTimeSetListener onTimeSetListener = new OnTimeSetListener(){

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			Calendar calNow = Calendar.getInstance();
			Calendar calSet = (Calendar) calNow.clone();

			calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calSet.set(Calendar.MINUTE, minute);
			calSet.set(Calendar.SECOND, 0);
			calSet.set(Calendar.MILLISECOND, 0);
			
			if(calSet.compareTo(calNow) <= 0){
				//Today Set time passed, count to tomorrow
				calSet.add(Calendar.DATE, 1);
			}
			
			setAlarm(calSet);
		}};

	private void setAlarm(Calendar targetCal){

//		textAlarmPrompt.setText(
//				"\n\n***\n"
//				+ "Alarm is set@ " + targetCal.getTime() + "\n"
//				+ "***\n");
		
		
		Intent intent = new Intent("intent_filter");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
		
		// Schedule the alarm!
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		
		// For debugging if we want the thing to start after 1 seconds ----------------------
		Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 1);
        //am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        
        // Else comment the above and uncomment the line below-------------------------
        am.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
	}
	
	//*******************************************************************************************
	
	public void setWifi(View v){
		Log.i("debug", "setWifi");
		Intent intent = new Intent(this, wifiList.class);
		startActivityForResult(intent, 1);
		
	}
	public void confirmAppointment(View v){
		newApp.setSSID(String.valueOf(((EditText) findViewById(R.id.home_SSID)).getText()));
		newApp.setRecipient(String.valueOf(((EditText) findViewById(R.id.recipient)).getText()));
		/*Open SQL management
		 * 
		 * Add object to database
		 * Fix database to contain all datas
		 * Appointment needs tostring function
		 * 
		 */
		 datasource = new AppointmentsDataSource(this);
		 datasource.open();
		datasource.createAppointment(newApp);
		Intent intent = new Intent(NewAppointmentActivity.this, MainActivity.class);		
		startActivity(intent);
	}
	public void changeState(View v){
		Button changeStateButton = (Button)findViewById(R.id.change_state);
		if(newApp.getIsGuardian()){
			changeStateButton.setText("Dependent");
			newApp.setIsGuardian(false);
		}else{
			changeStateButton.setText("Guardian");
			newApp.setIsGuardian(true);
		}
	}
	public Appointment getNewApp() {
		return newApp;
	}
	public void setNewApp(Appointment newApp) {
		this.newApp = newApp;
	}
	
	public void checkIfSelectedWifiIsInRange(Context context){
		Log.i("debug", "in checkIfSelectedWifiIsInRange");
	    
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	   
		// If wifi is not turned on, we must turn it on 
		boolean connected = false;
	    WifiInfo info = wifiManager.getConnectionInfo();
	    
	    if(info.getBSSID() != null){
		    connected = true;
	    }else{ 
		   wifiManager.setWifiEnabled(true);
		   try {
		    	Thread.sleep(4000);
		    } catch (InterruptedException e) {
		    	// TODO Auto-generated catch block
		    	e.printStackTrace();
		    }
	    }

		// Get WiFi status and show in a toast (not helpful for the app)
		info = wifiManager.getConnectionInfo();
		//Toast.makeText( context, "name: " + info.getSSID(), Toast.LENGTH_LONG).show();
		
		// Compare a list of available wifi access-points to the chosen access-point (chosenAP)
		List<ScanResult> configs = wifiManager.getScanResults();
		boolean matchFound = false;
		for (ScanResult config : configs) {
			if(config.SSID.equals(chosenAP)){
				Log.i("debug", "Match found for " + chosenAP);
				Toast.makeText(context, "Match found for " + chosenAP, Toast.LENGTH_LONG).show();
				matchFound = true;
				break;
			}
		}
		if (!matchFound){
			Log.i("debug", "No match found for " + chosenAP);
			Toast.makeText(context, "No match found for " + chosenAP, Toast.LENGTH_LONG).show();
		}
			
		// If wifi was off -> turn off wifi
		if(connected == false){	
			wifiManager.setWifiEnabled(false);
		}
	}
	
	
}
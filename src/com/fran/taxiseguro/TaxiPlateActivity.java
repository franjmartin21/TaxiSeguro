package com.fran.taxiseguro;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fran.taxiseguro.loader.RESTLoader;
import com.google.android.gcm.GCMRegistrar;

public class TaxiPlateActivity extends Activity implements LoaderCallbacks<RESTLoader.RESTResponse>{
	
	private static final String TAG = TaxiPlateActivity.class.getName();
	
	// This is the project id generated from the Google console when
	// you defined a Google APIs project.
	private static final String PROJECT_ID = "647262737049";
	
	// This string will hold the lengthy registration id that comes
	// from GCMRegistrar.register()
	private String regId = "";
	
	// This intent filter will be set to filter on the string "GCM_RECEIVED_ACTION"
	IntentFilter gcmFilter;

	private String broadcastMessage = "No broadcast message";

	
	private EditText matriculaText;
	private Button comenzarButton;
	private TextView resultView;
	private TextView tvBroadcastMessage;
	ProgressDialog dialog;
	
	private static final String ARGS_URI    = "com.fran.taxiseguro.ARGS_URI";
	private static final String ARGS_PARAMS = "com.fran.taxiseguro.ARGS_PARAMS";
	
	private static final int LOADER_MAP_SEARCH = 0x1;
	
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<RESTLoader.RESTResponse> mCallbacks;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_taxiplate);
		comenzarButton = (Button)findViewById(R.id.comenzarButton);
		matriculaText = (EditText)findViewById(R.id.matriculaText);
		resultView = (TextView)findViewById(R.id.mapResultView);
		tvBroadcastMessage = (TextView)findViewById(R.id.tvBroadcastMessage);
		/**
		 * When clicking the button, it will call the RestService
		 */
		comenzarButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//String text = validatePlate();
				//onClickWhatsApp(text);
				getRestData();
			}
		});
		
		// Create our IntentFilter, which will be used in conjunction with a
		// broadcast receiver.
		gcmFilter = new IntentFilter();
		gcmFilter.addAction("GCM_RECEIVED_ACTION");

		registerClient();
		Log.i("REG",getDeviceID(this));
		
	}
	
	private String validatePlate(){
		String matricula = matriculaText.getText().toString();
		if(matricula != null && matricula.length() == 6){
			matriculaText.setText(matricula.substring(0, 3));
			matriculaText.append("-");
			matriculaText.append(matricula.substring(3));
		}
		return matriculaText.getText().toString();
	}
	
	/**
	 * TO SEND MESSAGES THROUGH WHATSAPP!!
	 * @param message
	 */
	public void onClickWhatsApp(String message) {

	    Intent waIntent = new Intent(Intent.ACTION_SEND);
	    waIntent.setType("text/plain");
	            String text = "Estoy en el taxi: " + message;
	    waIntent.setPackage("com.whatsapp");
	    if (waIntent != null) {
	        waIntent.putExtra(Intent.EXTRA_TEXT, text);//
	        startActivity(Intent.createChooser(waIntent, "Share with"));
	    } else {
	        Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
	                .show();
	    }
	}
	
	/**
	 * **************************************************************************
	 */
	
	/**
	 * LOADERS THAT CONNECT TO THE RESTSERVICE AND RETRIEVE THE DATA FROM THE SERVER
	 */
	
	private void getRestData(){
		 dialog = ProgressDialog.show(this, "Searching", "Getting data from server");
		 LoaderManager lm = getLoaderManager();
		 Uri gMapsSearchUri = Uri.parse("http://maps.googleapis.com/maps/api/geocode/json");
	     Bundle params = new Bundle();
	     params.putBoolean("sensor", false);
	     params.putString("address", matriculaText.getText().toString());
	     Bundle args = new Bundle();
	     args.putParcelable(ARGS_URI, gMapsSearchUri);
	     args.putParcelable(ARGS_PARAMS, params);
	     // Initialize the Loader.
	     //getSupportLoaderManager().initLoader(LOADER_MAP_SEARCH, args, this);
	     lm.initLoader(LOADER_MAP_SEARCH, args, this);
	}
	
	@Override
	public Loader<RESTLoader.RESTResponse> onCreateLoader(int id, Bundle args) {
		if (args != null && args.containsKey(ARGS_URI) && args.containsKey(ARGS_PARAMS)) {
			Uri    action = args.getParcelable(ARGS_URI);
			Bundle params = args.getParcelable(ARGS_PARAMS);
			return new RESTLoader(this, RESTLoader.HTTPVerb.GET, action, params);
		}
		return null;
	}
 
	@Override
	public void onLoadFinished(Loader<RESTLoader.RESTResponse> loader, RESTLoader.RESTResponse data) {
	     int    code = data.getCode();
	     String json = data.getData();
	     
	     // Check to see if we got an HTTP 200 code and have some data.
	     if (code == 200 && !json.equals("")) {
	         
	         // For really complicated JSON decoding I usually do my heavy lifting
	         // Gson and proper model classes, but for now let's keep it simple
	         // and use a utility method that relies on some of the built in
	         // JSON utilities on Android.
	         String resultMap = getJson(json);
	         
	         resultView.setText(resultMap);
	     }
	     else{
	    	 Toast.makeText(this, "Failed to load map data. Check your internet settings.", Toast.LENGTH_SHORT).show();
	     }
	     handler.sendEmptyMessage(0);
	}

	 @Override
	 public void onLoaderReset(Loader<RESTLoader.RESTResponse> loader) {
	 }
 
	 private static String getJson(String json) {
	
		JSONObject mapJson = null;
		try {
			mapJson = (JSONObject) new JSONTokener(json).nextValue();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Failed to parse JSON.", e);
		}
		return mapJson.toString();
	 }
	
	 private Handler handler = new Handler() {
	        public void handleMessage(Message msg) {
	            dialog.dismiss();
	        }
	 };

		


		

	// This broadcastreceiver instance will receive messages broadcast
	// with the action "GCM_RECEIVED_ACTION" via the gcmFilter
	
	// A BroadcastReceiver must override the onReceive() event.
	private BroadcastReceiver gcmReceiver = new BroadcastReceiver() {
	
		@Override
		public void onReceive(Context context, Intent intent) {
	
			broadcastMessage = intent.getExtras().getString("gcm");
	
			if (broadcastMessage != null) {
				// display our received message
				tvBroadcastMessage.setText(broadcastMessage);
			}
		}
	};
		
	// This registerClient() method checks the current device, checks the
	// manifest for the appropriate rights, and then retrieves a registration id
	// from the GCM cloud.  If there is no registration id, GCMRegistrar will
	// register this device for the specified project, which will return a
	// registration id.
	public void registerClient() {
	
		try {
			// Check that the device supports GCM (should be in a try / catch)
			GCMRegistrar.checkDevice(this);
	
			// Check the manifest to be sure this app has all the required
			// permissions.
			GCMRegistrar.checkManifest(this);
	
			// Get the existing registration id, if it exists.
			regId = GCMRegistrar.getRegistrationId(this);
	
			if (regId.equals("")) {
	
				Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT);
	
				// register this device for this project
				GCMRegistrar.register(this, PROJECT_ID);
				regId = GCMRegistrar.getRegistrationId(this);
	
				Toast.makeText(this, "Registered", Toast.LENGTH_LONG);
	
				// This is actually a dummy function.  At this point, one
				// would send the registration id, and other identifying
				// information to your server, which should save the id
				// for use when broadcasting messages.
				sendRegistrationToServer();
			} else {
				Toast.makeText(this, "Already registered", Toast.LENGTH_LONG);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
		}
		// This is part of our CHEAT.  For this demo, you'll need to
		// capture this registration id so it can be used in our demo web
		// service.
		Log.d(TAG, regId);
	}
	

	private void sendRegistrationToServer() {
		// This is an empty placeholder for an asynchronous task to post the
		// registration
		// id and any other identifying information to your server.
	}

	// If the user changes the orientation of his phone, the current activity
	// is destroyed, and then re-created.  This means that our broadcast message
	// will get wiped out during re-orientation.
	// So, we save the broadcastmessage during an onSaveInstanceState()
	// event, which is called prior to the destruction of the activity.
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putString("BroadcastMessage", broadcastMessage);

	}

	// When an activity is re-created, the os generates an onRestoreInstanceState()
	// event, passing it a bundle that contains any values that you may have put
	// in during onSaveInstanceState()
	// We can use this mechanism to re-display our last broadcast message.
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		
		super.onRestoreInstanceState(savedInstanceState);

		broadcastMessage = savedInstanceState.getString("BroadcastMessage");
		tvBroadcastMessage.setText(broadcastMessage);

	}

	// If our activity is paused, it is important to UN-register any
	// broadcast receivers.
	@Override
	protected void onPause() {
		
		unregisterReceiver(gcmReceiver);
		super.onPause();
	}
	
	// When an activity is resumed, be sure to register any
	// broadcast receivers with the appropriate intent
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(gcmReceiver, gcmFilter);

	}


	// NOTE the call to GCMRegistrar.onDestroy()
	@Override
	public void onDestroy() {

		GCMRegistrar.onDestroy(this);

		super.onDestroy();
	}
	
	
	/**
	 * TEST TO GET THE REGISTRY ID
	 */
	public static String getDeviceID(Context context) {
	    final TelephonyManager tm = (TelephonyManager) context
	            .getSystemService(Context.TELEPHONY_SERVICE);

	    final String tmDevice, tmSerial, tmPhone, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "";// + tm.getSimSerialNumber();
	    androidId = ""
	            + android.provider.Settings.Secure.getString(
	                    context.getContentResolver(),
	                    android.provider.Settings.Secure.ANDROID_ID);

	    UUID deviceUuid = new UUID(androidId.hashCode(),
	            ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    String deviceId = deviceUuid.toString();

	    return deviceId;
	}
}

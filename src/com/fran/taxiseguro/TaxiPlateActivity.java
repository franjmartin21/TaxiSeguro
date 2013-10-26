package com.fran.taxiseguro;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fran.taxiseguro.loader.RESTLoader;

public class TaxiPlateActivity extends Activity implements LoaderCallbacks<RESTLoader.RESTResponse>{
	
	private static final String TAG = TaxiPlateActivity.class.getName();
	
	private EditText matriculaText;
	private Button comenzarButton;
	private TextView resultView;
	
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
		
		comenzarButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//String text = validatePlate();
				//onClickWhatsApp(text);
				getRestData();
			}
		});
		
		LoaderManager lm = getLoaderManager();
		// ****************************************
		Uri gMapsSearchUri = Uri.parse("http://maps.googleapis.com/maps/api/geocode/json");
	     Bundle params = new Bundle();
	     Bundle args = new Bundle();
	     args.putParcelable(ARGS_URI, gMapsSearchUri);
	     args.putParcelable(ARGS_PARAMS, params);
	     // Initialize the Loader.
	     //getSupportLoaderManager().initLoader(LOADER_MAP_SEARCH, args, this);
	     lm.initLoader(LOADER_MAP_SEARCH, args, this);
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
	
	private void getRestData(){
		// This is our REST action.
	     Uri gMapsSearchUri = Uri.parse("http://maps.googleapis.com/maps/api/geocode/json");
	     Bundle params = new Bundle();
	     Bundle args = new Bundle();
	     args.putParcelable(ARGS_URI, gMapsSearchUri);
	     args.putParcelable(ARGS_PARAMS, params);
	     // Initialize the Loader.
	     //getSupportLoaderManager().initLoader(LOADER_MAP_SEARCH, args, this);
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
	
	
	
	
}

package com.fran.taxiseguro;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class TaxiPlateActivity extends Activity{
	
	private EditText matriculaText;
	private Button comenzarButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_taxiplate);
		comenzarButton = (Button)findViewById(R.id.comenzarButton);
		matriculaText = (EditText)findViewById(R.id.matriculaText);
		comenzarButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				validatePlate();
				
			}
		});
	}
	
	private void validatePlate(){
		String matricula = matriculaText.getText().toString();
		if(matricula != null && matricula.length() == 6){
			matriculaText.setText(matricula.substring(0, 3));
			matriculaText.append("-");
			matriculaText.append(matricula.substring(3));
		}
		
	}
	
	
}

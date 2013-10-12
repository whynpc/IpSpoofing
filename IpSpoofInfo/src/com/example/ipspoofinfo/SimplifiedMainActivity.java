package com.example.ipspoofinfo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.os.Bundle;
import android.os.Environment;
import android.R.bool;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class SimplifiedMainActivity extends Activity {


	private CheckBox checkBoxCase1, checkBoxCase2, checkBoxCase3,
			checkBoxCase4, checkBoxCase1P, checkBoxCase2P, checkBoxCase3P,
			checkBoxCase4P;

	private MobileInfo mobileInfo;
	private BinaryHandler binaryHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simplified_main);

		startService(new Intent(this, BackgroundService.class));

		checkBoxCase1 = (CheckBox) findViewById(R.id.checkBoxCase1);
		checkBoxCase2 = (CheckBox) findViewById(R.id.checkBoxCase2);
		checkBoxCase3 = (CheckBox) findViewById(R.id.checkBoxCase3);
		checkBoxCase4 = (CheckBox) findViewById(R.id.checkBoxCase4);

		checkBoxCase1P = (CheckBox) findViewById(R.id.checkBoxCase1P);
		checkBoxCase2P = (CheckBox) findViewById(R.id.checkBoxCase2P);
		checkBoxCase3P = (CheckBox) findViewById(R.id.checkBoxCase3P);
		checkBoxCase4P = (CheckBox) findViewById(R.id.checkBoxCase4P);
		
		mobileInfo = new MobileInfo(this);
		binaryHandler = new BinaryHandler(this, mobileInfo);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simplified_main, menu);
		return true;
	}

	public void onClickButtonRun(View view) {
		binaryHandler.deployBinary();		
		
		if (checkBoxCase1.isChecked()) {
			binaryHandler.runSimpleCase(true, 16, false);
		}		
		if (checkBoxCase2.isChecked()) {
			binaryHandler.runSimpleCase(true, 32, false);
		}		
		if (checkBoxCase3.isChecked()) {
			binaryHandler.runSimpleCase(false, 16, false);
		}
		if (checkBoxCase4.isChecked()) {
			binaryHandler.runSimpleCase(false, 32, false);
		}		
		if (checkBoxCase1P.isChecked()) {
			binaryHandler.runSimpleCase(true, 16, true);
		}		
		if (checkBoxCase2P.isChecked()) {
			binaryHandler.runSimpleCase(true, 32, true);
		}		
		if (checkBoxCase3P.isChecked()) {
			binaryHandler.runSimpleCase(false, 16, true);
		}
		if (checkBoxCase4P.isChecked()) {
			binaryHandler.runSimpleCase(false, 16, true);
		}				
		
	}
		
	
	public void onClickButtonDetailedSettings(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	public void onClickButtonCaseMin(View view) {
		checkBoxCase1.setChecked(false);
		checkBoxCase2.setChecked(true);
		checkBoxCase3.setChecked(false);
		checkBoxCase4.setChecked(false);
		checkBoxCase1P.setChecked(false);
		checkBoxCase2P.setChecked(true);
		checkBoxCase3P.setChecked(false);
		checkBoxCase4P.setChecked(false);		
	}
	
	public void onClickButtonCaseMax(View view) {
		checkBoxCase1.setChecked(false);
		checkBoxCase2.setChecked(false);
		checkBoxCase3.setChecked(false);
		checkBoxCase4.setChecked(false);
		checkBoxCase1P.setChecked(true);
		checkBoxCase2P.setChecked(true);
		checkBoxCase3P.setChecked(true);
		checkBoxCase4P.setChecked(true);
	}

}

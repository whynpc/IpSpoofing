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

	public static final String BIN_FILE = "/data/local/ipspoof";
	public static final String BIN_CONFIG_FILE = "spoofing.txt";
	public static final String BIN_CONFIG_SEPARATOR = " ";

	private CheckBox checkBoxCase1, checkBoxCase2, checkBoxCase3,
			checkBoxCase4, checkBoxCase1P, checkBoxCase2P, checkBoxCase3P,
			checkBoxCase4P;

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

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.simplified_main, menu);
		return true;
	}

	public void onClickButtonRun(View view) {		
		
		deployBinary();
		
		if (checkBoxCase1.isChecked()) {
			runSingleCase("0 131.179.176.74 9999 16 0");
		}		
		if (checkBoxCase2.isChecked()) {
			runSingleCase("0 131.179.176.74 9999 32 0");
		}		
		if (checkBoxCase3.isChecked()) {
			runSingleCase("1 131.179.176.74 9999 16 0");
		}
		if (checkBoxCase4.isChecked()) {
			runSingleCase("1 131.179.176.74 9999 32 0");
		}
		
		if (checkBoxCase1P.isChecked()) {
			runSingleCase("0 131.179.176.74 9999 16 1");
		}		
		if (checkBoxCase2P.isChecked()) {
			runSingleCase("0 131.179.176.74 9999 32 1");
		}		
		if (checkBoxCase3P.isChecked()) {
			runSingleCase("1 131.179.176.74 9999 16 1");
		}
		if (checkBoxCase4P.isChecked()) {
			runSingleCase("1 131.179.176.74 9999 32 1");
		}				
		
	}
	
	private void deployBinary() {
		InputStream is = getResources().openRawResource(R.raw.ipspoof);

		File outf = new File(this.getCacheDir(), "ipspoof");
		if (outf.exists()) {			
			outf.delete();
		}
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(outf);
			Log.d("ipspoofinfo", outf.getAbsolutePath());
			byte[] buffer = new byte[1024];
			int bufferlen;
			while ((bufferlen = is.read(buffer)) != -1) {
				fos.write(buffer, 0, bufferlen);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("ipspoofinfo", e.toString());
		} finally {
			if (fos != null) {
				try {
					fos.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.d("ipspoofinfo", e.toString());
				}
			}
		}

		try {
			// Preform su to get root privledges
			Process p;
			p = Runtime.getRuntime().exec("su");

			// Attempt to write a file to a root-only
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("cp " + outf.getAbsolutePath() + " /data/local\n");
			os.writeBytes("chmod 777 /data/local/ipspoof\n");

			// Close the terminal
			os.writeBytes("exit\n");
			os.flush();
			try {
				p.waitFor();

			} catch (InterruptedException e) {
				// TODO Code to run in interrupted exception

			}
		} catch (IOException e) {
			// TODO Code to run in input/output exception

		}
		
	}
	
	private void runSingleCase(String caseConfiguration) {
		updateSpoofinfo();
		
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + BIN_CONFIG_FILE);
		PrintWriter writer;
		try {
			writer = new PrintWriter(file);
			writer.println(caseConfiguration);
			writer.flush();
			writer.close();						
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		runBinary();
	}
	
	private void runSingleCase(boolean isSampling, int maskLen, boolean isProbing) { 
		updateSpoofinfo();
		
		StringBuffer sb = new StringBuffer();
		
		/* 
		 * if (checkBoxCase1.isChecked()) {
			runSingleCase("0 131.179.176.74 9999 16 0");
		}		
		if (checkBoxCase2.isChecked()) {
			runSingleCase("0 131.179.176.74 9999 32 0");
		}		
		if (checkBoxCase3.isChecked()) {
			runSingleCase("1 131.179.176.74 9999 16 0");
		}
		if (checkBoxCase4.isChecked()) {
			runSingleCase("1 131.179.176.74 9999 32 0");
		}
		
		if (checkBoxCase1P.isChecked()) {
			runSingleCase("0 131.179.176.74 9999 16 1");
		}		
		if (checkBoxCase2P.isChecked()) {
			runSingleCase("0 131.179.176.74 9999 32 1");
		}		
		if (checkBoxCase3P.isChecked()) {
			runSingleCase("1 131.179.176.74 9999 16 1");
		}
		if (checkBoxCase4P.isChecked()) {
			runSingleCase("1 131.179.176.74 9999 32 1");
		}
		 * */
		
		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + BIN_CONFIG_FILE);
		PrintWriter writer;
		try {
			writer = new PrintWriter(file);
			writer.println(sb.toString());
			writer.flush();
			writer.close();						
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		runBinary();
		
	}

	
	private void runBinary() {
		try {
			Process process = Runtime.getRuntime().exec("su -c ." + BIN_FILE);
			StringBuffer sb = new StringBuffer();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				if (sb.length() == 0)
					sb.append(line);
			}
			Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateSpoofinfo() {
		BackgroundService.runOnce();
		try {
			// to ensure update finish before invoke the binary
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

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
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.StaticLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

	private EditText editTextInterval;
	private EditText editTextStatus;
	private Spinner spinnerMode;
	private EditText editTextPhoneIp;
	private EditText editTextSpoofIp;
	private EditText editTextDestIp;
	private EditText editTextPort;
	private EditText editTextMask;
	private CheckBox checkBoxScan;
	private EditText editTextPktSize;
	private EditText editTextSrcRate;
	private EditText editTextTime;

	private static final String MODE_SAMPLE_IPS = "Sample IPs";
	private static final String MODE_ALL_IPS = "All IPs";
	private static final String MODE_ONE_IP_ATTACK = "One IP Attack";
	private static final String MODE_ONE_IP_SPOOF = "One IP Spoof";

	public static final int MSG_STATUS_UPDATE = 1;
	public static final int MSG_INTERVAL_UPDATE = 2;
	public static final int MSG_PHONEIP_UPDATE = 3;

	public static final String PREF_FILE = "pref";
	private SharedPreferences settings;

	public static final String BIN_FILE = "/data/local/ipspoof";
	public static final String BIN_CONFIG_FILE = "spoofing.txt";
	public static final String BIN_CONFIG_SEPARATOR = " ";

	private static final String KEY_MODE = "Mode";
	private static final String KEY_SPOOF_IP = "SpoofIP";
	private static final String KEY_DEST_IP = "DestIP";
	private static final String KEY_PORT = "Port";
	private static final String KEY_MASK = "Mask";
	private static final String KEY_SCAN = "Scan";
	private static final String KEY_PKG_SIZE = "PktSize";
	private static final String KEY_SRC_RATE = "SrcRate";
	private static final String KEY_TIME = "Time";
	
	private MobileInfo mobileInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		editTextInterval = (EditText) findViewById(R.id.editTextInterval);
		editTextStatus = (EditText) findViewById(R.id.editTextStatus);
		spinnerMode = (Spinner) findViewById(R.id.spinnerMode);
		editTextPhoneIp = (EditText) findViewById(R.id.editTextPhoneIp);
		editTextSpoofIp = (EditText) findViewById(R.id.editTextSpoofIp);
		editTextDestIp = (EditText) findViewById(R.id.editTextDestIp);
		editTextPort = (EditText) findViewById(R.id.editTextPort);
		editTextMask = (EditText) findViewById(R.id.editTextMask);
		checkBoxScan = (CheckBox) findViewById(R.id.checkBoxScan);
		editTextPktSize = (EditText) findViewById(R.id.editTextPktSize);
		editTextSrcRate = (EditText) findViewById(R.id.editTextSrcRate);
		editTextTime = (EditText) findViewById(R.id.editTextTime);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, new String[] {
						MODE_SAMPLE_IPS, MODE_ALL_IPS, MODE_ONE_IP_ATTACK,
						MODE_ONE_IP_SPOOF });
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerMode.setAdapter(adapter);
		spinnerMode
				.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int pos, long arg3) {
						switch (pos) {
						case 0:
							editTextSpoofIp.setEnabled(false);
							editTextMask.setEnabled(true);
							checkBoxScan.setEnabled(true);
							editTextPktSize.setEnabled(false);
							editTextSrcRate.setEnabled(false);
							editTextTime.setEnabled(false);
							break;
						case 1:
							editTextSpoofIp.setEnabled(false);
							editTextMask.setEnabled(true);
							checkBoxScan.setEnabled(true);
							editTextPktSize.setEnabled(false);
							editTextSrcRate.setEnabled(false);
							editTextTime.setEnabled(false);
							break;
						case 2:
							editTextSpoofIp.setEnabled(true);
							editTextMask.setEnabled(false);
							checkBoxScan.setEnabled(false);
							editTextPktSize.setEnabled(true);
							editTextSrcRate.setEnabled(true);
							editTextTime.setEnabled(true);
							break;
						case 3:
							editTextSpoofIp.setEnabled(true);
							editTextMask.setEnabled(false);
							checkBoxScan.setEnabled(false);
							editTextPktSize.setEnabled(false);
							editTextSrcRate.setEnabled(true);
							editTextTime.setEnabled(true);
							break;
						default:
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// do nothing

					}

				});

		settings = getSharedPreferences(PREF_FILE, 0);
		restoreSettings();

		startService(new Intent(this, BackgroundService.class));
		BackgroundService.registerMsgClient(msgHandler); // editTextPhoneIp set
															// by
															// registerMsgClient()

		mobileInfo = new MobileInfo(this);
	}

	private void restoreSettings() {
		spinnerMode.setSelection(settings.getInt(KEY_MODE, 0));
		editTextSpoofIp.setText(settings.getString(KEY_SPOOF_IP, "10.0.0.1"));
		editTextDestIp.setText(settings
				.getString(KEY_DEST_IP, "131.179.176.74"));
		editTextPort.setText("" + settings.getInt(KEY_PORT, 9999));
		editTextMask.setText("" + settings.getInt(KEY_MASK, 16));
		checkBoxScan.setChecked(settings.getBoolean(KEY_SCAN, false));
		editTextPktSize.setText("" + settings.getInt(KEY_PKG_SIZE, 1000));
		editTextSrcRate.setText("" + settings.getInt(KEY_SRC_RATE, 100));
		editTextTime.setText("" + settings.getInt(KEY_TIME, 1));
	}

	private void updateSettings() {
		Editor editor = settings.edit();
		editor.putInt(KEY_MODE, spinnerMode.getSelectedItemPosition());
		editor.putString(KEY_SPOOF_IP, editTextSpoofIp.getText().toString());
		editor.putString(KEY_DEST_IP, editTextDestIp.getText().toString());
		editor.putInt(KEY_PORT,
				Integer.parseInt(editTextPort.getText().toString()));
		editor.putInt(KEY_MASK,
				Integer.parseInt(editTextMask.getText().toString()));
		editor.putBoolean(KEY_SCAN, checkBoxScan.isChecked());
		editor.putInt(KEY_PKG_SIZE,
				Integer.parseInt(editTextPktSize.getText().toString()));
		editor.putInt(KEY_SRC_RATE,
				Integer.parseInt(editTextSrcRate.getText().toString()));
		editor.putInt(KEY_TIME,
				Integer.parseInt(editTextTime.getText().toString()));
		editor.commit();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		BackgroundService.deregisterMsgClient(msgHandler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onClickButtonStart(View view) {
		int interval = Integer.parseInt(editTextInterval.getText().toString());
		BackgroundService.setInterval(interval);
		BackgroundService.start();
	}

	public void onClickButtonStop(View view) {
		BackgroundService.stop();
	}

	public void onClickButtonReset(View view) {
		int interval = Integer.parseInt(editTextInterval.getText().toString());
		BackgroundService.setInterval(interval);
		BackgroundService.reset();
	}

	public void onClickButtonRunOnce(View view) {
		BackgroundService.runOnce();
	}

	public void onClickButtonGateway(View view) {
		try {
			// Process process =
			// Runtime.getRuntime().exec("su -c ./data/local/ipspoof");
			Process process = Runtime.getRuntime().exec("cat /proc/net/route");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				String[] words = line.split("\\s");
				if (words.length >= 3) {
					try {
						if (Integer.valueOf(words[1], 16) == 0) {
							String rawStr = words[2];
							int ipaddr = Integer.valueOf(rawStr, 16);
							byte[] addr = new byte[4];
							addr[0] = (byte) (ipaddr & 0xFF);
							addr[1] = (byte) ((ipaddr >> 8) & 0xFF);
							addr[2] = (byte) ((ipaddr >> 16) & 0xFF);
							addr[3] = (byte) ((ipaddr >> 24) & 0xFF);

							try {
								InetAddress inetAddress = InetAddress
										.getByAddress(addr);
								Toast.makeText(this,
										inetAddress.getHostAddress(),
										Toast.LENGTH_LONG).show();
								;
							} catch (UnknownHostException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					} catch (Exception e) {

					}
				}

			}

			// Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onClickButtonRunBin(View view) {
		updateSettings();

		// update the configuration file of binary
		StringBuilder sb1 = new StringBuilder();
		switch (spinnerMode.getSelectedItemPosition()) {
		case 0:
			sb1.append(0);
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextDestIp.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextPort.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextMask.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(checkBoxScan.isChecked() ? "1" : "0");
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(mobileInfo.isLowRateNetwork() ? 10 : 100);
			break;
		case 1:
			sb1.append(1);
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextDestIp.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextPort.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextMask.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(checkBoxScan.isChecked() ? "1" : "0");
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(mobileInfo.isLowRateNetwork() ? 10 : 100);
			break;
		case 2:
			sb1.append(2);
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextSpoofIp.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextDestIp.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextPort.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextPktSize.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextSrcRate.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextTime.getText().toString());
			break;
		case 3:
			sb1.append(3);
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextSpoofIp.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextDestIp.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextPort.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextSrcRate.getText().toString());
			sb1.append(BIN_CONFIG_SEPARATOR);
			sb1.append(editTextTime.getText().toString());
			break;
		default:
			return;
		}

		File file = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + BIN_CONFIG_FILE);
		try {
			PrintWriter writer = new PrintWriter(file);
			writer.print(sb1.toString());
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		// check binary
		//File binFile = new File(BIN_FILE);
		if (true) {
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

			// String cmd = "su -c \"cp  "
			// + outf.getAbsolutePath()
			// + " /data/local/; chmod 777 /data/local/ipspoof\"";

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

		// for debug
		if (false) {
			return;
		}

		// run binary and show output
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
			Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
			;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Handler msgHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			case MSG_STATUS_UPDATE:
				if (BackgroundService.isRunning()) {
					editTextStatus.setText("Running");
				} else {
					editTextStatus.setText("Stopped");
				}
				break;
			case MSG_INTERVAL_UPDATE:
				editTextInterval.setText("" + BackgroundService.getInterval());
				break;
			case MSG_PHONEIP_UPDATE:
				editTextPhoneIp.setText(BackgroundService.getPhoneIp());
				break;
			default:
				break;
			}
		}
	};

}

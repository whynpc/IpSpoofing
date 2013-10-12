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

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class BinaryHandler {
	
	public static final long LATEST_UPDATE_TIME = 1381618429516l; // v1.8
	public static final String BIN_FILE = "/data/local/ipspoof";
	public static final String BIN_CONFIG_FILE = "spoofing.txt";
	public static final String BIN_CONFIG_SEPARATOR = " ";
	
	public static final String DEFAULT_SERVER_ADDR = "131.179.176.74";
	public static final int DEFAULT_SERVER_PORT = 9999;
	
	private Context context;
	private MobileInfo mobileInfo;
	
	public BinaryHandler(Context context, MobileInfo mobileInfo) {
		this.context = context;
		this.mobileInfo = mobileInfo;
	}
	
	public void deployBinary() { 
		InputStream is = context.getResources().openRawResource(R.raw.ipspoof);

		File outf = new File(context.getCacheDir(), "ipspoof");
		if (outf.exists()) {
			if (outf.lastModified() < BinaryHandler.LATEST_UPDATE_TIME) {
				outf.delete();				
			} else {
				// the exiting binary is later than the embedded binary
				return;
			}			
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
	
	private String runBinary() {
		StringBuffer sb = new StringBuffer();
		try {
			Process process = Runtime.getRuntime().exec("su -c ." + BIN_FILE);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				if (sb.length() == 0)
					sb.append(line);
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
			
	public String runSimpleCase(boolean isSampling, int maskLen, boolean isProbing) {
		// update ipspoofinginfo first
		BackgroundService.runOnce();
		try {
			// to ensure update finish before invoke the binary
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(isSampling ? 0 : 1);
		sb.append(BIN_CONFIG_SEPARATOR);
		sb.append(DEFAULT_SERVER_ADDR);
		sb.append(BIN_CONFIG_SEPARATOR);
		sb.append(DEFAULT_SERVER_PORT);
		sb.append(BIN_CONFIG_SEPARATOR);
		sb.append(maskLen);
		sb.append(BIN_CONFIG_SEPARATOR);
		sb.append(isProbing ? 0 : 1);
		sb.append(BIN_CONFIG_SEPARATOR);
		sb.append(mobileInfo.isLowRateNetwork() ? 10 : 100);
						
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
		
		return runBinary();
		
	}
	
	
	

}

package com.example.ipspoofinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;
import android.R.anim;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MobileInfo {
	
	private int networkType = 0;	private int signalStrengthDBM = -113;
	private int cellId = 0; /*Cell Id*/
	private int lac = 0; /*Location Area*/
	private int psc = 0; /* Scrambling code*/
	private float speed = 0;		
	private String geoLat = ""; /*ex: 41.11665289*/
	private String geoLong = ""; /*ex: -73.71944653*/
	private String networkTech = ""; /*ex: GSM or CDMA*/
	private String neighborStatus = "";
	private String networkTypeStr = ""; /*ex: GPRS, UMTS, HSPA, ..*/	
	private TelephonyManager        telMgr;
	private MobilePhoneStateListener    mListener;	
	private LocationManager mlocManager;
	private LocationListener mlocListener;
	private Context mContext;
	private LocationManager locMgr;
	private ConnectivityManager cMgr;
	//private Timer statsPullTimer = new Timer();
	
	private static final Pattern PATTERN_IPINTRACEROUTE = Pattern.compile("\\s*\\d+.+\\((\\d+\\.\\d+\\.\\d+\\.\\d+)\\).+");
	
	MobileInfo (Context context){		
		/*Telephony Service*/
		mContext = context;
		mListener = new MobilePhoneStateListener();
		
        telMgr = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        telMgr.listen(mListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //telMgr.listen(mListener ,PhoneStateListener.LISTEN_CELL_LOCATION);
        telMgr.listen(mListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        //telMgr.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);
        
        /*Location Service*/
        mlocManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MobileLocationListener();
        mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
        
        cMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
       // statsPullTimer.schedule(new statusPullTask(250), 250);
        
        locMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        
      
        
	}
	
	/*Retrieve Phone Model*/
	public String getPhoneModel() {
		return Build.MODEL;		
	}
	
	/*Retrieve Operator Name*/
	public String getOperatorName() {
		return telMgr.getNetworkOperatorName();
	}

	public String getDeviceId(){		
		return telMgr.getDeviceId();		
	}
	/*Retrieve Network Type*/
	public int getNetworkType() {
		networkType = telMgr.getNetworkType();
		return networkType;
	}

	/*Retrieve Signal Strength*/
	public int getSignalStrengthDBM() {	         
		return signalStrengthDBM;
	}

	/*Retrieve Cell id*/
	public int getCellId() {
		CellLocation location = telMgr.getCellLocation();
		int newCellId = 0;
		if (location instanceof GsmCellLocation)
        {
      	  GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
      	  newCellId = gsmCellLocation.getCid();
        } else {
      	  CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
      	  newCellId = cdmaCellLocation.getBaseStationId();      	  
        }
		return newCellId;
	}

	/*Retrieve Location Area Id*/
	public int getLac() {
		return lac;
	}

	/*Retrieve primary scrambling code*/
	public int getPsc() {
		return psc;
	}

	/*Retrieve current mobility speed*/
	public float getSpeed() {
		return speed;
	}
	
	public String getGeoLocation() {
		if (locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			
		} else if (locMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { 
			
		} else {
						
		}
		return ";";
		
	}

	/*Retrieve the latitude of current location */
	public String getGeoLat() {		
		return geoLat;
	}

	/*Retrieve the longitude of current location */
	public String getGeoLong() {
		return geoLong;
	}

	/*Retrieve the networks technologies, GSM or CDMA */
	public String getNetworkTech() {
		NetworkInfo info = cMgr.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			return "NULL";
		} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			return "WIFI";
		} else {
			CellLocation location = telMgr.getCellLocation();
			if (location instanceof GsmCellLocation) {
				return "GSM";
			} else {
				return "CDMA";
			}			
		}				
	}

	/*Retrieve the network type, GPRS, UMTS, LTE, HSPA */
	public String getNetworkTypeStr() {
		return networkTypeStr;
	}
	
	public String getTraceRoute() {
		try {
			Process process = Runtime.getRuntime().exec("su -c traceroute google.com");			
			StringBuffer sb = new StringBuffer();
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				//Log.d("ipspoofinfo", "Traceroute: " + line);
				Matcher matcher = MobileInfo.PATTERN_IPINTRACEROUTE.matcher(line);
				if (matcher.find()) {
					String addrStr = matcher.group(1);
					InetAddress address = InetAddress.getByName(addrStr);
					if (address.isSiteLocalAddress()) {
						if (sb.length() > 0) {
							sb.append('_');							
						}
						sb.append(addrStr);
					} else {
						// get to public IP, terminating traceroute
						break;
					}					
				} else {
					
				}				
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "NULL";
	}
	
	/*Retrieve the neighboring information*/
	public String getNeighborStatus() {
		return neighborStatus;
	}

	/*Get total byte of sent via mobile networks*/
	public long getMobileTxByte(){				
		return TrafficStats.getMobileTxBytes();
	}
	
	/*Get total byte of received via mobile networks*/
	public long getMobileRxByte(){		
		return TrafficStats.getMobileRxBytes();
	}
	
	public String getPhoneNum() {
		return telMgr.getLine1Number();
	}
	
	/*Get total number of packets sent via mobile networks*/
	public long getMobileTxPacketNum(){		
		return TrafficStats.getMobileTxPackets();
	}
	
	/*Get total number of packets received via mobile networks*/
	public long getMobileRxPacketNum(){		
		return TrafficStats.getMobileRxPackets();
	}

	/*Get total byte of sent via all interfaces including mobile networks and wifi networks.*/
	public long getTotalTxByte(){		
		return TrafficStats.getTotalTxBytes();
	}
	
	/*Get total byte of received via all interfaces including mobile networks and wifi networks.*/
	public long getTotalRxByte(){		
		return TrafficStats.getTotalRxBytes();
	}
	
	/*Get total number of packets sent all interfaces including mobile networks and wifi networks.*/
	public long getTotalTxPacketNum(){		
		return TrafficStats.getTotalTxPackets();
	}
	
	/*Get total number of packets received all interfaces including mobile networks and wifi networks.*/
	public long getTotalRxPacketNum(){		
		return TrafficStats.getTotalRxPackets();
	}
	
	/*Get total byte of data sent by particular user id via all interfaces including mobile networks and wifi networks.*/
	public long getUidTxBytes(int uid){
		return TrafficStats.getUidTxBytes(uid);
	}
	
	/*Get total byte of data received by particular user id via all interfaces including mobile networks and wifi networks.*/
	public long getUidRxBytes(int uid){
		return TrafficStats.getUidRxBytes(uid);
	}
	
	public String getDns() {
		try {
			Process process = Runtime.getRuntime().exec("getprop net.dns1");			
			StringBuffer sb = new StringBuffer();
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return "";
		}
	}
	
	public void setNetworkType(int networkType) {
		this.networkType = networkType;
	}
	
	public boolean isLowRateNetwork() {
		boolean res = false;
		switch (getNetworkType()) {
		case 2:
			res = true;
			break;
		case 1:
			res = true;
			break;
		default:
			break;
		}
		return res;
		
	}
	
    public String getNetworkType(int networkType) {

	    switch (networkType)
	    {
		    case 7:
		        return "1xRTT";   
		    case 4:
		    	return "CDMA";    
		    case 2:
		    	return "EDGE";
		    case 14:
		    	return "eHRPD";   
		    case 5:
		    	return "EVDO rev. 0";
		    case 6:
		    	return "EVDO rev. A"; 
		    case 12:
		    	return "EVDO rev. B";
		    case 1:
		    	return "GPRS";    
		    case 8:
		    	return "HSDPA";    
		    case 10:
		    	return "HSPA";        
		    case 15:
		    	return "HSPA+";        
		    case 9:
		    	return "HSUPA";         
		    case 11:
		    	return "iDen";
		    case 13:
		        return "LTE";
		    case 3:
		    	return "UMTS";        
		    case 0:
		    	return "Unknown";
		    default:
		    	return "" + networkType;
	    }
    }
	/*Telephony Service*/
    public class MobilePhoneStateListener extends PhoneStateListener
    {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {         
            int dbm; 
            if (signalStrength.isGsm() == true) {
            	dbm = signalStrength.getGsmSignalStrength()*2-113;
            } else {
            	dbm = signalStrength.getCdmaDbm();
            }
            if (dbm != signalStrengthDBM) {
            	signalStrengthDBM = dbm;
            }            		
            super.onSignalStrengthsChanged(signalStrength);
      }
        
      @Override
      public void onDataConnectionStateChanged(int state, int networkType) {
    	  //Log.d("autocaller", "Conn state change: " + networkType);
    	  //EventLog.write(Type.HANDOVER, "" + networkType);
    	  setNetworkType(networkType);
    	  super.onDataConnectionStateChanged(state, networkType);
      }
              
      @Override
      public void onCellLocationChanged(CellLocation location) 
      {
          int newCellId = 0, newLAC = 0, newPSC = 0;
          
          networkTypeStr = getNetworkType(telMgr.getNetworkType());                  

          
          if (location instanceof GsmCellLocation)
          {
        	  GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
        	  newCellId = gsmCellLocation.getCid();
        	  newLAC = gsmCellLocation.getLac();
        	  networkTech = "GSM";
          } else {
        	  CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
        	  newCellId = cdmaCellLocation.getBaseStationId();
        	  newLAC = cdmaCellLocation.getNetworkId();
        	  networkTech = "CDMA";
          }
	      if (newCellId != cellId || newLAC != lac || newPSC != psc) {	    	  
		      cellId = newCellId;
		      lac = newLAC;
		      psc = newPSC;
	      }	 
    	  super.onCellLocationChanged(location);
    	  //this.onCallStateChanged(state, incomingNumber)
      }
      
      @Override
      public void onCallStateChanged(int state, String incomingNumber) {
    	  
      }

    };/* End of private Class */
	
    
	public class MobileLocationListener implements LocationListener
	{

		@Override
        public void onLocationChanged(Location loc)
        {
            geoLat = "" + loc.getLatitude();
            geoLong = "" + loc.getLongitude();
            speed = loc.getSpeed();
        }

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	}/* End of Class MyLocationListener */
	
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
	                    //String ip = Formatter.formatIpAddress(inetAddress.hashCode());
	                    String ip = inetAddress.getHostAddress();
	                    
	                    return ip;
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        
	    }
	    return null;
	}
	
    public String getNeighboringCells() {
    	String result = "";
    	List<NeighboringCellInfo> neighboringCells=telMgr.getNeighboringCellInfo();
    	for (int i = 0; i < neighboringCells.size(); i++) {
    		NeighboringCellInfo a = neighboringCells.get(i);
    		int signalStrength = 0;
    		//if (a.getNetworkType() == 1 || a.getNetworkType() == 2) {
    			/*GPRS or EDGE*/
   		 	    signalStrength = a.getRssi()*2-113; 
    		//} else {
    		//	signalStrength = a.getRssi();
    		//}
    		result += getNetworkType(neighboringCells.get(i).getNetworkType())+";" + 
    				neighboringCells.get(i).getLac()+":"+
    				neighboringCells.get(i).getCid()+":"+
    				neighboringCells.get(i).getPsc()+":"+
    				signalStrength +":";
    	}
    	return result;
    }
    
    class statusPullTask extends TimerTask {
    	long timePeriod;
    	
    	statusPullTask(long timePeriod) {
    		this.timePeriod = timePeriod;
    	}
        public void run() {
        	try {
        		//statsPullTimerEnabled = false;
        		neighborStatus = getNeighboringCells();        		
        		//telMgr.listen(mListener ,PhoneStateListener.LISTEN_NONE);
		        //telMgr.listen(mListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		        //telMgr.listen(mListener ,PhoneStateListener.LISTEN_CELL_LOCATION);
		        
		        mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		        //statsPullTimer.schedule(new statusPullTask(timePeriod), timePeriod);
		        //statsPullTimerEnabled = true;
        	}catch (Exception e) {
        		e.printStackTrace();
        	}	
        }
    }    
}

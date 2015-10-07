package com.textapp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity
		implements ServerThread.MessageDetector, TextAppBroadcastReceiver.P2PEnabledListener{
	
	public static final String EXTRA_MESSAGE = "com.textapp.MESSAGE";
	
	private static final String TAG = "MainActivity";
	
	private boolean p2pEnabled = false;
	private boolean isGroupOwner = false;
	
	private String macAddress;
	private boolean macFound = false;
	
	private WifiP2pManager wifiP2pManager;
	private WifiManager wifiManager;
	private WifiManager.MulticastLock lock;
	private Channel mChannel;
	private BroadcastReceiver bcReceiver;
	private ServerThread serverThread;
	private GroupOwner groupOwner;
	private LocalContactManager lcm;
//	private MulticastReceiver mcReceiver;
//	private MulticastBroadcaster mcBroadcaster;
	private IntentFilter mIntentFilter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(Constants.VERBOSE) Log.v(TAG, "onCreate()");
		
		setContentView(R.layout.activity_main);
		
		//Initialize WiFi Multicast
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		macAddress = wifiManager.getConnectionInfo().getMacAddress();
//		lock = wifiManager.createMulticastLock(TAG);
//		lock.acquire();
		
		//Initialize WiFi P2P and BroadcastReceiver
		wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = wifiP2pManager.initialize(this, getMainLooper(), null);
		bcReceiver = new TextAppBroadcastReceiver(wifiP2pManager, mChannel, this);
		serverThread = new ServerThread(Constants.PORT_IN, this);

//		try{
//			InetAddress address = InetAddress.getByName(Constants.IP_ADDRESS);
//			mcReceiver = new MulticastReceiver(address, Constants.PORT_IN, this);
//			mcBroadcaster = new MulticastBroadcaster(address, Constants.PORT_OUT);
//		}
//		catch(UnknownHostException e){
//			Log.i(TAG, "FAILED TO FIND MULTICAST HOST ADDRESS");
//		}
		
		//Create intent filter for WiFi-Direct
		mIntentFilter = new IntentFilter();
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}
	
	
	@Override
	protected void onResume(){
		super.onResume();
		if(Constants.VERBOSE) Log.v(TAG, "onResume()");
		registerReceiver(bcReceiver, mIntentFilter);
	}
	
	
	@Override
	protected void onPause(){
		super.onPause();
		if(Constants.VERBOSE) Log.v(TAG, "onPause()");
		unregisterReceiver(bcReceiver);
		if(!serverThread.isCancelled()) serverThread.cancel(true);
	}
	
	
//	@Override
//	protected void onStop(){
//		if(!mcReceiver.isCancelled()) mcReceiver.cancel(true);
//		super.onStop();
//	}
	
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		if(Constants.VERBOSE) Log.v(TAG, "onDestroy()");
		if(!serverThread.isCancelled()) serverThread.cancel(true);
		lock.release();
		wifiManager.disconnect();
	}
	
	
	@Override
	public void onDetect(Transmittable transmittable){
//		String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
//		TextView textView = (TextView) findViewById(R.id.messageDisplay_helloWorld);
//		textView.setId(idCount++);
		
		Transmittable.Type type = transmittable.getType();
		
		if(type == Transmittable.Type.MESSAGE){
			Transmittable.Message message = (Transmittable.Message) transmittable;
			updateMessageView(message.getMessage());
		}
		
		if(type == Transmittable.Type.CONTACT){
			Transmittable.Contact contact = (Transmittable.Contact) transmittable;
			if(isGroupOwner){
				groupOwner.addContact(contact.getMacAddress(), contact.getInetAddress());
			}
			else{
				lcm.addContact(contact.getMacAddress(), contact.getInetAddress());
			}
		}
		
		if(type == Transmittable.Type.CONTACT_REQUEST){
			if(isGroupOwner){
				Transmittable.ContactRequest request = (Transmittable.ContactRequest) transmittable;
				groupOwner.distributeContact(request.getLocalMacAddress(),
						request.getSentAddress(), request.pullContacts(), lcm);
			}
		}
	}
	
	@Override
	public void makeGroupOwner(InetAddress groupOwnerAddress){
		//if groupowner, instantiate groupowner manager class and add self as contact
		if(!isGroupOwner){
			groupOwner = new GroupOwner(macAddress);
			groupOwner.addContact(macAddress, groupOwnerAddress);
		}
		isGroupOwner = true;
	}
	
	@Override
	public void removeGroupOwner(){
		if(isGroupOwner){
			isGroupOwner = false;
			groupOwner = null;
		}
	}
	
	@Override
	public void enableP2P(InetAddress groupOwnerAddress, Collection<WifiP2pDevice> devices){
		//Alwways initiate local contact manager. Request contacts based upon device role.
		//Enable p2p and start server communication thread.
		lcm = new LocalContactManager(macAddress, groupOwnerAddress);
		if(!p2pEnabled){
			serverThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			p2pEnabled = true;
		}
		//If you are a client, wait 1sec to allow group owner time to establish server thread.
		//Request contacts from the group owner.
		if(!isGroupOwner){
			try{
				Thread.sleep(1000);
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
			lcm.requestContacts(devices);
		}
		//Otherwise, if you are the group owner, request other contact info from your local data.
		else{
			lcm.requestContactsAsGroupOwner(devices, groupOwner);
		}
	}
	
	
	@Override
	public void disableP2P(){
		isGroupOwner = false;
		groupOwner = null;
		lcm = null;
		p2pEnabled = false;
		serverThread.cancel(true);
	}
	
//	public MulticastBroadcaster getBroadcaster(){
//		return mcBroadcaster;
//	}
	
//	public MulticastReceiver getReceiver(){
//		return mcReceiver;
//	}
	
	public void sendMessage(View view){
		if(p2pEnabled){
			String message = ((EditText) findViewById(R.id.edit_message)).getText().toString();
			lcm.messageAll(message);
//			try{
//				new MulticastBroadcaster(InetAddress.getByName(Constants.IP_ADDRESS), 
//						Constants.PORT_IN,Constants.PORT_OUT).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message);
//			}
//			catch(UnknownHostException e){
//				Log.i(TAG, "FAILED TO FIND MULTICAST HOST");
//			}
		}
	}
	
	public void updateMessageView(String message){
		TextView textView = new TextView(this);
		textView.setText(message);
		textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		LinearLayout layout = (LinearLayout) findViewById(R.id.messageDisplay_layout);
		layout.addView(textView);
		layout.postInvalidate();
	}
	
	public String getMacAddress(){
		return macAddress;
	}
	
	public boolean macFound(){
		return macFound;
	}
	
	public void macDetector(String mac){
		macAddress = mac;
		macFound = true;
	}
	
	public LocalContactManager getLocalContactManager(){
		return lcm;
	}
	
	public boolean p2pEnabled(){
		return p2pEnabled;
	}
}

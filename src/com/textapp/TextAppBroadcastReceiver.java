package com.textapp;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

public class TextAppBroadcastReceiver extends BroadcastReceiver /*implements WifiP2pManager.PeerListListener*/{
	
	public interface P2PEnabledListener{
		public void makeGroupOwner(InetAddress groupOwnerAddress);
		public void removeGroupOwner();
		public void enableP2P(InetAddress groupOwnerAddress, Collection<WifiP2pDevice> deviceList);
		public void disableP2P();
		public void macDetector(String mac);
	}

	private static final String TAG = "TextAppBroadcastReceiver";
	
	private WifiP2pManager mManager;
	private Channel mChannel;
	private MainActivity mActivity;

	public TextAppBroadcastReceiver(WifiP2pManager manager, Channel channel, MainActivity activity) {
	        super();
	        
	        if(Constants.VERBOSE) Log.v(TAG, TAG + " CALLED");

	        this.mManager = manager;
	        this.mChannel = channel;
	        this.mActivity = activity;
	    }

	@Override
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		
		if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
			//WiFi P2P changed action--get current state  (enabled or disabled)
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
	        	//WiFi P2P is enabled
	        	if(Constants.VERBOSE) Log.v(TAG, "WIFI P2P ENABLED");
//	        	((P2PEnabledListener) mActivity).enableP2P();
	        	mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener(){
	        		
	        		@Override
	        		public void onSuccess(){
	        			Log.i(TAG, "PEERS FOUND");
	        		}
	        		
	        		@Override
	        		public void onFailure(int code){
	        			Log.i(TAG, "FAILURE TO FIND PEERS. FAILURE CODE: " + code);
	        		}
	        	});
	        }
	        else {
	            // WiFi P2P is not enabled
	        	if(Constants.VERBOSE) Log.v(TAG, "WIFI P2P DISABLED");
	        	((P2PEnabledListener) mActivity).disableP2P();
	        }
		}
		if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
//			mActivity.getReceiver().execute();
			if(mManager != null) mManager.requestPeers(mChannel, new PeerListListener(){
				@Override
				public void onPeersAvailable(WifiP2pDeviceList peers){
					if(Constants.VERBOSE) Log.v(TAG, "PEERS AVAILABLE");
					WifiP2pDevice device;
					WifiP2pConfig config = new WifiP2pConfig();
					Iterator<WifiP2pDevice> deviceIterator = peers.getDeviceList().iterator();
					while(deviceIterator.hasNext()){
						device = deviceIterator.next();
						config.deviceAddress = device.deviceAddress;
						mManager.connect(mChannel, config, new ActionListener(){
							
							@Override
							public void onSuccess(){
								Log.i(TAG, "CONNECTED");
							}
							
							@Override
							public void onFailure(int code){
								Log.i(TAG, "FAILURE TO CONNECT. " + code);
							}
						});
					}
				}
			});
		}
		if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
			if(mManager.equals(null)) return;
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if(networkInfo.isConnected()) mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener(){
				@Override
				public void onConnectionInfoAvailable(final WifiP2pInfo info){
					final InetAddress groupOwnerAddress = info.groupOwnerAddress;
					if(info.isGroupOwner && info.groupFormed){
						mActivity.makeGroupOwner(groupOwnerAddress);
					}
					if(!info.isGroupOwner){
						mActivity.removeGroupOwner();
					}
					if(info.groupFormed){
						mManager.requestPeers(mChannel, new PeerListListener(){
							@Override
							public void onPeersAvailable(WifiP2pDeviceList peers){
								mActivity.enableP2P(groupOwnerAddress, peers.getDeviceList());
							}
						});
					}
				}
			});
		}
		if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
			WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

	        mActivity.macDetector(device.deviceAddress);
		}
//		if(Constants.SEND_MESSAGE.equals(action)){
//			try{
//				MulticastBroadcaster broadcaster = new MulticastBroadcaster(
//						InetAddress.getByName(Constants.IP_ADDRESS), Constants.PORT);
//				broadcaster.execute(intent.getStringExtra(MainActivity.EXTRA_MESSAGE));
//				Log.i(TAG, "MESSAGE SENT SUCCESSFULLY");
//			}
//			catch(UnknownHostException e){
//				Log.i(TAG, "MESSAGE FAILED TO SEND: HOST ADDRESS UNKNOWN");
//			}
//		}
	}
	
//	@Override
//	public void onPeersAvailable(WifiP2pDeviceList peers){
//		if(Constants.VERBOSE) Log.v(TAG, "PEERS AVAILABLE");
//		WifiP2pDevice device;
//		WifiP2pConfig config = new WifiP2pConfig();
//		Iterator<WifiP2pDevice> deviceIterator = peers.getDeviceList().iterator();
//		while(deviceIterator.hasNext()){
//			device = deviceIterator.next();
//			config.deviceAddress = device.deviceAddress;
//			mManager.connect(mChannel, config, new ActionListener(){
//				
//				@Override
//				public void onSuccess(){
//					Log.i(TAG, "CONNECTED");
//				}
//				
//				@Override
//				public void onFailure(int code){
//					Log.i(TAG, "FAILURE TO CONNECT. " + code);
//				}
//			});
//		}
//	}
}

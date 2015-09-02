package com.textapp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.os.AsyncTask;
import android.util.Log;

public class MulticastBroadcaster extends AsyncTask<String, Void, Boolean>{

	private int portIn;
	private int portOut;
	private static final int TIME_TO_LIVE = 2;
	
	private static final String TAG = "MulticastBroadcaster";
	
	private InetAddress address;
	
	public MulticastBroadcaster(InetAddress address, int portIn, int portOut){
		this.address = address;
		this.portIn = portIn;
		this.portOut = portOut;
	}
	
	@Override
	protected Boolean doInBackground(String... params){
		String message = params[0];
		DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, portIn);
		if(Constants.VERBOSE) Log.v(TAG, "DATAPACKET CREATED");
		try{
			MulticastSocket multicastSocket = new MulticastSocket(portOut);
			multicastSocket.joinGroup(address);
			multicastSocket.setTimeToLive(TIME_TO_LIVE);
			multicastSocket.send(packet);
			multicastSocket.close();
			if(Constants.VERBOSE) Log.i(TAG, "MESSAGE SENT");
			return true;
		}
		catch(IOException e){
			if(Constants.VERBOSE) Log.i(TAG, "MESSAGE FAILED TO SEND");
			return false;
		}
	}
}

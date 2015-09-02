package com.textapp;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class MulticastReceiver extends AsyncTask<Void, DatagramPacket, Boolean>{
	
	public interface MessageDetector{
		public void onDetect(DatagramPacket packet);
	}

	private static final String TAG = "MulticastReceiver";
	
	private static final int BUFFER_LENGTH = 640;
	private static final int TIMEOUT_LENGTH = 100;
	private static final int WAIT_LENGTH = 100;
	
	private int port;
	private byte[] bytes = new byte[BUFFER_LENGTH];
	private InetAddress address;
	private DatagramPacket packet;
	private MessageDetector detector;
	
	public MulticastReceiver(InetAddress address, int port, MessageDetector detector){
		this.address = address;
		this.port = port;
		this.detector = detector;
		packet = new DatagramPacket(bytes, BUFFER_LENGTH);
	}
	
	@Override
	protected Boolean doInBackground(Void... unused){
		try{
			MulticastSocket multicastSocket = new MulticastSocket(port);
			multicastSocket.joinGroup(address);
			while(!isCancelled()){
				multicastSocket.receive(packet);
				publishProgress(packet);
				packet = new DatagramPacket(bytes, BUFFER_LENGTH);
			}
			multicastSocket.close();
			return false;
		}
		catch(IOException e){
			Log.i(TAG, "SOCKET CREATION FAILED");
			return false;
		}
	}
	
	@Override
	protected void onProgressUpdate(DatagramPacket... received){
		for(DatagramPacket rPacket: received){
			detector.onDetect(rPacket);;
		}
	}
	
//	private void receive(MulticastSocket socket, DatagramPacket packet)
//			throws IOException{
//		if(!isCancelled()){
//			try{
//				Log.i(TAG, "WAITING ON MESSAGE");
//				socket.receive(packet);
//				Log.i(TAG, "MESSAGE RECEIVED!");
//				detector.onDetect();
//			}
//			catch(InterruptedIOException e){
//				Log.i(TAG, "NO MESSAGE RECEIVED");
//			}
//			finally{
//				receive(socket, packet);
//				try{
//					wait(WAIT_LENGTH);
//				}
//				catch(InterruptedException e){
//					Log.i(TAG, "MESSAGE WAIT INTERRUPTED");
//				}
//			}
//		}
//	}
}

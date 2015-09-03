package com.textapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

public class ServerThread extends AsyncTask<Void, Transmittable, Boolean>{
	
	public interface MessageDetector{
		public void onDetect(Transmittable transmittable);
	}
	
	
	private static final String TAG = "ServerThread";
	
//	private InetAddress address;
	private int port;
	private MessageDetector detector;
	

	public ServerThread(int port, MessageDetector detector){
		this.port = port;
		this.detector = detector;
	}
	
	
//	public TextAppP2pServer(InetAddress address, int port, MessageDetector detector){
//		this.address = address;
//		this.port = port;
//		this.detector = detector;
//		buffer = new byte[Constants.BUFFER_LENGTH];
//	}
	
	
	@Override
	protected Boolean doInBackground(Void... unused){
		android.os.Debug.waitForDebugger();
		try{
			ServerSocket socket = new ServerSocket(port);
//			socket.setSoTimeout(TIMEOUT_LENGTH);
			while(!isCancelled()){
				Socket client = socket.accept();
				TextAppStreamReader sr = new TextAppStreamReader(client.getInputStream());
				Transmittable data = sr.resolveObject(sr.readObject());
				data.setSentAddress(client.getInetAddress());
				sr.close();
				publishProgress(data);
			}
			socket.close();
			return true;
		}
		catch(IOException e){
			Log.e(TAG, e.getMessage());
            return false;
		}
		catch(ClassNotFoundException e){
			Log.e(TAG, e.getMessage());
			return false;
		}
	}
	
	
	@Override
	protected void onProgressUpdate(Transmittable... transmittables){
		for(Transmittable transmittable: transmittables){
			detector.onDetect(transmittable);
		}
	}
}
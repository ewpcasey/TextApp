package com.textapp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

public class MessageTask extends AsyncTask<Transmittable, Void, Boolean>{
	
	private static final String TAG = "MessageTask";
	
	private InetSocketAddress address;

	
	public MessageTask(int port, InetAddress address){
		this.address = new InetSocketAddress(address, port);
	}
	
	
	protected Boolean doInBackground(Transmittable... params){
		android.os.Debug.waitForDebugger();
		
		Socket socket = new Socket();
		try{
			socket.bind(null);	
			socket.connect(address);
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			for(Transmittable toSend: params){
				oos.writeObject(toSend);
			}
			oos.close();
			return true;
		}
		catch(IOException e){
			Log.e(TAG, e.getMessage());
			return false;
		}
		finally{
			if(socket != null){
				if(socket.isConnected()){
					try{
						socket.close();
					}
					catch(IOException e){
						Log.e(TAG, e.getMessage());
					}
				}
			}
		}
	}
}

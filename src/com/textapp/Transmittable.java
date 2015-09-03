package com.textapp;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public abstract class Transmittable
		implements Serializable{
	
	/*This class acts as a superclass and as a container for all the various transmittable types we want to use.
	On its own, it contains the address this transmittable was sent from, if applicable, the set of possible types,
	and a record of what type is currently being used. It is serializable so that it may be sent over a data stream.*/
	
	private static final long serialVersionUID = 0L;
	
//	private static final String TAG = "Transmittable";
	public final String TAG;
	
	protected Type type;
	private InetAddress sentAddress;

	private Transmittable(String tag){
		TAG = tag;
	}
	
	public Type getType(){
		return type;
	}
	
	public void setSentAddress(InetAddress address){
		sentAddress = address;
	}
	
	public InetAddress getSentAddress(){
		return sentAddress;
	}
	
	
	//Delineate the possible types that a Transmittable can act as
	public enum Type{
		
		CONTACT, MESSAGE, CONTACT_REQUEST;
		
	}
	
	
	//A transmittable that contains a matching ip/mac address pair
	public static class Contact extends Transmittable{
		
		private static final long serialVersionUID = 0L;
		
		public static final String TAG = "Transmittable.Contact";
		
		private String macAddress;
		private InetAddress ipAddress;
		
		private boolean hasIP;
		
		public Contact(String macAddress){
			super(TAG);
			constructorHelper(macAddress, null);
		}
		
		public Contact(String macAddress, InetAddress ipAddress){
			super(TAG);
			constructorHelper(macAddress, ipAddress);
		}
		
		public boolean hasIP(){
			return hasIP;
		}
		
		public InetAddress getInetAddress(){
			return ipAddress;
		}
		
		public String getMacAddress(){
			return macAddress;
		}
		
		private void constructorHelper(String macAddress, InetAddress ipAddress){
			type = Type.CONTACT;
			this.macAddress = macAddress;
			if(ipAddress != null){
				this.ipAddress = ipAddress;
				hasIP = true;
			}
			else{
				hasIP = false;
			}
			
		}
	}
	
	
	public static class ContactRequest extends Transmittable{
		
		private static final long serialVersionUID = 0L;
		
		public static final String TAG = "Transmittable.ContactRequest";
		
		private String macAddress;
		private ArrayList<String> requested;
		
		public ContactRequest(String macAddress){
			super(TAG);
			this.type = Type.CONTACT_REQUEST;
			this.macAddress = macAddress;
			requested = new ArrayList<String>();
		}
		
		public ContactRequest(String macAddress, List<String> macList){
			super(TAG);
			this.type = Type.CONTACT_REQUEST;
			this.macAddress = macAddress;
			requested = new ArrayList<String>();
			requestContacts(macList);
		}

		
		public void requestContacts(List<String> requests){
			requested.addAll(requests);
		}
		
		public void requestContact(String macAddress){
			requested.add(macAddress);
		}
		
		public List<String> pullContacts(){
			return requested;
		}
		
		public String getLocalMacAddress(){
			return macAddress;
		}
	}
	
	
	//A transmittable that contains a simple String object for text messaging.
	public static final class Message extends Transmittable{
		
		private static final long serialVersionUID = 0L;
		
		public static final String TAG = "Transmittable.Message";
		
		private String message;
		
		public Message(String message){
			super(TAG);
			this.type = Type.MESSAGE;
			this.message = message;
		}
		
		public String getMessage(){
			return message;
		}
	}
}

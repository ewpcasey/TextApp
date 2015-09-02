package com.textapp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.net.wifi.p2p.WifiP2pDevice;

public class LocalContactManager{

	private HashMap<String, InetAddress> contacts;
	private String macAddress;
	private InetAddress groupOwnerAddress;
	
	public LocalContactManager(String macAddress, InetAddress groupOwnerAddress){
		this.macAddress = macAddress;
		this.groupOwnerAddress = groupOwnerAddress;
		contacts = new HashMap<String, InetAddress>();
	}
	
	public LocalContactManager(String macAddress, InetAddress groupOwnerAddress, Map<String, InetAddress> toTransfer){
		this.macAddress = macAddress;
		this.groupOwnerAddress = groupOwnerAddress;
		contacts = new HashMap<String, InetAddress>();
		contacts.putAll(toTransfer);
	}
	
	
	public void requestContacts(Collection<WifiP2pDevice> deviceList){
		ArrayList<String> macList = new ArrayList<String>();
		for(WifiP2pDevice device: deviceList){
			macList.add(device.deviceAddress);
		}
		Transmittable.ContactRequest request = new Transmittable.ContactRequest(macAddress);
		new MessageTask(Constants.PORT_IN, groupOwnerAddress).executeOnExecutor(MessageTask.SERIAL_EXECUTOR, request);
	}
	
	public void requestContactsAsGroupOwner(Collection<WifiP2pDevice> deviceList, GroupOwner groupOwner){
		for(WifiP2pDevice device: deviceList){
			if(groupOwner.getMACList().contains(device.deviceAddress)){
				contacts.put(device.deviceAddress, groupOwner.getContact(device.deviceAddress));
			}
		}
	}
	
	public void messageAll(String message){
		Transmittable toSend = new Transmittable.Message(message);
		for(InetAddress contact: contacts.values()){
			new MessageTask(Constants.PORT_IN, contact).executeOnExecutor(MessageTask.SERIAL_EXECUTOR, toSend);
		}
	}
	
	public void addContact(String mac, InetAddress ip){
		contacts.put(mac, ip);
	}
	
	public void remove(String mac){
		contacts.remove(mac);
	}
	
	public InetAddress get(String mac){
		return contacts.get(mac);
	}
}

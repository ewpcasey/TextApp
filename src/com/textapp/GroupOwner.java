package com.textapp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupOwner {

	private HashMap<String, InetAddress> contacts;
	private String thisAddress;
	
	public GroupOwner(String macAddress){
		thisAddress = macAddress;
		contacts = new HashMap<String, InetAddress>();
	}
	
	public GroupOwner(String macAddress, Map<String, InetAddress> toTransfer){
		thisAddress = macAddress;
		contacts = new HashMap<String, InetAddress>();
		contacts.putAll(toTransfer);
	}
	
	
	public void distributeContact(String mac, InetAddress ip, List<String> macAddresses, LocalContactManager lcm){
		//Add the joining address to the mac/ip database
		addContact(mac, ip);
		
		//Make a new transmittable contact object for this new contact
		Transmittable.Contact newContact = new Transmittable.Contact(mac, ip);
		
		//Initialize contact request list
		Transmittable.ContactList toSend = new Transmittable.ContactList();
		
		//For every address we find that has been requested, add a new transmittable
		//contact to the list we're going to send out.
		for(String address: macAddresses){
			InetAddress contact = getContact(address);
			if(contact != null){
				toSend.addContact(new Transmittable.Contact(mac, ip));
			}
		}
		
		//For every relevant contact in our database, send the new contact's info out to them.
		//If one of those addresses is the group owner's, add it to the local contact manager instead.
		for(Transmittable.Contact contact: toSend.pullContacts()){
			if(contact.getMacAddress().equals(thisAddress)){
				lcm.addContact(mac, ip);
			}
			else{
				new MessageTask(Constants.PORT_IN, contact.getInetAddress()).executeOnExecutor(
						MessageTask.SERIAL_EXECUTOR, newContact);
			}
		}
		
		//Send the contact list to the new member
		new MessageTask(Constants.PORT_IN, ip).executeOnExecutor(MessageTask.SERIAL_EXECUTOR, toSend);
	}
	
	public void addContact(String mac, InetAddress ip){
		contacts.put(mac, ip);
	}
	
	public void removeContact(String mac){
		contacts.remove(mac);
	}
	
	public InetAddress getContact(String mac){
		return contacts.get(mac);
	}
	
	public Collection<String> getMACList(){
		return contacts.keySet();
	}
	
	public Collection<InetAddress> getIPList(){
		return contacts.values();
	}
}

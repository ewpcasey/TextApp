package com.textapp;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MessageFragment extends Fragment{
	
	private static final String TAG = "MessageFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		
		if(Constants.VERBOSE) Log.v(TAG, "onCreateView()");
		
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.message_fragment, container, false);
		
		if(Constants.VERBOSE) Log.v(TAG, "onCreateView()");
		
		return rootView;
	}
	
	public static MessageFragment newInstance(){
		MessageFragment fragment = new MessageFragment();
		return fragment;
	}
}

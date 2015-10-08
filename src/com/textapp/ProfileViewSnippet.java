package com.textapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class ProfileViewSnippet extends TextView{

	private String username;
	private String feed;
	
	private OnClickListener onClickListener;
	
	
	public ProfileViewSnippet(Context context, String username, String feed){
		super(context);
		setSnippetInfo(username, feed);
		initializeListener();
	}
	
	public ProfileViewSnippet(Context context, AttributeSet attrs, String username, String feed){
		super(context, attrs);
		setSnippetInfo(username, feed);
		initializeListener();
	}
	
	public ProfileViewSnippet(Context context, AttributeSet attrs, int defStyleAttr, String username, String feed){
		super(context, attrs, defStyleAttr);
		setSnippetInfo(username, feed);
		initializeListener();
	}
	
	public void setSnippetInfo(String username, String feed){
		this.username = username;
		this.feed = feed;
	}
	
	public void initializeListener(){
		onClickListener = new OnClickListener(){
			@Override
			public void onClick(View v){
				//Start new activity here!
			}
		};
	}
	
	
	public String getUsername(){
		return username;
	}
	
	public String getFeed(){
		return feed;
	}
}

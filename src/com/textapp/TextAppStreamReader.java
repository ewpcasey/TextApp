package com.textapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class TextAppStreamReader extends ObjectInputStream{

	public TextAppStreamReader(InputStream stream)
		throws IOException{
		super(stream);
	}
	
	
	@Override
	public Transmittable resolveObject(Object object){
		return (Transmittable) object;
	}
}

package pl.edu.mimuw.chatnfc.messanging;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import pl.edu.mimuw.chatnfc.tools.BinaryData;


public abstract class Message<T> extends BinaryData
{
	String senderUID; //package-private on purpose, do not change
	String timestamp;
	
	public Message()
	{
	}
	
	public Message(byte[] data)
	{
		super(data);
	}
	
	private void readInitial(byte[] data)
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(new BufferedInputStream(bis));
		
		try
		{
			int msgType = in.readInt();
			if (msgType != getMessageType().ordinal())
				throw new MessageTypeException(getMessageType().getWrapperClass(),
						Type.values()[msgType].getWrapperClass());
			
			senderUID = in.readUTF();
			timestamp = in.readUTF();
			
			readContentFromStream(in);
		}
		catch (IOException ex) //should never occur with ByteArray, so I'll catch it here
		{
			Log.e("IOException", "Armaggeddon: Error reading from ByteArrayInputStream!");
			throw new RuntimeException(ex);
		}
	}
	
	public final byte[] toByteArray()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(bos));
		
		try
		{
			out.writeInt(getMessageType().ordinal());
			out.writeUTF(senderUID);
			out.writeUTF(timestamp);
			
			writeContentToStream(out);
			out.flush();
		}
		catch (IOException ex)
		{
			Log.e("IOException", "Armaggeddon: Error writing to ByteArrayOutputStream!");
			throw new RuntimeException(ex);
		}
		
		return bos.toByteArray();
	}
	
	@Override
	public final void fromByteArray(byte[] data)
	{
		readInitial(data);
	}
	
	abstract void readContentFromStream(DataInputStream data) throws IOException;
	
	abstract void writeContentToStream(DataOutputStream out) throws IOException;
	
	public abstract Type getMessageType();
	
	public abstract T getMessageContent();
	
	public String getSenderUID()
	{
		return senderUID;
	}
	
	public String getTimestamp()
	{
		return timestamp;
	}
	
	public enum Type
	{
		TEXT_MESSAGE(String.class, TextMessage.class),
		CONFIG_MESSAGE(Map.class, ConfigMessage.class),
		IMAGE_MESSAGE(Bitmap.class, ImageMessage.class);
		//FILE_MESSAGE(byte[].class),
		//append new to the end for backward compatibility
		
		private Class<?> contentClass;
		private Class<? extends Message> wrapperClass;
		
		<T> Type(Class<T> contentClass, Class<? extends Message> wrapperClass)
		{
			this.contentClass = contentClass;
			this.wrapperClass = wrapperClass;
		}
		
		public Class<?> getContentClass()
		{
			return contentClass;
		}
		
		public Class<? extends Message> getWrapperClass()
		{
			return wrapperClass;
		}
	}
}

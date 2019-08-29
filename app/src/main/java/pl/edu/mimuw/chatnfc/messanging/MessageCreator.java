package pl.edu.mimuw.chatnfc.messanging;

import android.util.Log;

import pl.edu.mimuw.chatnfc.security.SecurityTools;

public class MessageCreator
{
	private MessageCreator()
	{
	}
	
	
	public static Message<?> createMessageFromBytes(byte[] data)
	{
		int msgType = SecurityTools.bytesToInt(data);
		
		Class<? extends Message> messageClass = Message.Type.values()[msgType].getWrapperClass();
		try
		{
			return messageClass.getConstructor(byte[].class)
					.newInstance(data); //Soooo non-objective, but convenient!
		}
		catch (Exception ex)
		{
			Log.e("createMessageFromBytes", ex.getMessage());
			return null; //cannot actually happen
		}
	}
}

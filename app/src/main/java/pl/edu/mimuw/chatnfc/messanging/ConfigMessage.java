package pl.edu.mimuw.chatnfc.messanging;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.chatnfc.tools.ObjectIO;

public class ConfigMessage extends Message<Map<String, String>>
{
	private int messageType;
	private Map<String, String> configuration;
	
	public ConfigMessage(byte[] data)
	{
		super(data);
	}
	
	public ConfigMessage(String sender, String timestamp, int msgType, Map<String, String> config)
	{
		this.senderUID = sender;
		this.timestamp = timestamp;
		this.messageType = msgType;
		this.configuration = config;
	}
	
	@Override
	void readContentFromStream(DataInputStream in) throws IOException
	{
		messageType = in.readInt();
		
		int capacity = in.readInt();
		configuration = new HashMap<>(capacity);
		
		for (int i = 0; i < capacity; ++i)
		{
			configuration.put(ObjectIO.readLongStringFromStream(in),
					ObjectIO.readLongStringFromStream(in));
		}
	}
	
	@Override
	void writeContentToStream(DataOutputStream out) throws IOException
	{
		out.writeInt(messageType);
		out.writeInt(configuration.size());
		
		for (Map.Entry<String, String> config : configuration.entrySet())
		{
			ObjectIO.writeLongStringToStream(config.getKey(), out);
			ObjectIO.writeLongStringToStream(config.getValue(), out);
		}
	}
	
	public int getMessageFlags()
	{
		return messageType;
	}
	
	@Override
	public Type getMessageType()
	{
		return Type.CONFIG_MESSAGE;
	}
	
	@Override
	public Map<String, String> getMessageContent()
	{
		return configuration;
	}
	
	public static int CONFIG_FLAG_CHANGE_AVATAR = 1;
	public static int CONFIG_FLAG_CHANGE_STATUS = 2;
	public static int CONFIG_FLAG_DELETE_ACCOUNT = 4; //rozróżniamy usuwanie konta i usuwanie z kontaktów?
}

package pl.edu.mimuw.chatnfc.messanging;

import android.util.Log;

import java.io.*;

public class TextMessage extends Message<String>
{
	String text;
	
	public TextMessage(byte[] data)
	{
		super(data);
	}
	
	public TextMessage(String senderUID, String timestamp, String text)
	{
		this.senderUID = senderUID;
		this.timestamp = timestamp;
		this.text = text;
	}
	
	@Override
	void readContentFromStream(DataInputStream in) throws IOException
	{
		text = in.readUTF();
	}
	
	@Override
	void writeContentToStream(DataOutputStream out) throws IOException
	{
		out.writeUTF(text);
	}
	
	@Override
	public Type getMessageType()
	{
		return Type.TEXT_MESSAGE;
	}
	
	@Override
	public String getMessageContent()
	{
		return text;
	}
}

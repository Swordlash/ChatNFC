package pl.edu.mimuw.chatnfc.messanging;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ImageMessage extends Message<Bitmap>
{
	public static int IMAGE_QUALITY = 20;
	
	private byte[] bitmapData;
	private Bitmap bmp;
	
	public ImageMessage(byte[] data)
	{
		super(data);
	}
	
	public ImageMessage(String sender, String timestamp, byte[] bitmapData)
	{
		this.senderUID = sender;
		this.timestamp = timestamp;
		this.bitmapData = bitmapData;
		
		readBitmapFromByteArray();
	}
	
	public ImageMessage(String sender, String timestamp, Bitmap bmp)
	{
		this.senderUID = sender;
		this.timestamp = timestamp;
		this.bmp = bmp;
		
		writeBitmapToByteArray();
	}
	
	@Override
	void readContentFromStream(DataInputStream data) throws IOException
	{
		int size = data.readInt();
		bitmapData = new byte[size];
		data.read(bitmapData);
		
		readBitmapFromByteArray();
	}
	
	@Override
	void writeContentToStream(DataOutputStream out) throws IOException
	{
		out.writeInt(bitmapData.length);
		out.write(bitmapData);
	}
	
	private void readBitmapFromByteArray()
	{
		bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
	}
	
	private void writeBitmapToByteArray()
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, out);
		bitmapData = out.toByteArray();
	}
	
	@Override
	public Type getMessageType()
	{
		return Type.IMAGE_MESSAGE;
	}
	
	@Override
	public Bitmap getMessageContent()
	{
		return bmp;
	}
}

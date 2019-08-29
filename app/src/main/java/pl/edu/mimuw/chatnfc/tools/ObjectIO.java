package pl.edu.mimuw.chatnfc.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ObjectIO
{
	private ObjectIO()
	{
	}
	
	public static void writeNullableStringToStream(String string, DataOutputStream out)
			throws IOException
	{
		if (string == null)
			out.writeUTF("");
		else
			out.writeUTF(string);
	}
	
	public static String readNullableStringFromStream(DataInputStream in) throws IOException
	{
		return in.readUTF();
	}
	
	@Nullable
	public static Bitmap readBitmapFromStream(DataInputStream stream) throws IOException
	{
		int len = stream.readInt();
		if (len == 0)
			return null;
		
		byte[] arr = new byte[len];
		stream.read(arr);
		
		return BitmapFactory.decodeByteArray(arr, 0, arr.length);
	}
	
	public static void writeUncompressedBitmapToStream(Bitmap bmp, DataOutputStream out)
			throws IOException
	{
		if (bmp == null)
		{
			out.writeInt(0);
			return;
		}
		
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, data);
		
		byte[] arr = data.toByteArray();
		out.writeInt(arr.length);
		
		out.write(arr);
	}
	
	public static void writeCompressedBitmapToStream(Bitmap bmp, DataOutputStream out, int quality)
			throws IOException
	{
		if (bmp == null)
		{
			out.writeInt(0);
			return;
		}
		
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, quality, data);
		
		byte[] arr = data.toByteArray();
		out.writeInt(arr.length);
		
		out.write(arr);
	}
	
	public static Bitmap bitmapFromString(String str)
	{
		byte[] bmp = Base64.decode(str, Base64.DEFAULT);
		return BitmapFactory.decodeByteArray(bmp, 0, bmp.length);
	}
	
	public static String bitmapToString(Bitmap bmp, int quality)
	{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, quality, data);
		
		byte[] arr = data.toByteArray();
		return Base64.encodeToString(arr, Base64.DEFAULT);
	}
	
	public static void writeByteArrayToStream(byte[] arr, DataOutputStream stream)
			throws IOException
	{
		if (arr == null)
		{
			stream.writeInt(-1);
			return;
		}
		
		stream.writeInt(arr.length);
		stream.write(arr);
	}
	
	public static byte[] readByteArrayFromStream(DataInputStream stream) throws IOException
	{
		int len = stream.readInt();
		if (len < 0)
			return null;
		
		byte[] rv = new byte[len];
		stream.read(rv);
		
		return rv;
	}
	
	public static String readLongStringFromStream(DataInputStream in) throws IOException
	{
		byte[] sn = readByteArrayFromStream(in);
		return new String(sn, "UTF-8");
	}
	
	public static void writeLongStringToStream(String str, DataOutputStream out) throws IOException
	{
		writeByteArrayToStream(str.getBytes("UTF-8"), out);
	}
}

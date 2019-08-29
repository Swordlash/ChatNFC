package pl.edu.mimuw.chatnfc.config;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.edu.mimuw.chatnfc.tools.BinaryData;
import pl.edu.mimuw.chatnfc.tools.ObjectIO;

public class Contact extends BinaryData implements Comparable<Contact>
{
	private String userID;
	private String name;
	private String surname;
	private String status;
	
	private Bitmap avatar;
	private byte[] communicationKey;
	private byte[] authenticationKey;
	
	public Contact(byte[] data)
	{
		super(data);
	}
	
	public Contact(String userID, String name, String surname, String status, Bitmap avatar,
	               byte[] communicationKey, byte[] authenticationKey)
	{
		this.userID = userID;
		this.name = name;
		this.surname = surname;
		this.status = status;
		this.avatar = avatar;
		this.communicationKey = communicationKey;
		this.authenticationKey = authenticationKey;
	}
	
	public String getUserID()
	{
		return userID;
	}
	
	public void setUserID(String userID)
	{
		this.userID = userID;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getSurname()
	{
		return surname;
	}
	
	public String getStatus()
	{
		return status;
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public Bitmap getAvatar()
	{
		return avatar;
	}
	
	public void setAvatar(Bitmap avatar)
	{
		this.avatar = avatar;
	}
	
	public byte[] getCommunicationKey()
	{
		return communicationKey;
	}
	
	public byte[] getAuthenticationKey()
	{
		return authenticationKey;
	}
	
	public String getNameSurname()
	{
		return String.format("%s %s", name, surname);
	}
	
	@Override
	public int compareTo(Contact c)
	{
		int cmpSurname = surname.compareTo(c.surname);
		int cmpName = name.compareTo(c.name);
		int cmpUid = userID.compareTo(c.userID); //Comparator.comparing available from API 24
		
		if (cmpSurname == 0 && cmpName == 0)
			return cmpUid;
		else if (cmpSurname == 0)
			return cmpName;
		return cmpName;
	}
	
	@Override
	public void fromByteArray(byte[] data)
	{
		ByteArrayInputStream bin = new ByteArrayInputStream(data);
		DataInputStream in = new DataInputStream(new BufferedInputStream(bin));
		try
		{
			userID = ObjectIO.readNullableStringFromStream(in);
			name = ObjectIO.readNullableStringFromStream(in);
			surname = ObjectIO.readNullableStringFromStream(in);
			status = ObjectIO.readNullableStringFromStream(in);
			
			avatar = ObjectIO.readBitmapFromStream(in);
			communicationKey = ObjectIO.readByteArrayFromStream(in);
			authenticationKey = ObjectIO.readByteArrayFromStream(in);
		}
		catch (IOException ex)
		{
			Log.e("Contact", "Error fromByteArray\n" + ex.getMessage());
		}
	}
	
	@Override
	public byte[] toByteArray()
	{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(data));
		
		try
		{
			ObjectIO.writeNullableStringToStream(userID, out);
			ObjectIO.writeNullableStringToStream(name, out);
			ObjectIO.writeNullableStringToStream(surname, out);
			ObjectIO.writeNullableStringToStream(status, out);
			
			ObjectIO.writeUncompressedBitmapToStream(avatar, out);
			ObjectIO.writeByteArrayToStream(communicationKey, out);
			ObjectIO.writeByteArrayToStream(authenticationKey, out);
			
			out.flush();
		}
		catch (IOException ex)
		{
			Log.e("Contact", "Error writing to byte array\n" + ex.getMessage());
			return null;
		}
		
		return data.toByteArray();
	}
}

package pl.edu.mimuw.chatnfc.config;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import pl.edu.mimuw.chatnfc.security.ECDHKeyPair;
import pl.edu.mimuw.chatnfc.security.KeyStoreProvider;
import pl.edu.mimuw.chatnfc.tools.BinaryData;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectIO;

public class UserProfile extends BinaryData
{
	private static UserProfile LOCAL;
	
	private String userID;
	private String name;
	private String surname;
	private String status;
	
	private Bitmap avatar;
	
	private Map<String, Contact> contacts;
	
	private ECDHKeyPair pairingKey = null;
	private ECDHKeyPair pairingAuthKey = null;
	
	public UserProfile(byte[] rawData)
	{
		super(rawData);
	}
	
	private UserProfile(String userID, String name, String surname)
	{
		this.userID = userID;
		this.name = name;
		this.surname = surname;
		
		this.status = "Hello! I'm using Unific chat app!";
		this.avatar = null;
		
		this.contacts = new TreeMap<>();
	}
	
	public String getUserID()
	{
		return userID;
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
	
	public Contact getContactByUID(String userID)
	{
		return contacts.get(userID);
	}
	
	public byte[] getCommunicationKey(String userID)
	{
		return contacts.get(userID).getCommunicationKey();
	}
	
	public byte[] getAuthenticationKey(String userID)
	{
		return contacts.get(userID).getAuthenticationKey();
	}
	
	public Map<String, Contact> getContacts()
	{
		return contacts;
	}
	
	public ECDHKeyPair getPairingKey()
	{
		return pairingKey;
	}
	
	public ECDHKeyPair getPairingAuthKey()
	{
		return pairingAuthKey;
	}
	
	public void generateNewPairingKey() throws InvalidAlgorithmParameterException
	{
		pairingKey = ECDHKeyPair.generateNewInstance();
		pairingAuthKey = ECDHKeyPair.generateNewInstance();
	}
	
	public void addContact(String uid, Contact c)
	{
		if (uid == null || uid == "")
			return;
		contacts.put(uid, c);
	}
	
	public static UserProfile getLocalProfile()
	{
		if (LOCAL == null && FirebaseTools.getInstance().getCurrentUser() != null)
			return (LOCAL = UserProfileProvider
					.getLocalUserProfile(FirebaseTools.getInstance().getCurrentUser().getUid()));
		
		return LOCAL;
	}
	
	public static UserProfile getProfile(String userUID)
	{
		if (LOCAL != null && LOCAL.getUserID().equals(userUID))
			return LOCAL;
		
		return (LOCAL = UserProfileProvider.getLocalUserProfile(userUID));
	}
	
	@Override
	public void fromByteArray(byte[] data)
	{
		try
		{
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
			
			userID = in.readUTF();
			name = in.readUTF();
			surname = in.readUTF();
			status = in.readUTF();
			
			avatar = ObjectIO.readBitmapFromStream(in);
			
			int contactsCount = in.readInt();
			contacts = new TreeMap<>();
			
			for (int i = 0; i < contactsCount; ++i)
			{
				String contactUID = in.readUTF();
				
				Contact contact = new Contact(ObjectIO.readByteArrayFromStream(in));
				contacts.put(contactUID, contact);
			}
			
			Log.d("UserProfile#frByteArray", String.format("%s, name: %s, surname %s, contacts %d",
					userID, name, surname, contacts.size()));
		}
		catch (IOException ex)
		{
			Log.e("UserConfig#load", ex.getMessage());
		}
	}
	
	@Override
	public byte[] toByteArray()
	{
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(data));
		
		try
		{
			Log.d("UserProfile#toByteArray", String.format("%s, name: %s, surname %s, contacts %d",
					userID, name, surname, contacts.size()));
			out.writeUTF(userID);
			out.writeUTF(name);
			out.writeUTF(surname);
			out.writeUTF(status);
			
			ObjectIO.writeCompressedBitmapToStream(avatar, out, 30);
			
			out.writeInt(contacts.size());
			for (Map.Entry<String, Contact> contact : contacts.entrySet())
			{
				out.writeUTF(contact.getKey());
				ObjectIO.writeByteArrayToStream(contact.getValue().toByteArray(), out);
			}
			out.flush();
		}
		catch (Exception ex)
		{
			Log.e("UserLocalConfig#save", ex.getMessage());
		}
		
		return data.toByteArray();
	}
	
	public static void setLocalProfile(UserProfile profile)
	{
		LOCAL = profile;
	}
	
	public Set<String> getContactIDs()
	{
		return contacts.keySet();
	}
	
	public static UserProfile createNewProfile(String uid, String password, String name, String surname)
	{
		KeyStoreProvider.encryptAndSaveUserPassword(uid, password);
		return (LOCAL = new UserProfile(uid, name, surname));
	}
}

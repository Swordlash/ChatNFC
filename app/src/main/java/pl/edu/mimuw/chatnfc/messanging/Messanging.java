package pl.edu.mimuw.chatnfc.messanging;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.chatnfc.config.Contact;
import pl.edu.mimuw.chatnfc.config.MessageHandler;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.security.AuthenticatedDataPackage;
import pl.edu.mimuw.chatnfc.security.AuthenticationException;
import pl.edu.mimuw.chatnfc.security.CryptographicException;
import pl.edu.mimuw.chatnfc.security.KeyManager;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectIO;
import pl.edu.mimuw.chatnfc.tools.TimeProvider;
import pl.edu.mimuw.chatnfc.ui.UnificApp;

public class Messanging
{
	private Messanging()
	{
	}
	
	public static Message<?> receiveMessage(String friendContact, DataSnapshot dataSnapshot, boolean dispatch)
	{
		if (dataSnapshot.getKey().equals("null"))
			return null;
		
		if (dataSnapshot.getValue() instanceof Map) //take only first one
		{
			dataSnapshot = dataSnapshot.getChildren().iterator().next();
		}
		
		String dbTimestamp = dataSnapshot.getKey();
		String dataString = (String) dataSnapshot.getValue();
		
		byte[] data = Base64.decode(dataString, Base64.DEFAULT);
		
		AuthenticatedDataPackage pack = new AuthenticatedDataPackage(data);
		byte[] decrypted;
		
		try
		{
			decrypted = KeyManager.verifyAndDecryptDataPackage(friendContact, pack);
		}
		catch (BadPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
				| IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException e)
		{
			dataSnapshot.getRef().removeValue();
			return new TextMessage(friendContact, dbTimestamp, "This message failed to be decrypted");
		}
		catch (AuthenticationException e)
		{
			dataSnapshot.getRef().removeValue();
			return new TextMessage(friendContact, dbTimestamp, "This message failed to authenticate");
		}
		
		Message<?> msg = MessageCreator.createMessageFromBytes(decrypted);
		
		if (!msg.getTimestamp().equals(dbTimestamp))
		{
			dataSnapshot.getRef().removeValue();
			return new TextMessage(friendContact, Long
					.toString(System.currentTimeMillis()), "This message had malicious timestamp!");
		}
		
		
		if (dispatch)
			MessageHandler.dispatchMessage(friendContact, msg);
		
		if (msg.getMessageType() == Message.Type.CONFIG_MESSAGE)
		{
			dataSnapshot.getRef().removeValue();
			return null; //TODO: Inform the friendContact of success
		}
		
		return msg;
	}
	
	public static void sendMessage(String sender, String recipient, String timestamp, Message<?> message)
			throws BadPaddingException, CryptographicException, InvalidKeyException, NoSuchAlgorithmException,
			IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException
	{
		AuthenticatedDataPackage pack = KeyManager
				.createAndSignDataPackage(recipient, message.toByteArray());
		
		String messageString = Base64.encodeToString(pack.toByteArray(), Base64.DEFAULT);
		putMessageInDB(sender, recipient, timestamp, messageString);
	}
	
	private static void putMessageInDB(String sender, String recipient, String timestamp, String messageDataString)
	{
		FirebaseTools.getInstance()
				.setValueInDB("Users/" + sender + "/messages/" + recipient + "/" + timestamp, messageDataString);
		FirebaseTools.getInstance()
				.setValueInDB("Users/" + recipient + "/contacts/" + sender + "/seen", Boolean.FALSE);
		FirebaseTools.getInstance()
				.setValueInDB("Users/" + recipient + "/messages/" + sender + "/" + timestamp, messageDataString);
	}
	
	public static void notifyChangeImage()
	{
		new Thread(() ->
		{
			UserProfile local = UserProfile.getLocalProfile();
			long time = TimeProvider.getCurrentTimeMillisOrLocal(150);
			
			Map<String, String> mp = new HashMap<>();
			mp.put("IMAGE", ObjectIO.bitmapToString(local.getAvatar(), 30));
			ConfigMessage profileImage = new ConfigMessage(local.getUserID(),
					Long.toString(time),
					ConfigMessage.CONFIG_FLAG_CHANGE_AVATAR,
					mp);
			
			for (Map.Entry<String, Contact> en : local.getContacts().entrySet())
			{
				try
				{
					sendMessage(local.getUserID(), en.getValue().getUserID(),
							Long.toString(time), profileImage);
				}
				catch (Exception ex)
				{
					Toast.makeText(UnificApp.getUnificAppContext(),
							"Not all friends could get updated image", Toast.LENGTH_LONG).show();
					Log.e("UpdateContact", ex.getLocalizedMessage());
				}
			}
			
		}).start();
	}
	
	public static void notifyChangeStatus()
	{
		new Thread(() ->
		{
			UserProfile local = UserProfile.getLocalProfile();
			long time = TimeProvider.getCurrentTimeMillisOrLocal(150);
			
			Map<String, String> mp = new HashMap<>();
			mp.put("STATUS", local.getStatus());
			ConfigMessage profileImage = new ConfigMessage(local.getUserID(),
					Long.toString(time),
					ConfigMessage.CONFIG_FLAG_CHANGE_STATUS,
					mp);
			
			for (Map.Entry<String, Contact> en : local.getContacts().entrySet())
			{
				try
				{
					sendMessage(local.getUserID(), en.getValue().getUserID(),
							Long.toString(time), profileImage);
				}
				catch (Exception ex)
				{
					Toast.makeText(UnificApp.getUnificAppContext(),
							"Not all friends could get updated image", Toast.LENGTH_LONG).show();
					Log.e("UpdateContact", ex.getLocalizedMessage());
				}
			}
			
		}).start();
	}
}

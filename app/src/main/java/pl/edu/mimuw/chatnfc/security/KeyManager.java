package pl.edu.mimuw.chatnfc.security;


import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.messanging.Message;
import pl.edu.mimuw.chatnfc.messanging.MessageCreator;

public class KeyManager
{
//	private static String AES_TEST_KEY = "bqJS9pRAo9b19b07wwsiYT5KQBF3N7OSfu5bo9FblEY=";
//	private static String TEST_PASSWORD = "Gdzie jest JSON???";
//
//	private static byte[] AES_TEST_KEY_BYTES = Base64.decode(AES_TEST_KEY, Base64.DEFAULT);
//	private static byte[] TEST_PASSWORD_BYTES;
//
//	static
//	{
//		try
//		{
//			TEST_PASSWORD_BYTES = SecurityTools
//					.deriveKeyBytesFromPassword(TEST_PASSWORD.toCharArray(),
//							SecurityTools.getRandomPBKDFSalt());
//		}
//		catch (InvalidKeySpecException | NoSuchAlgorithmException e)
//		{
//			e.printStackTrace();
//		}
//	}
	
	private KeyManager(){}
	
	
	public static byte[] verifyAndDecryptDataPackage(String sender, AuthenticatedDataPackage pack)
			throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, AuthenticationException
	{
		UserProfile profile = UserProfile.getLocalProfile();
		
		byte[] decryptionKey = profile.getCommunicationKey(sender);
		byte[] authKey = profile.getAuthenticationKey(sender);
		
		if (pack.authenticates(authKey))
		{
			return pack.getDataDecrypted(decryptionKey);
		}
		else
			throw new AuthenticationException("AuthenticatedDataPackage does not authenticate!");
	}
	
	public static byte[] verifyAndDecryptLocalDataPackage(AuthenticatedDataPackage pack)
			throws BadPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, AuthenticationException
	{
		byte[] decryptionKey = KeyStoreProvider.getConfigEncryptionKey();
		byte[] authKey = KeyStoreProvider.getConfigAuthenticationKey();
		
		if (pack.authenticates(authKey))
		{
			return pack.getDataDecrypted(decryptionKey);
		}
		else
			throw new AuthenticationException("AuthenticatedDataPackage does not authenticate!");
	}
	
	public static byte[] decrypt(PasswordEncryptedDataPackage pack)
			throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException
	{
		return pack.getDataDecrypted(KeyStoreProvider
				.getSavedUserPassword(UserProfile.getLocalProfile().getUserID()));
	}
	
	
	public static AuthenticatedDataPackage createAndSignDataPackage(String recipientID, byte[] data)
			throws BadPaddingException, CryptographicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException
	{
		UserProfile local = UserProfile.getLocalProfile();
		byte[] iv = SecurityTools.getRandomAESIv();
		
		byte[] encryptionKey = local.getCommunicationKey(recipientID);
		byte[] authKey = local.getAuthenticationKey(recipientID);
		
		return new AuthenticatedDataPackage(iv, data, encryptionKey, authKey);
	}
	
	public static AuthenticatedDataPackage createAndSignLocalDataPackage(byte[] data)
			throws BadPaddingException, CryptographicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException,
			IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException
	{
		byte[] iv = SecurityTools.getRandomAESIv();
		
		byte[] authenticationKey = KeyStoreProvider.getConfigEncryptionKey();
		byte[] authKey = KeyStoreProvider.getConfigAuthenticationKey();
		
		return new AuthenticatedDataPackage(iv, data, authenticationKey, authKey);
	}
	
	public static String extractTextFromEncodedMessage(String sender, String dataString, long expectedTimestamp)
	{
		String ret;
		try
		{
			byte[] data = Base64.decode(dataString, Base64.DEFAULT);
			
			AuthenticatedDataPackage pack = new AuthenticatedDataPackage(data);
			byte[] decrypted = KeyManager.verifyAndDecryptDataPackage(sender, pack);
			Message<?> msg = MessageCreator.createMessageFromBytes(decrypted);
			
			switch (msg.getMessageType())
			{
				case TEXT_MESSAGE:
					ret = msg.getMessageContent().toString();
					break;
				case CONFIG_MESSAGE:
					ret = "Config message";
					break;
				case IMAGE_MESSAGE:
					ret = "Image";
					break;
				default:
					ret = "";
					break;
			}
			
			if(expectedTimestamp != Long.valueOf(msg.getTimestamp()))
			{
				throw new AuthenticationException("Invalid timestamp");
			}
		}
		catch (NoSuchAlgorithmException | InvalidKeyException |  InvalidAlgorithmParameterException |
				NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e)
		{
			return "Cryptographic exception occured";
		}
		catch(AuthenticationException ex)
		{
			return "Authentication exception occured";
		}
		catch(Exception e)
		{
			return "Encoding exception occured";
		}
		return ret;
	}
}



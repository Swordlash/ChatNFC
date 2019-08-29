package pl.edu.mimuw.chatnfc.security;


import android.util.Log;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PasswordEncryptedDataPackage extends EncryptedDataPackage
{
	private byte[] data;
	
	public PasswordEncryptedDataPackage(byte[] rawData)
	{
		super(rawData);
	}
	
	public PasswordEncryptedDataPackage(byte[] aasGcmNonce, byte[] pbdkfSalt, byte[] dataToEncrypt, char[] password)
			throws NoSuchPaddingException, InvalidKeySpecException, NoSuchAlgorithmException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException,
			CryptographicException
	{
		if (pbdkfSalt.length != SecurityTools.PBKDF2_SALT_LENGTH)
			throw new CryptographicException("Invalid PBDKF2 salt length!");
		
		if (aasGcmNonce.length != SecurityTools.AES_GCM_NONCE_LENGTH)
			throw new CryptographicException("Invalid AES/GCM nonce length!");
		
		byte[] encrypted = SecurityTools.encryptWithAESGCM(aasGcmNonce,
				SecurityTools.deriveKeyBytesFromPassword(password, pbdkfSalt), dataToEncrypt);
		
		data = new byte[aasGcmNonce.length + pbdkfSalt.length + encrypted.length];
		
		System.arraycopy(aasGcmNonce, 0, data, 0, aasGcmNonce.length);
		System.arraycopy(pbdkfSalt, 0, data, aasGcmNonce.length, pbdkfSalt.length);
		System.arraycopy(encrypted, 0, data, aasGcmNonce.length + pbdkfSalt.length, encrypted.length);
		
		Log.e("PEDPack", "In: " + dataToEncrypt.length + " bytes, out: " + data.length + " bytes");
	}
	
	@Override
	public byte[] getDataEncrypted()
	{
		byte[] encrypted = new byte[data.length - SecurityTools.AES_GCM_NONCE_LENGTH - SecurityTools.PBKDF2_SALT_LENGTH];
		
		System.arraycopy(data, SecurityTools.AES_GCM_NONCE_LENGTH + SecurityTools.PBKDF2_SALT_LENGTH,
				encrypted, 0, encrypted.length);
		return encrypted;
	}
	
	public byte[] getPBDKFSalt()
	{
		byte[] salt = new byte[SecurityTools.PBKDF2_SALT_LENGTH];
		
		System.arraycopy(data, SecurityTools.AES_GCM_NONCE_LENGTH, salt, 0, salt.length);
		return salt;
	}
	
	@Override
	public byte[] getDataDecrypted(byte[] aesKeyBytes)
			throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException
	{
		byte[] encrypted = getDataEncrypted();
		
		byte[] iv = new byte[SecurityTools.AES_GCM_NONCE_LENGTH];
		System.arraycopy(data, 0, iv, 0, SecurityTools.AES_GCM_NONCE_LENGTH);
		
		return SecurityTools.decryptWithAESGCM(iv, aesKeyBytes, encrypted);
	}
	
	@Override
	public void fromByteArray(byte[] rawData)
	{
		data = new byte[rawData.length];
		System.arraycopy(rawData, 0, data, 0, data.length);
	}
	
	@Override
	public byte[] toByteArray()
	{
//		byte[] ret = new byte[data.length];
//		System.arraycopy(data, 0, ret, 0, data.length);
//		return ret;
		return data;
	}
	
	public byte[] getDataDecrypted(char[] password)
			throws NoSuchPaddingException, InvalidKeySpecException, NoSuchAlgorithmException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException
	{
		byte[] salt = getPBDKFSalt();
		byte[] encrypted = getDataEncrypted();
		
		byte[] iv = new byte[SecurityTools.AES_GCM_NONCE_LENGTH];
		System.arraycopy(data, 0, iv, 0, SecurityTools.AES_GCM_NONCE_LENGTH);
		
		return SecurityTools.decryptWithAESGCM(iv,
				SecurityTools.deriveKeyBytesFromPassword(password, salt), encrypted);
	}
}

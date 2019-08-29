package pl.edu.mimuw.chatnfc.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class AuthenticatedDataPackage extends EncryptedDataPackage
{
	private byte[] data;
	
	public AuthenticatedDataPackage(byte[] rawData)
	{
		super(rawData);
	}
	
	/**
	 * Create new AuthenticatedDataPackage from given input.
	 * <p>
	 * AuthenticatedDataPackage contains all necessary information for decryption and authentication of contained
	 * dataToEncrypt, i.e. AES IV and MAC.
	 * <p>
	 * It is implemented as a variadic-length byte array, which first bytes contain AES IV
	 * of fixed length as specified in {@link SecurityTools}, next bytes contain message
	 * authentication code (MAC) of encrypted data and IV, and the rest of the array contains
	 * data encrypted with given aesKey, using aesIV as the initialization vector.
	 *
	 * @param aesIV         Initialization vector to be used to encrypt contained dataToEncrypt
	 * @param dataToEncrypt Bytes of data to encrypt
	 * @param aesKey        AES key to be used to encrypt given data
	 * @param hmacKey       Key to be used to authenticate encrypted data
	 * @throws InvalidKeyException                If bytes of aesKey contain invalid AES key
	 * @throws BadPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws CryptographicException             If any other cryptographic exception occured
	 */
	public AuthenticatedDataPackage(byte[] aesIV, byte[] dataToEncrypt, byte[] aesKey, byte[] hmacKey)
			throws InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, NoSuchAlgorithmException, CryptographicException, NoSuchPaddingException
	{
		if (aesIV.length != SecurityTools.AES_IV_LENGTH)
			throw new CryptographicException("Invalid AES IV length!");
		
		byte[] encrypted = SecurityTools.encryptWithAES(aesIV, aesKey, dataToEncrypt);
		byte[] messageBytes = new byte[aesIV.length + encrypted.length];
		
		System.arraycopy(aesIV, 0, messageBytes, 0, aesIV.length);
		System.arraycopy(encrypted, 0, messageBytes, aesIV.length, encrypted.length);
		
		byte[] mac = SecurityTools.generateHMAC(hmacKey, messageBytes);
		
		if (mac.length != SecurityTools.MAC_LENGTH)
			throw new CryptographicException("Mac length invalid! Expected: " + SecurityTools.MAC_LENGTH + ", found " + mac.length);
		
		this.data = new byte[messageBytes.length + mac.length];
		
		System.arraycopy(messageBytes, 0, this.data, 0, aesIV.length);
		System.arraycopy(mac, 0, this.data, aesIV.length, mac.length);
		System.arraycopy(messageBytes, aesIV.length, this.data, aesIV.length + mac.length, encrypted.length);
	}
	
	public boolean authenticates(byte[] hmacKey)
			throws InvalidKeyException, NoSuchAlgorithmException
	{
		int ivlen = SecurityTools.AES_IV_LENGTH;
		int ividmaclen = ivlen + SecurityTools.MAC_LENGTH;
		
		byte[] messageBytes = new byte[data.length - SecurityTools.MAC_LENGTH];
		System.arraycopy(data, 0, messageBytes, 0, ivlen);
		System.arraycopy(data, ividmaclen, messageBytes, ivlen, data.length - ividmaclen);
		
		byte[] authBytes = new byte[SecurityTools.MAC_LENGTH];
		System.arraycopy(data, ivlen, authBytes, 0, authBytes.length);
		
		byte[] authCorrect = SecurityTools.generateHMAC(hmacKey, messageBytes);
		
		return Arrays.equals(authBytes, authCorrect);
	}
	
	@Override
	public void fromByteArray(byte[] rawData)
	{
		data = new byte[rawData.length];
		System.arraycopy(rawData, 0, data, 0, rawData.length);
	}
	
	@Override
	public byte[] toByteArray()
	{
//		byte[] ret = new byte[data.length];
//		System.arraycopy(data, 0, ret, 0, data.length);
//		return ret;
		return data;
	}
	
	public byte[] getDataEncrypted()
	{
		int overhead = SecurityTools.AES_IV_LENGTH + SecurityTools.MAC_LENGTH;
		byte[] messageBytes = new byte[data.length - overhead];
		System.arraycopy(data, overhead, messageBytes, 0, messageBytes.length);
		return messageBytes;
	}
	
	public byte[] getDataDecrypted(byte[] aesKey)
			throws BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
			NoSuchPaddingException, InvalidAlgorithmParameterException
	{
		int overhead = SecurityTools.AES_IV_LENGTH + SecurityTools.MAC_LENGTH;
		byte[] messageBytes = new byte[data.length - overhead];
		byte[] iv = new byte[SecurityTools.AES_IV_LENGTH];
		
		System.arraycopy(data, overhead, messageBytes, 0, messageBytes.length);
		System.arraycopy(data, 0, iv, 0, iv.length);
		return SecurityTools.decryptWithAES(iv, aesKey, messageBytes);
	}
}

package pl.edu.mimuw.chatnfc.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;

public class AESGCMKey extends Key
{
	private SecretKey key;
	private GCMParameterSpec spec;
	private byte[] aadTag;
	
	private final Cipher cipher;
	
	public AESGCMKey(SecretKey key, byte[] aadTag)
			throws NoSuchPaddingException, NoSuchAlgorithmException
	{
		this.key = key;
		this.cipher = Cipher.getInstance(SecurityTools.AES_GCM_ALGORITHM);
		this.aadTag = Arrays.copyOf(aadTag, aadTag.length);
	}
	
	public AESGCMKey(byte[] key, byte[] aadTag)
			throws NoSuchPaddingException, NoSuchAlgorithmException
	{
		this.key = new SecretKeySpec(key, "AES");
		this.cipher = Cipher.getInstance(SecurityTools.AES_GCM_ALGORITHM);
		this.aadTag = Arrays.copyOf(aadTag, aadTag.length);
	}
	
	@Override
	public byte[] decrypt(byte[] nonce, byte[] message)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException
	{
		synchronized (cipher)
		{
			spec = new GCMParameterSpec(SecurityTools.AES_GCM_TAG_BIT_LENGTH, nonce);
			cipher.init(Cipher.DECRYPT_MODE, key, spec);
			cipher.updateAAD(aadTag);
			
			return cipher.doFinal(message);
		}
	}
	
	@Override
	public byte[] encrypt(byte[] nonce, byte[] message)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException
	{
		synchronized (cipher)
		{
			spec = new GCMParameterSpec(SecurityTools.AES_GCM_TAG_BIT_LENGTH, nonce);
			cipher.init(Cipher.ENCRYPT_MODE, key, spec);
			cipher.updateAAD(aadTag);
			
			return cipher.doFinal(message);
		}
	}
	
	@Override
	public void destroy() throws DestroyFailedException
	{
		key.destroy();
	}
}

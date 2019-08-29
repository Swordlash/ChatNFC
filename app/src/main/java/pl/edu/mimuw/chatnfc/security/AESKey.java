package pl.edu.mimuw.chatnfc.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;

/**
 * Created by mateusz on 05.03.18.
 */

public class AESKey extends Key {
	private SecretKey key;
	private Cipher cipher;
	
	public AESKey(SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException {
		this.key = key;
		this.cipher = Cipher.getInstance(SecurityTools.AES_ALGORITHM);
	}
	
	public AESKey(byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException
	{
		this.key = new SecretKeySpec(key, "AES");
		this.cipher = Cipher.getInstance(SecurityTools.AES_ALGORITHM);
	}
	
	@Override
	public byte[] decrypt(byte[] iv, byte[] message) throws
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
		if (iv == null)
			cipher.init(Cipher.DECRYPT_MODE, key);
		else {
			IvParameterSpec spec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, key, spec);
		}
		
		
		return cipher.doFinal(message);
	}
	
	@Override
	public byte[] encrypt(byte[] iv, byte[] message) throws
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
		if (iv == null)
			cipher.init(Cipher.ENCRYPT_MODE, key);
		else {
			IvParameterSpec spec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, spec);
		}
		
		return cipher.doFinal(message);
	}
	
	@Override
	public void destroy() throws DestroyFailedException
	{
		key.destroy();
	}
}

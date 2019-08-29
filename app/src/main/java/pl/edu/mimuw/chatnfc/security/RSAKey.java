package pl.edu.mimuw.chatnfc.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.DestroyFailedException;

/**
 * Created by mateusz on 05.03.18.
 */

public class RSAKey extends Key {
	private KeyPair keyPair;
	private Cipher cipher;
	
	public RSAKey(KeyPair keyPair) throws NoSuchPaddingException, NoSuchAlgorithmException {
		this.keyPair = keyPair;
		this.cipher = Cipher.getInstance(SecurityTools.KEYSTORE_RSA_ALGORITHM);
	}
	
	public RSAKey(byte[] publicKey, byte[] privateKey)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		KeyFactory factory = KeyFactory.getInstance("RSA");
		keyPair = new KeyPair(factory.generatePublic(new X509EncodedKeySpec(publicKey)),
				factory.generatePrivate(new PKCS8EncodedKeySpec(privateKey)));
	}
	
	@Override
	public byte[] decrypt(byte[] iv, byte[] message) throws
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException
	
	{
		cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		
		return cipher.doFinal(message);
	}
	
	@Override
	public byte[] encrypt(byte[] iv, byte[] message) throws
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
		cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
		return cipher.doFinal(message);
	}
	
	public void destroy() throws DestroyFailedException
	{
		keyPair.getPrivate().destroy();
		//public key is not destroyable
	}
}

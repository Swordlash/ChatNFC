package pl.edu.mimuw.chatnfc.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.security.auth.DestroyFailedException;

/**
 * Created by mateusz on 05.03.18.
 */

public abstract class Key {
	public abstract byte[] decrypt(byte[] iv, byte[] message)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException;
	
	public abstract byte[] encrypt(byte[] iv, byte[] message)
			throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException;
	
	public abstract void destroy() throws DestroyFailedException;
}

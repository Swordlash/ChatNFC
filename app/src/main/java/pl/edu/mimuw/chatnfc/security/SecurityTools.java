package pl.edu.mimuw.chatnfc.security;

import android.support.annotation.NonNull;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by mateusz on 05.03.18.
 */

public class SecurityTools {
	public static String AES_ALGORITHM = "AES/CTR/PKCS7Padding";
	public static String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
	public static String KEYSTORE_RSA_ALGORITHM = "RSA/ECB/OAEPwithSHA-256andMGF1Padding";
	public static String HMAC_ALGORITHM = "HmacSHA256";
	public static String SHA_ALGORITHM = "SHA-256";
	public static String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
	public static String NIST_EC = "secp521r1";
	
	public static int AES_IV_LENGTH = 16;
	public static int AES_GCM_NONCE_LENGTH = 12;
	public static int MAC_LENGTH = 32;
	public static int PBKDF2_SALT_LENGTH = 32;
	public static int AES_GCM_TAG_BIT_LENGTH = 128;
	
	public static byte[] GCM_AAD_TAG = "UNIFIC_CRYPTO".getBytes(Charset.forName("UTF-8"));
	
	
	public static String KEYSTORE_PROVIDER = "AndroidKeyStore";
	
	private static SecureRandom prng = new SecureRandom();
	
	private SecurityTools() {
	}
	
	@NonNull
	public static RSAKey generateRSAKey(int length)
			throws NoSuchAlgorithmException, NoSuchPaddingException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(length);
		
		return new RSAKey(keyPairGen.generateKeyPair());
	}
	
	@NonNull
	public static AESKey generateAESKey(int length) throws
			NoSuchAlgorithmException, NoSuchPaddingException
	{
		return new AESKey(generateAESKeyBytes(length));
	}
	
	@NonNull
	public static AESGCMKey generateAESGCMKey(int length) throws
			NoSuchAlgorithmException, NoSuchPaddingException
	{
		return new AESGCMKey(generateAESKeyBytes(length), GCM_AAD_TAG);
	}
	
	public static byte[] generateAESKeyBytes(int length) throws NoSuchAlgorithmException
	{
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		keygen.init(length);
		
		return keygen.generateKey().getEncoded();
	}
	
	
	public static byte[] encryptWithAES(byte[] iv, byte[] keyBytes, byte[] message)
			throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		AESKey key = new AESKey(keyBytes);
		byte[] result = key.encrypt(iv, message);
		
		return result;
	}
	
	public static byte[] encryptWithAESGCM(byte[] nonce, byte[] keyBytes, byte[] message)
			throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		AESGCMKey key = new AESGCMKey(keyBytes, GCM_AAD_TAG);
		byte[] result = key.encrypt(nonce, message);
		
		return result;
	}
	
	public static byte[] decryptWithAES(byte[] iv, byte[] keyBytes, byte[] message)
			throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		AESKey key = new AESKey(keyBytes);
		byte[] result = key.decrypt(iv, message);
		
		return result;
	}
	
	public static byte[] decryptWithAESGCM(byte[] nonce, byte[] keyBytes, byte[] message)
			throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		AESGCMKey key = new AESGCMKey(keyBytes, GCM_AAD_TAG);
		byte[] result = key.decrypt(nonce, message);
		
		return result;
	}
	
	public static byte[] encryptWithRSA(byte[] publicKeyBytes, byte[] message)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException,
			NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException {
		Cipher cipher = Cipher.getInstance(KEYSTORE_RSA_ALGORITHM);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		PublicKey key = fact.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
		
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(message);//public key is not destroyable
	}
	
	public static byte[] decryptWithRSA(byte[] privateKeyBytes, byte[] message)
			throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException,
			NoSuchAlgorithmException, NoSuchPaddingException
	{
		Cipher cipher = Cipher.getInstance(KEYSTORE_RSA_ALGORITHM);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		PrivateKey key = fact.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
		
		cipher.init(Cipher.DECRYPT_MODE, key);
		
		byte[] result = cipher.doFinal(message);
		
		return result;
	}
	
	public static byte[] generateHMAC(byte[] macKeyBytes, byte[] bytes)
			throws NoSuchAlgorithmException, InvalidKeyException
	{
		SecretKeySpec spec = new SecretKeySpec(macKeyBytes, HMAC_ALGORITHM);
		Mac m = Mac.getInstance(HMAC_ALGORITHM);
		m.init(spec);
		
		byte[] hmac = m.doFinal(bytes);
		
		return hmac;
	}
	
	public static byte[] deriveKeyBytesFromPassword(char[] password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		PBEKeySpec ks = new PBEKeySpec(password, salt, 25000, 128);
		SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
		return skf.generateSecret(ks).getEncoded();
	}
	
	public static AESKey deriveKeyFromPassword(char[] password, byte[] salt)
			throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		return new AESKey(deriveKeyBytesFromPassword(password, salt));
	}
	
	public static byte[] getRandomAESIv()
	{
		return getRandomIv(AES_IV_LENGTH);
	}
	
	public static byte[] getRandomGCMNonce()
	{
		return getRandomIv(AES_GCM_NONCE_LENGTH);
	}
	
	private static byte[] getRandomIv(int length)
	{
		byte[] output = longToBytes(System.currentTimeMillis());
		int len = output.length;
		
		output = Arrays.copyOf(output, length);
		
		byte[] prngOutput = new byte[length - len];
		prng.nextBytes(prngOutput);
		
		System.arraycopy(prngOutput, 0, output, len, prngOutput.length);
		return output;
	}
	
	public static byte[] getRandomPBKDFSalt()
	{
		byte[] output = new byte[PBKDF2_SALT_LENGTH];
		prng.nextBytes(output);
		return output;
	}
	
	
	public static byte[] longToBytes(long l)
	{
		byte[] result = new byte[Long.SIZE / Byte.SIZE];
		for (int i = Long.SIZE / Byte.SIZE - 1; i >= 0; i--)
		{
			result[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
		return result;
	}
	
	public static long bytesToLong(byte[] b)
	{
		long result = 0;
		for (int i = 0; i < Long.SIZE / Byte.SIZE; i++)
		{
			result <<= 8;
			result |= (b[i] & 0xFF);
		}
		return result;
	}
	
	public static byte[] intToBytes(int l)
	{
		byte[] result = new byte[Integer.SIZE / Byte.SIZE];
		for (int i = Integer.SIZE / Byte.SIZE - 1; i >= 0; i--)
		{
			result[i] = (byte) (l & 0xFF);
			l >>= 8;
		}
		return result;
	}
	
	public static int bytesToInt(byte[] b)
	{
		int result = 0;
		for (int i = 0; i < Integer.SIZE / Byte.SIZE; i++)
		{
			result <<= 8;
			result |= (b[i] & 0xFF);
		}
		return result;
	}
}

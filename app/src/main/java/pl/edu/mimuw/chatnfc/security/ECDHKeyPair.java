package pl.edu.mimuw.chatnfc.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.DestroyFailedException;

public class ECDHKeyPair
{
	private static SecureRandom secureRandom = new SecureRandom();
	private static ECGenParameterSpec curveSpec = new ECGenParameterSpec(SecurityTools.NIST_EC);
	private static MessageDigest sha256;
	private static KeyPairGenerator keyPairGenerator;
	private static KeyAgreement ecdhKeyAgreement;
	private static KeyFactory ecKeyFactory;
	
	static
	{
		try
		{
			sha256 = MessageDigest.getInstance(SecurityTools.SHA_ALGORITHM);
			keyPairGenerator = KeyPairGenerator.getInstance("EC");
			ecdhKeyAgreement = KeyAgreement.getInstance("ECDH");
			ecKeyFactory = KeyFactory.getInstance("EC");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
	}
	
	private KeyPair ecKeyPair;
	
	
	private ECDHKeyPair()
	{
	}
	
	public static ECDHKeyPair generateNewInstance()
			throws InvalidAlgorithmParameterException
	{
		keyPairGenerator.initialize(curveSpec, secureRandom);
		
		ECDHKeyPair pair = new ECDHKeyPair();
		pair.ecKeyPair = keyPairGenerator.generateKeyPair();
		
		return pair;
	}
	
	public byte[] getPublicKey()
	{
		return ecKeyPair.getPublic().getEncoded();
	}
	
	public byte[] computeSharedSecretKeyBytes(byte[] otherPublicKey)
			throws InvalidKeySpecException, InvalidKeyException
	{
		PublicKey otherKey = ecKeyFactory.generatePublic(new X509EncodedKeySpec(otherPublicKey));
		
		ecdhKeyAgreement.init(ecKeyPair.getPrivate());
		ecdhKeyAgreement.doPhase(otherKey, true);
		
		return ecdhKeyAgreement.generateSecret();
	}
	
	public byte[] computeAESSharedSecretKeyBytes(byte[] otherPublicKey)
			throws InvalidKeySpecException, InvalidKeyException
	{
		return sha256.digest(computeSharedSecretKeyBytes(otherPublicKey));
	}
	
	public AESKey computeSharedSecretKey(byte[] otherPublicKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException
	{
		return new AESKey(computeAESSharedSecretKeyBytes(otherPublicKey));
	}
	
	public void destroy() throws DestroyFailedException
	{
		ecKeyPair.getPrivate().destroy();
	}
}

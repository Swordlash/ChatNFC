package pl.edu.mimuw.chatnfc;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Random;

import pl.edu.mimuw.chatnfc.security.AESGCMKey;
import pl.edu.mimuw.chatnfc.security.AESKey;
import pl.edu.mimuw.chatnfc.security.AuthenticatedDataPackage;
import pl.edu.mimuw.chatnfc.security.CryptographicException;
import pl.edu.mimuw.chatnfc.security.ECDHKeyPair;
import pl.edu.mimuw.chatnfc.security.PasswordEncryptedDataPackage;
import pl.edu.mimuw.chatnfc.security.RSAKey;
import pl.edu.mimuw.chatnfc.security.SecurityTools;
import pl.edu.mimuw.chatnfc.tools.TimeProvider;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
@PowerMockIgnore({"javax.crypto.*", "javax.security.*"})
public class ExampleUnitTest {
	
	@Before
	public void setup()
	{
		PowerMockito.mockStatic(Log.class);
	}
	
    @Test
    public void addition_isCorrect()
    {
        assertEquals(4, 2 + 2);
    }
	
	@Test
	public void aes_encryption_isCorrect() throws Exception {
    	
		AESKey key = SecurityTools.generateAESKey(128);
		
		String test = "Zażółć is a test";
		byte[] iv = SecurityTools.getRandomAESIv();
		
		assertArrayEquals(test.getBytes(), key.decrypt(iv, key.encrypt(iv, test.getBytes())));
		
		AESGCMKey appKey = SecurityTools.generateAESGCMKey(128);
		
		byte[] rv = SecurityTools.getRandomGCMNonce();
		byte[] passwordBytes = test.getBytes(Charset.forName("UTF-8"));
		
		
		byte[] encrypted = appKey.encrypt(rv, passwordBytes);
		System.out.println("Size: " + encrypted.length);
		
		rv = Arrays.copyOf(rv, SecurityTools.AES_GCM_NONCE_LENGTH + encrypted.length);
		System.arraycopy(encrypted, 0, rv, SecurityTools.AES_GCM_NONCE_LENGTH, encrypted.length);
		System.out.println("Size 2: " + rv.length);
		
		
		byte[] passwordEncBytes = rv;
		
		byte[] passwdBytes = appKey.decrypt(
				Arrays.copyOfRange(passwordEncBytes, 0, SecurityTools.AES_GCM_NONCE_LENGTH),
				Arrays.copyOfRange(passwordEncBytes, SecurityTools.AES_GCM_NONCE_LENGTH, passwordEncBytes.length));
		
		assertArrayEquals(passwordBytes, passwdBytes);
	}
	
	@Test
	public void rsa_encryption_isCorrect() throws Exception {
		RSAKey key = SecurityTools.generateRSAKey(1024);
		
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(1024);
		
		KeyPair key2 = keyPairGen.generateKeyPair();
		
		String test = "Zażółć is a test";
		
		assertArrayEquals(test.getBytes(), key.decrypt(null, key.encrypt(null, test.getBytes())));
		assertArrayEquals(test.getBytes(), SecurityTools
				.decryptWithRSA(key2.getPrivate().getEncoded(),
						SecurityTools
								.encryptWithRSA(key2.getPublic().getEncoded(), test.getBytes())));
	}
	
	@Test
	public void long_to_bytes_is_correct()
	{
		Random rnd = new Random();
		
		for (int i = 0; i < 1000; ++i)
		{
			long l = rnd.nextLong();
			byte[] bytes = SecurityTools.longToBytes(l);
			assertEquals(l, SecurityTools.bytesToLong(bytes));
		}
	}
	
	@Test
	public void message_packaging_is_correct() throws Exception
	{
		Random r = new Random();
		byte[] message = new byte[1000];
		r.nextBytes(message);
		
		byte[] iv = SecurityTools.getRandomAESIv();
		byte[] key = SecurityTools.generateAESKeyBytes(128);
		byte[] hmac = SecurityTools.generateAESKeyBytes(128);
		
		
		AuthenticatedDataPackage pack = new AuthenticatedDataPackage(iv, message, key, hmac);
		assertTrue(pack.authenticates(hmac));
	}
	
	@Test(expected = CryptographicException.class)
	public void message_packaging_is_correct_exception() throws Exception
	{
		Random r = new Random();
		byte[] message = new byte[1000];
		r.nextBytes(message);
		
		byte[] invalidIV = new byte[5];
		byte[] key = SecurityTools.generateAESKeyBytes(128);
		byte[] hmac = SecurityTools.generateAESKeyBytes(128);
		
		
		AuthenticatedDataPackage pack = new AuthenticatedDataPackage(invalidIV, message, key, hmac);
	}
	
	@Test
	public void password_encryption_is_correct() throws Exception
	{
		String text = "Gdzie jest ten JSON??";
		String password = "admin1";
		byte[] iv = SecurityTools.getRandomAESIv();
		byte[] salt = SecurityTools.getRandomPBKDFSalt();
		
		AESKey keyEnc = SecurityTools.deriveKeyFromPassword(password.toCharArray(), salt);
		AESKey keyDec = SecurityTools.deriveKeyFromPassword(password.toCharArray(), salt);
		
		byte[] encrypted = keyDec.decrypt(iv, text.getBytes());
		byte[] decrypted = keyEnc.decrypt(iv, encrypted);
		
		assertEquals(text, new String(decrypted));
	}
	
	@Test
	public void ecdh_is_correct() throws Exception
	{
		ECDHKeyPair pair1 = ECDHKeyPair.generateNewInstance();
		ECDHKeyPair pair2 = ECDHKeyPair.generateNewInstance();
		
		byte[] generatedAesKey = pair2.computeAESSharedSecretKeyBytes(pair1.getPublicKey());
		
		assertArrayEquals(generatedAesKey,
				pair1.computeAESSharedSecretKeyBytes(pair2.getPublicKey()));
		
		assertEquals(null, generatedAesKey.length, 32);
	}
	
	@Test
	public void print_timestamp()
	{
		Log.d("TIME:", "" + TimeProvider.getCurrentTimeMillisOrLocal(250));
	}
	
	@Test
	public void password_package_encrypting_is_correct() throws Exception
	{
		String text = "Gdzie jest ten JSON??";
		String password = "admin1";
		byte[] iv = SecurityTools.getRandomAESIv();
		byte[] salt = SecurityTools.getRandomPBKDFSalt();
		
		PasswordEncryptedDataPackage pack = new PasswordEncryptedDataPackage(iv, salt,
				text.getBytes(), password.toCharArray());
		
		assertArrayEquals(null, text.getBytes(),
				pack.getDataDecrypted(password.toCharArray()));
	}
}
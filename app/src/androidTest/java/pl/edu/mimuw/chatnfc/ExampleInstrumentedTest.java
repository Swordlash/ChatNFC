package pl.edu.mimuw.chatnfc;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.Random;

import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.config.UserProfileProvider;
import pl.edu.mimuw.chatnfc.security.AESKey;
import pl.edu.mimuw.chatnfc.security.AuthenticatedDataPackage;
import pl.edu.mimuw.chatnfc.security.SecurityTools;
import pl.edu.mimuw.chatnfc.tools.TimeProvider;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext()
    {
    
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("pl.edu.mimuw.chatnfc", appContext.getPackageName());
    }
    
    @Test
    public void aes_encryption_isCorrect() throws Exception {
	
	    AESKey key = SecurityTools.generateAESKey(256);
	    Random r = new Random();
	
	
	    byte[] message = new byte[1000];
	    r.nextBytes(message);
	    byte[] iv = SecurityTools.getRandomAESIv();
	
	    assertArrayEquals(message, key.decrypt(iv, key.encrypt(iv, message)));
    }
	
	
	@Test
	public void message_packaging_is_correct() throws Exception
	{
		Random r = new Random();
		byte[] message = new byte[1000];
		r.nextBytes(message);
		
		byte[] iv = SecurityTools.getRandomAESIv();
		byte[] key = SecurityTools.generateAESKeyBytes(256);
		byte[] hmac = SecurityTools.generateAESKeyBytes(256);
		long id = r.nextLong();
		
		
		AuthenticatedDataPackage pack = new AuthenticatedDataPackage(iv, message, key, hmac);
		assertTrue(pack.authenticates(hmac));
	}
	
	@Test
	public void get_server_time_is_correct() throws Exception
	{
		//long time = TimeProvider
		//		.getCurrentTimeMillisOrLocal(InstrumentationRegistry.getTargetContext());
		long time = System.currentTimeMillis();
		Log.d("Time", new Date(TimeProvider.getCurrentTimeMillis().get()).toString());
		Log.d("Time reply time", (System.currentTimeMillis() - time) + " ms");
	}
	
	@Test
	public void new_account_is_correct()
	{
		String uid = "test";
		String password = "password";
		String name = "Lolz";
		String surname = "Lolckovsky";
		
		UserProfile local = UserProfile.createNewProfile(uid, password, name, surname);
		UserProfileProvider.saveLocalUserProfile(local);
		
		UserProfileProvider.saveRemoteUserProfile(local);
	}
}

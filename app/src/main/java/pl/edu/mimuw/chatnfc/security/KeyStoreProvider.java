package pl.edu.mimuw.chatnfc.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import pl.edu.mimuw.chatnfc.ui.UnificApp;

public class KeyStoreProvider
{
	
	public static final String TAG = "KeyStoreProvider";
	public static final String APP_KEY_ALIAS = "UnificConfigKey";
	
	public static final String CONFIGURATION_KEYS = "Unific.configurationKeys";
	public static final String CONFIG_ENCRYPTION_KEY = "configEncryptionKey";
	public static final String CONFIG_AUTH_KEY = "configAuthKey";
	
	public static final String PROFILE_KEYS = "Unific.profileKeys";
	
	private static SecretKey APPLICATION_KEY;
	
	static
	{
		try
		{
			byte[] key = SecurityTools
					.deriveKeyBytesFromPassword("TEST_UNIFIC_PASSWORD".toCharArray(),
							new byte[SecurityTools.PBKDF2_SALT_LENGTH]);
			APPLICATION_KEY = new SecretKeySpec(key, "AES");
		}
		catch (Exception ex)
		{
		}
	}
	
	public static void initializeKeyStore()
	{
		Context context = UnificApp.getUnificAppContext();
		try
		{
			//createKeystoreKeys(context, APP_KEY_ALIAS, false); //TODO: change to true
			createConfigurationKeys();
		}
		catch (Exception ex)
		{
			Log.e("KeyStore", ex.getMessage());
			//throw new RuntimeException(ex);
			showKeyStoreAlert();
		}
	}
	
	public static void createConfigurationKeys()
	{
		createConfigurationKey(CONFIG_ENCRYPTION_KEY);
		createConfigurationKey(CONFIG_AUTH_KEY);
	}
	
	private static void createKey(String keyType, String alias)
	{
		return;
	}
	
	private static void createConfigurationKey(String alias)
	{
		createKey(CONFIGURATION_KEYS, alias);
	}
	
	private static byte[] getKey(String keyType, String alias)
	{
		return null;
	}
	
	public static byte[] getConfigurationKey(String alias)
	{
		return getKey(CONFIGURATION_KEYS, alias);
	}
	
	public static byte[] getConfigEncryptionKey()
	{
		return getConfigurationKey(CONFIG_ENCRYPTION_KEY);
	}
	
	public static byte[] getConfigAuthenticationKey()
	{
		return getConfigurationKey(CONFIG_AUTH_KEY);
	}
	
	public static void encryptAndSaveUserPassword(String userUID, String password)
	{
		byte[] iv = SecurityTools.getRandomAESIv();
		byte[] passwordBytes;
		
		try
		{
			passwordBytes = password.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			passwordBytes = password.getBytes();
		}
		
		try
		{
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			IvParameterSpec ivspec = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, APPLICATION_KEY, ivspec);
			
			byte[] encrypted = cipher.doFinal(passwordBytes);
			
			passwordBytes = Arrays.copyOf(iv, SecurityTools.AES_IV_LENGTH + encrypted.length);
			System.arraycopy(encrypted, 0, passwordBytes,
					SecurityTools.AES_IV_LENGTH, encrypted.length);
		}
		catch (Exception ex)
		{
			Log.e("EncryptionError", ex.getMessage());
		}
		
		
		SharedPreferences.Editor edit = UnificApp.getUnificPrefs(PROFILE_KEYS).edit();
		
		edit.putString(userUID, Base64.encodeToString(passwordBytes, Base64.DEFAULT));
		edit.commit();
	}
	
	public static char[] getSavedUserPassword(String userUID)
	{
		String encrypted = UnificApp.getUnificPrefs(PROFILE_KEYS)
				.getString(userUID, null);
		
		byte[] passwordBytes = Base64.decode(encrypted, Base64.DEFAULT);
		
		try
		{
			byte[] iv = Arrays.copyOfRange(passwordBytes, 0, SecurityTools.AES_IV_LENGTH);
			byte[] enc = Arrays
					.copyOfRange(passwordBytes, SecurityTools.AES_IV_LENGTH, passwordBytes.length);
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			IvParameterSpec spec = new IvParameterSpec(iv);
			
			cipher.init(Cipher.DECRYPT_MODE, APPLICATION_KEY, spec);
			
			passwordBytes = cipher.doFinal(enc);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
		try
		{
			return new String(passwordBytes, "UTF-8").toCharArray();
		}
		catch (UnsupportedEncodingException e)
		{
			return new String(passwordBytes).toCharArray();
		}
	}
	
	private static void createKeystoreKeys(Context context, String alias, boolean requireAuth)
			throws NoSuchProviderException,
			NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			CertificateException, UnrecoverableEntryException, KeyStoreException, IOException
	{
		if (existsKey(alias))
		{
			Log.d("createKeys", "Key exists");
			APPLICATION_KEY = getKey(alias);
			return;
		}
		
		final KeyGenerator keyGenerator = KeyGenerator
				.getInstance(KeyProperties.KEY_ALGORITHM_AES, SecurityTools.KEYSTORE_PROVIDER);
		
		final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias,
				KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
				.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
				.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
				.setKeySize(128)
				.setRandomizedEncryptionRequired(false)
				.setUserAuthenticationValidityDurationSeconds(3600)
				.setUserAuthenticationRequired(requireAuth)
				.build();
		
		keyGenerator.init(keyGenParameterSpec);
		
		APPLICATION_KEY = keyGenerator.generateKey();
	}
	
	private static boolean existsKey(String alias)
	{
		try
		{
			KeyStore keyStore = KeyStore.getInstance(SecurityTools.KEYSTORE_PROVIDER);
			keyStore.load(null);
			return keyStore.containsAlias(alias);
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
			return false;
		}
	}
	
	private static SecretKey getKey(String alias)
			throws NoSuchAlgorithmException, IOException, CertificateException, KeyStoreException,
			UnrecoverableEntryException
	{
		KeyStore keyStore = KeyStore.getInstance(SecurityTools.KEYSTORE_PROVIDER);
		keyStore.load(null);
		return ((KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null)).getSecretKey();
	}
	
	private static void showKeyStoreAlert()
	{
		new AlertDialog.Builder(UnificApp
				.getCurrentActivity(), android.R.style.Theme_Material_Dialog_Alert)
				.setTitle("KeyStore Initialization Error")
				.setMessage("Unific was unable to properly initialize keystore. Maybe you don't use secure lock or use older version. " +
						"Try to set secure pattern for your device (PIN, draw pattern etc.), or Unific will not work properly\n" +
						"If you do not want to do this, consider using non-secure messanging app (e.g. Facebook Messenger).\n\n" +
						"Application will now close.")
				.setPositiveButton("OK", (dialog, which) ->
				{
					System.exit(1);
				})
				.setCancelable(false)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
	}
}
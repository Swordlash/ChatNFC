package pl.edu.mimuw.chatnfc.config;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;

import pl.edu.mimuw.chatnfc.security.KeyStoreProvider;
import pl.edu.mimuw.chatnfc.security.PasswordEncryptedDataPackage;
import pl.edu.mimuw.chatnfc.security.SecurityTools;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;
import pl.edu.mimuw.chatnfc.tools.ObjectWrapper;
import pl.edu.mimuw.chatnfc.ui.UnificApp;

public class UserProfileProvider
{
	public static String USER_PROFILES = "Unific.userProfiles";
	
	private UserProfileProvider()
	{
	}
	
	private static UserProfile profileFromEncryptedString(String userProfileString, String userUID)
	{
		Log.d("UserStr", userProfileString != null ? userProfileString : "NULL");
		
		if (userProfileString == null)
			return null;
		
		try
		{
			byte[] userProfileData = Base64.decode(userProfileString, Base64.DEFAULT);
			PasswordEncryptedDataPackage pack = new PasswordEncryptedDataPackage(userProfileData);
			
			char[] userPassword = KeyStoreProvider.getSavedUserPassword(userUID);
			
			return new UserProfile(pack.getDataDecrypted(userPassword));
		}
		catch (Exception ex)
		{
			//Log.e("profileFromEncString", ex.getMessage());
			throw new RuntimeException(ex);
			//return null;
		}
	}
	
	private static String profileToEncryptedString(UserProfile profile)
	{
		String userUID = profile.getUserID();
		
		byte[] userProfileData = profile.toByteArray();
		try
		{
			PasswordEncryptedDataPackage pack =
					new PasswordEncryptedDataPackage(SecurityTools.getRandomGCMNonce(),
							SecurityTools.getRandomPBKDFSalt(),
							userProfileData,
							KeyStoreProvider.getSavedUserPassword(userUID));
			return Base64.encodeToString(pack.toByteArray(), Base64.DEFAULT);
		}
		catch (Exception ex)
		{
			Log.e("profileToString", ex.getMessage());
			return null;
		}
	}
	
	public static void saveLocalUserProfile(UserProfile profile)
	{
		String uid = profile.getUserID();
		
		SharedPreferences.Editor edit = UnificApp.getUnificPrefs(USER_PROFILES).edit();
		edit.putString(uid, profileToEncryptedString(profile));
		edit.commit();
		Log.d("Commited to ", profile.getUserID());
	}
	
	public static void saveLocalUserProfileAsync(UserProfile profile)
	{
		new Thread(() -> saveRemoteUserProfile(profile)).start();
	}
	
	public static void saveRemoteUserProfile(UserProfile profile)
	{
		String uid = profile.getUserID();
		FirebaseTools.getInstance()
				.setValueInDB("Users/" + uid + "/profile", profileToEncryptedString(profile));
	}
	
	public static void saveRemoteUserProfileAsync(UserProfile profile)
	{
		new Thread(() -> saveRemoteUserProfile(profile)).start();
	}
	
	public static UserProfile getLocalUserProfile(String userUID)
	{
		String userProfileString = UnificApp.getUnificPrefs(USER_PROFILES).getString(userUID, null);
		return profileFromEncryptedString(userProfileString, userUID);
	}
	
	public static ObjectWrapper<UserProfile> getRemoteUserProfile(String uid, boolean overwriteLocal)
	{
		final ObjectWrapper<UserProfile> wrapper = new ObjectWrapper<>();
		//I should use CompletableFuture, but it requires API 24
		
		ProgressDialog dialog = ProgressDialog.show(UnificApp.getCurrentActivity(), "Please wait",
				"Acquiring your profile from remote database...");
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		FirebaseTools.getInstance()
				.acquireDataFromDB("Users/" + uid + "/profile", new ObjectAcquireListener()
				{
					@Override
					public void onObjectAcquired(Object obj)
					{
						String profileString = obj.toString();
						
						UserProfile fetched = profileFromEncryptedString(profileString, uid);
						
						wrapper.setObject(fetched);
						if (overwriteLocal)
							UserProfile.setLocalProfile(wrapper.getObject());
						
						saveLocalUserProfile(fetched);
						
						dialog.dismiss();
					}
					
					@Override
					public void onError(DatabaseError err)
					{
						dialog.dismiss();
						Toast.makeText(UnificApp
								.getCurrentActivity(), "Unknown exception occured!", Toast.LENGTH_SHORT)
								.show();
					}
				});
		
		return wrapper;
	}
	
	public static void getRemoteUserProfileAndStartActivity(String uid, Intent intent)
	{
		FirebaseTools.getInstance()
				.acquireDataFromDB("Users/" + uid + "/profile", new ObjectAcquireListener()
				{
					@Override
					public void onObjectAcquired(Object obj)
					{
						if (obj == null)
						{
							onError(null);
							return;
						}
						
						String profileString = obj.toString();
						
						UserProfile fetched = profileFromEncryptedString(profileString, uid);
						UserProfile.setLocalProfile(fetched);
						
						saveLocalUserProfile(fetched);
						
						UnificApp.getCurrentActivity().startActivity(intent);
					}
					
					@Override
					public void onError(DatabaseError err)
					{
						Toast.makeText(UnificApp
								.getCurrentActivity(), "Unknown exception occured!", Toast.LENGTH_SHORT)
								.show();
					}
				});
	}
}

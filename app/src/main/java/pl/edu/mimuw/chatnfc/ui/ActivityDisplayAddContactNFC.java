package pl.edu.mimuw.chatnfc.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.Contact;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.config.UserProfileProvider;
import pl.edu.mimuw.chatnfc.messanging.ConfigMessage;
import pl.edu.mimuw.chatnfc.messanging.Messanging;
import pl.edu.mimuw.chatnfc.security.ECDHKeyPair;
import pl.edu.mimuw.chatnfc.tools.ObjectIO;
import pl.edu.mimuw.chatnfc.tools.TimeProvider;

public class ActivityDisplayAddContactNFC extends AppCompatActivity
{
    private TextView mTextView;
    private Button button;
	private Contact c;
	private String id;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_add_contact);
		
		setSupportActionBar(findViewById(R.id.nfc_display_toolbar));
		getSupportActionBar().setTitle("Contact Added");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		mTextView = findViewById(R.id.text_view);
		button = findViewById(R.id.button_ok);
		button.setOnClickListener(v ->
		{
			addContact();
			UnificApp.getBackToMenu();
			finish();
		});
    }
	
	void addContact()
	{
		ProgressDialog pd = new ProgressDialog(this);
		
		long time = TimeProvider.getCurrentTimeMillisOrLocal(150);
		
		UserProfile local = UserProfile.getLocalProfile();
		local.addContact(id, c);
		
		UserProfileProvider.saveLocalUserProfileAsync(local);
		UserProfileProvider.saveRemoteUserProfileAsync(local);
		
		new Thread(() ->
		{
			UserProfileProvider.saveLocalUserProfile(local);
			UserProfileProvider.saveRemoteUserProfile(local);
			
			try
			{
				if (local.getAvatar() != null)
				{
					Map<String, String> mp = new HashMap<>();
					mp.put("IMAGE", ObjectIO.bitmapToString(local.getAvatar(), 30));
					ConfigMessage profileImage = new ConfigMessage(local.getUserID(),
							Long.toString(time),
							ConfigMessage.CONFIG_FLAG_CHANGE_AVATAR,
							mp);
					
					Messanging.sendMessage(local.getUserID(), c.getUserID(), Long.toString(time),
							profileImage);
				}
			}
			catch (Exception ex)
			{
				Log.e("AddContact", "Couldn't send avatar");
				throw new RuntimeException(ex);
			}
		}).start();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
	
	    if (UserProfile.getLocalProfile().getPairingKey() == null)
	    {
		    Toast.makeText(UnificApp
				    .getUnificAppContext(), "KeyPair not initialized!", Toast.LENGTH_LONG)
				    .show();
		    UnificApp.getBackToMenu();
		    finish();
	    }
        
        Intent intent = getIntent();
        String hash = "ok?";
	
	    if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
	    {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
		
		    byte[] data = message.getRecords()[0].getPayload();
		
		    ByteArrayInputStream in = new ByteArrayInputStream(data);
		    DataInputStream din = new DataInputStream(in);
		
		    UserProfile local = UserProfile.getLocalProfile();
		
		    try
		    {
			    id = din.readUTF();
			    String name = din.readUTF();
			    String surname = din.readUTF();
			    String status = din.readUTF();
			
			    String newContactPublicKey = din.readUTF();
			    byte[] publicKeyBytes = Base64.decode(newContactPublicKey, Base64.DEFAULT);
			
			    String newContactPublicAuthKey = din.readUTF();
			    byte[] publicAuthKeyBytes = Base64.decode(newContactPublicAuthKey, Base64.DEFAULT);
			
			    ECDHKeyPair pair = local.getPairingKey();
			    byte[] key = pair.computeAESSharedSecretKeyBytes(publicKeyBytes);
			
			    ECDHKeyPair pairAuth = local.getPairingAuthKey();
			    byte[] keyAuth = pairAuth.computeAESSharedSecretKeyBytes(publicAuthKeyBytes);
			
			    c = new Contact(id, name, surname, status, null, key, keyAuth);
		    }
		    catch (Exception ex)
		    {
			    Log.e("AddContact", ex.getLocalizedMessage());
			    Toast.makeText(this, "Unknown error occured", Toast.LENGTH_LONG).show();
			    UnificApp.getBackToMenu();
			    finish();
		    }
            
            mTextView.setText(hash);
        } else
            mTextView.setText("Waiting for NDEF Message");

    }
}

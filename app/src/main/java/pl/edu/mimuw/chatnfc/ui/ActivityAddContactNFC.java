package pl.edu.mimuw.chatnfc.ui;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.security.ECDHKeyPair;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;


public class ActivityAddContactNFC extends AppCompatActivity implements NfcAdapter.CreateNdefMessageCallback
{
    private TextView textView;
	private android.support.v7.widget.Toolbar toolbar;


	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        toolbar = findViewById(R.id.nfc_toolbar);
		String UID = FirebaseTools.getInstance().getCurrentUser().getUid();
		FirebaseTools.getInstance().acquireDataFromDB("Users/" + UID + "/color_primary", new ObjectAcquireListener() {
			@Override
			public void onObjectAcquired(Object obj) {
				if (obj != null) {
					toolbar.setBackgroundColor(Integer.parseInt(obj.toString()));
				}
			}

			@Override
			public void onError(DatabaseError err) {

			}
		});
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add new contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
	
	    generateNewKey();
	
	    textView = findViewById(R.id.edit_text_field);
        
        textView.setText("1) Bring devices close together\n\n2) Grant permissions to send and receive data on both devices\n\n" +
                "3) If authentication hashes are the same, click 'OK'\n\n4) Chat!");
	
	    NfcAdapter mAdapter = NfcAdapter.getDefaultAdapter(this);
	    if (mAdapter == null)
	    {
		    Toast.makeText(this, "Sorry this device does not have NFC.", Toast.LENGTH_LONG).show();
            return;
        }
	
	    if (!mAdapter.isEnabled())
	    {
            Toast.makeText(this, "Please enable NFC via Settings.", Toast.LENGTH_LONG).show();
        }

        mAdapter.setNdefPushMessageCallback(this, this);
    }
	
	void generateNewKey()
	{
		try
		{
			UserProfile.getLocalProfile().generateNewPairingKey();
		}
		catch (InvalidAlgorithmParameterException e)
		{
			Toast.makeText(UnificApp.getUnificAppContext(), "Your telephone does not support ECDH!",
					Toast.LENGTH_LONG).show();
			UnificApp.getBackToMenu();
			finish();
		}
	}
	
	String getEncodedKey()
	{
		ECDHKeyPair pair = UserProfile.getLocalProfile().getPairingKey();
		return Base64.encodeToString(pair.getPublicKey(), Base64.DEFAULT);
	}
	
	String getEncodedAuthKey()
	{
		ECDHKeyPair pair = UserProfile.getLocalProfile().getPairingAuthKey();
		return Base64.encodeToString(pair.getPublicKey(), Base64.DEFAULT);
	}


    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent)
    {
	    UserProfile local = UserProfile.getLocalProfile();
	
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    DataOutputStream dout = new DataOutputStream(out);
	
	    try
	    {
		    dout.writeUTF(local.getUserID());
		    dout.writeUTF(local.getName());
		    dout.writeUTF(local.getSurname());
		    dout.writeUTF(local.getStatus());
		    dout.writeUTF(getEncodedKey());
		    dout.writeUTF(getEncodedAuthKey());
		    dout.flush();
	    }
	    catch (IOException ex)
	    {
	    } //ByteArrayOutputStream does not throw anything
	
	    NdefRecord ndefRecord = NdefRecord.createMime("text/plain", out.toByteArray());

        NdefMessage ndefMessage = new NdefMessage(ndefRecord);
        return ndefMessage;
    }
}

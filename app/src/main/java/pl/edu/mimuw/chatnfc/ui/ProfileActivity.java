package pl.edu.mimuw.chatnfc.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseError;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.config.UserProfileProvider;
import pl.edu.mimuw.chatnfc.messanging.Messanging;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;

public class ProfileActivity extends AppCompatActivity {
    Button saveButton;
    TextInputLayout statusTextInput;
    private android.support.v7.widget.Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.profile_toolbar);

        if (FirebaseTools.getInstance().getCurrentUser() != null) {
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
        }


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        statusTextInput = findViewById(R.id.profile_status_text);

        String userUID = FirebaseTools.getInstance().getCurrentUser().getUid();
	    UserProfile local = UserProfile.getProfile(userUID);
	
	    statusTextInput.getEditText().setText(UserProfile.getLocalProfile().getUserID());

        saveButton = findViewById(R.id.profile_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
	            //String newFriend = statusTextInput.getEditText().getText().toString();
	
	            //local.addContact(newFriend, new Contact(newFriend, "", "", newFriend, null, null, null));
	            UserProfile.getLocalProfile()
			            .setStatus(statusTextInput.getEditText().getText().toString());
	
	            UserProfileProvider.saveLocalUserProfile(local);
	            UserProfileProvider.saveRemoteUserProfileAsync(local);
	            Messanging.notifyChangeStatus();

                Intent intent = new Intent(ProfileActivity.this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("menu_fragment", 2);
                startActivity(intent);
                finish();
            }
        });
    }
}

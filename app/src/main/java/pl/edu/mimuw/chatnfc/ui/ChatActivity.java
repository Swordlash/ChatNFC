package pl.edu.mimuw.chatnfc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.Contact;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.messanging.ImageMessage;
import pl.edu.mimuw.chatnfc.messanging.Message;
import pl.edu.mimuw.chatnfc.messanging.Messanging;
import pl.edu.mimuw.chatnfc.messanging.TextMessage;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;
import pl.edu.mimuw.chatnfc.tools.OnlineProvider;
import pl.edu.mimuw.chatnfc.tools.TimeAgo;
import pl.edu.mimuw.chatnfc.tools.TimeProvider;

public class ChatActivity extends AppCompatActivity {

    private final int RESULT_LOAD_IMAGE = 1;
    private StorageReference imageStorage;
	
	private Contact friendContact;
    private android.support.v7.widget.Toolbar chatToolbar;

    private TextView contactNameText;
    private TextView contactOnlineStatusText;
    private CircleImageView contactImage;
    private ImageView contactOnlineIcon;

    private TextView messageTextView;
    private ImageButton sendButton;
    private ImageButton sendImageButton;

    private RecyclerView messagesRecyclerView;
    private final List<Message<?>> pastMessages = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnlineProvider.setOnline();
        setContentView(R.layout.activity_chat);
	
	    friendContact = UserProfile.getLocalProfile()
			    .getContactByUID(getIntent().getStringExtra("identificator"));

        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vi = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(vi);
        String UID = FirebaseTools.getInstance().getCurrentUser().getUid();
        FirebaseTools.getInstance().acquireDataFromDB("Users/" + UID + "/color_primary", new ObjectAcquireListener() {
            @Override
            public void onObjectAcquired(Object obj) {
                if (obj != null) {
                    chatToolbar.setBackgroundColor(Integer.parseInt(obj.toString()));
                    vi.setBackgroundColor(Integer.parseInt(obj.toString()));
                }
            }

            @Override
            public void onError(DatabaseError err) {

            }
        });

        contactNameText = findViewById(R.id.chat_bar_name);
        contactOnlineStatusText = findViewById(R.id.chat_bar_status);
        contactImage = findViewById(R.id.chat_bar_image);
        contactOnlineIcon = findViewById(R.id.chat_bar_online);
	
	    contactNameText.setText(friendContact.getNameSurname());
	
	    FirebaseTools.getInstance().installDBDataChangeListener("Users/" + friendContact
			    .getUserID() + "/online", new ObjectAcquireListener()
	    {
            @Override
            public void onObjectAcquired(Object obj) {
                if (obj.equals(true)) {
                    contactOnlineStatusText.setText("Online");
                } else {
	                FirebaseTools.getInstance().acquireDataFromDB("Users/" + friendContact
			                .getUserID() + "/last_seen", new ObjectAcquireListener()
	                {
                        @Override
                        public void onObjectAcquired(Object obj) {
                            contactOnlineStatusText.setText(TimeAgo.getTimeAgo((long)obj));
                        }

                        @Override
                        public void onError(DatabaseError err) {

                        }
                    });
                }
            }

            @Override
            public void onError(DatabaseError err) {

            }
        });
	
	    if (friendContact.getAvatar() != null)
		    contactImage.setImageBitmap(friendContact.getAvatar());
	    else contactImage.setImageResource(R.drawable.avatar);
	
	
	    FirebaseTools.getInstance().installDBDataChangeListener("Users/" + friendContact
			    .getUserID() + "/online/", new ObjectAcquireListener()
	    {
            @Override
            public void onObjectAcquired(Object obj) {
                if (obj.equals(true)) {
                    contactOnlineIcon.setVisibility(View.VISIBLE);
                } else {
                    contactOnlineIcon.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onError(DatabaseError err) {

            }
        });

        messageAdapter = new MessageAdapter(pastMessages);
        messagesRecyclerView = findViewById(R.id.chat_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);

        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);
        messagesRecyclerView.setAdapter(messageAdapter);

        messagesRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                // Call smooth scroll
                messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });

        loadMessages();

        sendButton = findViewById(R.id.chat_send_button);
        sendImageButton = findViewById(R.id.chat_send_image);
        messageTextView = findViewById(R.id.chat_message);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
	            sendTextMessage();
                messageTextView.setText("");
            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
	
	    if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK)
		    sendImageMessage(data.getData());
    }


    private void loadMessages() {
        String identificator = FirebaseTools.getInstance().getCurrentUser().getUid();


        FirebaseTools.getInstance()
		        .getReference("Users/" + identificator + "/messages/" + friendContact
				        .getUserID() + "/")
                .orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
	            Message<?> msg = Messanging
			            .receiveMessage(friendContact.getUserID(), dataSnapshot, true);
	            if (msg == null)
		            return;
                
                messageAdapter.getMessageList().add(msg);
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        String uid = FirebaseTools.getInstance().getCurrentUser().getUid();
	    FirebaseTools.getInstance().setValueInDB("Users/" + uid + "/contacts/" + friendContact
			    .getUserID() + "/seen", true);
    }
	
	private void sendTextMessage()
	{
		String message = messageTextView.getText().toString();
		
		if (!message.isEmpty()) {
			
			String identificator = FirebaseTools.getInstance().getCurrentUser().getUid();
			String currentTimestamp = Long.toString(TimeProvider.getCurrentTimeMillisOrLocal(250));
			
			try
			{
				Messanging.sendMessage(identificator, friendContact.getUserID(), currentTimestamp,
						new TextMessage(identificator, currentTimestamp, message));
			}
			catch (Exception e)
			{
				Log.e("ChatActivity", "Error sending message!\n" + e.getMessage());
				Toast.makeText(getApplicationContext(), "Error sending message!", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}
	
	private void sendImageMessage(Uri imageUri)
	{
		new Thread(() ->
		{
			String sender = FirebaseTools.getInstance().getCurrentUser().getUid();
			String recipient = friendContact.getUserID();
			String currentTimestamp = Long.toString(TimeProvider.getCurrentTimeMillisOrLocal(250));
			Bitmap bmp;
			try
			{
				bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
			}
			catch (IOException e)
			{
				Log.e("ChatActivity", "Cannot send image message:\n" + e.getMessage());
				return;
			}
			
			try
			{
				Messanging.sendMessage(sender, recipient, currentTimestamp,
						new ImageMessage(sender, currentTimestamp, bmp));
			}
			catch (Exception e)
			{
				Log.e("ChatActivity", "Error sending message!\n" + e.getMessage());
				Toast.makeText(UnificApp
						.getCurrentActivity(), "Error sending image message!", Toast.LENGTH_SHORT)
						.show();
			}
		}).start();
	}
}

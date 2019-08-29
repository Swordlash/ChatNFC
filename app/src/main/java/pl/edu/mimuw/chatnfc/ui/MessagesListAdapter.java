package pl.edu.mimuw.chatnfc.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.Contact;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.messanging.Message;
import pl.edu.mimuw.chatnfc.messanging.Messanging;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MessagesListAdapter extends BaseAdapter
{
	private UserProfile profile;
	private List<Contact> contacts;

    private static LayoutInflater inflater = null;
	
	public MessagesListAdapter()
	{
		inflater = (LayoutInflater) UnificApp.getCurrentActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void rebuildMessagesList(UserProfile profile)
	{
		this.profile = profile;
		
		contacts = new ArrayList<>(profile.getContacts().size());
		for (Map.Entry<String, Contact> entry : profile.getContacts().entrySet())
		{
			//if (!entry.getValue().getLastMessage().equals(""))
				contacts.add(entry.getValue());
		}
	}
	
	public void rebuildMessagesList()
	{
		profile = UserProfile.getLocalProfile();
		
		contacts = new ArrayList<>(profile.getContacts().size());
		for (Map.Entry<String, Contact> entry : profile.getContacts().entrySet())
		{
			//if (!entry.getValue().getLastMessage().equals(""))
				contacts.add(entry.getValue());
		}
    }

    @Override
    public int getCount() {
	    return contacts.size();
    }

    @Override
    public String getItem(int position) {
	    return contacts.get(position).getNameSurname();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View vi = convertView;
        if (vi == null) vi = inflater.inflate(R.layout.message_entity, null);

        TextView identifier = vi.findViewById(R.id.msg_identifier);
        TextView lastMsg = vi.findViewById(R.id.msg_stat);
        TextView msgTime = vi.findViewById(R.id.msg_time);
        CircleImageView image = vi.findViewById(R.id.msg_picture);
        ImageView online = vi.findViewById(R.id.msg_online_icon);

        String user = FirebaseTools.getInstance().getCurrentUser().getUid();
	
	    Contact friend = contacts.get(position);
	    Context activity = UnificApp.getCurrentActivity();
	
	    if (friend.getAvatar() != null)
		    image.setImageBitmap(friend.getAvatar());
	
	    else image.setImageResource(R.drawable.avatar);
	
	    identifier.setText(friend.getNameSurname());
	
	    FirebaseTools.getInstance()
			    .getReference("Users/" + user + "/messages/" + friend.getUserID())
			    .orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener()
	    {
		    @Override
		    public void onDataChange(DataSnapshot dataSnapshot)
		    {
			    if (dataSnapshot.getValue() == null)
				    return;
			
			    Message<?> msg = Messanging.receiveMessage(friend.getUserID(), dataSnapshot, true);
			    String shortmsg = "";
			
			    if (msg == null)
				    return;
			    
			    if (msg.getMessageType() != Message.Type.CONFIG_MESSAGE)
				    shortmsg = msg.getMessageType() == Message.Type.TEXT_MESSAGE ? msg
						    .getMessageContent().toString()
						    : "Image";
			
			    if (shortmsg.length() > 60)
				    shortmsg = shortmsg.substring(0, 60) + "...";
			
			    lastMsg.setText(shortmsg);
			    msgTime.setText(SimpleDateFormat.getDateTimeInstance()
					    .format(new Date(Long.parseLong(msg.getTimestamp()))));
		    }
		
		    @Override
		    public void onCancelled(DatabaseError databaseError)
		    {
		    }
	    });
	    
	    
	    FirebaseTools.getInstance()
			    .installDBDataChangeListener("Users/" + user + "/contacts/" + friend.getUserID()
					    + "/seen", new ObjectAcquireListener()
			    {
            @Override
            public void onObjectAcquired(Object obj) {
                if (obj != null) {
                    if (obj.equals(false)) {
                        lastMsg.setTypeface(null, Typeface.BOLD_ITALIC);
                        lastMsg.setTextSize(16);
	
	                    Intent chatIntent = new Intent(activity, ChatActivity.class);
	                    chatIntent.putExtra("identificator", friend.getUserID());
                        
// use                  System.currentTimeMillis() to have a unique ID for the pending intent
	                    PendingIntent pIntent = PendingIntent.getActivity(activity, (int) System
			                    .currentTimeMillis(), chatIntent, 0);

                        Bitmap b = null;
	                    b = BitmapFactory.decodeResource(activity.getResources(),
                                R.drawable.avatar);
	
	                    Notification n = new Notification.Builder(activity)
			                    .setContentTitle(friend.getNameSurname())
			                    .setContentText(lastMsg.getText())
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setLargeIcon(b)
                                .setContentIntent(pIntent)
                                .setAutoCancel(true).build();


                        NotificationManager notificationManager =
		                        (NotificationManager) activity
				                        .getSystemService(NOTIFICATION_SERVICE);

                        notificationManager.notify(0, n);

                    } else {
                        lastMsg.setTypeface(null, Typeface.NORMAL);
                        lastMsg.setTextSize(14);
                    }
                }
            }

            @Override
            public void onError(DatabaseError err)
            {
            }
        });
	
	    vi.setOnClickListener(new View.OnClickListener()
	    {
            @Override
            public void onClick(View v) {
	            Intent chatIntent = new Intent(activity, ChatActivity.class);
	            chatIntent.putExtra("identificator", friend.getUserID());
	            activity.startActivity(chatIntent);
            }
        });
	
	    FirebaseTools.getInstance().installDBDataChangeListener("Users/" + friend
			    .getUserID() + "/online/", new ObjectAcquireListener()
	    {
            @Override
            public void onObjectAcquired(Object obj) {
                if (obj != null) {
                    if (obj.equals(true)) {
                        online.setVisibility(View.VISIBLE);
                    } else {
                        online.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onError(DatabaseError err) {

            }
        });
	
	    return vi;
    }
}

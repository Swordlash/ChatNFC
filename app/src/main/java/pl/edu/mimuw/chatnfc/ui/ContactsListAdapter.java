package pl.edu.mimuw.chatnfc.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.Contact;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;

public class ContactsListAdapter extends BaseAdapter
{
	private UserProfile profile;
	private List<Contact> contacts;

    private static LayoutInflater inflater = null;
	
	public ContactsListAdapter()
	{
		inflater = (LayoutInflater) UnificApp.getCurrentActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		rebuildContactsList();
	}
	
	public void rebuildContactsList(UserProfile profile)
	{
		this.profile = profile;
		
		contacts = new ArrayList<>(profile.getContacts().size());
		for (Map.Entry<String, Contact> entry : profile.getContacts().entrySet())
		{
			contacts.add(entry.getValue());
		}
	}
	
	public void rebuildContactsList()
	{
		profile = UserProfile.getLocalProfile();
		
		contacts = new ArrayList<>(profile.getContacts().size());
		for (Map.Entry<String, Contact> entry : profile.getContacts().entrySet())
		{
			contacts.add(entry.getValue());
		}
    }

    @Override
    public int getCount()
    {
	    return profile.getContacts().size();
    }

    @Override
    public String getItem(int position)
    {
	    return contacts.get(position).getNameSurname();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
	    if (vi == null)
		    vi = inflater.inflate(R.layout.contact_entity, null);

        TextView identifier = vi.findViewById(R.id.identifier);
        TextView status = vi.findViewById(R.id.stat);
        CircleImageView image = vi.findViewById(R.id.picture);
        ImageView online = vi.findViewById(R.id.online_icon);
	
	    Contact contact = contacts.get(position);
	
	    vi.setOnClickListener(new View.OnClickListener()
	    {
            @Override
            public void onClick(View v) {
	            Intent chatIntent = new Intent(UnificApp.getCurrentActivity(), ChatActivity.class);
	            chatIntent.putExtra("identificator", contact.getUserID());
	            UnificApp.getCurrentActivity().startActivity(chatIntent);
            }
        });
	
	    FirebaseTools.getInstance()
			    .installDBDataChangeListener("Users/" + contact.getUserID() +
					    "/online/", new ObjectAcquireListener()
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
	
	    identifier.setText(contact.getNameSurname());
	    status.setText(contact.getStatus());
	
	    if (contact.getAvatar() != null)
		    image.setImageBitmap(contact.getAvatar());
	    else image.setImageResource(R.drawable.avatar);
        
        return vi;
    }
}

package pl.edu.mimuw.chatnfc.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;


public class ContactsFragment extends Fragment
{
	private ContactsListAdapter adapter = new ContactsListAdapter();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_contacts, container, false);
	}
	
	@Override
	public void onViewCreated(final @NonNull View view, @Nullable Bundle savedInstanceState)
	{
		refreshContacts();
    }
	
	@Override
	public void setUserVisibleHint(boolean visible)
	{
		super.setUserVisibleHint(visible);
		if (visible)
		{
			refreshContacts();
		}
	}
	
	private void refreshContacts()
	{
		if (getContext() == null)
			return;
		
		TextView contactsMessage = getView().findViewById(R.id.contacts_message);
		FirebaseUser user = FirebaseTools.getInstance().getCurrentUser();
		ListView contactsList = getView().findViewById(R.id.contacts_list);
		
		UserProfile profile = UserProfile.getProfile(user.getUid());
		
		if (profile.getContacts().size() == 0)
			contactsMessage.setText(R.string.no_contacts);
		else
		{
			contactsMessage.setText(R.string.any_contacts);
			adapter.rebuildContactsList(profile);
			contactsList.setAdapter(adapter);
		}
    }
}

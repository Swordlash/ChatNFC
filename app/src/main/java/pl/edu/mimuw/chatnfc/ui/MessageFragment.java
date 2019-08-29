package pl.edu.mimuw.chatnfc.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DatabaseError;

import pl.edu.mimuw.chatnfc.R;
import pl.edu.mimuw.chatnfc.config.UserProfile;
import pl.edu.mimuw.chatnfc.tools.FirebaseTools;
import pl.edu.mimuw.chatnfc.tools.ObjectAcquireListener;


public class MessageFragment extends Fragment
{
    private boolean dirty = false;
	private MessagesListAdapter adapter = new MessagesListAdapter();

    @Override
    public void setUserVisibleHint(boolean visible)
    {
        super.setUserVisibleHint(visible);
	    if (visible)
        {
	        if (dirty && getView() != null)
	        {
                dirty = false;
                refreshMessages();
            }
        }
    }

    @Override
    public void onViewCreated(final @NonNull View view, @Nullable Bundle savedInstanceState) {
	    String userID = FirebaseTools.getInstance().getCurrentUser().getUid();
	    adapter.rebuildMessagesList(UserProfile.getProfile(userID));
        FirebaseTools.getInstance()
                .installDBDataChangeListener("Users/" + FirebaseTools.getInstance().getCurrentUser()
                        .getUid()
                        + "/messages", new ObjectAcquireListener() {

                    @Override
                    public void onObjectAcquired(Object obj) {
	                    String userID = FirebaseTools.getInstance().getCurrentUser().getUid();
	                    adapter.rebuildMessagesList(UserProfile.getProfile(userID));
	                    
                        if (MessageFragment.this
		                        .getUserVisibleHint() && getView() != null) //if visible, then refresh
                        {
	                        refreshMessages();
                        }
                        else
                        {
	                        dirty = true;
                        }
                    }

                    @Override
                    public void onError(DatabaseError err) {
                        Log.e("Error MessageFragment", err.getMessage());
                    }
                });
    }

    private void refreshMessages() {
	    ListView contactsList = getView().findViewById(R.id.conversations_list);
	    contactsList.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }
}

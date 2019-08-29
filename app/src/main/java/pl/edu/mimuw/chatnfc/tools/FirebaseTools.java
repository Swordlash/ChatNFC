package pl.edu.mimuw.chatnfc.tools;

import android.app.ProgressDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Map;

public class FirebaseTools
{
	private static final FirebaseTools INSTANCE = new FirebaseTools();
	
	private FirebaseAuth auth;
	private FirebaseDatabase database;
	private FirebaseStorage storage;
	
	private FirebaseTools()
	{
		auth = FirebaseAuth.getInstance();
		database = FirebaseDatabase.getInstance();
		storage = FirebaseStorage.getInstance();
	}
	
	public static FirebaseTools getInstance()
	{
		return INSTANCE;
	}
	
	public void installDBDataChangeListener(String tag, final ObjectAcquireListener listener)
	{
		DatabaseReference ref = database.getReference(tag);
		ref.keepSynced(true);
		ref.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				listener.onObjectAcquired(dataSnapshot.getValue());
			}
			
			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				listener.onError(databaseError);
			}
		});
	}
	
	public DatabaseReference getReference(String tag)
	{
		return database.getReference(tag);
	}
	
	public void installDBDataChangeListenerByKey(String tag, final ObjectAcquireListener listener)
	{
		DatabaseReference ref = database.getReference(tag);
		ref.keepSynced(true);
		ref.orderByKey().addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				listener.onObjectAcquired(dataSnapshot.getValue());
			}
			
			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				listener.onError(databaseError);
			}
		});
	}
	
	public void acquireDataFromDB(String tag, final ObjectAcquireListener listener)
	{
		final DatabaseReference ref = database.getReference(tag);
		//ref.keepSynced(true);
		ref.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				listener.onObjectAcquired(dataSnapshot.getValue());
			}
			
			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				listener.onError(databaseError);
			}
		});
	}
	
	public void acquireDataFromDBWithDialog(String tag, final ProgressDialog dialog, final ObjectAcquireListener listener)
	{
		final DatabaseReference ref = database.getReference(tag);
		ref.keepSynced(true);
		ref.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				listener.onObjectAcquired(dataSnapshot.getValue());
				dialog.dismiss();
				ref.removeEventListener(this);
			}
			
			@Override
			public void onCancelled(DatabaseError databaseError)
			{
				dialog.hide();
				listener.onError(databaseError);
				ref.removeEventListener(this);
			}
		});
	}
	
	public void updateValuesInDB(String tag, Map<String, Object> map)
	{
		DatabaseReference ref = database.getReference(tag);
		ref.updateChildren(map);
	}
	
	public void updateValuesInDB(String tag, Map<String, Object> map, OnCompleteListener listener)
	{
		DatabaseReference ref = database.getReference(tag);
		ref.updateChildren(map).addOnCompleteListener(listener);
	}
	
	public void setValuesInDB(String tag, Map<String, Object> map)
	{
		DatabaseReference ref = database.getReference(tag);
		ref.setValue(map);
	}
	
	public void setValuesInDB(String tag, Map<String, Object> map, OnCompleteListener listener)
	{
		DatabaseReference ref = database.getReference(tag);
		ref.setValue(map).addOnCompleteListener(listener);
	}
	
	public void setValueInDB(String tag, Object data)
	{
		DatabaseReference ref = database.getReference(tag);
		ref.setValue(data);
	}
	
	public void setValueInDB(String tag, Object data, OnCompleteListener listener)
	{
		DatabaseReference ref = database.getReference(tag);
		ref.setValue(data).addOnCompleteListener(listener);
	}
	
	public void createUserWithEmailAndPassword(String email, String password, OnCompleteListener listener)
	{
		auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(listener);
	}
	
	public void loginUser(String login, String password, OnCompleteListener listener)
	{
		AuthCredential credential = EmailAuthProvider.getCredential(login, password);
		auth.signInWithCredential(credential).addOnCompleteListener(listener);
	}
	
	public FirebaseUser getCurrentUser()
	{
		return auth.getCurrentUser();
	}
}

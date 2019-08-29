package pl.edu.mimuw.chatnfc.tools;

import com.google.firebase.database.DatabaseError;

public interface ObjectAcquireListener
{
	void onObjectAcquired(Object obj);
	void onError(DatabaseError err);
}

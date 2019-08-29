package pl.edu.mimuw.chatnfc.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import pl.edu.mimuw.chatnfc.tools.FirebaseTools;

public class UnificApp extends Application
{
    private DatabaseReference userDatabase;
    private String myPublicKey;
	
	private static UnificApp UNIFIC_APP = null;
	private static Activity CURRENT_ACTIVITY = null;
	
	public static SharedPreferences getUnificPrefs(String name)
	{
		return getUnificAppContext().getSharedPreferences(name, MODE_PRIVATE);
	}
	
	public static void getBackToMenu()
	{
		Intent intent = new Intent(getCurrentActivity(), MenuActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		getCurrentActivity().startActivity(intent);
	}
	
	public static Context getUnificAppContext()
	{
		return UNIFIC_APP.getApplicationContext();
	}
	
	public static UnificApp getUnificApp()
	{
		return UNIFIC_APP;
	}
	
	public static Activity getCurrentActivity()
	{
		return CURRENT_ACTIVITY;
	}
	
	static void setCurrentActivity(Activity activity)
	{
		CURRENT_ACTIVITY = activity;
	}
	
	public String getMyPublicKey()
	{
		return myPublicKey;
	}
	
	public void setMyPublicKey(String key)
	{
		myPublicKey = key;
	}
    
    @Override
    public void onCreate()
    {
        super.onCreate();
	    UNIFIC_APP = this;
	    registerActivityLifecycleCallbacks(new ActivityCallbacks());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        if (FirebaseTools.getInstance().getCurrentUser() == null)
        {
            return;
        }

        String uid = FirebaseTools.getInstance().getCurrentUser().getUid();

        userDatabase = FirebaseTools.getInstance().getReference("Users/" + FirebaseTools.getInstance().getCurrentUser().getUid());

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null) {
                    userDatabase.child("online").onDisconnect().setValue(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

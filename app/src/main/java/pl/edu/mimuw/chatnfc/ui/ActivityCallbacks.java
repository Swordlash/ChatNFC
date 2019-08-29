package pl.edu.mimuw.chatnfc.ui;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ActivityCallbacks implements Application.ActivityLifecycleCallbacks
{
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState)
	{
		UnificApp.setCurrentActivity(activity);
	}
	
	@Override
	public void onActivityStarted(Activity activity)
	{
		UnificApp.setCurrentActivity(activity);
	}
	
	@Override
	public void onActivityResumed(Activity activity)
	{
		UnificApp.setCurrentActivity(activity);
	}
	
	@Override
	public void onActivityPaused(Activity activity)
	{
	
	}
	
	@Override
	public void onActivityStopped(Activity activity)
	{
	
	}
	
	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState)
	{
	
	}
	
	@Override
	public void onActivityDestroyed(Activity activity)
	{
	
	}
}

package com.darkrockstudios.apps.ringmyphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopRingingReceiver extends BroadcastReceiver
{
	public StopRingingReceiver()
	{
	}

	@Override
	public void onReceive( final Context context, final Intent intent )
	{
		if( intent != null && RingerService.ACTION_STOP_RINGING.equals( intent.getAction() ) )
		{
			Intent serviceIntent = new Intent( context, RingerService.class );
			serviceIntent.setAction( RingerService.ACTION_STOP_RINGING );

			context.startService( serviceIntent );
		}
	}
}

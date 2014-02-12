package com.darkrockstudios.apps.ringmyphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adam on 12/28/13.
 */
public class Purchase
{
	public static final Uri  PURCHASE_URI = Uri.parse( "ringmyphone://com.darkrockstudios.apps.ringmyphone/purchase" );
	public static final long TRIAL_LENGTH = TimeUnit.DAYS.toMillis( 7 );
	// For testing
	//public static final long TRIAL_LENGTH = TimeUnit.HOURS.toMillis( 4 );

	public static boolean isActive( final Context context )
	{
		return isPurchased( context ) || !isTrialPeriodOver( context );
	}

	public static boolean isPurchased( final Context context )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		return settings.getBoolean( Preferences.KEY_IS_PRO, false );
	}

	public static boolean isTrialPeriodOver( final Context context )
	{
		boolean isPast = true;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		long installTimeStamp = settings.getLong( Preferences.KEY_FIRST_INSTALL_DATE, 0 );
		if( installTimeStamp > 0 )
		{
			Date now = new Date();
			long timeSinceInstall = now.getTime() - installTimeStamp;

			if( timeSinceInstall < TRIAL_LENGTH )
			{
				isPast = false;
			}
		}

		return isPast;
	}

	public static long trialTimeRemaining( final Context context )
	{
		long timeRemaining = -1;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		long installTimeStamp = settings.getLong( Preferences.KEY_FIRST_INSTALL_DATE, 0 );
		if( installTimeStamp > 0 )
		{
			Date now = new Date();
			long timeSinceInstall = now.getTime() - installTimeStamp;

			timeRemaining = TRIAL_LENGTH - timeSinceInstall;
		}

		return timeRemaining;
	}
}

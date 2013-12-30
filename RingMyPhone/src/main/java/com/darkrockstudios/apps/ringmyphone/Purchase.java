package com.darkrockstudios.apps.ringmyphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adam on 12/28/13.
 */
public class Purchase
{
	public static final long TRAIL_LENGTH = TimeUnit.DAYS.toMillis( 1 );

	public static boolean isActive( Context context )
	{
		return isPurchased( context ) || !isTrailPeriodOver( context );
	}

	public static boolean isPurchased( Context context )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		return settings.getBoolean( Preferences.KEY_IS_PRO, false );
	}

	public static boolean isTrailPeriodOver( Context context )
	{
		boolean isPast = true;

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		long installTimeStamp = settings.getLong( Preferences.KEY_FIRST_INSTALL_DATE, 0 );
		if( installTimeStamp > 0 )
		{
			Date now = new Date();
			long timeSinceInstall = now.getTime() - installTimeStamp;

			if( timeSinceInstall < TRAIL_LENGTH )
			{
				isPast = false;
			}
		}

		return isPast;
	}
}

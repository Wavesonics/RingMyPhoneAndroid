package com.darkrockstudios.apps.ringmyphone;

import android.os.Build;

import java.lang.reflect.Field;

/**
 * Created by Adam on 1/4/2015.
 */
public class OsUtil
{
	public static boolean atLeastICS()
	{
		return osAtLeast( Build.VERSION_CODES.ICE_CREAM_SANDWICH );
	}

	public static boolean atLeastLollipop()
	{
		return osAtLeast( Build.VERSION_CODES.LOLLIPOP );
	}

	private static int sVersion = -1;

	private static boolean osAtLeast( int requiredVersion )
	{
		if( sVersion == -1 )
		{
			try
			{
				Field field = Build.VERSION.class.getDeclaredField( "SDK_INT" );
				sVersion = field.getInt( null );
			}
			catch( Exception e )
			{
				// ignore exception - field not available
				sVersion = 0;
			}
		}

		return sVersion >= requiredVersion;
	}
}

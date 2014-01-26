package com.darkrockstudios.apps.ringmyphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class UpgradeReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive( final Context context, final Intent intent )
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( context );
		int installedAppVersion = settings.getInt( Preferences.KEY_INSTALLED_APP_VERSION, -1 );
		int newAppVersion = context.getResources().getInteger( R.integer.pebble_app_version_code );

		// Only notify the user if there is a new watch app version
		if( installedAppVersion < newAppVersion )
		{
			Bitmap icon = BitmapFactory.decodeResource( context.getResources(), R.drawable.ic_launcher );

			Intent installRequestIntent = new Intent( context, MainActivity.class );
			installRequestIntent.setAction( MainActivity.ACTION_REQUEST_INSTALL );
			PendingIntent pendingInstallRequestIntent =
					PendingIntent.getActivity( context, 0, installRequestIntent, PendingIntent.FLAG_UPDATE_CURRENT );

			NotificationCompat.Builder builder =
					new NotificationCompat.Builder( context )
							.setSmallIcon( R.drawable.ic_action_download )
							.setLargeIcon( icon )
							.setContentTitle( context.getString( R.string.notification_update_title ) )
							.setContentText( context.getString( R.string.notification_update_text ) )
							.setStyle( new NotificationCompat.BigTextStyle()
									           .bigText( context.getString( R.string.notification_update_text ) ) )
							.setAutoCancel( true )
							.setContentIntent( pendingInstallRequestIntent );
			Notification notification = builder.build();

			NotificationManager notificationManager =
					(NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );
			notificationManager.notify( NotificationId.APP_UPDATE, notification );
		}
	}
}

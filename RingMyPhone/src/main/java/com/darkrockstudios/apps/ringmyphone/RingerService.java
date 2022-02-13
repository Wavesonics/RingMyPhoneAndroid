package com.darkrockstudios.apps.ringmyphone;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;

/**
 * Created by Adam on 10/14/13.
 */
// TODO(Noah): Restore vibration mode and ringer volume
// TODO(Noah): Prevent notification sound when starting ring
public class RingerService extends Service
{
	private static final String TAG                 = RingerService.class.getSimpleName();
	public static final  String ACTION_STOP_RINGING = RingerService.class.getName() + ".STOP_RINGING";
	private static final String RINGS_NOTIFICATION_CHANNEL = "ringsNotificationChannel";

	private static final int CMD_KEY = 0x1;

	private static final int CMD_START = 0x01;
	private static final int CMD_STOP  = 0x02;

	public static void createNotificationChannels(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = context.getString(R.string.notification_channel_rings_name);
			int importance = NotificationManager.IMPORTANCE_HIGH;
			NotificationChannel channel = new NotificationChannel(RINGS_NOTIFICATION_CHANNEL, name, importance);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
	}

	private PowerManager.WakeLock wakeLock;
	private Ringtone ringtone;
	private int savedVolume;

	public IBinder onBind(final Intent intent )
	{
		return null;
	}

	@Override
	public int onStartCommand( final Intent intent, final int flags, final int startId )
	{
		if( intent != null )
		{
			if( ACTION_STOP_RINGING.equals( intent.getAction() ) )
			{
				silencePhone( this );
			}
			else
			{
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
				final boolean silentMode = settings.getBoolean( Preferences.KEY_SILENT_MODE, false );
				final String jsonData = intent.getStringExtra( Constants.MSG_DATA );

				if( jsonData != null )
				{
					try
					{
						final long cmd;
						final PebbleDictionary data = PebbleDictionary.fromJson( jsonData );
						cmd = data.getUnsignedIntegerAsLong( CMD_KEY );

						if( cmd == CMD_START )
						{
							Log.w( TAG, "Ring Command Received!" );
							setMaxVolume( this );
							ringPhone( this, silentMode );
						}
						else if( cmd == CMD_STOP )
						{
							Log.w( TAG, "Silence Command Received!" );
							silencePhone( this );
						}
						else
						{
							Log.w( TAG, "Bad command received from pebble app: " + cmd );
						}
					}
					catch( final JSONException e )
					{
						Log.w( TAG, "failed retrieved -> dict" + e );
					}
				}
				else
				{
					Log.w( TAG, "No data from Pebble" );
				}
			}
		}

		return START_NOT_STICKY;
	}

	private void dismissRingingNotification()
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
		notificationManager.cancel( NotificationId.RINGING );
	}

	@SuppressLint("LaunchActivityFromNotification")
	private void postRingingNotification()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder( this, RINGS_NOTIFICATION_CHANNEL );
		builder.setTicker( getString( R.string.notification_ringing_ticker ) );
		builder.setContentTitle( getString( R.string.notification_ringing_ticker ) );
		builder.setContentText( getString( R.string.notification_ringing_text ) );
		builder.setSmallIcon( R.drawable.ic_action_volume_up );

		builder.setContentIntent( createStopRingingIntent() );
		builder.setOngoing( true );

		NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
		notificationManager.notify( NotificationId.RINGING, builder.build() );
	}

	private PendingIntent createStopRingingIntent()
	{
		Intent intent = new Intent( ACTION_STOP_RINGING );
		int flags = PendingIntent.FLAG_UPDATE_CURRENT;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			flags |= PendingIntent.FLAG_IMMUTABLE;
		}

		return PendingIntent.getBroadcast(this, 0, intent, flags );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		silencePhone( this );
	}

	private void setMaxVolume( final Context context )
	{
		AudioManager am =
				(AudioManager) context.getSystemService( Context.AUDIO_SERVICE );

		savedVolume = am.getStreamVolume( AudioManager.STREAM_RING );

		am.setStreamVolume(
				                  AudioManager.STREAM_RING,
				                  am.getStreamMaxVolume( AudioManager.STREAM_MUSIC ),
				                  0 );
	}

	private void restorePreviousVolume( final Context context )
	{
		AudioManager am =
				(AudioManager) context.getSystemService( Context.AUDIO_SERVICE );

		am.setStreamVolume( AudioManager.STREAM_RING, savedVolume, 0 );
		savedVolume = -1;
	}

	private void silencePhone( final Context context )
	{
		if( ringtone != null )
		{
			Log.w( TAG, "Silencing ringtone..." );
			ringtone.stop();
			ringtone = null;
		}
		else
		{
			Log.w( TAG, "Ringtone was null, can't silence!" );
		}

		restorePreviousVolume( context );

		dismissRingingNotification();

		releaseWakeLock();
	}

	private void ringPhone( final Context context, final boolean silentMode )
	{
		getWakeLock( context );

		postRingingNotification();

		if( ringtone == null )
		{
			Uri notification = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_RINGTONE );
			ringtone = RingtoneManager.getRingtone( context, notification );
		}

		if( ringtone != null && !ringtone.isPlaying() && !silentMode )
		{
			if( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP )
			{
				AudioAttributes audioAttributes = new AudioAttributes.Builder()
						                                  .setUsage( AudioAttributes.USAGE_NOTIFICATION_RINGTONE )
						                                  .setContentType( AudioAttributes.CONTENT_TYPE_SONIFICATION )
						                                  .setFlags( AudioAttributes.FLAG_AUDIBILITY_ENFORCED )
						                                  .build();
				ringtone.setAudioAttributes( audioAttributes );
			}
			else
			{
				ringtone.setStreamType( AudioManager.STREAM_RING );
			}
			ringtone.play();
		}
	}

	private void getWakeLock( final Context context )
	{
		if( wakeLock == null )
		{
			PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
			wakeLock = pm.newWakeLock( PowerManager.FULL_WAKE_LOCK |
			                             PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG );
			wakeLock.acquire(5 * 60 * 1000L /* 5 minutes */);
		}
	}

	private void releaseWakeLock()
	{
		if( wakeLock != null )
		{
			if( wakeLock.isHeld() )
			{
				wakeLock.release();
			}

			wakeLock = null;
		}
	}
}

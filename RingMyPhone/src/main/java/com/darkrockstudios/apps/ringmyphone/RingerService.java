package com.darkrockstudios.apps.ringmyphone;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;

/**
 * Created by Adam on 10/14/13.
 */
public class RingerService extends Service
{
	private static final String TAG                 = RingerService.class.getSimpleName();
	public static final  String ACTION_STOP_RINGING = RingerService.class.getName() + ".STOP_RINGING";

	private static final int CMD_KEY = 0x1;

	private static final int CMD_START = 0x01;
	private static final int CMD_STOP  = 0x02;

	private PowerManager.WakeLock m_wakeLock;
	private Ringtone              m_ringtone;
	private int                   m_savedVolume;

	public IBinder onBind( final Intent intent )
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
				if( Purchase.isActive( this ) )
				{
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
					final boolean silentMode = settings.getBoolean( Preferences.KEY_SILENT_MODE, false );

					final int transactionId = intent.getIntExtra( Constants.TRANSACTION_ID, -1 );
					final String jsonData = intent.getStringExtra( Constants.MSG_DATA );

					if( jsonData != null )
					{
						try
						{
							final long cmd;
							final PebbleDictionary data = PebbleDictionary.fromJson( jsonData );
							if( data != null )
							{
								cmd = data.getUnsignedIntegerAsLong( CMD_KEY );
							}
							else
							{
								cmd = -1;
							}

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
				else
				{
					postExpiredNotification();
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

	private void postRingingNotification()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder( this );
		builder.setTicker( getString( R.string.notification_ringing_ticker ) );
		builder.setContentTitle( getString( R.string.notification_ringing_ticker ) );
		builder.setContentText( getString( R.string.notification_ringing_text ) );
		builder.setSmallIcon( R.drawable.ic_action_volume_up );

		builder.setContentIntent( createStopRingingIntent() );
		builder.setOngoing( true );

		NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
		notificationManager.notify( NotificationId.RINGING, builder.build() );
	}

	private void postExpiredNotification()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder( this );
		builder.setTicker( getString( R.string.notification_expired_ticker ) );
		builder.setContentTitle( getString( R.string.notification_expired_ticker ) );
		builder.setContentText( getString( R.string.notification_expired_text ) );
		builder.setSmallIcon( R.drawable.ic_stat_clock );

		BitmapDrawable largeIcon = (BitmapDrawable) getResources().getDrawable( R.drawable.ic_launcher );
		builder.setLargeIcon( largeIcon.getBitmap() );
		builder.setContentIntent( createDefaultIntent() );

		builder.setAutoCancel( true );
		NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
		bigStyle.bigText( getString( R.string.notification_expired_text ) );
		builder.setStyle( bigStyle );

		builder.addAction( R.drawable.ic_notification_lock_open, getString( R.string.notification_expired_purchase_button ),
		                   createPurchaseIntent() );

		NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
		notificationManager.notify( NotificationId.TRIAL_EXPIRED, builder.build() );
	}

	private PendingIntent createStopRingingIntent()
	{
		Intent intent = new Intent( ACTION_STOP_RINGING );

		PendingIntent pendingIntent = PendingIntent.getBroadcast( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
		return pendingIntent;
	}

	private PendingIntent createDefaultIntent()
	{
		Intent intent = new Intent( this, MainActivity.class );

		PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
		return pendingIntent;
	}

	private PendingIntent createPurchaseIntent()
	{
		Intent intent = new Intent( this, MainActivity.class );
		intent.setData( Purchase.PURCHASE_URI );

		PendingIntent pendingIntent = PendingIntent.getActivity( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
		return pendingIntent;
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

		m_savedVolume = am.getStreamVolume( AudioManager.STREAM_RING );

		am.setStreamVolume(
				                  AudioManager.STREAM_RING,
				                  am.getStreamMaxVolume( AudioManager.STREAM_MUSIC ),
				                  0 );
	}

	private void restorePreviousVolume( final Context context )
	{
		AudioManager am =
				(AudioManager) context.getSystemService( Context.AUDIO_SERVICE );

		am.setStreamVolume( AudioManager.STREAM_RING, m_savedVolume, 0 );
		m_savedVolume = -1;
	}

	private void silencePhone( final Context context )
	{
		if( m_ringtone != null )
		{
			Log.w( TAG, "Silencing ringtone..." );
			m_ringtone.stop();
			m_ringtone = null;
		}
		else
		{
			Log.w( TAG, "Ringtone was null, can't silence!" );
		}

		restorePreviousVolume( context );

		dismissRingingNotification();

		releaseWakeLock( context );
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void ringPhone( final Context context, final boolean silentMode )
	{
		getWakeLock( context );

		postRingingNotification();

		if( m_ringtone == null )
		{
			Uri notification = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_RINGTONE );
			m_ringtone = RingtoneManager.getRingtone( context, notification );
		}

		if( m_ringtone != null && !m_ringtone.isPlaying() && !silentMode )
		{
			if( OsUtil.atLeastLollipop() )
			{
				AudioAttributes audioAttributes = new AudioAttributes.Builder()
						                                  .setUsage( AudioAttributes.USAGE_NOTIFICATION_RINGTONE )
						                                  .setContentType( AudioAttributes.CONTENT_TYPE_SONIFICATION )
						                                  .setFlags( AudioAttributes.FLAG_AUDIBILITY_ENFORCED )
						                                  .build();
				m_ringtone.setAudioAttributes( audioAttributes );
			}
			else
			{
				m_ringtone.setStreamType( AudioManager.STREAM_RING );
			}
			m_ringtone.play();
		}
	}

	private void getWakeLock( final Context context )
	{
		if( m_wakeLock == null )
		{
			PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
			m_wakeLock = pm.newWakeLock( PowerManager.FULL_WAKE_LOCK |
			                             PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG );
			m_wakeLock.acquire();
		}
	}

	private void releaseWakeLock( final Context context )
	{
		if( m_wakeLock != null )
		{
			if( m_wakeLock.isHeld() )
			{
				m_wakeLock.release();
			}

			m_wakeLock = null;
		}
	}
}

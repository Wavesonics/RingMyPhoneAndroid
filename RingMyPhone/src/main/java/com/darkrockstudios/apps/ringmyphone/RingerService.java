package com.darkrockstudios.apps.ringmyphone;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;

/**
 * Created by Adam on 10/14/13.
 */
public class RingerService extends Service
{
	private static final String TAG = RingerService.class.getSimpleName();

	private static final int CMD_KEY = 0x0;

	private static final int CMD_START = 0x01;
	private static final int CMD_STOP  = 0x02;

	private PowerManager.WakeLock m_wakeLock;
	private Ringtone              m_ringtone;
	private int                   m_savedVolume;

	public IBinder onBind( Intent intent )
	{
		return null;
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId )
	{
		final int transactionId = intent.getIntExtra( Constants.TRANSACTION_ID, -1 );
		final String jsonData = intent.getStringExtra( Constants.MSG_DATA );

		try
		{
			final PebbleDictionary data = PebbleDictionary.fromJson( jsonData );

			long cmd = data.getUnsignedInteger( CMD_KEY );
			if( cmd == CMD_START )
			{
				Log.w( TAG, "Ring Command Received!" );
				setMaxVolume( this );
				ringPhone( this );
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
		catch( JSONException e )
		{
			Log.w( TAG, "failed reived -> dict" + e );
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy ()
	{
		super.onDestroy();

		silencePhone( this );
	}

	private void setMaxVolume( Context context )
	{
		AudioManager am =
				(AudioManager) context.getSystemService( Context.AUDIO_SERVICE );

		m_savedVolume = am.getStreamVolume( AudioManager.STREAM_RING );

		am.setStreamVolume(
				                  AudioManager.STREAM_RING,
				                  am.getStreamMaxVolume( AudioManager.STREAM_MUSIC ),
				                  0 );
	}

	private void restorePreviousVolume( Context context )
	{
		AudioManager am =
				(AudioManager) context.getSystemService( Context.AUDIO_SERVICE );

		am.setStreamVolume(AudioManager.STREAM_RING,m_savedVolume,0 );
		m_savedVolume = -1;
	}

	private void silencePhone( Context context )
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

		releaseWakeLock( context );
	}

	private void ringPhone( Context context )
	{
		getWakeLock( context );

		if( m_ringtone == null )
		{
			Uri notification = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_RINGTONE );
			m_ringtone = RingtoneManager.getRingtone( context, notification );
		}

		if( m_ringtone != null && !m_ringtone.isPlaying() )
		{
			m_ringtone.play();
		}
	}

	private void getWakeLock( Context context )
	{
		if( m_wakeLock == null )
		{
			PowerManager pm = (PowerManager) context.getSystemService( Context.POWER_SERVICE );
			m_wakeLock = pm.newWakeLock( PowerManager.FULL_WAKE_LOCK |
			                             PowerManager.ACQUIRE_CAUSES_WAKEUP, TAG );
			m_wakeLock.acquire();
		}
	}

	private void releaseWakeLock( Context context )
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

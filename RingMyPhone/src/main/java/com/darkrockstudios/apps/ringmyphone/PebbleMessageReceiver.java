package com.darkrockstudios.apps.ringmyphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;

import java.util.UUID;

/**
 * Created by Adam on 10/14/13.
 */
public class PebbleMessageReceiver extends BroadcastReceiver
{
	private static final String TAG = PebbleMessageReceiver.class.getSimpleName();

	public void onReceive( Context context, Intent intent )
	{
		if( intent.getAction().equals( Constants.INTENT_APP_RECEIVE ) )
		{
			Log.i( TAG, "Received messaged from Pebble App." );

			final UUID receivedUuid = (UUID) intent.getSerializableExtra( Constants.APP_UUID );
			// Pebble-enabled apps are expected to be good citizens and only inspect broadcasts containing their UUID
			if( !PebbleApp.UUID.equals( receivedUuid ) )
			{
				Log.i( TAG, "not my UUID" );
				return;
			}

			final int transactionId = intent.getIntExtra( Constants.TRANSACTION_ID, -1 );
			final String jsonData = intent.getStringExtra( Constants.MSG_DATA );
			if( jsonData == null || jsonData.isEmpty() )
			{
				Log.w( TAG, "jsonData null" );
				PebbleKit.sendNackToPebble( context, transactionId );
				return;
			}

			Log.w( TAG, "Sending ACK to Pebble. " + transactionId );
			PebbleKit.sendAckToPebble( context, transactionId );

			Log.w( TAG, "Starting RingerService..." );
			Intent serviceIntent = new Intent( context, RingerService.class );
			serviceIntent.putExtra(Constants.TRANSACTION_ID, transactionId );
			serviceIntent.putExtra( Constants.MSG_DATA, jsonData );
			context.startService( serviceIntent );
		}
	}
}

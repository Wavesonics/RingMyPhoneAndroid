package com.darkrockstudios.apps.ringmyphone;

import android.content.Context;
import android.content.Intent;

import androidx.legacy.content.WakefulBroadcastReceiver;

@SuppressWarnings("deprecation") // WakefulBroadcastReceiver works fine since we don't target SDK 26
public class StopRingingReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = StopRingingReceiver.class.getSimpleName();

    public StopRingingReceiver() {
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent != null && RingerService.ACTION_STOP_RINGING.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, RingerService.class);
            serviceIntent.setAction(RingerService.ACTION_STOP_RINGING);

            startWakefulService(context, serviceIntent);
        }
    }
}

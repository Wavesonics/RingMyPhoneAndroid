package com.darkrockstudios.apps.ringmyphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpgradeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        BillingSecurity.updateInstallDate(context);
    }
}

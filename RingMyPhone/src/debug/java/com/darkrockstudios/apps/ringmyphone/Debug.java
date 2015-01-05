package com.darkrockstudios.apps.ringmyphone;

import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Adam on 1/4/2015.
 */
public final class Debug
{
	private static final String TAG = Debug.class.getSimpleName();

	public static void consumePurchase( final String purchaseData, IInAppBillingService service, String packageName )
	{
		try
		{
			// Consume the purchase for dev reset purposes
			JSONObject jo = new JSONObject( purchaseData );
			String token = jo.getString( "purchaseToken" );
			int consumeResponse = service.consumePurchase( 3, packageName, token );
			Log.d( TAG, "Purchase consumed! Response code: " + consumeResponse );
		}
		catch( JSONException | RemoteException e )
		{
			e.printStackTrace();
		}
	}
}

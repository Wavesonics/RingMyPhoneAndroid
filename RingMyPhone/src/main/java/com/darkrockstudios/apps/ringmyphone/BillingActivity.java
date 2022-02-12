package com.darkrockstudios.apps.ringmyphone;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Adam on 12/10/13.
 */
public abstract class BillingActivity extends AppCompatActivity
{
	private static final String TAG             = BillingActivity.class.getSimpleName();
	private static final String PRODUCT_SKU_PRO = "ringmyphone_pro";
	//private static final String PRODUCT_SKU_PRO = "android.test.purchased";

	private static final String EENY  =
			"ZVVOVwNAOtxduxvT9j0ONDRSNNBPND8NZVVOPtXPNDRNu3DWq6bksuJveGDE9cTgSksf4ltOtL/Dcu7sZ7sEA/N0Enkh2EqVGk";
	private static final String MEENY =
			"k1WaYqzbwOK9t4W1ylLD+XC0z+v8ZxuvYf5VXYs2wc2/OaztopRb3I5OL0UO4zWdFuwYV8RYaT9lsvBdNThGgXD8oCnm9PjzVE";

	private boolean m_isPro;

	private IInAppBillingService     m_service;
	private BillingServiceConnection m_serviceConn;

	private ProStatusListener m_statusListener;

	private String m_devPayload;

	public static interface ProStatusListener
	{
		public void onProStatusUpdate( final boolean isPro );
	}

	private class BillingServiceConnection implements ServiceConnection
	{
		@Override
		public void onServiceDisconnected( final ComponentName name )
		{
			m_service = null;
		}

		@Override
		public void onServiceConnected( final ComponentName name,
		                                final IBinder service )
		{
			m_service = IInAppBillingService.Stub.asInterface( service );

			Log.d( TAG, "Billing service connected." );

			if( !m_isPro )
			{
				runProCheck();
			}

			onBillingServiceConnected();
		}
	}

	protected abstract void onBillingServiceConnected();

	public void setProStatusListener( final ProStatusListener listener )
	{
		m_statusListener = listener;
	}

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		BillingSecurity.updateInstallDate( this );

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		m_isPro = settings.getBoolean( Preferences.KEY_IS_PRO, false );

		m_serviceConn = new BillingServiceConnection();
		connectToService();
	}

	private void connectToService()
	{
		Intent intent = new Intent( "com.android.vending.billing.InAppBillingService.BIND" );
		intent.setPackage( "com.android.vending" );
		bindService( intent, m_serviceConn, Context.BIND_AUTO_CREATE );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if( m_serviceConn != null )
		{
			unbindService( m_serviceConn );
		}
	}

	public boolean isPro()
	{
		return m_isPro;
	}

	public void runProCheck()
	{
		ProCheckThread proCheckThread = new ProCheckThread();
		proCheckThread.start();
	}

	private class ProCheckThread extends Thread
	{
		@Override
		public void run()
		{
			checkForPro();

			if( m_statusListener != null )
			{
				m_statusListener.onProStatusUpdate( isPro() );
			}
		}
	}

	private void checkForPro()
	{
		Log.d( TAG, "Checking pro purchase status..." );

		if( m_service != null )
		{
			try
			{
				Log.d( TAG, "sending request..." );
				Bundle ownedItems = m_service.getPurchases( 3, getPackageName(), "inapp", null );
				Log.d( TAG, "Request received!" );
				int response = ownedItems.getInt( "RESPONSE_CODE" );
				if( response == 0 )
				{
					ArrayList<String> ownedSkus = ownedItems.getStringArrayList( "INAPP_PURCHASE_ITEM_LIST" );
					ArrayList<String> purchaseDataList = ownedItems.getStringArrayList( "INAPP_PURCHASE_DATA_LIST" );
					ArrayList<String> signatureList = ownedItems.getStringArrayList( "INAPP_DATA_SIGNATURE_LIST" );
					//String continuationToken = ownedItems.getString( "INAPP_CONTINUATION_TOKEN" );

					for( int ii = 0; ii < purchaseDataList.size(); ++ii )
					{
						String purchaseData = purchaseDataList.get( ii );
						String dataSignature = signatureList.get( ii );
						String sku = ownedSkus.get( ii );

						if( sku.equals( PRODUCT_SKU_PRO ) && verifyPurchase( purchaseData, dataSignature ) )
						{
							Log.d( TAG, "Holy crap we're pro!" );
							cacheProLocally();
							m_isPro = true;

							// Uncomment to consume your purchase, this is only for testing the purchase process
							//Debug.consumePurchase( purchaseData, m_service, getPackageName() );
						}
					}
				}

				if( !m_isPro )
				{
					Log.d( TAG, "I don't think we're pro :(" );
				}
			}
			catch( RemoteException e )
			{
				e.printStackTrace();
			}
		}
	}

	public boolean purchasePro()
	{
		boolean shown = false;

		if( m_service != null )
		{
			try
			{
				m_devPayload = generateNewDevPayload();
				Bundle buyIntentBundle =
						m_service.getBuyIntent( 3, getPackageName(), PRODUCT_SKU_PRO, "inapp", m_devPayload );

				int responseCode = buyIntentBundle.getInt( "RESPONSE_CODE", 0 );
				if( responseCode == 0 )
				{
					PendingIntent pendingIntent = buyIntentBundle.getParcelable( "BUY_INTENT" );

					startIntentSenderForResult( pendingIntent.getIntentSender(),
					                            1001, new Intent(),
					                            0,
					                            0,
					                            0 );
					shown = true;
				}
			}
			catch( RemoteException | IntentSender.SendIntentException e )
			{
				e.printStackTrace();
			}
		}

		return shown;
	}

	@Override
	protected void onActivityResult( final int requestCode, final int resultCode, final Intent data )
	{
		if( requestCode == 1001 )
		{
			int responseCode = data.getIntExtra( "RESPONSE_CODE", 0 );
			String purchaseData = data.getStringExtra( "INAPP_PURCHASE_DATA" );
			String dataSignature = data.getStringExtra( "INAPP_DATA_SIGNATURE" );

			if( resultCode == RESULT_OK && responseCode == 0 )
			{
				try
				{
					JSONObject jo = new JSONObject( purchaseData );
					String sku = jo.getString( "productId" );
					String devPayload = jo.getString( "developerPayload" );

					if( PRODUCT_SKU_PRO.equals( sku ) &&
					    verifyPurchase( purchaseData, dataSignature ) &&
					    m_devPayload != null &&
					    m_devPayload.equals( devPayload ) )
					{
						Log.d( TAG, "You have bought " + sku );

						cacheProLocally();
						if( m_statusListener != null )
						{
							m_statusListener.onProStatusUpdate( true );
						}
					}
					else
					{
						Log.e( TAG, "Purchase failed." );
					}
				}
				catch( JSONException e )
				{
					Log.d( TAG, "Failed to parse purchase data." );
					e.printStackTrace();
				}
			}
		}
	}

	private String generateNewDevPayload()
	{
		final Date now = new Date();
		final long fudge = now.getTime() / 7l;
		return BillingSecurity.sha1Hash( fudge + "noh4xplz" );
	}

	@SuppressLint("CommitPrefEdits")
	private void cacheProLocally()
	{
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( this );
		settings.edit().putBoolean( Preferences.KEY_IS_PRO, true ).commit();
	}

	private String assemblePublicKey()
	{
		return BillingSecurity.superSecureCrypto( EENY ) +
		       BillingSecurity.superSecureCrypto( MEENY ) +
		       BillingSecurity.superSecureCrypto( getString( R.string.miny ) ) +
		       BillingSecurity.superSecureCrypto( getString( R.string.moe ) );
	}

	private boolean verifyPurchase( final String data, final String signature )
	{
		return BillingSecurity.verifySignature( assemblePublicKey(), data, signature );
	}
}

package com.darkrockstudios.apps.ringmyphone;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends BillingActivity implements BillingActivity.ProStatusListener
{
	private static final String TAG                = MainActivity.class.getSimpleName();
	private static final String ABOUT_FRAGMENT_TAG = "AboutFragment";

	public static final String ACTION_REQUEST_INSTALL = MainActivity.class.getName() + ".ACTION_REQUEST_INSTALL";

	private boolean m_showPurchaseDialog;
	private boolean m_showInstallDialog;

	@InjectView(R.id.listView)
	ListView m_listView;

	private MenuAdapter  m_menuAdapter;
	private TimeReceiver m_timeReceiver;

	@Override
	protected void onCreate( final Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		ButterKnife.inject( this );

		setProStatusListener( this );

		m_menuAdapter = new MenuAdapter();
		m_listView.setAdapter( m_menuAdapter );

		handleIntent( getIntent() );
	}

	@Override
	protected void onNewIntent( final Intent intent )
	{
		handleIntent( intent );
	}

	private void handleIntent( final Intent intent )
	{
		if( intent != null )
		{
			if( Purchase.PURCHASE_URI.equals( intent.getData() ) )
			{
				m_showPurchaseDialog = true;
			}

			if( ACTION_REQUEST_INSTALL.equals( intent.getAction() ) )
			{
				m_showInstallDialog = true;
			}
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		m_timeReceiver = new TimeReceiver();
		IntentFilter intentFilter = new IntentFilter( Intent.ACTION_TIME_TICK );
		registerReceiver( m_timeReceiver, intentFilter );
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if( m_showPurchaseDialog && purchasePro() )
		{
			m_showPurchaseDialog = false;
		}

		if( m_showInstallDialog )
		{
			m_showInstallDialog = false;
			installPebbleApp();
		}
	}

	@Override
	public boolean onCreateOptionsMenu( final Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		unregisterReceiver( m_timeReceiver );
		m_timeReceiver = null;
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item )
	{
		// Handle item selection
		switch( item.getItemId() )
		{
			case R.id.action_about:
				showAbout();
				return true;
			default:
				return super.onOptionsItemSelected( item );
		}
	}

	private void showAbout()
	{
		AboutFragment aboutFragment = new AboutFragment();
		aboutFragment.show( getFragmentManager(), ABOUT_FRAGMENT_TAG );
	}

	public void onStopClicked( final View v )
	{
		Log.i( TAG, "Stopping RingerService" );
		Crouton.makeText( this, R.string.stop_button_response, Style.CONFIRM, Configuration.DURATION_SHORT ).show();
		stopService( new Intent( this, RingerService.class ) );
	}

	public void onInstallClicked( final View v )
	{
		Log.i( TAG, "Installing Pebble App" );

		installPebbleApp();
	}

	public void onPurchaseClicked( final View v )
	{
		Log.i( TAG, "Purchasing App" );

		purchasePro();
	}

	@Override
	public void onProStatusUpdate( final boolean isPro )
	{
		m_menuAdapter.refresh();
	}

	protected void onBillingServiceConnected()
	{
		if( m_showPurchaseDialog )
		{
			m_showPurchaseDialog = !purchasePro();
		}
	}

	private void installPebbleApp()
	{
		displayOsSelectDialog();
	}

	private void displayOsSelectDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setTitle( R.string.alsert_os_title );
		builder.setIcon( R.drawable.ic_action_warning );
		builder.setMessage( R.string.alert_os_message );
		builder.setPositiveButton( R.string.alert_os_positive_button, new OsSelectListener( PebbleOsVersion.TWO ) );
		builder.setNegativeButton( R.string.alert_os_negative_button, new OsSelectListener( PebbleOsVersion.ONE ) );
		builder.create().show();
	}

	private static enum PebbleOsVersion
	{
		ONE, TWO
	}

	;

	private class OsSelectListener implements DialogInterface.OnClickListener
	{
		private PebbleOsVersion m_os;

		public OsSelectListener( final PebbleOsVersion os )
		{
			m_os = os;
		}

		@Override
		public void onClick( final DialogInterface dialog, final int which )
		{
			PebbleAppInstaller installerTask = new PebbleAppInstaller( m_os );
			installerTask.execute();
		}
	}

	private static enum InstallCode
	{
		SUCCESS,
		STORAGE_FAILURE,
		PEBBLE_INSTALL_FAILURE
	}

	private class PebbleAppInstaller extends AsyncTask<Void, Integer, InstallCode>
	{
		private final String PBW_FILE_NAME_1_0 = "ringmyphone_1.pbw";
		private final String PBW_FILE_NAME_2_0 = "ringmyphone_2.pbw";

		private PebbleOsVersion m_os;

		public PebbleAppInstaller( final PebbleOsVersion os )
		{
			m_os = os;
		}

		@Override
		protected InstallCode doInBackground( final Void... params )
		{
			final InstallCode installCode;
			if( copyRawFileToExternalStorage( getPbwAssetId() ) )
			{
				if( installPebbleApp() )
				{
					int installedVersion = getResources().getInteger( R.integer.pebble_app_version_code );

					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( MainActivity.this );
					settings.edit().putInt( Preferences.KEY_INSTALLED_APP_VERSION, installedVersion ).commit();

					installCode = InstallCode.SUCCESS;
				}
				else
				{
					installCode = InstallCode.PEBBLE_INSTALL_FAILURE;
				}
			}
			else
			{
				installCode = InstallCode.STORAGE_FAILURE;
			}

			return installCode;
		}

		@Override
		protected void onPostExecute( final InstallCode code )
		{
			switch( code )
			{
				case STORAGE_FAILURE:
					Crouton.makeText( MainActivity.this, R.string.install_watch_app_failed_storage, Style.ALERT,
					                  Configuration.DURATION_LONG ).show();
					break;
				case PEBBLE_INSTALL_FAILURE:
					Crouton.makeText( MainActivity.this, R.string.install_watch_app_failed_pebble, Style.ALERT,
					                  Configuration.DURATION_LONG ).show();
					break;
				case SUCCESS:
				default:
					break;
			}
		}

		private int getPbwAssetId()
		{
			final int assetId;
			if( m_os == PebbleOsVersion.ONE )
			{
				assetId = R.raw.ringmyphone_1;
			}
			else
			{
				assetId = R.raw.ringmyphone_2;
			}

			return assetId;
		}

		private String getPbwFileName()
		{
			final String fileName;
			if( m_os == PebbleOsVersion.ONE )
			{
				fileName = PBW_FILE_NAME_1_0;
			}
			else
			{
				fileName = PBW_FILE_NAME_2_0;
			}

			return fileName;
		}

		private boolean installPebbleApp()
		{
			boolean success = false;

			File path = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
			final File file = new File( path, getPbwFileName() );

			String mimeType = "application/octet-stream";

			Intent newIntent = new Intent( android.content.Intent.ACTION_VIEW );

			newIntent.setDataAndType( Uri.fromFile( file ), mimeType );
			newIntent.setFlags( Intent.FLAG_ACTIVITY_NO_HISTORY );
			try
			{
				startActivity( newIntent );
				success = true;
			}
			catch( final android.content.ActivityNotFoundException e )
			{
				e.printStackTrace();
			}

			return success;
		}

		private boolean copyRawFileToExternalStorage( final int resId )
		{
			boolean success = false;

			if( isExternalStorageWritable() )
			{
				File path = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS );
				File file = new File( path, getPbwFileName() );

				try
				{
					path.mkdirs();

					InputStream is = getResources().openRawResource( resId );
					OutputStream os = new FileOutputStream( file );

					byte[] data = new byte[ is.available() ];
					is.read( data );
					os.write( data );
					is.close();
					os.close();

					success = true;
				}
				catch( final IOException e )
				{
					e.printStackTrace();
				}
			}
			return success;
		}

		private boolean isExternalStorageWritable()
		{
			final boolean externalStorageAvailable;
			final boolean externalStorageWritable;

			String state = Environment.getExternalStorageState();
			if( Environment.MEDIA_MOUNTED.equals( state ) )
			{
				// We can read and write the media
				externalStorageAvailable = externalStorageWritable = true;
			}
			else if( Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) )
			{
				// We can only read the media
				externalStorageAvailable = true;
				externalStorageWritable = false;
			}
			else
			{
				// Something else is wrong. It may be one of many other states, but all we need
				//  to know is we can neither read nor write
				externalStorageAvailable = externalStorageWritable = false;
			}

			return externalStorageAvailable && externalStorageWritable;
		}
	}

	enum MenuItemType
	{
		Welcome,
		Install,
		Purchase,
		Stop
	}

	private class TimeReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive( final Context context, final Intent intent )
		{
			if( !isPro() )
			{
				m_menuAdapter.refresh();
			}
		}
	}

	class MenuAdapter extends BaseAdapter
	{
		private List<MenuItemType> m_menuItems;

		public MenuAdapter()
		{
			m_menuItems = new ArrayList<>();
			refresh();
		}

		public void refresh()
		{
			m_menuItems.clear();

			m_menuItems.add( MenuItemType.Welcome );
			m_menuItems.add( MenuItemType.Install );
			if( !isPro() )
			{
				m_menuItems.add( MenuItemType.Purchase );
			}
			m_menuItems.add( MenuItemType.Stop );

			runOnUiThread( new Runnable()
			{
				@Override
				public void run()
				{
					notifyDataSetChanged();
				}
			} );
		}

		public boolean areAllItemsEnabled()
		{
			return false;
		}

		public boolean isEnabled( final int position )
		{
			return false;
		}

		public int getCount()
		{
			return m_menuItems.size();
		}

		@Override
		public Object getItem( final int position )
		{
			return m_menuItems.get( position );
		}

		@Override
		public long getItemId( final int position )
		{
			return getItemViewType( position );
		}

		@Override
		public View getView( final int position, final View convertView, final ViewGroup parent )
		{
			final View view;

			final MenuItemType type = (MenuItemType) getItem( position );
			if( convertView == null )
			{
				LayoutInflater inflater = LayoutInflater.from( MainActivity.this );

				switch( type )
				{
					case Welcome:
						view = inflater.inflate( R.layout.row_welcome, parent, false );
						break;
					case Stop:
						view = inflater.inflate( R.layout.row_stop_ringing, parent, false );
						break;
					case Purchase:
						view = inflater.inflate( R.layout.row_purchase, parent, false );
						break;
					case Install:
						view = inflater.inflate( R.layout.row_install_watch_app, parent, false );
						break;
					default:
						view = null;
						break;
				}
			}
			else
			{
				view = convertView;
			}

			if( type == MenuItemType.Purchase )
			{
				setCountDown( (TextView) view.findViewById( R.id.purchase_app_count_down ) );
			}

			return view;
		}

		private void setCountDown( final TextView countDownView )
		{
			long timeRemaining = Purchase.trialTimeRemaining( MainActivity.this );

			if( timeRemaining > 0 )
			{
				final long days = TimeUnit.MILLISECONDS.toDays( timeRemaining );
				timeRemaining -= TimeUnit.DAYS.toMillis( days );

				final long hours = TimeUnit.MILLISECONDS.toHours( timeRemaining );
				timeRemaining -= TimeUnit.HOURS.toMillis( hours );

				final long minutes = TimeUnit.MILLISECONDS.toMinutes( timeRemaining );

				countDownView.setText( getString( R.string.purchase_app_countdown, days, hours, minutes ) );
			}
			else
			{
				countDownView.setText( getString( R.string.purchase_app_countdown_expired ) );
			}
		}

		@Override
		public int getItemViewType( final int position )
		{
			return ((MenuItemType) getItem( position )).ordinal();
		}

		@Override
		public int getViewTypeCount()
		{
			return MenuItemType.values().length;
		}

		@Override
		public boolean isEmpty()
		{
			return false;
		}
	}
}

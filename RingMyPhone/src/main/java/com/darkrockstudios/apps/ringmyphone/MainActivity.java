package com.darkrockstudios.apps.ringmyphone;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends Activity
{
	private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ABOUT_FRAGMENT_TAG = "AboutFragment";

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

        ListView listView = (ListView) findViewById( R.id.listView );
        listView.setAdapter( new MenuAdapter() );
	}

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAbout()
    {
        AboutFragment aboutFragment = new AboutFragment();
        aboutFragment.show( getFragmentManager(), ABOUT_FRAGMENT_TAG );
    }

	public void onStopClicked( View v )
	{
		Log.i( TAG, "Stopping RingerService" );
        Crouton.makeText( this, R.string.stop_button_response, Style.CONFIRM, Configuration.DURATION_SHORT ).show();
        stopService( new Intent( this, RingerService.class ) );
	}

    public void onInstallClicked( View v )
    {
        Log.i( TAG, "Installing Pebble App" );

        PebbleAppInstaller installerTask = new PebbleAppInstaller();
        installerTask.execute();
    }

    private static enum InstallCode
    {
        SUCCESS,
        STORAGE_FAILURE,
        PEBBLE_INSTALL_FAILURE
    }

    private class PebbleAppInstaller extends AsyncTask< Void, Integer, InstallCode >
    {
        private final String PBW_FILE_NAME = "RingMyPhone.pbw";

        @Override
        protected InstallCode doInBackground(Void... params)
        {
            final InstallCode installCode;
            if( copyRawFileToExternalStorage( R.raw.ringmyphone ) )
            {
                if( installPebbleApp() )
                {
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
        protected void onPostExecute(InstallCode code)
        {
            switch( code )
            {
                case STORAGE_FAILURE:
                    Crouton.makeText( MainActivity.this, R.string.install_watch_app_failed_storage, Style.ALERT, Configuration.DURATION_LONG ).show();
                    break;
                case PEBBLE_INSTALL_FAILURE:
                    Crouton.makeText( MainActivity.this, R.string.install_watch_app_failed_pebble, Style.ALERT, Configuration.DURATION_LONG ).show();
                    break;
                case SUCCESS:
                default:
                    break;
            }
        }

        private boolean installPebbleApp()
        {
            boolean success = false;

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, PBW_FILE_NAME);

            String mimeType = "application/octet-stream";

            Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);

            newIntent.setDataAndType(Uri.fromFile(file),mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try
            {
                startActivity(newIntent);
                success = true;
            }
            catch (android.content.ActivityNotFoundException e)
            {
                e.printStackTrace();
            }

            return success;
        }

        private boolean copyRawFileToExternalStorage(int resId)
        {
            boolean success = false;

            if( isExternalStorageWritable() )
            {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, PBW_FILE_NAME);

                try
                {
                    path.mkdirs();

                    InputStream is = getResources().openRawResource(resId);
                    OutputStream os = new FileOutputStream(file);

                    byte[] data = new byte[is.available()];
                    is.read(data);
                    os.write(data);
                    is.close();
                    os.close();

                    success = true;
                }
                catch(IOException e)
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
            if (Environment.MEDIA_MOUNTED.equals(state))
            {
                // We can read and write the media
                externalStorageAvailable = externalStorageWritable = true;
            }
            else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
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
        Stop,
        COUNT,
        Invalid
    };

    private class MenuAdapter extends BaseAdapter
    {

        public boolean areAllItemsEnabled()
        {
            return false;
        }

        public boolean isEnabled(int position)
        {
            return false;
        }

        public int getCount()
        {
            return MenuItemType.COUNT.ordinal();
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return getItemViewType(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final View view;
            if( convertView == null )
            {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

                final MenuItemType type = getMenuItemType(position);
                switch( type )
                {
                    case Welcome:
                        view = inflater.inflate( R.layout.row_welcome, parent, false );
                        break;
                    case Stop:
                        view = inflater.inflate( R.layout.row_stop_ringing, parent, false );
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

            return view;
        }

        public MenuItemType getMenuItemType (int position)
        {
            final MenuItemType type;

            if( position < MenuItemType.COUNT.ordinal() )
            {
                type = MenuItemType.values()[position];
            }
            else
            {
                type = MenuItemType.Invalid;
            }

            return type;
        }

        @Override
        public int getItemViewType (int position)
        {
            return getMenuItemType(position).ordinal();
        }

        @Override
        public int getViewTypeCount()
        {
            return MenuItemType.COUNT.ordinal();
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }
    }
}

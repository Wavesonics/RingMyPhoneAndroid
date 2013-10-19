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
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends Activity
{
	private static final String TAG = MainActivity.class.getSimpleName();

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

	public void onStopClicked( View v )
	{
		Log.i( TAG, "Stopping RingerService" );
        Crouton.makeText( this, R.string.stop_button_response, Style.ALERT, Configuration.DURATION_SHORT ).show();
        stopService( new Intent( this, RingerService.class ) );
	}

    public void onInstallClicked( View v )
    {
        Log.i( TAG, "Installing Pebble App" );

        PebbleAppInstaller installerTask = new PebbleAppInstaller();
        installerTask.execute();
    }

    private class PebbleAppInstaller extends AsyncTask< Void, Integer, Void >
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            if( copyRawFileToExternalStorage( R.raw.ringmyphone ) )
            {
                installPebbleApp();
            }

            return null;
        }

        private boolean installPebbleApp()
        {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "RingMyPhone.pbw");

            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mimeType = mimeTypeMap.getMimeTypeFromExtension( "pbw" );

            Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);

            newIntent.setDataAndType(Uri.fromFile(file),mimeType);
            newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
            try
            {
                startActivity(newIntent);
            }
            catch (android.content.ActivityNotFoundException e)
            {
                e.printStackTrace();
                //Toast.makeText(MainActivity.this, "No handler for this type of file.", 4000).show();
            }

            return true;
        }

        private boolean copyRawFileToExternalStorage(int resId)
        {
            boolean success = false;

            if( isExternalStorageWritable() )
            {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, "RingMyPhone.pbw");

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
            boolean externalStorageAvailable = false;
            boolean externalStorageWriteable = false;

            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state))
            {
                // We can read and write the media
                externalStorageAvailable = externalStorageWriteable = true;
            }
            else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
            {
                // We can only read the media
                externalStorageAvailable = true;
                externalStorageWriteable = false;
            }
            else
            {
                // Something else is wrong. It may be one of many other states, but all we need
                //  to know is we can neither read nor write
                externalStorageAvailable = externalStorageWriteable = false;
            }

            return externalStorageAvailable && externalStorageWriteable;
        }
    }

    private class MenuAdapter extends BaseAdapter
    {
        private static final int TYPE_INVALID = -1;
        private static final int TYPE_WELCOME = 0;
        private static final int TYPE_STOP = 1;
        private static final int TYPE_INSTALL = 2;
        private static final int N_TYPES = 3;

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
            return N_TYPES;
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

                final int type = getItemViewType( position );
                switch( type )
                {
                    case TYPE_WELCOME:
                        view = inflater.inflate( R.layout.row_welcome, parent, false );
                        break;
                    case TYPE_STOP:
                        view = inflater.inflate( R.layout.row_stop_ringing, parent, false );
                        break;
                    case TYPE_INSTALL:
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

        public int getItemViewType (int position)
        {
            final int type;

            switch( position )
            {
                case 0:
                    type = TYPE_WELCOME;
                    break;
                case 1:
                    type = TYPE_STOP;
                    break;
                case 2:
                    type = TYPE_INSTALL;
                    break;
                default:
                    type = TYPE_INVALID;
            }

            return type;
        }

        @Override
        public int getViewTypeCount()
        {
            return N_TYPES;
        }

        @Override
        public boolean isEmpty()
        {
            return false;
        }
    }
}

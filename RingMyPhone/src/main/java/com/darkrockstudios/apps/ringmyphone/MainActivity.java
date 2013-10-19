package com.darkrockstudios.apps.ringmyphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

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
        Log.i( TAG, "Stopping RingerService" );

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

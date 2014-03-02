package com.darkrockstudios.apps.ringmyphone;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by adam on 3/2/14.
 */
public class SettingsActivity extends Activity
{
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getFragmentManager().beginTransaction()
		                    .replace( android.R.id.content, new SettingsFragment() )
		                    .commit();
	}
}

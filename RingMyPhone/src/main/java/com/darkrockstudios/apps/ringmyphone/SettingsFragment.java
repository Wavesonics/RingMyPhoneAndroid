package com.darkrockstudios.apps.ringmyphone;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by adam on 3/2/14.
 */
public class SettingsFragment extends PreferenceFragment
{
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		addPreferencesFromResource( R.xml.settings );
	}
}

package com.darkrockstudios.apps.ringmyphone;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;


/**
 * Created by adam on 3/2/14.
 */
public class SettingsFragment extends PreferenceFragmentCompat
{
	@Override
	public void onCreatePreferences( Bundle savedInstanceState, String rootKey )
	{
		setPreferencesFromResource(R.xml.settings, rootKey);
	}
}

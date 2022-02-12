package com.darkrockstudios.apps.ringmyphone;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by adam on 3/2/14.
 */
public class SettingsActivity extends AppCompatActivity
{
	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		getSupportFragmentManager()
				.beginTransaction()
				.replace( android.R.id.content, new SettingsFragment() )
				.commit();
	}
}

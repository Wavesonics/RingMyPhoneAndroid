package com.darkrockstudios.apps.ringmyphone;

import android.app.Activity;
import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.darkrockstudios.apps.ringmyphone.databinding.FragmentAboutBinding;

/**
 * Created by adam on 10/20/13.
 */
public class AboutFragment extends DialogFragment implements View.OnClickListener
{
	private FragmentAboutBinding binding;

	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		binding = FragmentAboutBinding.inflate(inflater, container, false);
		View view = binding.getRoot();

		Dialog dialog = getDialog();
		if( dialog != null )
		{
			dialog.setTitle(R.string.about_title);
		}

		binding.marketButton.setOnClickListener( this );

		MovementMethod linkMovementMethod = LinkMovementMethod.getInstance();

		binding.githubAndroid.setMovementMethod( linkMovementMethod );
		binding.githubAndroid.setText( getText( R.string.about_body_github_android ) );

		binding.githubPebble.setMovementMethod( linkMovementMethod );
		binding.githubPebble.setText( getText( R.string.about_body_github_pebble ) );

		binding.feedback.setMovementMethod( linkMovementMethod);
		binding.feedback.setText( getText( R.string.about_body_feedback ) );

		binding.appVersion.setText(getString(R.string.about_body_version, getAppVersion()));

		return view;
	}

	@Override
	public void onClick( View v )
	{
		if( v.getId() == binding.marketButton.getId() && isAdded() )
		{
			Intent intent = new Intent( Intent.ACTION_VIEW );
			intent.setData( Uri.parse( "market://search?q=pub:Dark+Rock+Studios" ) );
			startActivity( intent );
		}
	}

	private String getAppVersion()
	{
		String version = "-";

		Activity activity = getActivity();
		if( activity != null )
		{
			try
			{
				PackageManager pm = activity.getPackageManager();
				if( pm != null )
				{
					PackageInfo pinfo = pm.getPackageInfo( activity.getPackageName(), 0 );
					version = pinfo.versionName;
				}
			}
			catch( PackageManager.NameNotFoundException e )
			{
				e.printStackTrace();
			}
		}

		return version;
	}
}

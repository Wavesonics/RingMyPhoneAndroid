package com.darkrockstudios.apps.ringmyphone;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by adam on 10/20/13.
 */
public class AboutFragment extends DialogFragment implements View.OnClickListener
{
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.fragment_about, container, false );

		Dialog dialog = getDialog();
		if( dialog != null )
		{
			dialog.setTitle( R.string.about_title );
		}

		Button marketButton = (Button) view.findViewById( R.id.ABOUT_market_button );
		marketButton.setOnClickListener( this );

		MovementMethod linkMovementMethod = LinkMovementMethod.getInstance();

		TextView githubAndroidView = (TextView) view.findViewById( R.id.ABOUT_github_android );
		githubAndroidView.setMovementMethod( linkMovementMethod );
		githubAndroidView.setText( getText( R.string.about_body_github_android ) );

		TextView githubPebbleView = (TextView) view.findViewById( R.id.ABOUT_github_pebble );
		githubPebbleView.setMovementMethod( linkMovementMethod );
		githubPebbleView.setText( getText( R.string.about_body_github_pebble ) );

		TextView githubFeedbackView = (TextView) view.findViewById( R.id.ABOUT_feedback );
		githubFeedbackView.setMovementMethod( linkMovementMethod);
		githubFeedbackView.setText( getText( R.string.about_body_feedback ) );

		TextView versionView = (TextView) view.findViewById( R.id.ABOUT_app_version );
		versionView.setText( "v" + getAppVersion() );

		return view;
	}

	@Override
	public void onClick( View v )
	{
		if( v.getId() == R.id.ABOUT_market_button && isAdded() )
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

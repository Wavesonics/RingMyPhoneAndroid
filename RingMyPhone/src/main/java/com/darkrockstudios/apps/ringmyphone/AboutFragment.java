package com.darkrockstudios.apps.ringmyphone;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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
}

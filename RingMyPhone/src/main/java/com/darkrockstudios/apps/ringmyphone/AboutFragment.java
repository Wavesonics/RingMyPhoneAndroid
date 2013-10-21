package com.darkrockstudios.apps.ringmyphone;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by adam on 10/20/13.
 */
public class AboutFragment extends DialogFragment
{
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		View view = inflater.inflate( R.layout.fragment_about, container, false );

		Dialog dialog = getDialog();
		if( dialog != null )
		{
			dialog.setTitle( R.string.about_title );
		}

		TextView bodyView = (TextView) view.findViewById( R.id.ABOUT_body );
		String aboutBody = getString( R.string.about_body );
		bodyView.setText( linkifyHtml( aboutBody, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES ) );

		return view;
	}

	public static Spannable linkifyHtml( String html, int linkifyMask )
	{
		Spanned text = Html.fromHtml( html );
		URLSpan[] currentSpans = text.getSpans( 0, text.length(), URLSpan.class );

		SpannableString buffer = new SpannableString( text );
		Linkify.addLinks( buffer, linkifyMask );

		for( URLSpan span : currentSpans )
		{
			int end = text.getSpanEnd( span );
			int start = text.getSpanStart( span );
			buffer.setSpan( span, start, end, 0 );
		}
		return buffer;
	}
}

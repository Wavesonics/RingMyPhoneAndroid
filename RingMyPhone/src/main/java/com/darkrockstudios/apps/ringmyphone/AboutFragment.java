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

		return view;
	}
}

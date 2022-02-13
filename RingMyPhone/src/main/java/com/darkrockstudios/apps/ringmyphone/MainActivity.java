package com.darkrockstudios.apps.ringmyphone;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.darkrockstudios.apps.ringmyphone.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

// TODO(Noah): Add button to send user to Rebble store (if possible)
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ABOUT_FRAGMENT_TAG = "AboutFragment";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        MenuAdapter m_menuAdapter = new MenuAdapter();
        binding.listView.setAdapter(m_menuAdapter);
        RingerService.createNotificationChannels(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final boolean handled;
        if (item.getItemId() == R.id.action_about) {
            showAbout();
            handled = true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            handled = true;
        } else {
            handled = super.onOptionsItemSelected(item);
        }
        return handled;
    }

    private void showAbout() {
        AboutFragment aboutFragment = new AboutFragment();
        aboutFragment.show(getSupportFragmentManager(), ABOUT_FRAGMENT_TAG);
    }

    public void onStopClicked(final View v) {
        Log.i(TAG, "Stopping RingerService");
        Crouton.makeText(this, R.string.stop_button_response, Style.CONFIRM, Configuration.DURATION_SHORT).show();
        stopService(new Intent(this, RingerService.class));
    }

    enum MenuItemType {
        Welcome,
        Stop
    }

    class MenuAdapter extends BaseAdapter {
        final private List<MenuItemType> m_menuItems;

        public MenuAdapter() {
            m_menuItems = new ArrayList<>();
            refresh();
        }

        public void refresh() {
            runOnUiThread(() -> {
                m_menuItems.clear();

                m_menuItems.add(MenuItemType.Welcome);
                m_menuItems.add(MenuItemType.Stop);

                notifyDataSetChanged();
            });
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(final int position) {
            return false;
        }

        public int getCount() {
            return m_menuItems.size();
        }

        @Override
        public Object getItem(final int position) {
            return m_menuItems.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return getItemViewType(position);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view;

            final MenuItemType type = (MenuItemType) getItem(position);
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

                switch (type) {
                    case Welcome:
                        view = inflater.inflate(R.layout.row_welcome, parent, false);
                        break;
                    case Stop:
                        view = inflater.inflate(R.layout.row_stop_ringing, parent, false);
                        break;
                    default:
                        view = null;
                        break;
                }
            } else {
                view = convertView;
            }

            return view;
        }

        @Override
        public int getItemViewType(final int position) {
            return ((MenuItemType) getItem(position)).ordinal();
        }

        @Override
        public int getViewTypeCount() {
            return MenuItemType.values().length;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}

package com.darkrockstudios.apps.ringmyphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MainActivity extends BillingActivity implements BillingActivity.ProStatusListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ABOUT_FRAGMENT_TAG = "AboutFragment";

    private boolean m_showPurchaseDialog;

    @InjectView(R.id.listView)
    ListView m_listView;

    private MenuAdapter m_menuAdapter;
    private TimeReceiver m_timeReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setProStatusListener(this);

        m_menuAdapter = new MenuAdapter();
        m_listView.setAdapter(m_menuAdapter);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(final Intent intent) {
        if (intent != null) {
            if (Purchase.PURCHASE_URI.equals(intent.getData())) {
                m_showPurchaseDialog = true;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        m_timeReceiver = new TimeReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(m_timeReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (m_showPurchaseDialog && purchasePro()) {
            m_showPurchaseDialog = false;
        }
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

        unregisterReceiver(m_timeReceiver);
        m_timeReceiver = null;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final boolean handled;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_about:
                showAbout();
                handled = true;
                break;
            case R.id.action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                handled = true;
            }
            break;
            default:
                handled = super.onOptionsItemSelected(item);
        }

        return handled;
    }

    private void showAbout() {
        AboutFragment aboutFragment = new AboutFragment();
        aboutFragment.show(getFragmentManager(), ABOUT_FRAGMENT_TAG);
    }

    public void onStopClicked(final View v) {
        Log.i(TAG, "Stopping RingerService");
        Crouton.makeText(this, R.string.stop_button_response, Style.CONFIRM, Configuration.DURATION_SHORT).show();
        stopService(new Intent(this, RingerService.class));
    }

    public void onPurchaseClicked(final View v) {
        Log.i(TAG, "Purchasing App");

        purchasePro();
    }

    @Override
    public void onProStatusUpdate(final boolean isPro) {
        m_menuAdapter.refresh();
    }

    protected void onBillingServiceConnected() {
        if (m_showPurchaseDialog) {
            m_showPurchaseDialog = !purchasePro();
        }
    }

    enum MenuItemType {
        Welcome,
        Purchase,
        Stop
    }

    private class TimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (!isPro()) {
                m_menuAdapter.refresh();
            }
        }
    }

    class MenuAdapter extends BaseAdapter {
        private List<MenuItemType> m_menuItems;

        public MenuAdapter() {
            m_menuItems = new ArrayList<>();
            refresh();
        }

        public void refresh() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    m_menuItems.clear();

                    m_menuItems.add(MenuItemType.Welcome);
                    if (!isPro()) {
                        m_menuItems.add(MenuItemType.Purchase);
                    }
                    m_menuItems.add(MenuItemType.Stop);

                    notifyDataSetChanged();
                }
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
                    case Purchase:
                        view = inflater.inflate(R.layout.row_purchase, parent, false);
                        break;
                    default:
                        view = null;
                        break;
                }
            } else {
                view = convertView;
            }

            if (type == MenuItemType.Purchase) {
                setCountDown((TextView) view.findViewById(R.id.purchase_app_count_down));
            }

            return view;
        }

        private void setCountDown(final TextView countDownView) {
            long timeRemaining = Purchase.trialTimeRemaining(MainActivity.this);

            if (timeRemaining > 0) {
                final long days = TimeUnit.MILLISECONDS.toDays(timeRemaining);
                timeRemaining -= TimeUnit.DAYS.toMillis(days);

                final long hours = TimeUnit.MILLISECONDS.toHours(timeRemaining);
                timeRemaining -= TimeUnit.HOURS.toMillis(hours);

                final long minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemaining);

                countDownView.setText(getString(R.string.purchase_app_countdown, days, hours, minutes));
            } else {
                countDownView.setText(getString(R.string.purchase_app_countdown_expired));
            }
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

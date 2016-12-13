package jp.realglobe.sugo.actor.arducopter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Random;

import jp.realglobe.sugo.actor.Actor;
import jp.realglobe.sugo.module.android.arducopter.ArduCopter;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();

    private Actor actor;
    private ArduCopter module;

    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初回に actor ID を生成する
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String actorSuffix = preferences.getString(getString(R.string.key_actor_id), null);
        if (actorSuffix == null) {
            preferences.edit().putString(getString(R.string.key_actor_id), String.valueOf(Math.abs((new Random(System.currentTimeMillis())).nextInt()))).apply();
        }


        this.module = new ArduCopter(getString(R.string.module_name), new Handler(), getApplicationContext());

        this.startButton = (Button) findViewById(R.id.button_start);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
        this.module.close();
    }

    private void disconnect() {
        this.module.disconnect();
        if (this.actor != null) {
            this.actor.disconnect();
            this.actor = null;
            this.changeToUnconnectState();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        if (item.getItemId() == R.id.item_disconnect) {
            disconnectAfterDialog();
        }
        return true;
    }

    private void disconnectAfterDialog() {
        if (this.actor != null) {
            (new DisconnectDialog()).show(getFragmentManager(), "dialog");
        }
    }

    /**
     * 切断ダイアログ
     */
    public static class DisconnectDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final MainActivity activity = (MainActivity) getActivity();
            return (new AlertDialog.Builder(activity))
                    .setTitle("切断しますか？")
                    .setPositiveButton("切断する", (dialog, which) -> {
                        activity.disconnect();
                    })
                    .create();
        }
    }

    public void onStartButtonTapped(View view) {
        startActor();

        this.startButton.post(this::changeToConnectingState);
    }

    private void startActor() {
        if (this.actor != null) {
            Log.i(LOG_TAG, "Already actor started");
            return;
        }

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String actorKey = getString(R.string.actor_key_prefix) + preferences.getString(getString(R.string.key_actor_id), getString(R.string.default_actor_id));
        final String actorName = MainActivity.class.getName();
        final String actorDescription = getString(R.string.actor_description);
        this.actor = new Actor(actorKey, actorName, actorDescription);

        final String moduleName = getString(R.string.module_name);
        final String moduleVersion;
        try {
            moduleVersion = getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        final String moduleDescription = getString(R.string.module_description);
        this.actor.addModule(moduleName, moduleVersion, moduleDescription, this.module);

        this.actor.setOnConnect(() -> this.startButton.post(this::changeToConnectedState));

        final String hub = preferences.getString(getString(R.string.key_hub), getString(R.string.default_hub));
        this.actor.connect(hub);
    }

    private void changeToConnectingState() {
        this.startButton.setEnabled(false);
        this.startButton.setText(getString(R.string.button_start_connecting));
    }

    private void changeToConnectedState() {
        this.startButton.setEnabled(false);
        this.startButton.setText(getString(R.string.button_start_connected));
    }

    private void changeToUnconnectState() {
        this.startButton.setEnabled(true);
        this.startButton.setText(getString(R.string.button_start));
    }

}

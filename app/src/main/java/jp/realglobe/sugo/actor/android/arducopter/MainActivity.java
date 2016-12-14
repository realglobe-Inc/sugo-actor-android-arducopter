package jp.realglobe.sugo.actor.android.arducopter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import jp.realglobe.sugo.actor.Actor;
import jp.realglobe.sugo.module.android.arducopter.ArduCopter;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();

    private static final int PERMISSION_REQUEST_CODE = 19807;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

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

        checkPermission();
    }

    /**
     * 必要な許可を取得しているか調べて、取得していなかったら要求する
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 位置情報には許可が必要。
            requestPermissions(REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
        } else {
            showPermissionStatus(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }

        final Set<String> required = new HashSet<>(Arrays.asList(REQUIRED_PERMISSIONS));
        for (int i = 0; i < permissions.length; i++) {
            if (!required.contains(permissions[i])) {
                continue;
            }
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            required.remove(permissions[i]);
        }

        showPermissionStatus(required.isEmpty());
    }

    /**
     * 許可の取得状態を表示する
     *
     * @param allowed 取得できているなら true
     */
    private void showPermissionStatus(boolean allowed) {
        final String message;
        if (allowed) {
            message = "適切な情報を利用できます";
        } else {
            message = "適切な情報を利用できません\nメニューから許可設定を行ってください";
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
            this.changeToDisconnectState();
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
        } else if (item.getItemId() == R.id.item_allow) {
            checkPermission();
        } else if (item.getItemId() == R.id.item_disconnect) {
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
                    .setPositiveButton("切断する", (dialog, which) -> activity.disconnect())
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

    private void changeToDisconnectState() {
        this.startButton.setEnabled(true);
        this.startButton.setText(getString(R.string.button_start));
    }

}

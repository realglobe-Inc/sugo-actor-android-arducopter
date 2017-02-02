package jp.realglobe.sugo.actor.android.arducopter;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getName();

    private static final int PERMISSION_REQUEST_CODE = 19807;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初回に actor ID を生成する
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String actorSuffix = preferences.getString(getString(R.string.key_actor_key), null);
        if (actorSuffix == null) {
            preferences.edit().putString(getString(R.string.key_actor_key), String.valueOf(Math.abs((new Random(System.currentTimeMillis())).nextInt()))).apply();
        }

        this.startButton = (Button) findViewById(R.id.button_start);

        checkPermission();
        checkActorRunning();
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
        } else if (item.getItemId() == R.id.item_stop) {
            disconnectAfterDialog();
        }
        return true;
    }

    private void disconnectAfterDialog() {
        if (checkActorRunning()) {
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
                    .setTitle("停止しますか？")
                    .setPositiveButton("停止する", (dialog, which) -> {
                        activity.stopActor();
                        activity.changeToNotRunningState();
                    })
                    .create();
        }
    }

    public void onStartButtonTapped(View view) {
        final Map<String, UsbDevice> devices = ((UsbManager) getSystemService(Context.USB_SERVICE)).getDeviceList();
        if (devices.isEmpty()) {
            startActor();
        } else {
            checkUsbPermission(devices.values());
        }

        this.startButton.post(this::changeToRunningState);
    }


    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Toast.makeText(MainActivity.this, device.getDeviceName() + " を使えます", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, device.getDeviceName() + " は使えません", Toast.LENGTH_LONG).show();
                    }
                    startActor();
                }
            }
        }
    };

    private void checkUsbPermission(Collection<UsbDevice> devices) {
        final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        final PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
        usbManager.requestPermission((new ArrayList<>(devices)).get(0), permissionIntent);
    }

    private void startActor() {
        if (checkActorRunning()) {
            Log.i(LOG_TAG, "Already actor started");
            return;
        }


        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String actorKey = getString(R.string.actor_key_prefix) + preferences.getString(getString(R.string.key_actor_key), getString(R.string.default_actor_id));
        final String hubAddress = preferences.getString(getString(R.string.key_hub), getString(R.string.default_hub));

        final Intent intent = new Intent(this, ActorService.class);
        intent.putExtra(ActorService.KEY_ACTOR_KEY, actorKey);
        intent.putExtra(ActorService.KEY_HUB_ADDRESS, hubAddress);
        startService(intent);
    }

    private void stopActor() {
        if (!checkActorRunning()) {
            Log.i(LOG_TAG, "Actor is not running");
        }
        stopService(new Intent(this, ActorService.class));
    }

    /**
     * Actor が動いているかどうか調べて、表示を合わせる
     *
     * @return Actor が動いていたら true
     */
    private boolean checkActorRunning() {
        if (isActorRunning()) {
            changeToRunningState();
            return true;
        } else {
            changeToNotRunningState();
            return false;
        }
    }

    private boolean isActorRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ActorService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void changeToRunningState() {
        this.startButton.setEnabled(false);
        this.startButton.setText(getString(R.string.button_started));
    }

    private void changeToNotRunningState() {
        this.startButton.setEnabled(true);
        this.startButton.setText(getString(R.string.button_start));
    }

}

package jp.realglobe.sugo.actor.android.arducopter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import jp.realglobe.sugo.actor.Actor;
import jp.realglobe.sugo.module.android.arducopter.ArduCopter;

/**
 * Actor を担うサービス。
 * Created by fukuchidaisuke on 16/12/14.
 */
public class ActorService extends Service {

    private static final String LOG_TAG = ActorService.class.getName();

    static final String KEY_ACTOR_KEY = "actorKey";
    static final String KEY_HUB_ADDRESS = "hubAddress";

    static final int NOTIFICATION_ID = 29493;

    private Actor actor;
    private ArduCopter module;

    private MyHandler myHandler;

    private final class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final Bundle data = msg.getData();
            startActor(data.getString(KEY_ACTOR_KEY), data.getString(KEY_HUB_ADDRESS));
        }
    }


    public void onCreate() {
        final HandlerThread thread = new HandlerThread(getClass().getName(), Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        final Looper looper = thread.getLooper();
        this.myHandler = new MyHandler(looper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Message msg = this.myHandler.obtainMessage();
        msg.arg1 = startId;
        final Bundle data = new Bundle();
        data.putString(KEY_ACTOR_KEY, intent.getStringExtra(KEY_ACTOR_KEY));
        data.putString(KEY_HUB_ADDRESS, intent.getStringExtra(KEY_HUB_ADDRESS));
        msg.setData(data);
        myHandler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        this.module.close();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void disconnect() {
        this.module.disconnect();
        if (this.actor != null) {
            this.actor.disconnect();
            this.actor = null;
        }
    }

    private void startActor(String actorKey, String hubAddress) {
        if (this.actor != null) {
            Log.i(LOG_TAG, "Already actor started");
            return;
        }

        this.module = new ArduCopter(getString(R.string.module_name), new Handler(), getApplicationContext());

        final String actorName = ActorService.class.getName();
        final String actorDescription = getString(R.string.actor_description);
        this.actor = new Actor(actorKey, actorName, actorDescription);

        final String moduleName = getString(R.string.module_name);
        final String moduleVersion = getString(R.string.module_version);
        final String moduleDescription = getString(R.string.module_description);
        this.actor.addModule(moduleName, moduleVersion, moduleDescription, this.module);

        this.actor.setOnConnect(() -> {
            Log.i(LOG_TAG, "connected");
            return;
        });

        this.actor.connect(hubAddress);

        final Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        final Notification notification = new Notification.Builder(this)
                .setContentTitle("AHO")
                .setContentText("BAKA")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

}

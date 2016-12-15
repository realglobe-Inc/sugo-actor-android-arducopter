package jp.realglobe.sugo.actor.android.arducopter;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import jp.realglobe.sugo.actor.Actor;
import jp.realglobe.sugo.module.android.arducopter.ArduCopter;

/**
 * Actor を担うサービス。
 * Created by fukuchidaisuke on 16/12/14.
 */
public class ActorService extends IntentService {

    private static final String LOG_TAG = ActorService.class.getName();

    static final String KEY_ACTOR_KEY = "actorKey";
    static final String KEY_HUB_ADDRESS = "hubAddress";

    static final int NOTIFICATION_ID = 29493;

    private Actor actor;
    private ArduCopter module;

    public ActorService() {
        super(ActorService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startActor(intent.getStringExtra(KEY_ACTOR_KEY), intent.getStringExtra(KEY_HUB_ADDRESS));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        this.module.close();
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

        this.actor.setOnConnect(() -> Log.i(LOG_TAG, "connected"));

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

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}

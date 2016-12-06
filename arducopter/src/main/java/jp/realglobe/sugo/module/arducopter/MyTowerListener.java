package jp.realglobe.sugo.module.arducopter;

import android.os.Handler;
import android.util.Log;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;

import jp.realglobe.sugo.actor.Emitter;

/**
 * ドローンコントローラー。
 * Created by fukuchidaisuke on 16/12/06.
 */
class MyTowerListener implements TowerListener {

    private static final String LOG_TAG = MyTowerListener.class.getName();

    private final ControlTower tower;
    private final Drone drone;
    private final Handler handler;
    private final DroneListener droneListener;

    MyTowerListener(ControlTower tower, Drone drone, Handler handler, Emitter emitter) {
        this.tower = tower;
        this.drone = drone;
        this.handler = handler;
        this.droneListener = new MyDroneListener(this.drone, emitter);
    }

    @Override
    public void onTowerConnected() {
        Log.d(LOG_TAG, "Drone tower connected");
        this.tower.registerDrone(this.drone, this.handler);
        this.drone.registerDroneListener(this.droneListener);
    }

    @Override
    public void onTowerDisconnected() {
        Log.d(LOG_TAG, "Drone tower disconnected");
        //this.tower.unregisterDrone(this.drone);
        this.drone.unregisterDroneListener(this.droneListener);
    }

}

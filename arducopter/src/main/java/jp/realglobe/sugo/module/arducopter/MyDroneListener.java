package jp.realglobe.sugo.module.arducopter;

import android.os.Bundle;
import android.util.Log;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;

import jp.realglobe.sugo.actor.Emitter;

/**
 * ドローンからのデータを受け取って中継する。
 * Created by fukuchidaisuke on 16/12/06.
 */
final class MyDroneListener implements DroneListener {

    private static final String LOG_TAG = MyDroneListener.class.getName();

    private final DroneWrapper drone;
    private final Emitter emitter;

    private Object lastPosition;

    MyDroneListener(Drone drone, Emitter emitter) {
        this.drone = new DroneWrapper(drone);
        this.emitter = emitter;

        this.lastPosition = null;
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED: {
                Log.d(LOG_TAG, "Drone connected");
                emitter.emit(ArduCopter.EVENT_CONNECTED, null);
                break;
            }

            case AttributeEvent.STATE_DISCONNECTED: {
                Log.d(LOG_TAG, "Drone disconnected");
                emitter.emit(ArduCopter.EVENT_DISCONNECTED, null);
                break;
            }

            case AttributeEvent.TYPE_UPDATED: {
                final Object data = this.drone.getType();
                Log.d(LOG_TAG, "Drone type updated: " + data);
                emitter.emit(ArduCopter.EVENT_TYPE, data);
                break;
            }

            case AttributeEvent.STATE_VEHICLE_MODE: {
                final Object data = this.drone.getMode();
                Log.d(LOG_TAG, "Drone mode updated: " + data);
                emitter.emit(ArduCopter.EVENT_MODE, data);
                break;
            }

            case AttributeEvent.STATE_ARMING: {
                if (this.drone.isArmed()) {
                    Log.d(LOG_TAG, "Drone armed");
                    emitter.emit(ArduCopter.EVENT_ARMED, null);
                } else {
                    Log.d(LOG_TAG, "Drone disarmed");
                    emitter.emit(ArduCopter.EVENT_DISARMED, null);
                }
                break;
            }

            case AttributeEvent.SPEED_UPDATED: {
                final Object data = this.drone.getSpeed();
                Log.d(LOG_TAG, "Drone speed updated: " + data);
                emitter.emit(ArduCopter.EVENT_SPEED, data);
                break;
            }

            case AttributeEvent.BATTERY_UPDATED: {
                final Object data = this.drone.getBattery();
                Log.d(LOG_TAG, "Drone battery updated: " + data);
                emitter.emit(ArduCopter.EVENT_BATTERY, data);
                break;
            }

            case AttributeEvent.HOME_UPDATED: {
                final Object data = this.drone.getHome();
                Log.d(LOG_TAG, "Drone home updated: " + data);
                emitter.emit(ArduCopter.EVENT_HOME, data);
                break;
            }

            case AttributeEvent.ALTITUDE_UPDATED: {
                final Object data = this.drone.getPosition();
                if (data.equals(this.lastPosition)) {
                    return;
                }
                this.lastPosition = data;
                Log.d(LOG_TAG, "Drone altitude updated: " + data);
                emitter.emit(ArduCopter.EVENT_POSITION, data);
                break;
            }

            case AttributeEvent.GPS_POSITION: {
                final Object data = this.drone.getPosition();
                if (data.equals(this.lastPosition)) {
                    return;
                }
                this.lastPosition = data;
                Log.d(LOG_TAG, "Drone GPS position updated: " + data);
                emitter.emit(ArduCopter.EVENT_POSITION, data);
                break;
            }

            case AttributeEvent.MISSION_RECEIVED: {
                final Object data = this.drone.getMission();
                Log.d(LOG_TAG, "Drone mission received: " + data);
                emitter.emit(ArduCopter.EVENT_MISSION, data);
                break;
            }

            case AttributeEvent.MISSION_SENT: {
                Log.d(LOG_TAG, "Drone mission saved");
                emitter.emit(ArduCopter.EVENT_MISSION_SAVED, null);
                break;
            }

            case AttributeEvent.MISSION_ITEM_REACHED: {
                final Object data = this.drone.getReachedCommand();
                Log.d(LOG_TAG, "Drone mission command reached: " + data);
                emitter.emit(ArduCopter.EVENT_COMMAND_REACHED, data);
                break;
            }

            default: {
                Log.d(LOG_TAG, "Drone event: " + event);
                break;
            }
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        Log.d(LOG_TAG, "Drone interrupted");
    }

}

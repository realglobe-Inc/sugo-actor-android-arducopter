package jp.realglobe.sugo.module.arducopter;

import android.os.Bundle;
import android.util.Log;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;

import jp.realglobe.sugo.actor.Emitter;

/**
 * ドローンからのデータを受け取って中継する。
 * Created by fukuchidaisuke on 16/12/06.
 */
class MyDroneListener implements DroneListener {

    private static final String LOG_TAG = MyDroneListener.class.getName();

    private final Drone drone;
    private final Emitter emitter;

    MyDroneListener(Drone drone, Emitter emitter) {
        this.drone = drone;
        this.emitter = emitter;
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        switch (event) {
            case AttributeEvent.STATE_CONNECTED: {
                Log.d(LOG_TAG, "Drone connected state: " + this.drone.isConnected());
                emitter.emit(ArduCopter.EVENT_CONNECTED, this.drone.isConnected());
                break;
            }

            case AttributeEvent.STATE_DISCONNECTED: {
                Log.d(LOG_TAG, "Drone disconnected state: " + (!this.drone.isConnected()));
                emitter.emit(ArduCopter.EVENT_DISCONNECTED, !this.drone.isConnected());
                break;
            }

            case AttributeEvent.STATE_VEHICLE_MODE: {
                final State state = this.drone.getAttribute(AttributeType.STATE);
                Log.d(LOG_TAG, "Drone mode state: " + state.getVehicleMode().getLabel());
                emitter.emit(ArduCopter.EVENT_VEHICLE_MODE, state.getVehicleMode().getLabel());
                break;
            }

            case AttributeEvent.STATE_ARMING: {
                final State state = this.drone.getAttribute(AttributeType.STATE);
                Log.d(LOG_TAG, "Drone arming state: " + state.isArmed());
                emitter.emit(ArduCopter.EVENT_ARMING, state.isArmed());
                break;
            }

            case AttributeEvent.TYPE_UPDATED: {
                final Type type = this.drone.getAttribute(AttributeType.TYPE);
                Log.d(LOG_TAG, "Drone type updated: " + type.getDroneType());
                emitter.emit(ArduCopter.EVENT_DRONE_TYPE, type.getDroneType());
                break;
            }

            case AttributeEvent.SPEED_UPDATED: {
                final Speed speed = this.drone.getAttribute(AttributeType.SPEED);
                Log.d(LOG_TAG, "Drone speed updated: " + speed.getGroundSpeed());
                emitter.emit(ArduCopter.EVENT_SPEED, speed.getGroundSpeed());
                break;
            }

            case AttributeEvent.BATTERY_UPDATED: {
                final Battery battery = this.drone.getAttribute(AttributeType.BATTERY);
                Log.d(LOG_TAG, "Drone battery updated: " + battery.getBatteryRemain());
                emitter.emit(ArduCopter.EVENT_BATTERY, battery.getBatteryRemain());
                break;
            }

            case AttributeEvent.HOME_UPDATED: {
                final Home home = this.drone.getAttribute(AttributeType.HOME);
                Log.d(LOG_TAG, "Drone home updated: " + home.getCoordinate());
                emitter.emit(ArduCopter.EVENT_HOME, Coordinates.encode(home.getCoordinate()));
                break;
            }

            case AttributeEvent.ALTITUDE_UPDATED: {
                final Altitude altitude = this.drone.getAttribute(AttributeType.ALTITUDE);
                Log.d(LOG_TAG, "Drone altitude updated: " + altitude.getAltitude());
                emitter.emit(ArduCopter.EVENT_ALTITUDE, altitude.getAltitude());
                break;
            }

            case AttributeEvent.GPS_POSITION: {
                final Gps gps = this.drone.getAttribute(AttributeType.GPS);
                Log.d(LOG_TAG, "Drone GPS position: " + gps.getPosition());
                emitter.emit(ArduCopter.EVENT_GPS_POSITION, Coordinates.encode(gps.getPosition()));
                break;
            }

            case AttributeEvent.MISSION_RECEIVED: {
                final Mission mission = new Mission();
                Log.d(LOG_TAG, "Drone mission received: " + mission);
                emitter.emit(ArduCopter.EVENT_MISSION, Missions.encode(mission));
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

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

import java.util.HashMap;
import java.util.Map;

import jp.realglobe.sugo.actor.Emitter;

/**
 * ドローンからのデータを受け取って中継する。
 * Created by fukuchidaisuke on 16/12/06.
 */
final class MyDroneListener implements DroneListener {

    private static final String LOG_TAG = MyDroneListener.class.getName();

    private static final String KEY_TYPE = "type";
    private static final String KEY_FIRMWARE = "firmware";
    private static final String KEY_VERSION = "version";
    private static final String KEY_MODE = "mode";
    private static final String KEY_ARMING = "arming";
    private static final String KEY_GROUND = "ground";
    private static final String KEY_VERTICAL = "vertical";
    private static final String KEY_AIR = "air";
    private static final String KEY_REMAIN = "remain";
    private static final String KEY_VOLTAGE = "voltage";
    private static final String KEY_CURRENT = "current";
    private static final String KEY_COORDINATE = "coordinate";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_COMMANDS = "commands";
    private static final String KEY_INDEX = "index";

    private final Drone drone;
    private final Emitter emitter;

    MyDroneListener(Drone drone, Emitter emitter) {
        this.drone = drone;
        this.emitter = emitter;
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        // ArduCopter のコメントと同期すること。
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
                final Type type = this.drone.getAttribute(AttributeType.TYPE);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_TYPE, type.getDroneType());
                data.put(KEY_FIRMWARE, type.getFirmware().getLabel());
                data.put(KEY_VERSION, type.getFirmwareVersion());
                Log.d(LOG_TAG, "Drone type updated: " + data);
                emitter.emit(ArduCopter.EVENT_TYPE, data);
                break;
            }

            case AttributeEvent.STATE_VEHICLE_MODE: {
                final State state = this.drone.getAttribute(AttributeType.STATE);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_MODE, state.getVehicleMode().getLabel());
                Log.d(LOG_TAG, "Drone mode updated: " + data);
                emitter.emit(ArduCopter.EVENT_MODE, data);
                break;
            }

            case AttributeEvent.STATE_ARMING: {
                final State state = this.drone.getAttribute(AttributeType.STATE);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_ARMING, state.isArmed());
                Log.d(LOG_TAG, "Drone arming updated: " + data);
                emitter.emit(ArduCopter.EVENT_ARMING, data);
                break;
            }

            case AttributeEvent.SPEED_UPDATED: {
                final Speed speed = this.drone.getAttribute(AttributeType.SPEED);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_GROUND, speed.getGroundSpeed());
                data.put(KEY_VERTICAL, speed.getVerticalSpeed());
                data.put(KEY_AIR, speed.getAirSpeed());
                Log.d(LOG_TAG, "Drone speed updated: " + data);
                emitter.emit(ArduCopter.EVENT_SPEED, data);
                break;
            }

            case AttributeEvent.BATTERY_UPDATED: {
                final Battery battery = this.drone.getAttribute(AttributeType.BATTERY);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_REMAIN, battery.getBatteryRemain());
                data.put(KEY_VOLTAGE, battery.getBatteryVoltage());
                data.put(KEY_CURRENT, battery.getBatteryCurrent());
                Log.d(LOG_TAG, "Drone battery updated: " + data);
                emitter.emit(ArduCopter.EVENT_BATTERY, data);
                break;
            }

            case AttributeEvent.HOME_UPDATED: {
                final Home home = this.drone.getAttribute(AttributeType.HOME);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_COORDINATE, Coordinates.encode(home.getCoordinate()));
                Log.d(LOG_TAG, "Drone home updated: " + data);
                emitter.emit(ArduCopter.EVENT_HOME, data);
                break;
            }

            case AttributeEvent.ALTITUDE_UPDATED: {
                final Altitude altitude = this.drone.getAttribute(AttributeType.ALTITUDE);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_ALTITUDE, altitude.getAltitude());
                Log.d(LOG_TAG, "Drone altitude updated: " + data);
                emitter.emit(ArduCopter.EVENT_ALTITUDE, data);
                break;
            }

            case AttributeEvent.GPS_POSITION: {
                final Gps gps = this.drone.getAttribute(AttributeType.GPS);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_COORDINATE, Coordinates.encode(gps.getPosition()));
                Log.d(LOG_TAG, "Drone GPS position updated: " + data);
                emitter.emit(ArduCopter.EVENT_GPS_POSITION, data);
                break;
            }

            case AttributeEvent.MISSION_RECEIVED: {
                final Mission mission = this.drone.getAttribute(AttributeType.MISSION);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_COMMANDS, Missions.encode(mission));
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
                final Mission mission = this.drone.getAttribute(AttributeType.MISSION);
                final Map<String, Object> data = new HashMap<>();
                data.put(KEY_INDEX, mission.getCurrentMissionItem());
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

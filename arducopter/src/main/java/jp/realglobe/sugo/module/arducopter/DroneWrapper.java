package jp.realglobe.sugo.module.arducopter;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Attitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ドローンの情報を JSON 互換形式で取得するためのラッパー。
 * Created by fukuchidaisuke on 16/12/12.
 */
final class DroneWrapper {

    // 返り値は ArduCopter のコメントと同期すること。

    private static final String KEY_TYPE = "type";
    private static final String KEY_FIRMWARE = "firmware";
    private static final String KEY_VERSION = "version";
    private static final String KEY_MODE = "mode";
    private static final String KEY_GROUND = "ground";
    private static final String KEY_VERTICAL = "vertical";
    private static final String KEY_AIR = "air";
    private static final String KEY_REMAIN = "remain";
    private static final String KEY_VOLTAGE = "voltage";
    private static final String KEY_CURRENT = "current";
    private static final String KEY_COORDINATE = "coordinate";
    private static final String KEY_COMMANDS = "commands";
    private static final String KEY_PITCH = "pitch";
    private static final String KEY_PITCH_SPEED = "pitchSpeed";
    private static final String KEY_ROLL = "roll";
    private static final String KEY_ROLL_SPEED = "rollSpeed";
    private static final String KEY_YAW = "yaw";
    private static final String KEY_YAW_SPEED = "yawSpeed";
    private static final String KEY_INDEX = "index";

    private final Drone drone;

    DroneWrapper(Drone drone) {
        this.drone = drone;
    }

    boolean isConnected() {
        final State state = this.drone.getAttribute(AttributeType.STATE);
        return state.isConnected();
    }

    boolean isArmed() {
        final State state = this.drone.getAttribute(AttributeType.STATE);
        return state.isArmed();
    }

    boolean isFlying() {
        final State state = this.drone.getAttribute(AttributeType.STATE);
        return state.isFlying();
    }

    Map<String, Object> getType() {
        final Type type = this.drone.getAttribute(AttributeType.TYPE);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_TYPE, type.getDroneType());
        data.put(KEY_FIRMWARE, type.getFirmware().getLabel());
        data.put(KEY_VERSION, type.getFirmwareVersion());
        return data;
    }

    Map<String, Object> getMode() {
        final State state = this.drone.getAttribute(AttributeType.STATE);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_MODE, state.getVehicleMode().getLabel());
        return data;
    }

    Map<String, Object> getSpeed() {
        final Speed speed = this.drone.getAttribute(AttributeType.SPEED);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_GROUND, speed.getGroundSpeed());
        data.put(KEY_VERTICAL, speed.getVerticalSpeed());
        data.put(KEY_AIR, speed.getAirSpeed());
        return data;
    }

    Map<String, Object> getBattery() {
        final Battery battery = this.drone.getAttribute(AttributeType.BATTERY);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_REMAIN, battery.getBatteryRemain());
        data.put(KEY_VOLTAGE, battery.getBatteryVoltage());
        data.put(KEY_CURRENT, battery.getBatteryCurrent());
        return data;
    }

    Map<String, Object> getHome() {
        final Home home = this.drone.getAttribute(AttributeType.HOME);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_COORDINATE, Coordinates.encode(home.getCoordinate()));
        return data;
    }

    Map<String, Object> getPosition() {
        final Gps gps = this.drone.getAttribute(AttributeType.GPS);
        final LatLong latLong = gps.getPosition();
        final Altitude altitude = this.drone.getAttribute(AttributeType.ALTITUDE);
        final List<Object> coordinate = Arrays.asList(latLong.getLatitude(), latLong.getLongitude(), altitude.getAltitude());
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_COORDINATE, coordinate);
        return data;
    }

    Map<String, Object> getAttitude() {
        final Attitude attitude = this.drone.getAttribute(AttributeType.ATTITUDE);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_PITCH, attitude.getPitch());
        data.put(KEY_PITCH_SPEED, attitude.getPitchSpeed());
        data.put(KEY_ROLL, attitude.getRoll());
        data.put(KEY_ROLL_SPEED, attitude.getRollSpeed());
        data.put(KEY_YAW, attitude.getYaw());
        data.put(KEY_YAW_SPEED, attitude.getYawSpeed());
        return data;
    }

    Map<String, Object> getMission() {
        final Mission mission = this.drone.getAttribute(AttributeType.MISSION);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_COMMANDS, Missions.encode(mission));
        return data;
    }

    Map<String, Object> getReachedCommand() {
        final Mission mission = this.drone.getAttribute(AttributeType.MISSION);
        final Map<String, Object> data = new HashMap<>();
        data.put(KEY_INDEX, mission.getCurrentMissionItem());
        return data;
    }

}

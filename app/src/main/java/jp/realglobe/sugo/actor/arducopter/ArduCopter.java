package jp.realglobe.sugo.actor.arducopter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.property.Altitude;
import com.o3dr.services.android.lib.drone.property.Battery;
import com.o3dr.services.android.lib.drone.property.Gps;
import com.o3dr.services.android.lib.drone.property.Home;
import com.o3dr.services.android.lib.drone.property.Speed;
import com.o3dr.services.android.lib.drone.property.State;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import java.util.List;
import java.util.Map;

import jp.realglobe.sugo.actor.Emitter;
import jp.realglobe.sugo.actor.ModuleMethod;

/**
 * arduCopter モジュール
 * Created by fukuchidaisuke on 16/11/28.
 */

final class ArduCopter extends Emitter implements Cloneable {

    private static final String LOG_TAG = ArduCopter.class.getName();

    private static final String CONNECT_TYPE_UDP = "UDP";
    private static final String CONNECT_TYPE_USB = "USB";

    private final ControlTower tower;
    private final Drone drone;

    private final ControlApi control;
    private final VehicleApi vehicle;
    private final MissionApi mision;


    ArduCopter(String name, Handler handler, Context context) {
        super(name);

        this.tower = new ControlTower(context);
        this.drone = new Drone(context);

        this.control = ControlApi.getApi(this.drone);
        this.vehicle = VehicleApi.getApi(this.drone);
        this.mision = MissionApi.getApi(this.drone);


        this.tower.connect(new MyTowerListener(this.tower, this.drone, handler, this));
    }


    void close() {
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        }
        if (this.tower.isTowerConnected()) {
            this.tower.unregisterDrone(this.drone);
            this.tower.disconnect();
        }
    }

    @ModuleMethod
    public void connect(String type, String address) {
        if (this.drone.isConnected()) {
            Log.w(LOG_TAG, "Drone already connected");
            return;
        }
        final ConnectionParameter connectParams = parseConnectionParameter(type, address);
        Log.i(LOG_TAG, "Connect " + connectParams);
        this.drone.connect(connectParams);
    }

    private static ConnectionParameter parseConnectionParameter(String type, String address) {
        switch (type.toUpperCase()) {
            case CONNECT_TYPE_UDP: {
                if (address == null || address.isEmpty()) {
                    return ConnectionParameter.newUdpConnection(null);
                } else {
                    final UdpInfo udp = UdpInfo.parse(address);
                    final int localPort = udp.getLocalPort() > 0 ? udp.getLocalPort() : ConnectionType.DEFAULT_UDP_SERVER_PORT;
                    if (udp.getRemoteHost() == null || udp.getRemoteHost().isEmpty()) {
                        return ConnectionParameter.newUdpConnection(localPort, null);
                    } else {
                        final String remoteHost = udp.getRemoteHost();
                        final int remotePort = udp.getRemotePort() > 0 ? udp.getRemotePort() : ConnectionType.DEFAULT_UDP_SERVER_PORT;
                        return ConnectionParameter.newUdpWithPingConnection(localPort, remoteHost, remotePort, new byte[]{}, null);
                    }
                }
            }
            case CONNECT_TYPE_USB: {
                if (address == null || address.isEmpty()) {
                    return ConnectionParameter.newUsbConnection(null);
                } else {
                    final int baudRate = Integer.parseInt(address);
                    return ConnectionParameter.newUsbConnection(baudRate, null);
                }
            }
            default: {
                throw new IllegalArgumentException("unsupported connect type: " + type);
            }
        }
    }

    @ModuleMethod
    public void disconnect() {
        if (!this.drone.isConnected()) {
            Log.w(LOG_TAG, "Drone is not connecting");
            return;
        }
        this.drone.disconnect();
    }

    @ModuleMethod
    public void climbTo(double altitude) {
        this.control.climbTo(altitude);
    }

    @ModuleMethod
    public void goTo(double latitude, double longitude) {
        this.control.goTo(new LatLong(latitude, longitude), true, null);
    }

    @ModuleMethod
    public void pauseAtCurrentLocation() {
        this.control.pauseAtCurrentLocation(null);
    }

    @ModuleMethod
    public void takeoff(double altitude) {
        this.control.takeoff(altitude, null);
    }

    @ModuleMethod
    public void land() {
        this.vehicle.setVehicleMode(VehicleMode.COPTER_LAND);
    }

    @ModuleMethod
    public void returnToLaunch() {
        this.vehicle.setVehicleMode(VehicleMode.COPTER_RTL);
    }

    @ModuleMethod
    public void turnTo(double targetAngle, double turnSpeed, boolean isRelative) {
        this.control.turnTo((float) targetAngle, (float) turnSpeed, isRelative, null);
    }

    @ModuleMethod
    public void arm(boolean arm) {
        this.vehicle.arm(arm);
    }

    @ModuleMethod
    public void setVehicleMode(String newMode) {
        this.vehicle.setVehicleMode(VehicleMode.valueOf(newMode));
    }

    @ModuleMethod
    public void setVehicleHome(double latitude, double longitude, double altitude) {
        this.vehicle.setVehicleHome(new LatLongAlt(latitude, longitude, altitude), null);
    }

    @ModuleMethod
    public void goToWaypoint(int waypoint) {
        this.mision.gotoWaypoint(waypoint, null);
    }

    @ModuleMethod
    public List<Map<String, Object>> loadMission() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ModuleMethod
    public void saveMission(List<Map<String, Object>> mission) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @ModuleMethod
    public void startMission(boolean forceModeChange, boolean forceArm) {
        this.mision.startMission(forceModeChange, forceArm, null);
    }

}

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

class MyDroneListener implements DroneListener {

    private static final String LOG_TAG = MyDroneListener.class.getName();

    private static final String EVENT_CONNECTED = "connected";
    private static final String EVENT_DISCONNECTED = "disconnected";
    private static final String EVENT_VEHICLE_MODE = "vehicleMode";
    private static final String EVENT_DRONE_TYPE = "droneType";
    private static final String EVENT_ARMING = "arming";
    private static final String EVENT_SPEED = "speed";
    private static final String EVENT_BATTERY = "battery";
    private static final String EVENT_HOME = "home";
    private static final String EVENT_ALTITUDE = "altitude";
    private static final String EVENT_GPS_POSITION = "gpsPosition";

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
                emitter.emit(EVENT_CONNECTED, this.drone.isConnected());
                break;
            }

            case AttributeEvent.STATE_DISCONNECTED: {
                Log.d(LOG_TAG, "Drone disconnected state: " + (!this.drone.isConnected()));
                emitter.emit(EVENT_DISCONNECTED, !this.drone.isConnected());
                break;
            }

            case AttributeEvent.STATE_VEHICLE_MODE: {
                final State state = this.drone.getAttribute(AttributeType.STATE);
                Log.d(LOG_TAG, "Drone mode state: " + state.getVehicleMode().getLabel());
                emitter.emit(EVENT_VEHICLE_MODE, state.getVehicleMode().getLabel());
                break;
            }

            case AttributeEvent.STATE_ARMING: {
                final State state = this.drone.getAttribute(AttributeType.STATE);
                Log.d(LOG_TAG, "Drone arming state: " + state.isArmed());
                emitter.emit(EVENT_ARMING, state.isArmed());
                break;
            }

            case AttributeEvent.TYPE_UPDATED: {
                final Type type = this.drone.getAttribute(AttributeType.TYPE);
                Log.d(LOG_TAG, "Drone type updated: " + type.getDroneType());
                emitter.emit(EVENT_DRONE_TYPE, type.getDroneType());
                break;
            }

            case AttributeEvent.SPEED_UPDATED: {
                final Speed speed = this.drone.getAttribute(AttributeType.SPEED);
                Log.d(LOG_TAG, "Drone speed updated: " + speed.getGroundSpeed());
                emitter.emit(EVENT_SPEED, speed.getGroundSpeed());
                break;
            }

            case AttributeEvent.BATTERY_UPDATED: {
                final Battery battery = this.drone.getAttribute(AttributeType.BATTERY);
                Log.d(LOG_TAG, "Drone battery updated: " + battery.getBatteryRemain());
                emitter.emit(EVENT_BATTERY, battery.getBatteryRemain());
                break;
            }

            case AttributeEvent.HOME_UPDATED: {
                final Home home = this.drone.getAttribute(AttributeType.HOME);
                Log.d(LOG_TAG, "Drone home updated: " + home.getCoordinate());
                emitter.emit(EVENT_HOME, encode(home.getCoordinate()));
                break;
            }

            case AttributeEvent.ALTITUDE_UPDATED: {
                final Altitude altitude = this.drone.getAttribute(AttributeType.ALTITUDE);
                Log.d(LOG_TAG, "Drone altitude updated: " + altitude.getAltitude());
                emitter.emit(EVENT_ALTITUDE, altitude.getAltitude());
                break;
            }

            case AttributeEvent.GPS_POSITION: {
                final Gps gps = this.drone.getAttribute(AttributeType.GPS);
                Log.d(LOG_TAG, "Drone GPS position: " + gps.getPosition());
                emitter.emit(EVENT_GPS_POSITION, encode(gps.getPosition()));
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

    private Object encode(LatLong latLong) {
        return new Object[]{latLong.getLatitude(), latLong.getLongitude()};
    }

    private Object encode(LatLongAlt latLongAlt) {
        return new Object[]{latLongAlt.getLatitude(), latLongAlt.getLongitude(), latLongAlt.getAltitude()};
    }

}

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

import jp.realglobe.sugo.actor.ModuleMethod;

/**
 * arduCopter モジュール
 * Created by fukuchidaisuke on 16/11/28.
 */

final class ArduCopter implements Cloneable {

    private static final String LOG_TAG = ArduCopter.class.getName();

    private static final String CONNECT_TYPE_UDP = "UDP";
    private static final String CONNECT_TYPE_USB = "USB";

    private final Handler handler;

    private final ControlTower tower;
    private final Drone drone;

    private final ControlApi control;
    private final VehicleApi vehicle;
    private final MissionApi mision;


    ArduCopter(Handler handler, Context context) {
        this.handler = handler;

        this.tower = new ControlTower(context);
        this.drone = new Drone(context);

        this.control = ControlApi.getApi(this.drone);
        this.vehicle = VehicleApi.getApi(this.drone);
        this.mision = MissionApi.getApi(this.drone);


        this.tower.connect(new MyTowerListener());
    }

    private class MyTowerListener implements TowerListener {

        @Override
        public void onTowerConnected() {
            Log.d(LOG_TAG, "Drone tower connected");
            ArduCopter.this.tower.registerDrone(ArduCopter.this.drone, ArduCopter.this.handler);
            ArduCopter.this.drone.registerDroneListener(new MyDroneListener());
        }

        @Override
        public void onTowerDisconnected() {
            Log.d(LOG_TAG, "Drone tower disconnected");
        }
    }

    private class MyDroneListener implements DroneListener {

        @Override
        public void onDroneEvent(String event, Bundle extras) {
            switch (event) {
                case AttributeEvent.STATE_CONNECTED:
                    Log.d(LOG_TAG, "Drone connected state: " + ArduCopter.this.drone.isConnected());
                    break;

                case AttributeEvent.STATE_DISCONNECTED:
                    Log.d(LOG_TAG, "Drone disconnected state: " + (!ArduCopter.this.drone.isConnected()));
                    break;

                case AttributeEvent.STATE_VEHICLE_MODE:
                    final State state = ArduCopter.this.drone.getAttribute(AttributeType.STATE);
                    Log.d(LOG_TAG, "Drone mode state: " + state.getVehicleMode());
                    break;

                case AttributeEvent.TYPE_UPDATED:
                    final Type type = ArduCopter.this.drone.getAttribute(AttributeType.TYPE);
                    Log.d(LOG_TAG, "Drone type updated: " + type.getDroneType());
                    break;

                case AttributeEvent.SPEED_UPDATED:
                    final Speed speed = ArduCopter.this.drone.getAttribute(AttributeType.SPEED);
                    Log.d(LOG_TAG, "Drone speed updated: " + speed.getGroundSpeed());
                    break;

                case AttributeEvent.BATTERY_UPDATED:
                    final Battery battery = ArduCopter.this.drone.getAttribute(AttributeType.BATTERY);
                    Log.d(LOG_TAG, "Drone battery updated: " + battery.getBatteryRemain());
                    break;

                case AttributeEvent.HOME_UPDATED:
                    final Home home = ArduCopter.this.drone.getAttribute(AttributeType.HOME);
                    Log.d(LOG_TAG, "Drone home updated: " + home.getCoordinate());
                    break;

                case AttributeEvent.ALTITUDE_UPDATED:
                    final Altitude altitude = ArduCopter.this.drone.getAttribute(AttributeType.ALTITUDE);
                    Log.d(LOG_TAG, "Drone altitude updated: " + altitude.getAltitude());
                    break;

                case AttributeEvent.GPS_POSITION:
                    final Gps gps = ArduCopter.this.drone.getAttribute(AttributeType.GPS);
                    Log.d(LOG_TAG, "Drone GPS position: " + gps.getPosition());
                    break;

                default:
                    Log.d(LOG_TAG, "Drone event: " + event);
                    break;
            }
        }

        @Override
        public void onDroneServiceInterrupted(String errorMsg) {
            Log.d(LOG_TAG, "Drone interrupted");
        }

    }

    void close() {
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        }
        this.tower.disconnect();
    }

    @ModuleMethod
    public void connect(String type, String address) {
        if (this.drone.isConnected()) {
            Log.w(LOG_TAG, "Drone already connected");
            return;
        }
        final ConnectionParameter connectParams;
        switch (type.toUpperCase()) {
            case CONNECT_TYPE_UDP:
                if (address == null || address.isEmpty()) {
                    connectParams = ConnectionParameter.newUdpConnection(null);
                    Log.i(LOG_TAG, "Wait UDP");
                } else {
                    final UdpInfo udp = UdpInfo.parse(address);
                    final int localPort = udp.getLocalPort() > 0 ? udp.getLocalPort() : ConnectionType.DEFAULT_UDP_SERVER_PORT;
                    if (udp.getRemoteHost() == null || udp.getRemoteHost().isEmpty()) {
                        connectParams = ConnectionParameter.newUdpConnection(localPort, null);
                        Log.i(LOG_TAG, "Wait UDP " + localPort);
                    } else {
                        final String remoteHost = udp.getRemoteHost();
                        final int remotePort = udp.getRemotePort() > 0 ? udp.getRemotePort() : ConnectionType.DEFAULT_UDP_SERVER_PORT;
                        connectParams = ConnectionParameter.newUdpWithPingConnection(localPort, remoteHost, remotePort, new byte[]{}, null);
                        Log.i(LOG_TAG, "Wait UDP " + localPort + " and connect UDP " + remoteHost + ":" + remotePort);
                    }
                }
                break;
            case CONNECT_TYPE_USB:
                if (address == null || address.isEmpty()) {
                    connectParams = ConnectionParameter.newUsbConnection(null);
                    Log.i(LOG_TAG, "Connect USB");
                } else {
                    final int baudRate = Integer.parseInt(address);
                    connectParams = ConnectionParameter.newUsbConnection(baudRate, null);
                    Log.i(LOG_TAG, "Connect USB " + baudRate);
                }
                break;
            default:
                throw new IllegalArgumentException("unsupported connect type: " + type);
        }

        this.drone.connect(connectParams);
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
        this.mision.loadWaypoints();
        // TODO
        return null;
    }

    @ModuleMethod
    public void saveMission(List<Map<String, Object>> mission) {
        // TODO
    }

    @ModuleMethod
    public void startMission(boolean forceModeChange, boolean forceArm) {
        this.mision.startMission(forceModeChange, forceArm, null);
    }

}

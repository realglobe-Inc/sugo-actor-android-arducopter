package jp.realglobe.sugo.module.arducopter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import java.util.List;
import java.util.Map;

import jp.realglobe.sugo.actor.Emitter;
import jp.realglobe.sugo.actor.ModuleMethod;

/**
 * arduCopter モジュール
 * Created by fukuchidaisuke on 16/11/28.
 */
public class ArduCopter extends Emitter implements Cloneable {


    private static final String LOG_TAG = ArduCopter.class.getName();

    public static final String CONNECT_TYPE_UDP = "UDP";
    public static final String CONNECT_TYPE_USB = "USB";


    /**
     * 接続した
     */
    public static final String EVENT_CONNECTED = "connected";
    /**
     * 接続が切れた
     */
    public static final String EVENT_DISCONNECTED = "disconnected";
    /**
     * 動作モードが変わった
     */
    public static final String EVENT_VEHICLE_MODE = "vehicleMode";
    /**
     * 機種タイプが変わった
     */
    public static final String EVENT_DRONE_TYPE = "droneType";
    /**
     * プロペラの駆動状態が変わった
     */
    public static final String EVENT_ARMING = "arming";
    /**
     * 速度が変わった
     */
    public static final String EVENT_SPEED = "speed";
    /**
     * バッテリーの状態が変わった
     */
    public static final String EVENT_BATTERY = "battery";
    /**
     * 基点が変わった
     */
    public static final String EVENT_HOME = "home";
    /**
     * 高さが変わった
     */
    public static final String EVENT_ALTITUDE = "altitude";
    /**
     * GPS の示す位置が変わった
     */
    public static final String EVENT_GPS_POSITION = "gpsPosition";
    /**
     * 保存してあるミッションを読み込んだ
     */
    public static final String EVENT_MISSION = "mission";

    private final ControlTower tower;
    private final Drone drone;

    private final ControlApi control;
    private final VehicleApi vehicle;
    private final MissionApi mision;


    public ArduCopter(String name, Handler handler, Context context) {
        super(name);

        this.tower = new ControlTower(context);
        this.drone = new Drone(context);

        this.control = ControlApi.getApi(this.drone);
        this.vehicle = VehicleApi.getApi(this.drone);
        this.mision = MissionApi.getApi(this.drone);


        this.tower.connect(new MyTowerListener(this.tower, this.drone, handler, this));
    }


    public void close() {
        if (this.drone.isConnected()) {
            this.drone.disconnect();
        }
        if (this.tower.isTowerConnected()) {
            this.tower.unregisterDrone(this.drone);
            this.tower.disconnect();
        }
    }

    /**
     * ドローンにつなぐ
     * <p>
     * 引数は ("USB", "57600") や ("UDP", "192.168.1.3") など
     *
     * @param type    接続タイプ
     * @param address 詳細
     */
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

    /**
     * ドローンとの接続を切る
     */
    @ModuleMethod
    public void disconnect() {
        if (!this.drone.isConnected()) {
            Log.w(LOG_TAG, "Drone is not connecting");
            return;
        }
        this.drone.disconnect();
    }

    /**
     * 浮上
     *
     * @param altitude 浮上する高さ
     */
    @ModuleMethod
    public void climbTo(double altitude) {
        this.control.climbTo(altitude);
    }

    /**
     * 移動
     *
     * @param latitude  緯度
     * @param longitude 経度
     */
    @ModuleMethod
    public void goTo(double latitude, double longitude) {
        this.control.goTo(new LatLong(latitude, longitude), true, null);
    }

    /**
     * その場で止まる
     */
    @ModuleMethod
    public void pauseAtCurrentLocation() {
        this.control.pauseAtCurrentLocation(null);
    }

    /**
     * 離陸
     *
     * @param altitude 離陸後の目標高さ
     */
    @ModuleMethod
    public void takeoff(double altitude) {
        this.control.takeoff(altitude, null);
    }

    /**
     * 着陸
     */
    @ModuleMethod
    public void land() {
        this.vehicle.setVehicleMode(VehicleMode.COPTER_LAND);
    }

    /**
     * 離陸地点の上に帰る
     */
    @ModuleMethod
    public void returnToLaunch() {
        this.vehicle.setVehicleMode(VehicleMode.COPTER_RTL);
    }

    /**
     * 向きを変える
     *
     * @param targetAngle 角度
     * @param turnSpeed   向きを変える速度
     * @param isRelative  相対的な角度かどうか
     */
    @ModuleMethod
    public void turnTo(double targetAngle, double turnSpeed, boolean isRelative) {
        this.control.turnTo((float) targetAngle, (float) turnSpeed, isRelative, null);
    }

    /**
     * プロペラの駆動切り替え
     *
     * @param arm true ならプロペラを回す
     */
    @ModuleMethod
    public void arm(boolean arm) {
        this.vehicle.arm(arm);
    }

    /**
     * 動作モードを切り替える
     *
     * @param newMode 動作モード
     */
    @ModuleMethod
    public void setVehicleMode(String newMode) {
        this.vehicle.setVehicleMode(VehicleMode.valueOf(newMode));
    }

    /**
     * 基点を設定する
     *
     * @param latitude  緯度
     * @param longitude 経度
     * @param altitude  高さ
     */
    @ModuleMethod
    public void setVehicleHome(double latitude, double longitude, double altitude) {
        this.vehicle.setVehicleHome(new LatLongAlt(latitude, longitude, altitude), null);
    }

    /**
     * ミッション内の指定したコマンドに移る
     *
     * @param index コマンド位置
     */
    @ModuleMethod
    public void jumpToCommand(int index) {
        this.mision.gotoWaypoint(index, null);
    }

    /**
     * ドローンに保存されているミッションを読み込む
     * <p>
     * ミッションは EVENT_MISSION イベントで受け取る
     */
    @ModuleMethod
    public void loadMission() {
        this.mision.loadWaypoints();
    }

    /**
     * ドローンにミッションを保存する
     *
     * @param mission ミッション
     */
    @ModuleMethod
    public void saveMission(List<Map<String, Object>> mission) {
        final List<MissionItem> items = Missions.decode(mission);
        Mission currentMission = new Mission();
        currentMission.clear();
        for (MissionItem item : items) {
            currentMission.addMissionItem(item);
        }
        this.mision.setMission(currentMission, true);
    }

    @ModuleMethod
    public void startMission(boolean forceModeChange, boolean forceArm) {
        this.mision.startMission(forceModeChange, forceArm, null);
    }

}


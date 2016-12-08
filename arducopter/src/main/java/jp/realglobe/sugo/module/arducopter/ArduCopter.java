package jp.realglobe.sugo.module.arducopter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.ControlApi;
import com.o3dr.android.client.apis.MissionApi;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.realglobe.sugo.actor.Emitter;
import jp.realglobe.sugo.actor.ModuleMethod;

/**
 * arduCopter モジュール。
 * Created by fukuchidaisuke on 16/11/28.
 */
public class ArduCopter extends Emitter implements Cloneable {


    private static final String LOG_TAG = ArduCopter.class.getName();

    private static final SparseArray<String> MODE_PREFIXES;

    static {
        MODE_PREFIXES = new SparseArray<>();
        MODE_PREFIXES.put(Type.TYPE_COPTER, "COPTER_");
        MODE_PREFIXES.put(Type.TYPE_PLANE, "PLANE_");
        MODE_PREFIXES.put(Type.TYPE_ROVER, "ROVER_");
    }

    public static final String CONNECT_TYPE_UDP = "UDP";
    public static final String CONNECT_TYPE_USB = "USB";

    /**
     * {@value}: 接続した。
     * 添付データ無し
     */
    public static final String EVENT_CONNECTED = "connected";

    /**
     * {@value}: 接続が切れた。
     * 添付データ無し
     */
    public static final String EVENT_DISCONNECTED = "disconnected";

    /**
     * {@value}: 機種通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>type</th><th>機種名</th></tr>
     * <tr><th>firmware</th><th>ファームウェア名</th></tr>
     * <tr><th>version</th><th>ファームウェアバージョン</th></tr>
     * </table>
     */
    public static final String EVENT_TYPE = "type";

    /**
     * {@value}: 動作モード通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>mode</th><th>動作モード名</th></tr>
     * </table>
     */
    public static final String EVENT_MODE = "mode";

    /**
     * {@value}: 駆動状態通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>arming</th><th>駆動していれば {@code true}、していなければ {@code false}</th></tr>
     * </table>
     */
    public static final String EVENT_ARMING = "arming";

    /**
     * {@value}: 速度通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>ground</th><th>対地速度</th></tr>
     * <tr><th>air</th><th>対気速度</th></tr>
     * <tr><th>vertical</th><th>垂直方向の速度</th></tr>
     * </table>
     */
    public static final String EVENT_SPEED = "speed";

    /**
     * {@value}: バッテリーの状態通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>remain</th><th>残り</th></tr>
     * <tr><th>voltage</th><th>電圧</th></tr>
     * <tr><th>current</th><th>電流</th></tr>
     * </table>
     */
    public static final String EVENT_BATTERY = "battery";

    /**
     * {@value}: 基点の通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>coordinate</th><th>位置座標</th></tr>
     * </table>
     */
    public static final String EVENT_HOME = "home";

    /**
     * {@value}: 高さの通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>altitude</th><th>高さ</th></tr>
     * </table>
     */
    public static final String EVENT_ALTITUDE = "altitude";

    /**
     * {@value}: GPS の示す位置の通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>coordinate</th><th>位置座標</th></tr>
     * </table>
     */
    public static final String EVENT_GPS_POSITION = "gpsPosition";

    /**
     * {@value}: 読み込んだミッションの通知。
     * <table border=1>
     * <caption>添付データ</caption>
     * <tr><th>commands</th><th>コマンド列</th></tr>
     * </table>
     */
    public static final String EVENT_MISSION = "mission";

    private final ControlTower tower;
    private final Drone drone;

    private final ControlApi control;
    private final VehicleApi vehicle;
    private final MissionApi mission;


    public ArduCopter(String name, Handler handler, Context context) {
        super(name);

        this.tower = new ControlTower(context);
        this.drone = new Drone(context);

        this.control = ControlApi.getApi(this.drone);
        this.vehicle = VehicleApi.getApi(this.drone);
        this.mission = MissionApi.getApi(this.drone);


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
     * ドローンにつなぐ。
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
        switch (type.toUpperCase(Locale.US)) {
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
    public void pause() {
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
     * 駆動切り替え
     *
     * @param arm true なら駆動させる
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
    public void setMode(String newMode) {
        final Type type = this.drone.getAttribute(AttributeType.TYPE);
        final String prefix = MODE_PREFIXES.get(type.getDroneType());
        if (prefix == null) {
            throw new IllegalStateException("unsupported type");
        }
        this.vehicle.setVehicleMode(VehicleMode.valueOf(prefix + newMode.toUpperCase(Locale.US)));
    }

    /**
     * 基点を設定する
     *
     * @param latitude  緯度
     * @param longitude 経度
     * @param altitude  高さ
     */
    @ModuleMethod
    public void setHome(double latitude, double longitude, double altitude) {
        this.vehicle.setVehicleHome(new LatLongAlt(latitude, longitude, altitude), null);
    }

    /**
     * ミッション内の指定したコマンドに移る
     *
     * @param index コマンド位置
     */
    @ModuleMethod
    public void jumpToCommand(int index) {
        this.mission.gotoWaypoint(index, null);
    }

    /**
     * ドローンに保存されているミッションを読み込む。
     * ミッションは EVENT_MISSION イベントで受け取る
     */
    @ModuleMethod
    public void loadMission() {
        this.mission.loadWaypoints();
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
        this.mission.setMission(currentMission, true);
    }

    @ModuleMethod
    public void startMission(boolean forceModeChange, boolean forceArm) {
        this.mission.startMission(forceModeChange, forceArm, null);
    }

}


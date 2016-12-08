package jp.realglobe.sugo.module.arducopter;

import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.ChangeSpeed;
import com.o3dr.services.android.lib.drone.mission.item.command.DoJump;
import com.o3dr.services.android.lib.drone.mission.item.command.ReturnToLaunch;
import com.o3dr.services.android.lib.drone.mission.item.command.Takeoff;
import com.o3dr.services.android.lib.drone.mission.item.command.YawCondition;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Circle;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Land;
import com.o3dr.services.android.lib.drone.mission.item.spatial.SplineWaypoint;
import com.o3dr.services.android.lib.drone.mission.item.spatial.Waypoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ミッションについて。
 * Created by fukuchidaisuke on 16/12/05.
 */
public final class Missions {

    /**
     * 指定点を通れ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * <tr><th>coordinate</th><th>指定点の座標</th></tr>
     * <tr><th>delay</th><th>次の動作までの待機時間</th></tr>
     * </table>
     */
    public static final String COMMAND_WAYPOINT = "waypoint";

    /**
     * スプライン曲線の制御点として指定点を通れ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * <tr><th>coordinate</th><th>指定点の座標</th></tr>
     * <tr><th>delay</th><th>次の動作までの待機時間</th></tr>
     * </table>
     */
    public static final String COMMAND_SPLINE_WAYPOINT = "splineWaypoint";

    /**
     * 離陸しろ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * <tr><th>altitude</th><th>離陸後の目標高さ</th></tr>
     * </table>
     */
    public static final String COMMAND_TAKEOFF = "takeoff";

    /**
     * 指定の速さに変えろ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * <tr><th>speed</th><th>目標の速さ</th></tr>
     * </table>
     */
    public static final String COMMAND_CHANGE_SPEED = "changeSpeed";

    /**
     * 初期地点上空に戻れ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * <tr><th>altitude</th><th>戻ったあとの高さ</th></tr>
     * </table>
     */
    public static final String COMMAND_RETURN_TO_LAUNCH = "returnToLaunch";

    /**
     * 着陸しろ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * </table>
     */
    public static final String COMMAND_LAND = "land";

    /**
     * 指定点を中心に回れ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * <tr><th>coordinate</th><th>指定点の座標</th></tr>
     * <tr><th>radius</th><th>半径</th></tr>
     * <tr><th>turns</th><th>何回回るか</th></tr>
     * </table>
     */
    public static final String COMMAND_CIRCLE = "circle";

    /**
     * 向きを変えろ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * <tr><th>angle</th><th>角度</th></tr>
     * <tr><th>angularSpeed</th><th>変える速度</th></tr>
     * <tr><th>relative</th><th>相対的な角度かどうか</th></tr>
     * </table>
     */
    public static final String COMMAND_YAW_CONDITION = "yawCondition";

    /**
     * 指定のコマンドに移れ。
     * <table border=1>
     * <caption>データ</caption>
     * <tr><th>type</th><th>{@value}</th></tr>
     * <tr><th>repeatCount</th><th>繰り返し回数</th></tr>
     * <tr><th>index</th><th>コマンド位置</th></tr>
     * </table>
     */
    public static final String COMMAND_DO_JUMP = "doJump";

    private static final String KEY_TYPE = "type";
    private static final String KEY_COORDINATE = "coordinate";
    private static final String KEY_DELAY = "delay";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_RADIUS = "radius";
    private static final String KEY_TURNS = "turns";
    private static final String KEY_ANGLE = "angle";
    private static final String KEY_ANGULAR_SPEED = "angularSpeed";
    private static final String KEY_RELATIVE = "relative";
    private static final String KEY_REPEAT_COUNT = "repeatCount";
    private static final String KEY_INDEX = "index";


    private Missions() {
    }

    /**
     * JSON 互換形式に変換する
     *
     * @param mission ミッション
     * @return ミッションを表す JSON 互換データ
     */
    static List<Map<String, Object>> encode(Mission mission) {
        final List<Map<String, Object>> encodedItems = new ArrayList<>();
        for (MissionItem item : mission.getMissionItems()) {
            encodedItems.add(encode(item));
        }
        return encodedItems;
    }

    /**
     * JSON 互換形式から変換する
     *
     * @param items コマンドを表す JSON 互換のデータ列
     * @return コマンド列
     */
    static List<MissionItem> decode(Object[] items) {
        final List<MissionItem> decodedItems = new ArrayList<>();
        for (Object item : items) {
            decodedItems.add(decode((Map<String, Object>) item));
        }
        return decodedItems;
    }

    private static Map<String, Object> encode(MissionItem item) {
        if (item instanceof Waypoint) {
            return encode((Waypoint) item);
        } else if (item instanceof SplineWaypoint) {
            return encode((SplineWaypoint) item);
        } else if (item instanceof Takeoff) {
            return encode((Takeoff) item);
        } else if (item instanceof ChangeSpeed) {
            return encode((ChangeSpeed) item);
        } else if (item instanceof ReturnToLaunch) {
            return encode((ReturnToLaunch) item);
        } else if (item instanceof Land) {
            return encode((Land) item);
        } else if (item instanceof Circle) {
            return encode((Circle) item);
        } else if (item instanceof YawCondition) {
            return encode((YawCondition) item);
        } else if (item instanceof DoJump) {
            return encode((DoJump) item);
        } else {
            throw new IllegalArgumentException("unsupported mission command type " + item.getClass().getSimpleName());
        }
    }

    private static MissionItem decode(Map<String, Object> item) {
        final String type = (String) item.get(KEY_TYPE);
        if (COMMAND_WAYPOINT.equals(type)) {
            return decodeWaypoint(item);
        } else if (COMMAND_SPLINE_WAYPOINT.equals(type)) {
            return decodeSplineWaypoint(item);
        } else if (COMMAND_TAKEOFF.equals(type)) {
            return decodeTakeoff(item);
        } else if (COMMAND_CHANGE_SPEED.equals(type)) {
            return decodeChangeSpeed(item);
        } else if (COMMAND_RETURN_TO_LAUNCH.equals(type)) {
            return decodeReturnToLaunch(item);
        } else if (COMMAND_LAND.equals(type)) {
            return decodeLand(item);
        } else if (COMMAND_CIRCLE.equals(type)) {
            return decodeCircle(item);
        } else if (COMMAND_YAW_CONDITION.equals(type)) {
            return decodeYawCondition(item);
        } else if (COMMAND_DO_JUMP.equals(type)) {
            return decodeDoJump(item);
        } else {
            throw new IllegalArgumentException("unsupported mission command type " + type);
        }
    }

    private static Map<String, Object> encode(Waypoint item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_WAYPOINT);
        encoded.put(KEY_COORDINATE, Coordinates.encode(item.getCoordinate()));
        encoded.put(KEY_DELAY, item.getDelay());
        return encoded;
    }

    private static Waypoint decodeWaypoint(Map<String, Object> item) {
        final Waypoint decoded = new Waypoint();
        if (item.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(item.get(KEY_COORDINATE)));
        }
        if (item.containsKey(KEY_DELAY)) {
            decoded.setDelay(Numbers.decodeDouble(item.get(KEY_DELAY)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(SplineWaypoint item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_SPLINE_WAYPOINT);
        encoded.put(KEY_COORDINATE, Coordinates.encode(item.getCoordinate()));
        encoded.put(KEY_DELAY, item.getDelay());
        return encoded;
    }

    private static SplineWaypoint decodeSplineWaypoint(Map<String, Object> item) {
        final SplineWaypoint decoded = new SplineWaypoint();
        if (item.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(item.get(KEY_COORDINATE)));
        }
        if (item.containsKey(KEY_DELAY)) {
            decoded.setDelay(Numbers.decodeDouble(item.get(KEY_DELAY)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(Takeoff item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_TAKEOFF);
        encoded.put(KEY_ALTITUDE, item.getTakeoffAltitude());
        return encoded;
    }

    private static Takeoff decodeTakeoff(Map<String, Object> item) {
        final Takeoff decoded = new Takeoff();
        if (item.containsKey(KEY_ALTITUDE)) {
            decoded.setTakeoffAltitude(Numbers.decodeDouble(item.get(KEY_ALTITUDE)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(ChangeSpeed item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_CHANGE_SPEED);
        encoded.put(KEY_SPEED, item.getSpeed());
        return encoded;
    }

    private static ChangeSpeed decodeChangeSpeed(Map<String, Object> item) {
        final ChangeSpeed decoded = new ChangeSpeed();
        if (item.containsKey(KEY_SPEED)) {
            decoded.setSpeed(Numbers.decodeDouble(item.get(KEY_SPEED)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(ReturnToLaunch item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_RETURN_TO_LAUNCH);
        encoded.put(KEY_ALTITUDE, item.getReturnAltitude());
        return encoded;
    }

    private static ReturnToLaunch decodeReturnToLaunch(Map<String, Object> item) {
        final ReturnToLaunch decoded = new ReturnToLaunch();
        if (item.containsKey(KEY_ALTITUDE)) {
            decoded.setReturnAltitude(Numbers.decodeDouble(item.get(KEY_ALTITUDE)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(Land item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_LAND);
        encoded.put(KEY_COORDINATE, Coordinates.encode(item.getCoordinate()));
        return encoded;
    }

    private static Land decodeLand(Map<String, Object> item) {
        final Land decoded = new Land();
        if (item.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(item.get(KEY_COORDINATE)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(Circle item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_CIRCLE);
        encoded.put(KEY_COORDINATE, Coordinates.encode(item.getCoordinate()));
        encoded.put(KEY_RADIUS, item.getRadius());
        encoded.put(KEY_TURNS, item.getTurns());
        return encoded;
    }

    private static Circle decodeCircle(Map<String, Object> item) {
        final Circle decoded = new Circle();
        if (item.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(item.get(KEY_COORDINATE)));
        }
        if (item.containsKey(KEY_RADIUS)) {
            decoded.setRadius(Numbers.decodeDouble(item.get(KEY_RADIUS)));
        }
        if (item.containsKey(KEY_TURNS)) {
            decoded.setTurns(Numbers.decodeInt(item.get(KEY_TURNS)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(YawCondition item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_YAW_CONDITION);
        encoded.put(KEY_ANGLE, item.getAngle());
        encoded.put(KEY_ANGULAR_SPEED, item.getAngularSpeed());
        encoded.put(KEY_RELATIVE, item.isRelative());
        return encoded;
    }

    private static YawCondition decodeYawCondition(Map<String, Object> item) {
        final YawCondition decoded = new YawCondition();
        if (item.containsKey(KEY_ANGLE)) {
            decoded.setAngle(Numbers.decodeDouble(item.get(KEY_ANGLE)));
        }
        if (item.containsKey(KEY_ANGULAR_SPEED)) {
            decoded.setAngularSpeed(Numbers.decodeDouble(item.get(KEY_ANGULAR_SPEED)));
        }
        if (item.containsKey(KEY_RELATIVE)) {
            decoded.setRelative((boolean) item.get(KEY_RELATIVE));
        }
        return decoded;
    }

    private static Map<String, Object> encode(DoJump item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_DO_JUMP);
        encoded.put(KEY_REPEAT_COUNT, item.getRepeatCount());
        encoded.put(KEY_INDEX, item.getWaypoint());
        return encoded;
    }

    private static DoJump decodeDoJump(Map<String, Object> item) {
        final DoJump decoded = new DoJump();
        if (item.containsKey(KEY_REPEAT_COUNT)) {
            decoded.setRepeatCount(Numbers.decodeInt(item.get(KEY_REPEAT_COUNT)));
        }
        if (item.containsKey(KEY_INDEX)) {
            decoded.setWaypoint(Numbers.decodeInt(item.get(KEY_INDEX)));
        }
        return decoded;
    }

}

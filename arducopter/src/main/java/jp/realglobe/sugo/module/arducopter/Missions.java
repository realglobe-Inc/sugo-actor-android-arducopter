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
        final List<Map<String, Object>> encodedCommands = new ArrayList<>();
        for (MissionItem command : mission.getMissionItems()) {
            encodedCommands.add(encode(command));
        }
        return encodedCommands;
    }

    /**
     * JSON 互換形式から変換する
     *
     * @param commands ミッションを表す JSON 互換データ
     * @return ミッション
     */
    static Mission decode(Object[] commands) {
        final Mission mission = new Mission();
        for (Object command : commands) {
            mission.addMissionItem(decodeCommand((Map<String, Object>) command));
        }
        return mission;
    }

    private static Map<String, Object> encode(MissionItem command) {
        if (command instanceof Waypoint) {
            return encode((Waypoint) command);
        } else if (command instanceof SplineWaypoint) {
            return encode((SplineWaypoint) command);
        } else if (command instanceof Takeoff) {
            return encode((Takeoff) command);
        } else if (command instanceof ChangeSpeed) {
            return encode((ChangeSpeed) command);
        } else if (command instanceof ReturnToLaunch) {
            return encode((ReturnToLaunch) command);
        } else if (command instanceof Land) {
            return encode((Land) command);
        } else if (command instanceof Circle) {
            return encode((Circle) command);
        } else if (command instanceof YawCondition) {
            return encode((YawCondition) command);
        } else if (command instanceof DoJump) {
            return encode((DoJump) command);
        } else {
            throw new IllegalArgumentException("unsupported mission command type " + command.getClass().getSimpleName());
        }
    }

    private static MissionItem decodeCommand(Map<String, Object> command) {
        final String type = (String) command.get(KEY_TYPE);
        if (COMMAND_WAYPOINT.equals(type)) {
            return decodeWaypoint(command);
        } else if (COMMAND_SPLINE_WAYPOINT.equals(type)) {
            return decodeSplineWaypoint(command);
        } else if (COMMAND_TAKEOFF.equals(type)) {
            return decodeTakeoff(command);
        } else if (COMMAND_CHANGE_SPEED.equals(type)) {
            return decodeChangeSpeed(command);
        } else if (COMMAND_RETURN_TO_LAUNCH.equals(type)) {
            return decodeReturnToLaunch(command);
        } else if (COMMAND_LAND.equals(type)) {
            return decodeLand(command);
        } else if (COMMAND_CIRCLE.equals(type)) {
            return decodeCircle(command);
        } else if (COMMAND_YAW_CONDITION.equals(type)) {
            return decodeYawCondition(command);
        } else if (COMMAND_DO_JUMP.equals(type)) {
            return decodeDoJump(command);
        } else {
            throw new IllegalArgumentException("unsupported mission command type " + type);
        }
    }

    private static Map<String, Object> encode(Waypoint command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_WAYPOINT);
        encoded.put(KEY_COORDINATE, Coordinates.encode(command.getCoordinate()));
        encoded.put(KEY_DELAY, command.getDelay());
        return encoded;
    }

    private static Waypoint decodeWaypoint(Map<String, Object> command) {
        final Waypoint decoded = new Waypoint();
        if (command.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(command.get(KEY_COORDINATE)));
        }
        if (command.containsKey(KEY_DELAY)) {
            decoded.setDelay(Numbers.decodeDouble(command.get(KEY_DELAY)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(SplineWaypoint command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_SPLINE_WAYPOINT);
        encoded.put(KEY_COORDINATE, Coordinates.encode(command.getCoordinate()));
        encoded.put(KEY_DELAY, command.getDelay());
        return encoded;
    }

    private static SplineWaypoint decodeSplineWaypoint(Map<String, Object> command) {
        final SplineWaypoint decoded = new SplineWaypoint();
        if (command.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(command.get(KEY_COORDINATE)));
        }
        if (command.containsKey(KEY_DELAY)) {
            decoded.setDelay(Numbers.decodeDouble(command.get(KEY_DELAY)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(Takeoff command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_TAKEOFF);
        encoded.put(KEY_ALTITUDE, command.getTakeoffAltitude());
        return encoded;
    }

    private static Takeoff decodeTakeoff(Map<String, Object> command) {
        final Takeoff decoded = new Takeoff();
        if (command.containsKey(KEY_ALTITUDE)) {
            decoded.setTakeoffAltitude(Numbers.decodeDouble(command.get(KEY_ALTITUDE)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(ChangeSpeed command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_CHANGE_SPEED);
        encoded.put(KEY_SPEED, command.getSpeed());
        return encoded;
    }

    private static ChangeSpeed decodeChangeSpeed(Map<String, Object> command) {
        final ChangeSpeed decoded = new ChangeSpeed();
        if (command.containsKey(KEY_SPEED)) {
            decoded.setSpeed(Numbers.decodeDouble(command.get(KEY_SPEED)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(ReturnToLaunch command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_RETURN_TO_LAUNCH);
        encoded.put(KEY_ALTITUDE, command.getReturnAltitude());
        return encoded;
    }

    private static ReturnToLaunch decodeReturnToLaunch(Map<String, Object> command) {
        final ReturnToLaunch decoded = new ReturnToLaunch();
        if (command.containsKey(KEY_ALTITUDE)) {
            decoded.setReturnAltitude(Numbers.decodeDouble(command.get(KEY_ALTITUDE)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(Land command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_LAND);
        encoded.put(KEY_COORDINATE, Coordinates.encode(command.getCoordinate()));
        return encoded;
    }

    private static Land decodeLand(Map<String, Object> command) {
        final Land decoded = new Land();
        if (command.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(command.get(KEY_COORDINATE)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(Circle command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_CIRCLE);
        encoded.put(KEY_COORDINATE, Coordinates.encode(command.getCoordinate()));
        encoded.put(KEY_RADIUS, command.getRadius());
        encoded.put(KEY_TURNS, command.getTurns());
        return encoded;
    }

    private static Circle decodeCircle(Map<String, Object> command) {
        final Circle decoded = new Circle();
        if (command.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(command.get(KEY_COORDINATE)));
        }
        if (command.containsKey(KEY_RADIUS)) {
            decoded.setRadius(Numbers.decodeDouble(command.get(KEY_RADIUS)));
        }
        if (command.containsKey(KEY_TURNS)) {
            decoded.setTurns(Numbers.decodeInt(command.get(KEY_TURNS)));
        }
        return decoded;
    }

    private static Map<String, Object> encode(YawCondition command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_YAW_CONDITION);
        encoded.put(KEY_ANGLE, command.getAngle());
        encoded.put(KEY_ANGULAR_SPEED, command.getAngularSpeed());
        encoded.put(KEY_RELATIVE, command.isRelative());
        return encoded;
    }

    private static YawCondition decodeYawCondition(Map<String, Object> command) {
        final YawCondition decoded = new YawCondition();
        if (command.containsKey(KEY_ANGLE)) {
            decoded.setAngle(Numbers.decodeDouble(command.get(KEY_ANGLE)));
        }
        if (command.containsKey(KEY_ANGULAR_SPEED)) {
            decoded.setAngularSpeed(Numbers.decodeDouble(command.get(KEY_ANGULAR_SPEED)));
        }
        if (command.containsKey(KEY_RELATIVE)) {
            decoded.setRelative((boolean) command.get(KEY_RELATIVE));
        }
        return decoded;
    }

    private static Map<String, Object> encode(DoJump command) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, COMMAND_DO_JUMP);
        encoded.put(KEY_REPEAT_COUNT, command.getRepeatCount());
        encoded.put(KEY_INDEX, command.getWaypoint());
        return encoded;
    }

    private static DoJump decodeDoJump(Map<String, Object> command) {
        final DoJump decoded = new DoJump();
        if (command.containsKey(KEY_REPEAT_COUNT)) {
            decoded.setRepeatCount(Numbers.decodeInt(command.get(KEY_REPEAT_COUNT)));
        }
        if (command.containsKey(KEY_INDEX)) {
            decoded.setWaypoint(Numbers.decodeInt(command.get(KEY_INDEX)));
        }
        return decoded;
    }

}

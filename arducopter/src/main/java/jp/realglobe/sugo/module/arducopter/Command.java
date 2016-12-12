package jp.realglobe.sugo.module.arducopter;

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

import java.util.HashMap;
import java.util.Map;

/**
 * ミッションの構成要素。
 * コマンド列がミッションになる。
 * コマンドは以下のデータとコマンドごとの追加データで表す。
 * <table border=1>
 * <caption>データ</caption>
 * <tr><th>type</th><th>コマンド名</th></tr>
 * </table>
 * Created by fukuchidaisuke on 16/12/12.
 */
public enum Command {

    /**
     * 指定点を通れ。
     * <table border=1>
     * <caption>追加データ</caption>
     * <tr><th>coordinate</th><th>指定点の座標</th></tr>
     * <tr><th>delay</th><th>次の動作までの待機時間</th></tr>
     * </table>
     */
    waypoint {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final Waypoint waypoint = (Waypoint) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_COORDINATE, Coordinates.encode(waypoint.getCoordinate()));
            encoded.put(KEY_DELAY, waypoint.getDelay());
            return encoded;
        }

        @Override
        Waypoint decode(Map<String, Object> command) {
            final Waypoint decoded = new Waypoint();
            if (command.containsKey(KEY_COORDINATE)) {
                decoded.setCoordinate(Coordinates.decodeLatLongAlt(command.get(KEY_COORDINATE)));
            }
            if (command.containsKey(KEY_DELAY)) {
                decoded.setDelay(Numbers.decodeDouble(command.get(KEY_DELAY)));
            }
            return decoded;
        }
    },

    /**
     * スプライン曲線の制御点として指定点を通れ。
     * <table border=1>
     * <caption>追加データ</caption>
     * <tr><th>coordinate</th><th>指定点の座標</th></tr>
     * <tr><th>delay</th><th>次の動作までの待機時間</th></tr>
     * </table>
     */
    splineWaypoint {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final SplineWaypoint splineWaypoint = (SplineWaypoint) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_COORDINATE, Coordinates.encode(splineWaypoint.getCoordinate()));
            encoded.put(KEY_DELAY, splineWaypoint.getDelay());
            return encoded;
        }

        @Override
        SplineWaypoint decode(Map<String, Object> command) {
            final SplineWaypoint decoded = new SplineWaypoint();
            if (command.containsKey(KEY_COORDINATE)) {
                decoded.setCoordinate(Coordinates.decodeLatLongAlt(command.get(KEY_COORDINATE)));
            }
            if (command.containsKey(KEY_DELAY)) {
                decoded.setDelay(Numbers.decodeDouble(command.get(KEY_DELAY)));
            }
            return decoded;
        }
    },

    /**
     * 離陸しろ。
     * <table border=1>
     * <caption>追加データ</caption>
     * <tr><th>altitude</th><th>離陸後の目標高さ</th></tr>
     * </table>
     */
    takeoff {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final Takeoff takeoff = (Takeoff) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_ALTITUDE, takeoff.getTakeoffAltitude());
            return encoded;
        }

        @Override
        Takeoff decode(Map<String, Object> command) {
            final Takeoff decoded = new Takeoff();
            if (command.containsKey(KEY_ALTITUDE)) {
                decoded.setTakeoffAltitude(Numbers.decodeDouble(command.get(KEY_ALTITUDE)));
            }
            return decoded;
        }
    },

    /**
     * 指定の速さに変えろ。
     * <table border=1>
     * <caption>追加データ</caption>
     * <tr><th>speed</th><th>目標の速さ</th></tr>
     * </table>
     */
    changeSpeed {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final ChangeSpeed changeSpeed = (ChangeSpeed) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_SPEED, changeSpeed.getSpeed());
            return encoded;
        }

        @Override
        ChangeSpeed decode(Map<String, Object> command) {
            final ChangeSpeed decoded = new ChangeSpeed();
            if (command.containsKey(KEY_SPEED)) {
                decoded.setSpeed(Numbers.decodeDouble(command.get(KEY_SPEED)));
            }
            return decoded;
        }
    },

    /**
     * 起点上空に戻れ。
     * <table border=1>
     * <caption>追加データ</caption>
     * <tr><th>altitude</th><th>戻ったあとの高さ</th></tr>
     * </table>
     */
    returnToLaunch {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final ReturnToLaunch returnToLaunch = (ReturnToLaunch) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_ALTITUDE, returnToLaunch.getReturnAltitude());
            return encoded;
        }

        @Override
        ReturnToLaunch decode(Map<String, Object> command) {
            final ReturnToLaunch decoded = new ReturnToLaunch();
            if (command.containsKey(KEY_ALTITUDE)) {
                decoded.setReturnAltitude(Numbers.decodeDouble(command.get(KEY_ALTITUDE)));
            }
            return decoded;
        }
    },

    /**
     * 着陸しろ。
     * 追加データ無し
     */
    land {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final Land land = (Land) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_COORDINATE, Coordinates.encode(land.getCoordinate()));
            return encoded;
        }

        @Override
        Land decode(Map<String, Object> command) {
            final Land decoded = new Land();
            if (command.containsKey(KEY_COORDINATE)) {
                decoded.setCoordinate(Coordinates.decodeLatLongAlt(command.get(KEY_COORDINATE)));
            }
            return decoded;
        }
    },

    /**
     * 指定点を中心に回れ。
     * <table border=1>
     * <caption>追加データ</caption>
     * <tr><th>coordinate</th><th>指定点の座標</th></tr>
     * <tr><th>radius</th><th>半径</th></tr>
     * <tr><th>turns</th><th>何回回るか</th></tr>
     * </table>
     */
    circle {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final Circle circle = (Circle) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_COORDINATE, Coordinates.encode(circle.getCoordinate()));
            encoded.put(KEY_RADIUS, circle.getRadius());
            encoded.put(KEY_TURNS, circle.getTurns());
            return encoded;
        }

        @Override
        Circle decode(Map<String, Object> command) {
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
    },

    /**
     * 向きを変えろ。
     * <table border=1>
     * <caption>追加データ</caption>
     * <tr><th>angle</th><th>角度</th></tr>
     * <tr><th>angularSpeed</th><th>向きを変える速度</th></tr>
     * <tr><th>relative</th><th>相対的な角度かどうか</th></tr>
     * </table>
     */
    turnTo {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final YawCondition yawCondition = (YawCondition) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_ANGLE, yawCondition.getAngle());
            encoded.put(KEY_ANGULAR_SPEED, yawCondition.getAngularSpeed());
            encoded.put(KEY_RELATIVE, yawCondition.isRelative());
            return encoded;
        }

        @Override
        YawCondition decode(Map<String, Object> command) {
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
    },

    /**
     * 指定のコマンドに移れ。
     * <table border=1>
     * <caption>追加データ</caption>
     * <tr><th>repeatCount</th><th>繰り返し回数</th></tr>
     * <tr><th>index</th><th>コマンド位置</th></tr>
     * </table>
     */
    jumpTo {
        @Override
        Map<String, Object> encode(MissionItem command) {
            final DoJump doJump = (DoJump) command;
            final Map<String, Object> encoded = new HashMap<>();
            encoded.put(KEY_REPEAT_COUNT, doJump.getRepeatCount());
            encoded.put(KEY_INDEX, doJump.getWaypoint());
            return encoded;
        }

        @Override
        DoJump decode(Map<String, Object> command) {
            final DoJump decoded = new DoJump();
            if (command.containsKey(KEY_REPEAT_COUNT)) {
                decoded.setRepeatCount(Numbers.decodeInt(command.get(KEY_REPEAT_COUNT)));
            }
            if (command.containsKey(KEY_INDEX)) {
                decoded.setWaypoint(Numbers.decodeInt(command.get(KEY_INDEX)));
            }
            return decoded;
        }
    },;

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

    abstract Map<String, Object> encode(MissionItem command);

    abstract MissionItem decode(Map<String, Object> command);

    private static final Map<Class<? extends MissionItem>, Command> classMap = new HashMap<>();

    static {
        classMap.put(Waypoint.class, waypoint);
        classMap.put(SplineWaypoint.class, waypoint);
        classMap.put(Takeoff.class, takeoff);
        classMap.put(ChangeSpeed.class, changeSpeed);
        classMap.put(ReturnToLaunch.class, returnToLaunch);
        classMap.put(Land.class, land);
        classMap.put(Circle.class, circle);
        classMap.put(YawCondition.class, turnTo);
        classMap.put(DoJump.class, jumpTo);
    }

    static <T extends MissionItem> Command correspondTo(Class<T> clazz) {
        return classMap.get(clazz);
    }

}

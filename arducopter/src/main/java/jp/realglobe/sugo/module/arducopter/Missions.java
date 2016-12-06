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
 * ミッション周りの便利関数
 * <p>
 * Created by fukuchidaisuke on 16/12/05.
 */
final class Missions {

    private static final String KEY_TYPE = "type";
    private static final String KEY_COORDINATE = "coordinate";
    private static final String KEY_ACCEPTANCE_RADIUS = "acceptanceRadius";
    private static final String KEY_DELAY = "delay";
    private static final String KEY_ORBITAL_RADIUS = "orbitalRadius";
    private static final String KEY_ORBIT_CCW = "orbitCcw";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_PITCH = "pitch";
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
    static List<MissionItem> decode(List<Map<String, Object>> items) {
        final List<MissionItem> decodedItems = new ArrayList<>();
        for (Map<String, Object> item : items) {
            decodedItems.add(decode(item));
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
            throw new IllegalArgumentException("unsupported mission item type " + item.getClass().getSimpleName());
        }
    }

    private static MissionItem decode(Map<String, Object> item) {
        final String type = (String) item.get(KEY_TYPE);
        if (Waypoint.class.getSimpleName().equals(type)) {
            return decodeWaypoint(item);
        } else if (SplineWaypoint.class.getSimpleName().equals(type)) {
            return decodeSplineWaypoint(item);
        } else if (Takeoff.class.getSimpleName().equals(type)) {
            return decodeTakeoff(item);
        } else if (ChangeSpeed.class.getSimpleName().equals(type)) {
            return decodeChangeSpeed(item);
        } else if (ReturnToLaunch.class.getSimpleName().equals(type)) {
            return decodeReturnToLaunch(item);
        } else if (Land.class.getSimpleName().equals(type)) {
            return decodeLand(item);
        } else if (Circle.class.getSimpleName().equals(type)) {
            return decodeCircle(item);
        } else if (YawCondition.class.getSimpleName().equals(type)) {
            return decodeYawCondition(item);
        } else if (DoJump.class.getSimpleName().equals(type)) {
            return decodeDoJump(item);
        } else {
            throw new IllegalArgumentException("unsupported mission item type " + type);
        }
    }

    private static Map<String, Object> encode(Waypoint item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, Waypoint.class.getSimpleName());
        encoded.put(KEY_COORDINATE, Coordinates.encode(item.getCoordinate()));
        encoded.put(KEY_ACCEPTANCE_RADIUS, item.getAcceptanceRadius());
        encoded.put(KEY_DELAY, item.getDelay());
        encoded.put(KEY_ORBITAL_RADIUS, item.getOrbitalRadius());
        encoded.put(KEY_ORBIT_CCW, item.isOrbitCCW());
        return encoded;
    }

    private static Waypoint decodeWaypoint(Map<String, Object> item) {
        final Waypoint decoded = new Waypoint();
        if (item.containsKey(KEY_COORDINATE)) {
            decoded.setCoordinate(Coordinates.decodeLatLongAlt(item.get(KEY_COORDINATE)));
        }
        if (item.containsKey(KEY_ACCEPTANCE_RADIUS)) {
            decoded.setAcceptanceRadius((double) item.get(KEY_ACCEPTANCE_RADIUS));
        }
        if (item.containsKey(KEY_DELAY)) {
            decoded.setDelay((double) item.get(KEY_DELAY));
        }
        if (item.containsKey(KEY_ORBITAL_RADIUS)) {
            decoded.setOrbitalRadius((double) item.get(KEY_ORBITAL_RADIUS));
        }
        if (item.containsKey(KEY_ORBIT_CCW)) {
            decoded.setOrbitCCW((boolean) item.get(KEY_ORBIT_CCW));
        }
        return decoded;
    }

    private static Map<String, Object> encode(SplineWaypoint item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, SplineWaypoint.class.getSimpleName());
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
            decoded.setDelay((double) item.get(KEY_DELAY));
        }
        return decoded;
    }

    private static Map<String, Object> encode(Takeoff item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, Takeoff.class.getSimpleName());
        encoded.put(KEY_ALTITUDE, item.getTakeoffAltitude());
        encoded.put(KEY_PITCH, item.getTakeoffPitch());
        return encoded;
    }

    private static Takeoff decodeTakeoff(Map<String, Object> item) {
        final Takeoff decoded = new Takeoff();
        if (item.containsKey(KEY_ALTITUDE)) {
            decoded.setTakeoffAltitude((double) item.get(KEY_ALTITUDE));
        }
        if (item.containsKey(KEY_PITCH)) {
            decoded.setTakeoffPitch((double) item.get(KEY_PITCH));
        }
        return decoded;
    }

    private static Map<String, Object> encode(ChangeSpeed item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, ChangeSpeed.class.getSimpleName());
        encoded.put(KEY_SPEED, item.getSpeed());
        return encoded;
    }

    private static ChangeSpeed decodeChangeSpeed(Map<String, Object> item) {
        final ChangeSpeed decoded = new ChangeSpeed();
        if (item.containsKey(KEY_SPEED)) {
            decoded.setSpeed((double) item.get(KEY_SPEED));
        }
        return decoded;
    }

    private static Map<String, Object> encode(ReturnToLaunch item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, ReturnToLaunch.class.getSimpleName());
        encoded.put(KEY_ALTITUDE, item.getReturnAltitude());
        return encoded;
    }

    private static ReturnToLaunch decodeReturnToLaunch(Map<String, Object> item) {
        final ReturnToLaunch decoded = new ReturnToLaunch();
        if (item.containsKey(KEY_ALTITUDE)) {
            decoded.setReturnAltitude((double) item.get(KEY_ALTITUDE));
        }
        return decoded;
    }

    private static Map<String, Object> encode(Land item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, Land.class.getSimpleName());
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
        encoded.put(KEY_TYPE, Circle.class.getSimpleName());
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
            decoded.setRadius((double) item.get(KEY_RADIUS));
        }
        if (item.containsKey(KEY_TURNS)) {
            decoded.setRadius((double) item.get(KEY_TURNS));
        }
        return decoded;
    }

    private static Map<String, Object> encode(YawCondition item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, YawCondition.class.getSimpleName());
        encoded.put(KEY_ANGLE, item.getAngle());
        encoded.put(KEY_ANGULAR_SPEED, item.getAngularSpeed());
        encoded.put(KEY_RELATIVE, item.isRelative());
        return encoded;
    }

    private static YawCondition decodeYawCondition(Map<String, Object> item) {
        final YawCondition decoded = new YawCondition();
        if (item.containsKey(KEY_ANGLE)) {
            decoded.setAngle((double) item.get(KEY_ANGLE));
        }
        if (item.containsKey(KEY_ANGULAR_SPEED)) {
            decoded.setAngularSpeed((double) item.get(KEY_ANGULAR_SPEED));
        }
        if (item.containsKey(KEY_RELATIVE)) {
            decoded.setRelative((boolean) item.get(KEY_RELATIVE));
        }
        return decoded;
    }

    private static Map<String, Object> encode(DoJump item) {
        final Map<String, Object> encoded = new HashMap<>();
        encoded.put(KEY_TYPE, DoJump.class.getSimpleName());
        encoded.put(KEY_REPEAT_COUNT, item.getRepeatCount());
        encoded.put(KEY_INDEX, item.getWaypoint());
        return encoded;
    }

    private static DoJump decodeDoJump(Map<String, Object> item) {
        final DoJump decoded = new DoJump();
        if (item.containsKey(KEY_REPEAT_COUNT)) {
            decoded.setRepeatCount((int) item.get(KEY_REPEAT_COUNT));
        }
        if (item.containsKey(KEY_INDEX)) {
            decoded.setWaypoint((int) item.get(KEY_INDEX));
        }
        return decoded;
    }

}

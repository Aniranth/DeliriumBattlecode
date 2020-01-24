package qualsbot;

import battlecode.common.*;

/**
 * class for moving around the map
 */
public class Pathfinder {
    private RobotController rc;

    static Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };

    public Pathfinder(RobotController rc_param){
        rc = rc_param;
    }

    /**
     * returns whether the space immediately in the given direction is NOT flooded
     * @param type type of robot (here because sometimes we want to check for someone else)
     * @param dir direction to check in
     * @return true if type is drone or space is not flooded, false otherwise
     */
    public boolean sinkSafe(RobotType type, Direction dir) throws GameActionException{
        if(type == RobotType.DELIVERY_DRONE) return true;
        return !rc.senseFlooding(rc.adjacentLocation(dir));
    }

    /**
     * overload for readability
     */
    public boolean sinkSafe(Direction dir) throws GameActionException{
        return sinkSafe(rc.getType(), dir);
    }

    /**
     * attempts to move in the given direction
     * @param dir direction to move in
     * @return true if we move; false otherwise.
     */
    public boolean move(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMove(dir) && sinkSafe(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * pathfinding (currently lifted straight from lecturebot)
     * @param loc location to move to
     * @return true if we move, false otherwise
     */
    public boolean to(MapLocation loc) throws GameActionException {
        if(loc == null) return false;
        return to(rc.getLocation().directionTo(loc));
    }

    /**
     * pathfinding (currently lifted straight from lecturebot)
     * @param dir direction to move towards
     * @return true if we move, false otherwise
     */
    public boolean to(Direction dir) throws GameActionException{
        if(dir == null) return false;
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(),
                dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry){
            if(move(d))
                return true;
        }
        return false;
    }

    public Direction randomDir(){
        return directions[(int) (Math.random() * directions.length)];
    }
}

package qualsbot;

import battlecode.common.*;

/**
 * Class which all robot AI will inherit
 * Stores the robot controller as rc, then runs init
 */
public abstract class GameRobot {

    /**
     * stuff we use for everything
     */
    protected RobotController rc;
    protected Radio radio;
    protected Pathfinder path;

    /**
     * standard ctor; runs init
     *
     * @param rc_param robot controller
     */
    public GameRobot(RobotController rc_param) throws GameActionException {
        rc = rc_param;
        radio = new Radio(rc);
        path = new Pathfinder(rc);
        init(rc.getRoundNum());
    }

    /**
     * any first-time setup that needs to happen for the robot to function in loop
     *
     * @param turn
     */
    protected abstract void init(int turn) throws GameActionException;

    /**
     * this runs every game turn. make sure to not accidentally go over any bytecode limits.
     *
     * @param turn game turn number
     */
    abstract void loop(int turn) throws GameActionException;

    // Below are pathfinder methods that are brought here for nicety's sake


    protected boolean move(Direction dir) throws GameActionException {
        return path.move(dir);
    }

    protected boolean build(RobotType type, Direction dir) throws GameActionException {
        return path.build(type,dir);
    }

    protected boolean mine(Direction dir) throws GameActionException {
        return path.mine(dir);
    }

    protected boolean grab(RobotInfo target) throws GameActionException {
        return path.grab(target);
    }

    protected boolean drop(Direction dir) throws GameActionException {
        return path.drop(dir);
    }

    protected boolean drop(MapLocation m) throws GameActionException {
        return path.drop(m);
    }

    protected boolean refine(Direction dir) throws GameActionException {
        return path.refine(dir);
    }

    protected boolean shoot(RobotInfo target) throws GameActionException {
        return path.shoot(target);
    }

    protected boolean dig(Direction dir) throws GameActionException {
        return path.dig(dir);
    }

    protected boolean dig(MapLocation m) throws GameActionException{
        return path.dig(m);
    }

    protected boolean dump(Direction dir) throws GameActionException{
        return path.dump(dir);
    }

    protected boolean dump(MapLocation m) throws GameActionException{
        return path.dump(m);
    }
}

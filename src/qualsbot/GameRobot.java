package qualsbot;

import battlecode.common.*;

/**
 * Class which all robot AI will inherit
 * Stores the robot controller as rc, then runs init
 */
public abstract class GameRobot {

    /**
     * robot controller we use for everything
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

    /* * * * * * * * * * * * * * * * * * * * * * * * * *
     * SHARED METHODS BELOW                            *
     * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * tries to build a robot in the given direction
     *
     * @param type what to build
     * @param dir  where to build it
     * @return true if we successfully built it, false otherwise
     */
    protected boolean build(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir) && path.sinkSafe(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * tries to mine soup in the given direction
     *
     * @param dir where to mine
     * @return true if we mined it, false otherwise
     */
    protected boolean mine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * tries to pick up a robot
     *
     * @param target robot to grab
     * @return true if we grab it, false otherwise
     */
    protected boolean grab(RobotInfo target) throws GameActionException {
        if (rc.isReady() && rc.canPickUpUnit(target.getID())) {
            rc.pickUpUnit(target.getID());
            return true;
        } else return false;
    }

    /**
     * tries to drop a robot in the given direction
     *
     * @param dir where we droppin
     * @return true if dropped, false otherwise
     */
    protected boolean drop(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDropUnit(dir)) {
            rc.dropUnit(dir);
            return true;
        } else return false;
    }

    /**
     * tries to refine soup in the given dir
     *
     * @param dir where to refine
     * @return true if we refined, false otherwise
     */
    protected boolean refine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }

    /**
     * tries to shoot a unit
     *
     * @param target who to shoot
     * @return true if we shot them, false otherwise
     */
    protected boolean shoot(RobotInfo target) throws GameActionException {
        if (rc.isReady() && rc.canShootUnit(target.getID())) {
            rc.shootUnit(target.getID());
            return true;
        } else return false;
    }

    /**
     * tries to dig in a direction
     *
     * @param dir where to dig
     * @return true if we dig, false otherwise
     */
    protected boolean dig(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDigDirt(dir)) {
            rc.digDirt(dir);
            return true;
        } else return false;
    }

    /**
     * overload to change location to dir
     */
    protected boolean dig(MapLocation m) throws GameActionException{
        return dig(rc.getLocation().directionTo(m));
    }

    /**
     * tries to dump in a direction
     * @param dir where to dump
     * @return true if we dump, false otherwise
     */
    protected boolean dump(Direction dir) throws GameActionException{
        if(rc.isReady() && rc.canDepositDirt(dir)){
            rc.depositDirt(dir);
            return true;
        } else return false;
    }

    /**
     * overload to change location to dir
     */
    protected boolean dump(MapLocation m) throws GameActionException{
        return dump(rc.getLocation().directionTo(m));
    }
}

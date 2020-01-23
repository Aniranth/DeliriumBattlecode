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

    /**
     * standard ctor; runs init
     * @param rc_param
     */
    public GameRobot(RobotController rc_param){
        rc = rc_param;
        init();
    }

    /**
     * any first-time setup that needs to happen for the robot to function in loop
     */
    protected abstract void init();

    /**
     * this runs every game turn. make sure to not accidentally go over any bytecode limits.
     * @param turn game turn number
     */
    abstract void loop(int turn);

    /**
     * SHARED METHODS BELOW
     */

    /**
     * returns whether the space immediately in the given direction is NOT flooded
     * @param type type of robot (here because sometimes we want to check for someone else)
     * @param dir direction to check in
     * @return true if type is drone or space is not flooded, false otherwise
     */
    protected boolean sinkSafe(RobotType type, Direction dir) throws GameActionException{
        if(type == RobotType.DELIVERY_DRONE) return true;
        return !rc.senseFlooding(rc.adjacentLocation(dir));
    }

    /**
     * overload for readability
     */
    protected boolean sinkSafe(Direction dir) throws GameActionException{
        return sinkSafe(rc.getType(), dir);
    }

    /**
     * attempts to move in the given direction
     * @param dir direction to move in
     * @return true if we move; false otherwise.
     */
    protected boolean move(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMove(dir) && sinkSafe(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * tries to build a robot in the given direction
     * @param type what to build
     * @param dir where to build it
     * @return true if we successfully built it, false otherwise
     */
    protected boolean build(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir) && sinkSafe(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * tries to mine soup in the given direction
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
     * @param target robot to grab
     * @return true if we grab it, false otherwise
     */
    protected boolean grab(RobotInfo target) throws GameActionException {
        if(rc.isReady() && rc.canPickUpUnit(target.getID())){
            rc.pickUpUnit(target.getID());
            return true;
        } else return false;
    }

    /**
     * tries to drop a robot in the given direction
     * @param dir where we droppin
     * @return true if dropped, false otherwise
     */
    protected boolean drop(Direction dir) throws GameActionException {
        if(rc.isReady() && rc.canDropUnit(dir)){
            rc.dropUnit(dir);
            return true;
        } else return false;
    }

    /**
     * tries to refine soup in the given dir
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
     * bids a message to the blockchain
     * @param message list of 7 ints
     * @param bid how high to bid
     */
    protected void bid(int[] message, int bid) throws GameActionException{
        if(rc.canSubmitTransaction(message, bid)){
            rc.submitTransaction(message, bid);
        }
    }

}

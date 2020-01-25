package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class StarportBot extends GameRobot {

    // keep in mind this is PER STARPORT
    private static final int DESIRED_DRONES = 5;

    private int droneCount = 0;

    public StarportBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init() throws GameActionException {

    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(droneCount < DESIRED_DRONES){
            if(build(RobotType.DELIVERY_DRONE, path.randomDir())) droneCount++;
        }
    }
}

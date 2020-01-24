package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class NetBot extends GameRobot {
    public NetBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init() throws GameActionException {
        return; //nothing to initialize
    }

    @Override
    public void loop(int turn) throws GameActionException {
        RobotInfo[] scan = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
        RobotInfo target = null;
        for(RobotInfo i : scan){
            if(shoot(i)) break;
        }
    }
}

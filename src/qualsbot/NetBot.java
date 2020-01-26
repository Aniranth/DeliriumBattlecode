package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

import java.util.ArrayList;

public class NetBot extends GameRobot {
    public NetBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {
        return; //nothing to initialize
    }

    @Override
    public void loop(int turn) throws GameActionException {
        ArrayList<RobotInfo> targets = new ArrayList<>();
        int shootRange = Math.min(rc.getCurrentSensorRadiusSquared(), RobotType.NET_GUN.sensorRadiusSquared);
        RobotInfo[] scan = rc.senseNearbyRobots(shootRange, rc.getTeam().opponent());
        for(RobotInfo i : scan){
            if(rc.canShootUnit(i.getID())) targets.add(i);
        }
        if(targets.size() < 1) return; // no targets, leave
        RobotInfo target = targets.get(0);
        int minDistSq = rc.getLocation().distanceSquaredTo(target.getLocation());
        for(RobotInfo option : targets){
            int distSq = rc.getLocation().distanceSquaredTo(option.getLocation());
            if(distSq < minDistSq){
                target = option;
                minDistSq = distSq;
            }
        }
        shoot(target);
    }
}

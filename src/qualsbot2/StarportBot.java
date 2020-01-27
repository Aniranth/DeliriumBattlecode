package qualsbot2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class StarportBot extends GameRobot {

    // keep in mind this is PER STARPORT
    private static final int DESIRED_DRONES = 24;

    private static final int ENDGAME_TURN = 600;
    private static final int SLOW_FACTOR = 20;

    private int droneCount = 0;

    public StarportBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {
        radio.sendStarportBuiltSignal();
    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(droneCount < DESIRED_DRONES && (turn > ENDGAME_TURN || turn % SLOW_FACTOR == 0)){
            if(build(RobotType.DELIVERY_DRONE, path.randomDir())) {
                if(droneCount++ == 0) radio.sendProphecySignal();
            }
        }
    }
}

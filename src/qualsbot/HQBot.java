package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class HQBot extends NetBot {

    private static final int DESIRED_MINERS = 3;

    private int minerCount = 0;

    public HQBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init() throws GameActionException {
        radio.HQLoc(rc.getLocation());
        System.out.println("gl hf");
    }

    @Override
    public void loop(int turn) throws GameActionException {
        super.loop(turn);
        if(minerCount < DESIRED_MINERS){
            if(build(RobotType.MINER, path.randomDir())) minerCount++;
        }
    }
}

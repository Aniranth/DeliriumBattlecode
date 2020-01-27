package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;

public class HQBot extends NetBot {

    private static final int DESIRED_MINERS = 3;
	private boolean endgame = false;

    private int minerCount = 0;

    private static final int ENDGAME_LANDSCAPER_COUNT = 10;

    public HQBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {
        radio.HQLoc(rc.getLocation());
        System.out.println("gl hf");
    }

    @Override
    public void loop(int turn) throws GameActionException {
        super.loop(turn);
        if(minerCount < DESIRED_MINERS){
            if(build(RobotType.MINER, path.randomDir())) minerCount++;
        }
		
		int landscaper_count = 0;
		
		for(RobotInfo robot : rc.senseNearbyRobots(8, rc.getTeam())) {
			if (robot.getType().equals(RobotType.LANDSCAPER)) {
				landscaper_count++;
			}
		}
		if(landscaper_count >= ENDGAME_LANDSCAPER_COUNT && !endgame) {
			endgame = true;
			radio.sendEndgameSignal();
		}
    }
}

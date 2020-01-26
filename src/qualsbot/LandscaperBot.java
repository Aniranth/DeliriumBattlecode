package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class LandscaperBot extends GameRobot {
	private MapLocation hqLoc = null;
	private MapLocation digLoc = null;
	
	private boolean in_place = false;
	private boolean can_you_dig_it = true;
	
	private static int[][] offsets = {
        new int[]{2,0},
        new int[]{-2,0},
		new int[]{0,2},
		new int[]{0,-2},
	};
	
    public LandscaperBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {

    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(hqLoc == null){
            hqLoc = radio.getHQLoc();
        }
		if(in_place && hqLoc != null) {
			for(int[] offset : offsets) {
				if(rc.getLocation.isAdjacentTo(hqLoc.add(new Direction(offset[0], offset[1])))) {
					digLoc = hqLoc.add(new Direction(offset[0], offset[1]))
				}
			}
		}
		
		if (rc.canDigDirt(rc.getLocation().directionTo(hqLoc))) {
			rc.digDirt(rc.getLocation().directionTo(hqLoc));
		}
		
		if(in_place) {
			if(can_you_dig_it) {
				if(rc.canDigDirt(digLoc) {
					rc.digDirt(digLoc);
				}
			} else {
				if(rc.canDepositDirt(Direction.CENTER)) {
					rc.depositDirt(Direction.CENTER)
				}
			}
		}
		
		can_you_dig_it = !(getDirtCarrying() >= 1);
	}
}

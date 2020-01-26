package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;

import java.util.ArrayList;

public class LandscaperBot extends GameRobot {
	private MapLocation hqLoc = null;
	private MapLocation digLoc = null;
	
	private boolean in_place = false;
	private boolean can_you_dig_it = true;
	
	private static ArrayList<MapLocation> wall_locs = new ArrayList<>();
	
	private static final int[][] DIG_OFFSETS = {
        new int[]{2,0},
        new int[]{-2,0},
		new int[]{0,2},
		new int[]{0,-2},
	};
	
	private static final int[][] POSITION_OFFSETS = {
		new int[]{-1,1},
		new int[]{0,1},
		new int[]{1,1},
		new int[]{-1,0},
		new int[]{1,0},
		new int[]{0,-1},
		new int[]{1,-1},
		new int[]{-1,-1},
	};
	
    public LandscaperBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {
		if((hqLoc = radio.getHQLoc()) != null) wall_locs = path.offsetsToLocations(POSITION_OFFSETS, hqLoc);
    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(hqLoc == null) hqLoc = radio.getHQLoc();
        if(hqLoc == null) return; // can't do anything without this. be obvious about being broke.
		if(wall_locs.size() == 0) wall_locs = path.offsetsToLocations(POSITION_OFFSETS, hqLoc);

		if(!in_place) {
			in_place = path.assimilate(wall_locs, hqLoc);
		} else {
			for(int[] offset : DIG_OFFSETS) {
				MapLocation dig_check = new MapLocation(hqLoc.x + offset[0], hqLoc.y + offset[1]);
				if(rc.getLocation().isAdjacentTo(dig_check)) {
					digLoc = dig_check;
				}
			}
		}
		
		if (rc.canDigDirt(rc.getLocation().directionTo(hqLoc))) {
			rc.digDirt(rc.getLocation().directionTo(hqLoc));
		}
		
		if(in_place) {
			if(can_you_dig_it) {
				if(rc.canDigDirt(rc.getLocation().directionTo(digLoc))) {
					rc.digDirt(rc.getLocation().directionTo(digLoc));
				}
			} else {
				if(rc.canDepositDirt(Direction.CENTER)) {
					rc.depositDirt(Direction.CENTER);
				}
			}
		}
		
		can_you_dig_it = !(rc.getDirtCarrying() >= 1);
	}
}

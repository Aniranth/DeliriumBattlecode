package qualsbot;

import battlecode.common.*;

import java.util.ArrayList;

public class MinerBot extends GameRobot {

    private int factoryCount = 0;
    private int starportCount = 0;
    private ArrayList<MapLocation> soupLocs = new ArrayList<MapLocation>();

    private MapLocation hqLoc = null;

    private static final int DESIRED_FACTORIES = 1;
    private static final int DESIRED_STARPORTS = 1;
	
	private static final int DISTANCE_TO_BUILD = 15; // Change to make buildings further from base
	private static final int BUILD_STARPORT = 200;
	
	private boolean constructor_bot;

    public MinerBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException{
		constructor_bot = turn == 2;
		System.out.println("I am a " + (constructor_bot?"Constructor":"Miner"));
        return; // nothing to init
    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(hqLoc == null){
            hqLoc = radio.getHQLoc();
        }
        factoryCount = radio.updateFactoryCount(factoryCount);
        starportCount = radio.updateStarportCount(starportCount);
        radio.updateSoupLoc(soupLocs);

        System.out.println("F: " + factoryCount + "; S: " + starportCount);

        targetSoup();

        // mine around us
        for(Direction dir : Pathfinder.directions){
            if(mine(dir)){
                MapLocation sloc = rc.getLocation().add(dir);
                if(!soupLocs.contains(sloc)){
                    soupLocs.add(sloc);
                    radio.soupLoc(sloc);
                }
            }
        }

        // refine around us
        for(Direction dir : Pathfinder.directions){
            if(refine(dir)) break;
        }

        // build necessary units
        if(factoryCount < DESIRED_FACTORIES && constructor_bot){
			if(rc.getLocation().distanceSquaredTo(hqLoc) >= DISTANCE_TO_BUILD) {
				if(build(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(hqLoc).opposite())){
					radio.sendFactoryCt(++factoryCount);
				}
			} else if(rc.getTeamSoup() >= RobotType.DESIGN_SCHOOL.cost){
				path.to(rc.getLocation().directionTo(hqLoc).opposite());
			}
        } else if(starportCount < DESIRED_STARPORTS && constructor_bot && turn >= BUILD_STARPORT){
			if(rc.getLocation().distanceSquaredTo(hqLoc) >= DISTANCE_TO_BUILD){
				if(build(RobotType.FULFILLMENT_CENTER, rc.getLocation().directionTo(hqLoc).opposite())){
					radio.sendStarportCt(++starportCount);
				}
			} else if(rc.getTeamSoup() >= RobotType.FULFILLMENT_CENTER.cost){
				path.to(rc.getLocation().directionTo(hqLoc).opposite());
			}
        }

        // manage where we're going and what we're doing
        if(rc.getSoupCarrying() >= RobotType.MINER.soupLimit){
            path.to(hqLoc);
        } else if(soupLocs.size() > 0) {
            path.to(soupLocs.get(0));
        } else {
            MapLocation sloc = path.findSoup();
            if(sloc != null && !soupLocs.contains(sloc)){
                soupLocs.add(sloc);
                radio.soupLoc(sloc);
                path.to(sloc);
            } else {
                path.move(path.randomDir());
            }
        }
    }


    private void targetSoup() throws GameActionException{
        MapLocation target;
        while(soupLocs.size() > 0){
            target = soupLocs.get(0);
            if(rc.canSenseLocation(target) && rc.senseSoup(target) == 0){
                soupLocs.remove(0);
            } else break;
        }
    }
}

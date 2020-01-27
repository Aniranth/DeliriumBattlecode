package qualsbot;

import battlecode.common.*;

import java.util.ArrayList;

public class MinerBot extends GameRobot {

    private int factoryCount = 0;
    private int starportCount = 0;
    private int refineryCount = 0;
    private ArrayList<MapLocation> soupLocs = new ArrayList<MapLocation>();

    private MapLocation hqLoc = null;
    private MapLocation soupDeposit = null;

    private static final int DESIRED_FACTORIES = 1;
    private static final int DESIRED_STARPORTS = 1;
    private static final int DESIRED_REFINERIES = 1;
	
	private static final int DISTANCE_TO_BUILD = 15; // Change to make buildings further from base
	private static final int BUILD_STARPORT = 400;
	private static final int MAKE_WALL = 400;
	
	private boolean constructor_bot;

    public MinerBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException{
		constructor_bot = turn == 2;
		System.out.println("I am a " + (constructor_bot?"Constructor":"Miner"));
        return;
    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(hqLoc == null) hqLoc = radio.getHQLoc();
        if(soupDeposit == null || soupDeposit.equals(hqLoc)) soupDeposit = radio.getRefineryLoc(hqLoc);
        radio.updateSoupLoc(soupLocs);

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

        if(constructor_bot && construct(turn)) return; // if we built, focus on that

		
//		if(turn >= MAKE_WALL) { //Get out of the way
//		    path.scout();
//			//rc.disintegrate(); //I meant it
//		}


        // manage where we're going and what we're doing
        if(rc.getSoupCarrying() >= RobotType.MINER.soupLimit){
            System.out.println("I want to deposit");
            path.to(soupDeposit);
        } else if(soupLocs.size() > 0) {
            System.out.println("I am going to soup");
            path.to(soupLocs.get(0));
        } else {
            System.out.println("I am searching for soup");
            MapLocation sloc = path.findSoup();
            if(sloc != null && !soupLocs.contains(sloc)){
                soupLocs.add(sloc);
                radio.soupLoc(sloc);
                path.to(sloc);
            } else {
                path.scout();
            }
        }
    }

    /**
     * construction method
     * @return true if we moved towards building something, false otherwise
     */
    private boolean construct(int turn) throws GameActionException{
        Direction buildDir = rc.getLocation().directionTo(hqLoc).opposite();
        Direction[] buildDirs = new Direction[] {buildDir, buildDir.rotateRight(), buildDir.rotateLeft()};
        if(refineryCount < DESIRED_REFINERIES){
            if(path.awayFrom(hqLoc, DISTANCE_TO_BUILD)){
                // if we're here, we are far enough.
                for(Direction d : buildDirs) {
                    if (build(RobotType.REFINERY, d)) {
                        refineryCount++;
                        soupDeposit = rc.getLocation().add(d);
                        radio.soupLoc(soupDeposit);
                        break;
                    }
                }
                return true;
            }
        } else if(factoryCount < DESIRED_FACTORIES){
            if(path.awayFrom(hqLoc, DISTANCE_TO_BUILD)){
                // if we're here, we are far enough.
                for(Direction d : buildDirs) {
                    if (build(RobotType.DESIGN_SCHOOL, d)) {
                        factoryCount++;
                        break;
                    }
                }
                return true;
            }
        } else if(starportCount < DESIRED_STARPORTS && turn > BUILD_STARPORT){
            if(path.awayFrom(hqLoc, DISTANCE_TO_BUILD)){
                // if we're here, we are far enough.
                for(Direction d : buildDirs) {
                    if (build(RobotType.FULFILLMENT_CENTER, d)) {
                        starportCount++;
                        break;
                    }
                }
                return true;
            }
        } else {
            constructor_bot = false; //  My work here is done
        }
        return false;
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

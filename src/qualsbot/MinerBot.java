package qualsbot;

import battlecode.common.*;

import java.util.ArrayList;

public class MinerBot extends GameRobot {

    private int factoryCount = 0;
    private int starportCount = 0;
    private int refineryCount = 0;
    private ArrayList<MapLocation> soupLocs = new ArrayList<MapLocation>();
	private boolean stay_put = false;
	private boolean islandTime = false;

    private MapLocation hqLoc = null;
    private MapLocation soupDeposit = null;

    private static final int DESIRED_FACTORIES = 1;
    private static final int DESIRED_STARPORTS = 1;
    private static final int DESIRED_REFINERIES = 1;

	private static final int FORMATION_THRESHOLD = 18;
	private static final int DISTANCE_TO_BUILD = 19; // Change to make buildings further from base
	private static final int BUILD_STARPORT = 100;

	private boolean constructor_bot;
	private boolean was_constructor_bot = false;
	private boolean fledBase = false;

	// island things
    private static final Direction[] BUILDING_DIRS = new Direction[]{Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST};
    private static boolean[] buildingsBuilt = new boolean[]{false, false, false, false};



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
        if (hqLoc == null) hqLoc = radio.getHQLoc();
//        if(soupDeposit == null || soupDeposit.equals(hqLoc)) soupDeposit = radio.getRefineryLoc(hqLoc);
        if (soupDeposit == null) soupDeposit = hqLoc;
        if (was_constructor_bot && radio.checkEndgameSignal()) {
            System.out.println("I have requested evac");
            stay_put = true;
        }
        if (radio.listenMinerSafeSignal()) {
            stay_put = false;
            islandTime = true;
        }
        if(islandTime){
            colonizeIsland();
            return;
        }else if (stay_put) {
		    radio.minerLoc(rc.getLocation());
			System.out.println("I am waiting for evac.");
			return;
		}
		System.out.println(constructor_bot);
		System.out.println(radio.checkEndgameSignal() && constructor_bot);
		
        radio.updateSoupLoc(soupLocs);

        targetSoup();

        // mine around us
        for(Direction dir : Pathfinder.directions){
            if(mine(dir)) {
                MapLocation sloc = rc.getLocation().add(dir);
                if (!soupLocs.contains(sloc)) {
                    soupLocs.add(0, sloc);
                    radio.soupLoc(sloc);
                }
                // check for nearby refinery
            }
        }

        if(!fledBase && rc.getLocation().distanceSquaredTo(hqLoc) > FORMATION_THRESHOLD){
            path.addBadSpaces(path.offsetsToLocations(DroneBot.OFFSETS, hqLoc));
            fledBase = true;
        }

        // refine around us
        for(Direction dir : Pathfinder.directions){
            if(refine(dir)) break;
        }

        if(constructor_bot && construct(turn)) return; // if we built, focus on that

        // manage where we're going and what we're doing
        if(rc.getSoupCarrying() >= RobotType.MINER.soupLimit){
            if (rc.getLocation().distanceSquaredTo(hqLoc) > DISTANCE_TO_BUILD) { // if we're mining away from home
                System.out.println("got here");
                findOrMakeNearbyRefinery();
            }
            // System.out.println("I want to deposit");
            path.to(soupDeposit);
        } else if(soupLocs.size() > 0) {
            // System.out.println("I am going to soup");
            path.to(soupLocs.get(0));
        } else {
            // System.out.println("I am searching for soup");
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

    private void findOrMakeNearbyRefinery() throws GameActionException {
        RobotInfo[] scan = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam());
        boolean refineryNearby = false;
        for (RobotInfo i : scan) {
            if (i.getType().equals(RobotType.REFINERY)) {
                refineryNearby = true;
                soupDeposit = i.getLocation();
                break;
            }
        }
        if (!refineryNearby) build(RobotType.REFINERY, rc.getLocation().directionTo(hqLoc).opposite());
    }

    private void colonizeIsland() throws GameActionException {
        RobotType[] buildingsToMake = new RobotType[]{RobotType.VAPORATOR, RobotType.VAPORATOR, RobotType.FULFILLMENT_CENTER, RobotType.VAPORATOR};
        for(int i = 0; i < buildingsToMake.length; i++){
            if(!buildingsBuilt[i] && !build(buildingsToMake[i], BUILDING_DIRS[i])) return;
            else buildingsBuilt[i] = true;
        }
        // if we're here we've made everything. congradulations!
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
                        radio.refineryLoc(soupDeposit);
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
			was_constructor_bot = true; // but not really
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

package qualsbot2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;

import java.util.ArrayList;

public class LandscaperBot extends GameRobot {
    private MapLocation hqLoc = null;
    private MapLocation digLoc = null;

    private boolean inPlace = false;
    private boolean amOuter = false;
    private boolean amIsland = false;
    private boolean canYouDigIt = true;

    private static ArrayList<MapLocation> digLocs = new ArrayList<>();
    public static final int[][] DIG_OFFSETS = {
            new int[]{2, 0},
            new int[]{-2, 0},
            new int[]{0, 2},
            new int[]{0, -2},
            // we only get here if we're on the corners
            new int[]{3,3},
            new int[]{3,-3},
            new int[]{-3,3},
            new int[]{-3,-3}
    };

    private static ArrayList<MapLocation> wallLocs = new ArrayList<>();
    public static final int[][] INNER_WALL_OFFSETS = {
            new int[]{-1, 1},
            new int[]{0, 1},
            new int[]{1, 1},
            new int[]{-1, 0},
            new int[]{1, 0},
            new int[]{0, -1},
            new int[]{1, -1},
            new int[]{-1, -1},
    };

    private static final int OUTER_WALL_HEIGHT = 50; // desired height of outer wall before working on inner wall
    private static ArrayList<MapLocation> supportLocs = new ArrayList<>();
    public static final int[][] OUTER_WALL_OFFSETS = {
            new int[]{1,2},
            new int[]{2,2},
            new int[]{2,1},
            new int[]{2,-1},
            new int[]{2,-2},
            new int[]{1,-2},
            new int[]{-1,-2},
            new int[]{-2,-2},
            new int[]{-2,-1},
            new int[]{-2,1},
            new int[]{-2,2},
            new int[]{-1,2}
    };

    public static final int ISLAND_HEIGHT = 75; // desired island plateau height
    private static final int ISLAND_DISTANCE = 25; // how far the island must be
    private static MapLocation islandLoc = null;
    private static ArrayList<MapLocation> islandLocs = new ArrayList<>();
    public static final int[][] ISLAND_OFFSETS = {
            new int[]{0,0},
            new int[]{0,1},
            new int[]{1,0},
            new int[]{-1,0},
            new int[]{0,-1}
    };

    public LandscaperBot(RobotController rc) throws GameActionException {
        super(rc);
        radio.setBid(1); // lowest possible bid
    }

    @Override
    protected void init(int turn) throws GameActionException {
        if ((hqLoc = radio.getHQLoc()) != null) {
            wallLocs = path.offsetsToLocations(INNER_WALL_OFFSETS, hqLoc);
            digLocs = path.offsetsToLocations(DIG_OFFSETS, hqLoc);
            path.addBadSpaces(digLocs);
        }
    }

    @Override
    public void loop(int turn) throws GameActionException {
        if (hqLoc == null) {
            init(turn); // try to properly init ourselves
            if (hqLoc == null) return; // can't do anything without this. be obvious about being broke.
        }

        if(amIsland){
            buildIsland();
            return; // the rest of the code in loop does not apply to you
        }

        if (!inPlace) {
            inPlace = path.assimilate(amOuter? supportLocs : wallLocs, hqLoc);
            if(wallLocs.size() < 1){
                wallLocs = path.offsetsToLocations(INNER_WALL_OFFSETS, hqLoc);
                supportLocs = path.offsetsToLocations(OUTER_WALL_OFFSETS, hqLoc);
                amOuter = true;
            }
            if(amOuter && supportLocs.size() < 1){
                System.out.println("I have given myself to the church of Island");
                amIsland = true;
                path.addBadSpaces(path.offsetsToLocations(OUTER_WALL_OFFSETS, hqLoc));
                path.forget();
                buildIsland();
            }
        } else {
            // choose a spot to dig
            if(digLoc == null) {
                for (MapLocation l : digLocs) {
                    if (rc.getLocation().isAdjacentTo(l)) {
                        digLoc = l;
                        break;
                    }
                }
            }

            // un-drown our HQ
            dig(hqLoc);

            if (canYouDigIt && digLoc != null) {
                dig(digLoc);
            } else {
                if(!amOuter || rc.senseElevation(rc.getLocation()) < OUTER_WALL_HEIGHT) {
                    dump(Direction.CENTER);
                } else {
                    // find the lowest point on the outer wall, then dump our dirt there
                    MapLocation dumpSpot = null;
                    for(int i = 0; i < wallLocs.size(); i++){
                        MapLocation candidate = wallLocs.get(i);
                        if(!rc.canSenseLocation(candidate) || !rc.canDepositDirt(rc.getLocation().directionTo(candidate))) {
                            // we only have results in here the first time we iterate through wallLocs
                            wallLocs.remove(i--);
                        } else {
                            if(dumpSpot == null || rc.senseElevation(dumpSpot) > rc.senseElevation(candidate)){
                                dumpSpot = candidate;
                            }
                        }
                    }
                    dump(dumpSpot);
                }
            }

            canYouDigIt = !(rc.getDirtCarrying() >= 1);
        }
    }

    private boolean validIslandLoc(MapLocation m){
        return !(rc.getLocation().x % 2 == 1 && rc.getLocation().y % 2 == 1);
    }

    /**
     * make the island!
     */
    private void buildIsland() throws GameActionException { //TODONE landscaper kinda just stands there confused, in Awe of the sheer power of ISLAND
        System.out.println("Praise Island!");
        if(islandLoc == null){
            islandLoc = path.findSpotForIsland(hqLoc, ISLAND_DISTANCE);
        }
        if(rc.getLocation().equals(islandLoc)){
            if(islandLocs == null || islandLocs.size() < 1){
                islandLocs = path.offsetsToLocations(ISLAND_OFFSETS, islandLoc);
            }

            if (canYouDigIt) {
                dig(Direction.NORTHWEST);
            } else {
                boolean allDone = true;
                for(MapLocation space : islandLocs){
                    if(rc.senseElevation(space) < ISLAND_HEIGHT){
                        dump(space);
                        allDone = false;
                        break;
                    }
                }
                if(allDone){
                    System.out.println("All done");
                    radio.islandLoc(islandLoc);
                    path.moveUnsafe(Direction.NORTHEAST);
//                    if(radio.listenMinerSafeSignal()){
//                        System.out.println("I heard you");
//                        path.moveUnsafe(Direction.NORTHWEST);
//                    } else {
//                        dump(Direction.NORTHWEST);
//                    }
                }
            }

            canYouDigIt = !(rc.getDirtCarrying() >= 1);
        } else {
            path.to(islandLoc);
        }
    }
}

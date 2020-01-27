package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;

import java.util.ArrayList;

public class LandscaperBot extends GameRobot {
    private MapLocation hqLoc = null;
    private MapLocation digLoc = null;

    private boolean inPlace = false;
    private boolean amOuter = false;
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

    public LandscaperBot(RobotController rc) throws GameActionException {
        super(rc);
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


        if (!inPlace) {
            inPlace = path.assimilate(amOuter? supportLocs : wallLocs, hqLoc);
            if(wallLocs.size() < 1){
                wallLocs = path.offsetsToLocations(INNER_WALL_OFFSETS, hqLoc);
                supportLocs = path.offsetsToLocations(OUTER_WALL_OFFSETS, hqLoc);
                amOuter = true;
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
                System.out.println("I am digging from " + digLoc);
                dig(digLoc);
            } else {
                if(!amOuter || rc.senseElevation(rc.getLocation()) < OUTER_WALL_HEIGHT) {
                    dump(Direction.CENTER);
                } else {
                    // find the lowest point on the outer wall, then dump our dirt there
                    MapLocation dumpSpot = null;
                    for(int i = 0; i < wallLocs.size(); i++){
                        MapLocation candidate = wallLocs.get(i);
                        if(!rc.canDepositDirt(rc.getLocation().directionTo(candidate))) {
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
}

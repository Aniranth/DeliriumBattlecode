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

    private static ArrayList<MapLocation> wallLocs = new ArrayList<>();
    private static ArrayList<MapLocation> digLocs = new ArrayList<>();

    private static final int[][] DIG_OFFSETS = {
            new int[]{2, 0},
            new int[]{-2, 0},
            new int[]{0, 2},
            new int[]{0, -2},
    };

    private static final int[][] POSITION_OFFSETS = {
            new int[]{-1, 1},
            new int[]{0, 1},
            new int[]{1, 1},
            new int[]{-1, 0},
            new int[]{1, 0},
            new int[]{0, -1},
            new int[]{1, -1},
            new int[]{-1, -1},
    };

    public LandscaperBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {
        if ((hqLoc = radio.getHQLoc()) != null) {
            wallLocs = path.offsetsToLocations(POSITION_OFFSETS, hqLoc);
            digLocs = path.offsetsToLocations(DIG_OFFSETS, hqLoc);
        }
    }

    @Override
    public void loop(int turn) throws GameActionException {
        if (hqLoc == null) {
            init(turn); // try to properly init ourselves
            if (hqLoc == null) return; // can't do anything without this. be obvious about being broke.
        }

        if (!in_place) {
            in_place = path.assimilate(wallLocs, hqLoc);
        } else {
            // choose a spot to dig
            if(digLoc == null) {
                for (MapLocation l : digLocs) {
                    if (rc.getLocation().isAdjacentTo(l)) {
                        digLoc = l;
                    }
                }
            }

            // un-drown our HQ
            if (rc.canDigDirt(rc.getLocation().directionTo(hqLoc))) {
                rc.digDirt(rc.getLocation().directionTo(hqLoc));
            }

            if (can_you_dig_it && digLoc != null) {
                dig(digLoc);
            } else {
                dump(Direction.CENTER);
            }

            can_you_dig_it = !(rc.getDirtCarrying() >= 1);
        }
    }
}

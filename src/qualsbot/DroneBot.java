package qualsbot;

import battlecode.common.*;

import java.util.ArrayList;

public class DroneBot extends GameRobot {

    private static MapLocation hqLoc = null;

    // order in which to line up for the WALL
    public static final int[][] OFFSETS = {
            new int[]{3,3},
            new int[]{3,2},
            new int[]{3,1},
            new int[]{3,0},
            new int[]{3,-1},
            new int[]{3,-2},
            new int[]{3,-3},
            new int[]{2,-3},
            new int[]{1,-3},
            new int[]{0,-3},
            new int[]{-1,-3},
            new int[]{-2,-3},
            new int[]{-3,-3},
            new int[]{-3,-2},
            new int[]{-3,-1},
            new int[]{-3,0},
            new int[]{-3,1},
            new int[]{-3,2},
            new int[]{-3,3},
            new int[]{-2,3},
            new int[]{-1,3},
            new int[]{0,3},
            new int[]{1,3},
            new int[]{2,3},
    };

    // queue to check out
    private static ArrayList<MapLocation> buildPath = new ArrayList<>();

    private static boolean inWall = false; // am I in formation?
    private boolean chosen = false;  // do we save the miner?
    private MapLocation minerLoc = null;
    private MapLocation islandLoc = null;

    public DroneBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {
        if((hqLoc = radio.getHQLoc()) != null) buildPath = path.offsetsToLocations(OFFSETS, hqLoc);
    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(hqLoc == null) hqLoc = radio.getHQLoc();
        if(hqLoc == null) return; // if we can't find the HQ, something's gone terribly wrong. Be obvious about it.
        if(chosen){
            fulfillProphecy();
            return; // we're too important for the wall
        }
        if(buildPath.size() == 0) buildPath = path.offsetsToLocations(OFFSETS, hqLoc);

        if(!inWall){
            inWall = path.assimilate(buildPath, hqLoc);
        } else {
            if(radio.listenPullbackSignal()){
                move(rc.getLocation().directionTo(hqLoc));
            }
            System.out.println("Awaiting orders");
            return;
        }
    }

    private void fulfillProphecy() throws GameActionException {
        if(islandLoc == null){
            islandLoc = radio.getIslandLoc();
        }
        if(minerLoc == null){
            minerLoc = radio.getMinerLoc();
        }
        if(minerLoc == null && islandLoc == null) path.scout();
        if(islandLoc != null && rc.isCurrentlyHoldingUnit()){
            if(!drop(islandLoc)) {
                path.to(islandLoc.add(Direction.SOUTHWEST));
            }
        }
        if(minerLoc != null){
            if(rc.getLocation().isAdjacentTo(minerLoc)){
                RobotInfo miner = null;
                if((miner = rc.senseRobotAtLocation(minerLoc)) != null){
                    grab(miner);
                    radio.sendMinerSafeSignal();
                }
            } else {
                path.to(minerLoc);
            }
        }
    }
}

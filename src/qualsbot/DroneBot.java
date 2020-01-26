package qualsbot;

import battlecode.common.*;

import java.util.ArrayList;

public class DroneBot extends GameRobot {

    private static MapLocation hqLoc = null;

    // order in which to line up for the WALL
    private static final int[][] OFFSETS = {
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

    public DroneBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {
        if((hqLoc = radio.getHQLoc()) != null) buildPath = path.offsetsToLocations(OFFSETS, hqLoc);
    }

//    private void initBuildPath(){
//        if(hqLoc == null) return; // can't do anything without the HQ loc
//        int cx = hqLoc.x; // center x
//        int cy = hqLoc.y; // center y
//        int mx = rc.getMapHeight()-1; // max x
//        int my = rc.getMapWidth()-1; // max y
//        for(int[] offset : OFFSETS){
//            int x = cx + offset[0];
//            int y = cy + offset[1];
//            if(x > mx || y > my || x < 0 || y < 0) continue; // throw out impossible spaces
//            buildPath.add(new MapLocation(x,y));
//        }
//    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(hqLoc == null) hqLoc = radio.getHQLoc();
        if(hqLoc == null) return; // if we can't find the HQ, something's gone terribly wrong. Be obvious about it.
        if(buildPath.size() == 0) buildPath = path.offsetsToLocations(OFFSETS, hqLoc);

        if(!inWall){
            inWall = path.assimilate(buildPath, hqLoc);
        } else {
            // TODO await orders from HQ
            System.out.println("Awaiting Orders");
            return;
        }
    }
}

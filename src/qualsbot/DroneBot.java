package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;

public class DroneBot extends GameRobot {

    private static MapLocation hqLoc = null;

    // order in which to line up for the WALL
    private static int[][] offsets = {
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
    protected void init() throws GameActionException {
        if((hqLoc = radio.getHQLoc()) != null) initBuildPath();
    }

    private void initBuildPath(){
        if(hqLoc == null) return; // can't do anything without the HQ loc
        int cx = hqLoc.x; // center x
        int cy = hqLoc.y; // center y
        int mx = rc.getMapHeight()-1; // max x
        int my = rc.getMapWidth()-1; // max y
        for(int[] offset : offsets){
            int x = cx + offset[0];
            int y = cy + offset[1];
            if(x > mx || y > my || x < 0 || y < 0) continue; // throw out impossible spaces
            buildPath.add(new MapLocation(x,y));
        }
    }

    @Override
    public void loop(int turn) throws GameActionException {
        if(hqLoc == null) hqLoc = radio.getHQLoc();
        if(hqLoc == null) return; // if we can't find the HQ, something's gone terribly wrong. Be obvious about it.
        if(buildPath.size() == 0) initBuildPath();

        if(!inWall){
            if(buildPath.size() < 1){
                path.to(hqLoc);
            } else {
                for(MapLocation post : buildPath){
                    // if we don't know if a post is ok OR if we know it's ok, go to it
                    if(!rc.canSenseLocation(post) || !rc.isLocationOccupied(post)) {
                        path.to(post);
                        return;
                    }
                    // else, try the next post
                }
                // if we are here, the wall is finished
                // because we can sense each space, and know they are all occupied.
                if(buildPath.contains(rc.getLocation())) inWall = true; // we are in the right place!
                else path.to(hqLoc);
            }
        } else {
            // TODO await orders from HQ
            return;
        }
    }
}

package qualsbot;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Map;

/**
 * class for moving around the map
 */
public class Pathfinder {
    private RobotController rc;

    private Direction scoutDir;

    static Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };

    public Pathfinder(RobotController rc_param){
        rc = rc_param;
        scoutDir = randomDir();
    }

    /**
     * returns whether the space immediately in the given direction is NOT flooded
     * @param type type of robot (here because sometimes we want to check for someone else)
     * @param dir direction to check in
     * @return true if type is drone or space is not flooded, false otherwise
     */
    public boolean sinkSafe(RobotType type, Direction dir) throws GameActionException{
        if(type == RobotType.DELIVERY_DRONE) return true;
        return !rc.senseFlooding(rc.adjacentLocation(dir));
    }

    /**
     * overload for readability
     */
    public boolean sinkSafe(Direction dir) throws GameActionException{
        return sinkSafe(rc.getType(), dir);
    }

    /**
     * attempts to move in the given direction
     * @param dir direction to move in
     * @return true if we move; false otherwise.
     */
    public boolean move(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMove(dir) && sinkSafe(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * run in a straight direction; if we hit a wall, try a different direction.
     */
    public void scout() throws GameActionException {
        // try to scout in the same dir
        if(!move(scoutDir)){
            // try turning
            scoutDir = scoutDir.rotateRight();
            // avoids following the wall
            if(!move(scoutDir)){
                scoutDir = scoutDir.opposite();
                scout();
            }
        }
    }

    /**
     * pathfinding (currently lifted straight from lecturebot)
     * @param loc location to move to
     * @return true if we move, false otherwise
     */
    public boolean to(MapLocation loc) throws GameActionException {
        if(loc == null) return false;
        return to(rc.getLocation().directionTo(loc));
    }

    /**
     * pathfinding (currently lifted straight from lecturebot)
     * @param dir direction to move towards
     * @return true if we move, false otherwise
     */
    public boolean to(Direction dir) throws GameActionException{
        if(dir == null) return false;
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(),
                dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry){
            if(move(d))
                return true;
        }
        return false;
    }

    private MapLocation[] allSpacesInRadius(){
        return allSpacesInRadius(rc.getCurrentSensorRadiusSquared());
    }

    /**
     * WARNING WARNING WARNING
     * THIS IS SLOW AF
     * TRY TO AVOID CALLING THIS EXCEPT WHEN TOTALLY NECESSARY
     * @param r radius to check
     * @return an array of every square in our radius
     */
    private MapLocation[] allSpacesInRadius(int r){
        int max_x = rc.getLocation().x + r;
        int min_x = rc.getLocation().x - r;
        int max_y = rc.getLocation().y + r;
        int min_y = rc.getLocation().y - r;

        ArrayList<MapLocation> locs = new ArrayList<MapLocation>();

        // check all the squares in a box around us for if we can scan them
        // yes we waste a little time at the corners but it's an easy implementation
        for(int y = min_y; y <= max_y; y++){
            for(int x = min_x; x <= max_x; x++){
                MapLocation square = new MapLocation(x,y);
                if(!rc.getLocation().isWithinDistanceSquared(square, r)) continue;
                if(rc.canSenseLocation(square)) locs.add(square);
            }
        }

        return locs.toArray(new MapLocation[locs.size()]);
    }

    /**
     * get all spaces r distancesq away
     * @param r radius of circle in distancesq
     * @return all squares in that distance
     */
    private MapLocation[] quickScanRadius(int r){
        //TODO
        return null;
    }

    public MapLocation findSoup() throws GameActionException{
        for(MapLocation square : allSpacesInRadius()){
            if(rc.senseSoup(square) > 0) return square;
        }
        return null;
    }

    /**
     * work on creating a structure collaboratively
     * @param structure spaces that need to be filled, in order of what needs to be filled first
     * @param fallback location to path to if all the spaces are filled
     * @return true if the structure is done, false otherwise
     */
    public boolean assimilate(ArrayList<MapLocation> structure, MapLocation fallback) throws GameActionException {
        MapLocation targetPost = fallback;
        while(structure.size() > 0){
            MapLocation post = structure.get(0);
            // if we don't know a post is ok OR if we know it's ok, go to it
            if(!rc.canSenseLocation(post) || !rc.isLocationOccupied(post)) {
                targetPost = post;
                break;
            }
            // that post is occupied by a friendly drone? it's all good, forget about it.
            RobotInfo occupier = rc.senseRobotAtLocation(post);
            boolean occupier_friendly = occupier.getTeam().equals(rc.getTeam());
            boolean occupier_drone = occupier.getType().equals(RobotType.DELIVERY_DRONE);
            if(occupier_friendly && occupier_drone) structure.remove(post);
        }
        System.out.println("pathing to x: " + targetPost.x + ",y: " + targetPost.y);
        this.to(targetPost);
        if(rc.getLocation().equals(targetPost)) {
            return true; // we are in the right place!
        }
        return false;
    }

    public Direction randomDir(){
        return directions[(int) (Math.random() * directions.length)];
    }
}

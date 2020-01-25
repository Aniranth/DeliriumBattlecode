package qualsbot;

import battlecode.common.*;

import java.util.ArrayList;

public class MinerBot extends GameRobot {

    private int factoryCount = 0;
    private int starportCount = 0;
    private ArrayList<MapLocation> soupLocs = new ArrayList<MapLocation>();

    private MapLocation hqLoc = null;

    // you're gonna get a little more than this for various reasons
    private static final int DESIRED_FACTORIES = 3;
    private static final int DESIRED_STARPORTS = 3;

    public MinerBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init() throws GameActionException{
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
        if(factoryCount < DESIRED_FACTORIES){
            if(build(RobotType.DESIGN_SCHOOL, path.randomDir())){
                radio.sendFactoryCt(++factoryCount);
            }
        } else if(starportCount < DESIRED_STARPORTS){
            if(build(RobotType.FULFILLMENT_CENTER, path.randomDir())){
                radio.sendFactoryCt(++starportCount);
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

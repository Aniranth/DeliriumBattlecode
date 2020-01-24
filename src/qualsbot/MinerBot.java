package qualsbot;

import battlecode.common.*;

import java.util.ArrayList;

public class MinerBot extends GameRobot {

    private int factoryCount = 0;
    private int starportCount = 0;
    private ArrayList<MapLocation> soupLocs = new ArrayList<MapLocation>();

    private MapLocation hqLoc = null;

    private static final int DESIRED_FACTORIES = 3;
    private static final int DESIRED_STARPORTS = 3;

    public MinerBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init() throws GameActionException{
        hqLoc = radio.getHQLoc();
    }

    @Override
    public void loop(int turn) throws GameActionException {
        factoryCount = radio.updateFactoryCount();
        starportCount = radio.updateStarportCount();
        radio.updateSoupLoc(soupLocs);

        while(targetSoup());

        // mine around us
        for(Direction dir : Pathfinder.directions){
            if(refine(dir)){
                MapLocation sloc = rc.getLocation().add(dir);
                if(!soupLocs.contains(sloc)){
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
        } else if(soupLocs.size() > 0){
            path.to(soupLocs.get(0));
        } else {
            path.move(path.randomDir());
        }
    }


    private boolean targetSoup() throws GameActionException{
        if(soupLocs.size() > 0){
            MapLocation target = soupLocs.get(0);
            if(rc.canSenseLocation(target) && rc.senseSoup(target) == 0){
                soupLocs.remove(0);
                return true;
            }
        }
        return false;
    }
}

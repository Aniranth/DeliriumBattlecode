package buryandkill;

import battlecode.common.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class RobotPlayer {
    static RobotController rc;

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

    static Direction[][] dir_box = {
            {Direction.NORTHWEST, Direction.NORTH, Direction.NORTHEAST},
            {Direction.WEST, Direction.CENTER, Direction.EAST},
            {Direction.SOUTHWEST, Direction.SOUTH, Direction.SOUTHEAST}
    };

    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;

    static RobotInfo enemy_HQ = null;
    static RobotInfo my_HQ = null;
    static int num_factories = 0; // number of design schools built
    static int desired_factories = 2; // number of factories we want per miner

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        buryandkill.RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
                    case NET_GUN:            runNetGun();            break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runHQ() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.MINER, dir);
    }

    static void runMiner() throws GameActionException {
        tryBlockchain();
        tryMove(randomDirection());
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        if(num_factories < desired_factories || rc.getTeamSoup() > RobotType.DESIGN_SCHOOL.cost) {
            System.out.println("It's time to get educated");
            for (Direction dir : directions)
                if (tryBuild(RobotType.DESIGN_SCHOOL, dir)){
                    num_factories++;
                }
        } else {
            for (Direction dir : directions)
                tryBuild(RobotType.FULFILLMENT_CENTER, dir);
            for (Direction dir : directions)
                if (tryRefine(dir))
                    System.out.println("I refined soup! " + rc.getTeamSoup());
            for (Direction dir : directions)
                if (tryMine(dir))
                    System.out.println("I mined soup! " + rc.getSoupCarrying());
        }
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static final int DESIRED_LANDSCAPERS_PER_FACTORY = 2;
    static int landscapers_built = 0;
    static void runDesignSchool() throws GameActionException {
        if(landscapers_built > DESIRED_LANDSCAPERS_PER_FACTORY) return;
        System.out.println("RUSHRUSHRUSH");
        for (Direction dir : directions)
            if(tryBuild(RobotType.LANDSCAPER, dir)){
                landscapers_built++;
            }
    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    /*
    static void pathTowards(MapLocation goal) throws GameActionException{
        // + distance means goal is right/up. - distance means goal is left/down
        int[] dir_as_int = new int[]{Integer.signum(goal.x - rc.getLocation().x), Integer.signum(goal.y - rc.getLocation().y)};
        Direction move_dir = dir_box[dir_as_int[0] + 1][dir_as_int[1] + 1]; // 0 1 2; 0 1 2; 0 1 2
        ArrayList<Direction> dir_list = new ArrayList<>(Arrays.asList(directions));
        Collections.sort(dir_list, new Comparator<Direction>() {
            @Override
            public int compare(Direction o1, Direction o2) {
                int x_gap1 = move_dir.getDeltaX() - o1.getDeltaX();
                int x_gap2 = move_dir.getDeltaX() - o2.getDeltaX();
                int y_gap1 = move_dir.getDeltaY() - o1.getDeltaY();
                int y_gap2 = move_dir.getDeltaY() - o2.getDeltaY();
                return (x_gap1*x_gap1 + y_gap1*y_gap1) - (x_gap2*x_gap2 + y_gap2*y_gap2); // score by distance vector length
            }
        });
        for (Direction dir : dir_list) if(tryMove(dir)) break;
    }
    */

    static void runLandscaper() throws GameActionException {
        // move randomly until we detect the enemy base
        // then rush the bitch
        // then dig up dirt
        // then bury the bitch
        RobotInfo[] scan = rc.senseNearbyRobots();
        for(RobotInfo i : scan){
            if(i.getTeam() == rc.getTeam().opponent()){
                if(i.getType() == RobotType.HQ){
                    // WE FOUND THE BITCH
                    enemy_HQ = i;
                    break;
                }
            }
        }
        if(enemy_HQ != null){
            tryMove(randomDirection());
        } else if(!rc.getLocation().isAdjacentTo(enemy_HQ.getLocation())){
            tryMove(rc.getLocation().directionTo(enemy_HQ.getLocation()));
        } else {
            if(rc.getDirtCarrying() < RobotType.LANDSCAPER.dirtLimit){
                for (Direction dir : directions)
                    if (tryMine(dir)) {
                        System.out.println("Dirt acquired: " + rc.getDirtCarrying());
                    } else {
                        rc.depositDirt(rc.getLocation().directionTo(enemy_HQ.getLocation()));
                    }
            }
        }

    }

    static boolean is_a(RobotType t, RobotType[] type_arr){
        for(RobotType ty : type_arr){
            if(t == ty) return true;
        }
        return false;
    }

    static final int MAX_HOMEGUARD = 5; // how many people want to guard home? (plus home base)
    static Boolean station_to_defend = null;
    static MapLocation dunk_zone = null;
    static RobotInfo target = null;
    static RobotType[] grabbables = new RobotType[]{RobotType.MINER, RobotType.LANDSCAPER};
    static final int HOMEGUARD_DISTANCE = 4;
    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();

        /* priorities:
         * If we are holding an enemy robot, we dunk it in the lake!
         * If we see an enemy robot, we pick it up
         * If we see our charge swarmed, swap charges
         * If we see our charge, defend it
         * If we see nothing, we search
         */

        RobotInfo[] scan = rc.senseNearbyRobots();
        // my_HQ and enemy_HQ are the other important things
        for(RobotInfo i : scan){
            if(my_HQ == null && i.getTeam() != enemy && i.getType() == RobotType.HQ){
                my_HQ = i;
                station_to_defend = true;
            } else if(enemy_HQ == null && i.getTeam() == enemy && i.getType() == RobotType.HQ){
                enemy_HQ = i;
                station_to_defend = false;
            } else if(target == null && i.getTeam() == enemy && is_a(i.getType(), grabbables)){
                target = i;
            }
        }

        if (rc.isCurrentlyHoldingUnit()) {
            if (dunk_zone == null) {
                // scan all spaces in sensor radius for a lake
                final int hbsl = 2; // half box side length
                scan:
                for (int y = -hbsl; y <= hbsl; y++) {
                    for (int x = -hbsl; x <= hbsl; x++) {
                        MapLocation sq_to_check = new MapLocation(rc.getLocation().x + x, rc.getLocation().y + y);
                        if (rc.senseFlooding(sq_to_check)) {
                            dunk_zone = sq_to_check;
                            break scan;
                        }
                    }
                }
            }
            if (dunk_zone == null) { // if it's still null
                tryMove((randomDirection()));
            } else if (rc.getLocation().isAdjacentTo(dunk_zone)) {
                if(tryDrop(rc.getLocation().directionTo(dunk_zone))){
                    target = null;
                }
            } else {
                tryMove(rc.getLocation().directionTo(dunk_zone));
            }
        } else if(target != null) {
            if(rc.canPickUpUnit(target.getID())){
                rc.pickUpUnit(target.getID());
            } else {
                tryMove(rc.getLocation().directionTo(target.getLocation()));
            }
        } else if(station_to_defend != null) {
            RobotInfo home = station_to_defend ? my_HQ : enemy_HQ;
            if(home == null) {
                tryMove((randomDirection())); // search for the other base
            }
            if(!rc.getLocation().isWithinDistanceSquared(home.getLocation(), HOMEGUARD_DISTANCE)){
                tryMove(rc.getLocation().directionTo(home.getLocation()));
            } else if(rc.senseNearbyRobots(HOMEGUARD_DISTANCE, rc.getTeam()).length > MAX_HOMEGUARD){
                station_to_defend = !station_to_defend;
            }
        } else {
            // we have no idea where anything is, just drift
            tryMove((randomDirection()));
        }
    }

    static void runNetGun() throws GameActionException {
        // shoot anything that moves (from the enemy team)
        RobotInfo[] scan = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
        RobotInfo target = null;
        for(RobotInfo i : scan){
            if(rc.canShootUnit(i.getID())){
                target = i;
                break;
            }
        }
        rc.shootUnit(target.getID());
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        // we can no longer kill ourselves without purposeful intent (if you wanna die, do it explicitly)
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(rc.adjacentLocation(dir))) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    static boolean tryDrop(Direction dir) throws GameActionException {
        if(rc.isReady() && rc.canDropUnit(dir)){
            rc.dropUnit(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }


    static void tryBlockchain() throws GameActionException {
        if (turnCount < 3) {
            int[] message = new int[7];
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        }
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }
}


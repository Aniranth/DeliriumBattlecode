package deliriumv1;
import battlecode.common.*;

public strictfp class RobotPlayer {
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

    static int corner = -1; //Find corner of the map 0: Top Left, 1: Top Right, 2:Bottom Left, 3: Bottom Right

    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    static MapLocation hqLocation = null;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        //System.out.println("I'm a " + rc.getType() + " and I just got created!");
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                //System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
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
        // Lets build a list of directions that are away from the edges of the map
        for (Direction dir : directions)
            tryBuild(RobotType.MINER, dir);
    }

    static void runMiner() throws GameActionException {
        if (hqLocation == null) {
            grabHQLocation();
        }

        if (corner == -1) {
            grabExploreDirections();
        }
        
        rc.setIndicatorDot(hqLocation, 255, 255, 255);

        //TODO: Decide closest refinery to deliver
        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) { // Lets return to base to deliver soup
            Direction pathToHQ = rc.getLocation().directionTo(hqLocation);

            if (!rc.canDepositSoup(pathToHQ))
            {
                tryMove(pathToHQ);
            }
            else
            {
                if (tryRefine(pathToHQ))
                        System.out.println("Soup refined.");
            }
        }
        else { // We must find soup to mine (do we want to go back only when full or also when we deplete?)
            Direction maxSoupDir = null;
            int maxSoupCount = 0;
            for (Direction dir : directions) {
                int soupCount = rc.senseSoup(rc.adjacentLocation(dir));
                if (soupCount > maxSoupCount) {
                    maxSoupDir = dir;
                    maxSoupCount = soupCount;
                }
            }
            
            if (maxSoupCount != 0) {
                tryMine(maxSoupDir);
            }
            else {
                // TODO: Better movement logic than just going
                // Based on where we start lets explore in the direction we have the most ability to explore.
                // diagonal.
                switch (corner) { //TODO: Make corner numbers enums
                    case 0: tryMove(Direction.SOUTHEAST);
                            break;
                    case 1: tryMove(Direction.SOUTHWEST);
                            break;
                    case 2: tryMove(Direction.NORTHEAST);
                            break;
                    case 3: tryMove(Direction.NORTHWEST);
                            break;
                    default:
                            System.out.println("Undefined case");
                }
            }
        }

        //for (Direction dir : directions)
            //tryBuild(RobotType.FULFILLMENT_CENTER, dir);
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {

    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {

    }

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection());
        }
    }

    static void runNetGun() throws GameActionException {

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
        if (rc.isReady() && rc.canMove(dir)) {
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

    static void grabHQLocation()
    {
        RobotInfo[] nearby = rc.senseNearbyRobots();
        for(RobotInfo robots : nearby)
        {
            if(robots.type == RobotType.HQ)
            {
                hqLocation = robots.location;
            }
        }    
    }

    static void grabExploreDirections()
    {
        //System.out.println("Got here");
        int distanceToTopLeft = hqLocation.distanceSquaredTo(new MapLocation(0, rc.getMapHeight()));
        int distanceToTopRight = hqLocation.distanceSquaredTo(new MapLocation(rc.getMapWidth(), rc.getMapHeight()));
        int distanceToBottomLeft = hqLocation.distanceSquaredTo(new MapLocation(0, 0));
        int distanceToBottomRight = hqLocation.distanceSquaredTo(new MapLocation(rc.getMapWidth(), 0));
        
        if (distanceToTopLeft <=  distanceToTopRight &&
                distanceToTopLeft <= distanceToBottomLeft &&
                distanceToTopLeft <= distanceToBottomRight) {
            corner = 0;
            System.out.println("We are in the Top Left");
        }
        else if (distanceToTopRight <= distanceToTopLeft &&
                distanceToTopRight <= distanceToBottomLeft &&
                distanceToTopRight <= distanceToBottomRight) {
            corner = 1;
            System.out.println("We are in the Top Right");
        }
        else if (distanceToBottomLeft <= distanceToTopLeft &&
                distanceToBottomLeft <= distanceToTopRight &&
                distanceToBottomLeft <= distanceToBottomRight) {
            corner = 2;
            System.out.println("We are in the Bottom Left");
        }
        else {
            corner = 3;
            System.out.println("We are in the Bottom Right");
        }
    }
}

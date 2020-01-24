package qualsbot;


import battlecode.common.*;

public class RobotPlayer {
    static RobotController rc;


    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        GameRobot r = null;
        int turnCount = 0;

        switch(rc.getType()) {
            case HQ:                    r = new HQBot(rc);                  break;
            case MINER:                 r = new MinerBot(rc);               break;
            case REFINERY:              r = new RefineryBot(rc);            break;
            case VAPORATOR:             r = new VaporatorBot(rc);           break;
            case DESIGN_SCHOOL:         r = new FactoryBot(rc);             break;
            case FULFILLMENT_CENTER:    r = new StarportBot(rc);            break;
            case LANDSCAPER:            r = new LandscaperBot(rc);          break;
            case DELIVERY_DRONE:        r = new DroneBot(rc);               break;
            case NET_GUN:               r = new NetBot(rc);                 break;
        }
        while(r != null) {
            try {
                r.loop(++turnCount);
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Exception caught: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Failed to initialize");
        while(true) Clock.yield(); // do nothing
    }

}

package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class LandscaperBot extends GameRobot {
    public LandscaperBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException {

    }

    @Override
    public void loop(int turn) throws GameActionException {
        path.move(path.randomDir()); // TODO
    }
}

package qualsbot2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class FactoryBot extends GameRobot {

    // keep in mind this is PER FACTORY
    private static final int DESIRED_LANDSCAPERS = 21;

    private static final int OVERFLOW_THRESHOLD = 1000;

    private boolean isTime = false;
    private int lsCount = 0;

    public FactoryBot(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    protected void init(int turn) throws GameActionException{

    }

    @Override
    public void loop(int turn) throws GameActionException {
        isTime = isTime || radio.listenStarportBuildSignal();
        if(isTime && lsCount < DESIRED_LANDSCAPERS){
            if(build(RobotType.LANDSCAPER, path.randomDir())) lsCount++;
        }
    }
}

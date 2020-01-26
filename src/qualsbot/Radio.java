package qualsbot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Transaction;

import java.util.ArrayList;

/**
 * class for interfacing with the blockchain
 */
public class Radio {
    private RobotController rc;
    /**
     * we keep message outside, only update what we need to for the new message
     * hopefully it confuses our opponents
     */
    private int[] message;
    private int bid = 2;

    /**
     * inits this radio
     * @param rc_param us
     */
    public Radio(RobotController rc_param){
        rc = rc_param;
        message = new int[7];
        message[TID] = TEAM_IDENTIFIER;
        message[MISC] = rc.getID();
        if(!sanityCheck()) System.out.println("Our radio is busted :c");
    }

    public void setBid(int newBid){
        bid = newBid;
    }

    /**
     * team ID can be changed with little to no effect on the code
     */
    private static final int TEAM_IDENTIFIER = 133769666;

    /**
     * UNENCODED MESSAGE FORMAT (we read backwards, 6 to 0):
     * 6    -   team identifier
     * 5    -   message type
     * 4    -   x position
     * 3    -   sender ID
     * 2    -   y position
     * 1    -   building count
     * 0    -   "random" value that rounds out the sum
     */
    private static final int TID = 6;
    private static final int MSG_TYPE = 5;
    private static final int X = 4;
    private static final int MISC = 3;
    private static final int Y = 2;
    private static final int BLD_CT = 1;
    private static final int RAND = 0;

    /**
     * Message types
     */
    private static final int HOME_LOC = 0;
    private static final int ENEMY_LOC = 1;
    private static final int SOUP_LOC = 2;
    private static final int FACTORY_CT = 3;
    private static final int STARPORT_CT = 4;
    private static final int WALL_SET = 5;
    private static final int BAIT = 6; // pull a drone back to bait our opponents
    private static final int REFINERY_LOC = 7;

    /* ********
     * Methods for writing to the blockchain
     * ********/

    /**
     * generates a random number for the RAND portion of message
     */
    private void setSumInt(){
        int partialMsgSum = -message[RAND]; // get sum of ints without fill bit
        for(int i : message){
            partialMsgSum += i;
        }
        message[RAND] = TEAM_IDENTIFIER - partialMsgSum;
    }

    /**
     * adds a location to message
     * @param loc location to add
     */
    private void sendLoc(MapLocation loc){
        message[X] = loc.x;
        message[Y] = loc.y;
    }

    public void HQLoc(MapLocation loc) throws GameActionException{
        message[MSG_TYPE] = HOME_LOC;
        sendLoc(loc);
        bid();
    }

    public void enemyHQLoc(MapLocation loc) throws GameActionException{
        message[MSG_TYPE] = ENEMY_LOC;
        sendLoc(loc);
        bid();
    }

    public void soupLoc(MapLocation loc) throws GameActionException{
        message[MSG_TYPE] = SOUP_LOC;
        sendLoc(loc);
        bid();
    }

    public void refineryLoc(MapLocation loc) throws GameActionException{
        message[MSG_TYPE] = REFINERY_LOC;
        sendLoc(loc);
        bid();
    }

    public void sendFactoryCt(int newCt) throws GameActionException{
        message[MSG_TYPE] = FACTORY_CT;
        message[BLD_CT] = newCt;
        bid();
    }

    public void sendStarportCt(int newCt) throws GameActionException{
        message[MSG_TYPE] = STARPORT_CT;
        message[BLD_CT] = newCt;
        bid();
    }

    /**
     * bids our message to the blockchain
     */
    private void bid() throws GameActionException {
        setSumInt();
        int[] enc_message = encode(message);
        if(rc.canSubmitTransaction(enc_message, bid)){
            rc.submitTransaction(enc_message, bid);
        }
    }

    /* ********
     * encode/decode TODO
     * ********/

    private boolean fromUs(int[] m){
        int msgSum = 0;
        for(int i : m){
            msgSum += i;
        }
        return msgSum == TEAM_IDENTIFIER;
    }

    /** TODO
     * encode message to post to blockchain
     * @param message_to_encode uncoded message
     * @return encoded message
     */
    private int[] encode(int[] message_to_encode){
        return message_to_encode;
    }

    /** TODO
     * decode message from the blockchain
     * @param message_to_decode raw message
     * @return decoded message
     */
    private int[] decode(int[] message_to_decode){
        return message_to_decode;
    }

    /**
     * checks if decode properly decodes encoded messages
     * @return true if we can decode our messages, false otherwise
     */
    private boolean sanityCheck(){
        int[] m = decode(encode(message));
        for(int i = 0; i < 7; i++){
            if(m[i] != message[i]) return false;
        }
        return true;
    }

    /* ********
     * Methods for reading the blockchain
     * ********/

    /**
     * searches the blockchain back to front for HQ location
     * @return HQ map location if found, null else
     */
    public MapLocation getHQLoc() throws GameActionException{
        return getLoc(HOME_LOC);
    }

    /**
     * searches the blockchain back to front for enemy HQ location
     * @return HQ map location if found, null else
     */
    public MapLocation getEnemyHQLoc() throws GameActionException{
        return getLoc(ENEMY_LOC);
    }


    /**
     * searches the blockchain back to front
     * @return location if found, null else
     */
    private MapLocation getLoc(int msg_type) throws GameActionException{
        for(int i = 1; i < rc.getRoundNum(); i++){
            for(Transaction t : rc.getBlock(i)) {
                int[] m = decode(t.getMessage());
                if(fromUs(m) && m[MSG_TYPE] == msg_type){
                    return new MapLocation(m[X], m[Y]);
                }
            }
        }
        return null;
    }

    public void updateSoupLoc(ArrayList<MapLocation> sloc) throws GameActionException{
        updateLoc(sloc, SOUP_LOC);
    }

    public MapLocation getRefineryLoc(MapLocation def) throws GameActionException{
        return updateLoc(def, REFINERY_LOC);
    }

    /**
     * checks the previous round for updates to location
     * then updates the parameter with new locations
     * @param loc the unit's storage of locations
     */
    private void updateLoc(ArrayList<MapLocation> loc, int msg_type) throws GameActionException{
        for(Transaction t : rc.getBlock(rc.getRoundNum() - 1)) {
            int[] m = decode(t.getMessage());
            if(fromUs(m) && m[MSG_TYPE] == msg_type){
                MapLocation loc_to_add = new MapLocation(m[X], m[Y]);
                if(!loc.contains(loc_to_add)) loc.add(loc_to_add); //TODO do MapLocations do equality?
            }
        }
    }

    /**
     * similar to the above, but only returns one location
     * @param def default location if one is not found
     */
    private MapLocation updateLoc(MapLocation def, int msg_type) throws GameActionException{
        for(Transaction t : rc.getBlock(rc.getRoundNum() - 1)) {
            int[] m = decode(t.getMessage());
            if(fromUs(m) && m[MSG_TYPE] == msg_type){
                return new MapLocation(m[X], m[Y]);
            }
        }
        return def;
    }





    public int updateFactoryCount(int current_count) throws GameActionException{
        return updateCount(current_count, FACTORY_CT);
    }

    public int updateStarportCount(int current_count) throws GameActionException{
        return updateCount(current_count, STARPORT_CT);
    }

    /**
     * gets the number of buildings made last round
     * @param msg_type what building we looking for
     * @return number of buildings made last round
     * @throws GameActionException
     */
    private int updateCount(int current_count, int msg_type) throws GameActionException{
        int count = current_count;
        for(Transaction t : rc.getBlock(rc.getRoundNum() - 1)){
            int[] m = decode(t.getMessage());
            if(fromUs(m) && m[MSG_TYPE] == msg_type){
                if(count == current_count) count = Math.max(m[BLD_CT], current_count);
                else count = Math.max(count, m[BLD_CT]) + 1; // two people built at the same time
            }
        }
        return count;
    }

}

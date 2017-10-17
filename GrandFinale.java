import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.*;

public class GrandFinale {
    private int explorerMode=1; // 1=explore, 0=backtrack
    private int pollRun =0;
    private RobotData robotData;
    int[] RelDirArray= {IRobot.AHEAD, IRobot.RIGHT, IRobot.BEHIND, IRobot.LEFT};
    
    
    public void reset() {
        explorerMode =1;
        pollRun =0;
        robotData.resetCalls();
    }
    public void controlRobot(IRobot robot) {
        int direction = IRobot.AHEAD;
        if (robot.getRuns() >0) {
            direction = roundTwoControl(robot);
        } else {

        if (robot.getRuns()==0 && pollRun==0) {// i.e. robot is starting a new maze
            robotData = new RobotData();
            explorerMode =1;
        }
        if ((robotData.getDirArrivedStack().empty() == false) && (pollRun == robotData.getJuncPoll()+1)) {
           robotData.addExitDir(robot);
        }
        
            switch (explorerMode) { 
                case 1: direction = exploreControl(robot);
                        break;
                case 0: direction = backtrackControl(robot);
            }
    }
        robot.face(direction);
        pollRun++; // Increments after each move
                    // so that data is not reset after each move // direction that robot arrives from
        
    }
    public int roundTwoControl(IRobot robot) {
        int direction = IRobot.AHEAD;
        int exits = nonWallExits(robot);

        switch(exits) {
            case 1: // deadend
                    if (pollRun!=0) {
                        explorerMode=0; // go to backtrack mode
                    }
                    direction = atDeadend(robot);
                    return direction;
            case 2: // corridor
                    direction = inCorridor(robot); 
                    return direction;
            case 3: // junction
                    
            case 4: // crossroads
                    robot.setHeading(robotData.getExitDirList().get(robotData.getCalls()));
                    robotData.incrementCalls();
        }
        return IRobot.AHEAD;
    }
       
    public int exploreControl(IRobot robot) {
        int exits = nonWallExits(robot);
        int direction = IRobot.AHEAD;

        switch(exits) {
            case 1: // deadend
                    if (pollRun!=0) {
                        explorerMode=0; // go to backtrack mode
                    }
                    direction = atDeadend(robot);
                    return direction;
            case 2: // corridor
                    direction = inCorridor(robot); 
                    return direction;
            case 3: // junction
                    
            case 4: // crossroads
                    direction = atJunction(robot);
                    if (beenbeforeExits(robot)==1) {// junction is visited for the first time
                        robotData.addDir(robot);
                        robotData.setJuncPoll(pollRun);
                    }
                    return direction;
            }
        return IRobot.AHEAD;
    }
    public int backtrackControl(IRobot robot) {
        int exits = nonWallExits(robot);
        switch(exits) {
            case 2: return inCorridor(robot); 
            case 3: // at junction
            case 4: if (beenbeforeExits(robot) >1) {
                        robotData.popExitDir(robot); // pop exit direction if at an old junction
                    }
                    if (isPassage(robot) ==true) {// passage is available
                        robotData.setJuncPoll(pollRun);  //record junction exit direction                          
                        explorerMode=1;           // switch to explorer mode
                        return exploreControl(robot);
                     
                     } else { // no passage
                         if (robotData.peekLastDir()<=1001) {// head in opposite dir to dirArrived
                             robot.setHeading(robotData.lastDir()+2);
                         } else {
                             robot.setHeading(robotData.lastDir()-2);
                         }
                    }
                    break;
    }
    return IRobot.AHEAD;
}

    private int nonWallExits(IRobot robot) {
        int exits =0; // Default number of exits surrounding robot
        
        for (int RelDir : RelDirArray) {// for each direction in the array
		    if (robot.look(RelDir) != IRobot.WALL) {
		        exits += 1;// increment exits if wall is not detected in that direction
            }
        } 
        return exits;
    }
    private int chooseDirection(IRobot robot) {// Chooses a random non-wall direction
        int direction;
        double randnum;
        do {
    	    randnum=Math.random();// Choose random number
    	    if (randnum>=0 && randnum<0.25) {// Convert to direction
    	        direction = IRobot.AHEAD;
    	    } else if (randnum>=0.25 && randnum<0.5) {
    	        direction = IRobot.LEFT;
    	    }  else {
    	        direction = IRobot.RIGHT;
    	    }    
        } while (robot.look(direction)==IRobot.WALL);// while wall is not in that direction 
        return direction;
    }
    
    private boolean isPassage(IRobot robot) {// returns true if passage is available
        int passages =0;
        for (int RelDir : RelDirArray) {// for each direction in the array
            if (robot.look(RelDir) == IRobot.PASSAGE) {// if passage is in that direction
                passages +=1;
            }
        } 
        if (passages >=1) {
            return true;
        }
        return false;
    }           
    private int choosePassage(IRobot robot) {// Chooses randomly between avaiable passages
        int direction;
        double randnum;
        do {
        	    randnum=Math.random();// Choose random number
    	    if (randnum>=0 && randnum<0.25) {// Convert to direction
    	        direction = IRobot.AHEAD;
    	    } else if (randnum>=0.25 && randnum<0.5) {
    	        direction = IRobot.LEFT;
    	    }  else {
    	        direction = IRobot.RIGHT;
    	    }    
        } while (robot.look(direction)==IRobot.WALL || robot.look(direction)!=IRobot.PASSAGE);// while facing wall or not a passage
        return direction;
    }
    private int beenbeforeExits(IRobot robot) {
        int beenbeforeExits =0; // Default number of beenbefore exits surrounding robot
        
        for (int RelDir : RelDirArray) {// for each direction in the array
            if (robot.look(RelDir) == IRobot.BEENBEFORE) {
                beenbeforeExits += 1;// increment exits if wall is not detected in that direction
            }
        } 
        return beenbeforeExits;
    }
    private int atDeadend(IRobot robot) {
        for (int RelDir : RelDirArray) {// for each direction in the array
            if (robot.look(RelDir) != IRobot.WALL) {
                return RelDir;// direction = non-wall direction (only one)
            }
        } 
        return IRobot.CENTRE;
    }
    private int inCorridor(IRobot robot) {

        return chooseDirection(robot);
    }
    private int atJunction(IRobot robot) {
        if (isPassage(robot)==true) {
            return choosePassage(robot);
        }
        return chooseDirection(robot);
        }
        
    class RobotData {
    	private Stack<Integer> dirArrived;
    	private Stack<Integer> exitDir;
    	private int juncPoll = 0;
    	private int calls =0;
    	
    	public RobotData() {
    	    dirArrived = new Stack<Integer>(); // 
    	    exitDir = new Stack<Integer>();
    	}
    	public Stack<Integer> getDirArrivedStack() {
    	    return dirArrived;
    	}
    	public Stack<Integer> getExitDirStack() {
    	    return exitDir;
    	}
    	public List<Integer> getExitDirList() {// creates a list from exitDir stack. Indivdual elements can be accessed
    	    List<Integer> list = new ArrayList<Integer>(exitDir);
    	    return list;
    	}
    	public int getJuncPoll() {
    	    return juncPoll;
    	}
    	public void setJuncPoll(int a) {
    	    juncPoll = a;
    	}
    	public void resetDirArrivedStack() {
    	    dirArrived = new Stack<Integer>();
    	}
    	public void addDir(IRobot robot) {
    	    dirArrived.push(robot.getHeading()); // adds robot heading to stack
    	}
    	public void addExitDir(IRobot robot) {
    	    exitDir.push(robot.getHeading()); // adds robot heading to stack
    	}
    	public void popExitDir(IRobot robot) {
    	    exitDir.pop(); // removes last exit direction
    	}
    	public int peekLastDir() {
    	    int lastDir = dirArrived.peek(); // looks at top element in the stack
    	    return lastDir;
    	}
    	public int lastDir() {
    	    int lastDir = dirArrived.pop(); //returns the last element added to stack
    	    return lastDir;
    	}
    	public int getCalls() {
    	    return calls;
    	}
    	public void incrementCalls() {
    	    calls+=1;
    	}
    	public void resetCalls() {
    	    calls=0;
    	}
    }
}


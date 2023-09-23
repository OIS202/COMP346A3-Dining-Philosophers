import java.util.Iterator;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	enum actions {eating, thinking, hungry};//all different possible states for any philosopher
	public static actions[] philosopherState;//the action a particular philosopher is currently undergoing from the list defined above
	private static boolean[] chopsticks;//chopsticks represented as booleans to imply whether they are available or not
	private static boolean philosopherTalking;//denotes whether a philosopher is currently talking or not
	private static int philosopherNo;//number of philosophers in this session
	private static int[] eatingCounters;//counters of the number of times a particular philosopher has eaten
	
	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		philosopherNo = piNumberOfPhilosophers;
		philosopherState = new actions[philosopherNo];
		chopsticks = new boolean[philosopherNo];
		eatingCounters = new int[philosopherNo];
		philosopherTalking = false;
		for (int i = 0; i < philosopherNo; i++) {//initialize all philosophers to thinking, not eaten anything and not speaking with all the chopsticks available
			philosopherState[i] = actions.thinking;
			chopsticks[i] = true;
			eatingCounters[i] = 0;
		}
	}

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID)
	{
		philosopherState[piTID-1] = actions.hungry;//phil is now hungry
		if(((chopsticks[piTID-1] == false) || (chopsticks[(piTID)%philosopherNo] == false)) || (getStarvationStatus(piTID))) {
			//if chopstick to left or right not available or phil ate more than his neighbor, wait
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		chopsticks[piTID-1] = false;//make neighboring chopsticks unavailable
		chopsticks[(piTID)% philosopherNo] = false;
		philosopherState[piTID-1] = actions.eating;//phil now eating
		eatingCounters[piTID-1]++;//phil has ate one more time
	}

	private boolean getStarvationStatus(final int piTID) {
		int leftPhilCounter = eatingCounters[(piTID - 2 + philosopherNo) % philosopherNo];
		int rightPhilCounter = eatingCounters[(piTID) % philosopherNo];
		int philCounter = eatingCounters[piTID-1];
		
		return ((leftPhilCounter < philCounter) || (rightPhilCounter < philCounter)); 
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		philosopherState[piTID-1] = actions.thinking;//phil now thinking
		chopsticks[piTID-1] = true;//make neighboring chopsticks available
		chopsticks[(piTID)% philosopherNo] = true;
		notifyAll();
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk()
	{
		while(philosopherTalking) {//when other phil talking wait
			try {
				wait(); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			philosopherTalking = true;//else talk
		}
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		philosopherTalking = false;
		notifyAll();
	}
}

// EOF

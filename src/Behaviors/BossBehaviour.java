package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;

public class BossBehaviour extends StateBehavior {	
	
	public BossBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed =  false;
	private boolean finished = false;
	
	@Override
	public void action() {
		this.suppressed = false;
		
		this.hal.printOnDisplay("BossBehaviour started", 0, 0);
		while(!this.suppressed && !this.finished){
			
			
			finished = true;
			
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.BossState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}
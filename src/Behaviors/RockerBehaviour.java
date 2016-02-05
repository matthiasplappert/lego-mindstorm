package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;

public class RockerBehaviour extends StateBehavior {	
	
	public RockerBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed =  false;
	private boolean finished = false;
	
	@Override
	public void action() {
		this.hal.printOnDisplay("HangingBRockerBehaviourridgeBehaviour started", 0, 0);
		while(!this.suppressed && !this.finished){
			
			
			finished = true;
			
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.RockerState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}

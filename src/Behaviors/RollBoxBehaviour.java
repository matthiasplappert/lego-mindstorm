package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;

public class RollBoxBehaviour  extends StateBehavior {	
	
	public RollBoxBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed =  false;
	private boolean finished = false;
	
	@Override
	public void action() {
		this.hal.printOnDisplay("RollBoxBehaviour started", 1000);
		while(!this.surpressed && !this.finished){
			
			
			finished = true;
			
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.RollBoxState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}
}
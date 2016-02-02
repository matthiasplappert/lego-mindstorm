package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;

public class MazeBehaviour extends StateBehavior {	
	
	public MazeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed =  false;
	private boolean finished = false;
	
	@Override
	public void action() {
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 1000);
		while(!this.surpressed && !this.finished){
			
			
			finished = true;
			
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.MazeState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}
}

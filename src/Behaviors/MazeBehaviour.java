package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.MyState;

public class MazeBehaviour extends StateBehavior {	
	
	public MazeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed =  false;
	private boolean finished = false;
	
	@Override
	public void action() {
		this.suppressed = false;
		
		this.hal.printOnDisplay("MazeBehaviour started", 0, 0);
		while(!this.suppressed && !this.finished){
			
			
			finished = true;
			
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	MyState getTargetState() {
		return MyState.MazeState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}

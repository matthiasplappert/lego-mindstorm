package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;

public class LineSearchBehavior extends StateBehavior {
	private boolean firstRun;



	public LineSearchBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		this.firstRun = true;
	}

	@Override
	public boolean takeControl() {
		return super.takeControl();
	}

	@Override
	public void action() {
		// TODO: search for line, follow it until we read barcode and then modify state
		// this.sharedState.setState(folowUpState);

	
	}

	private void changeToFollowupState(){
		this.sharedState.switchState(State.TestState);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.LineSearch;
	}

	@Override
	public void suppress() {
		// TODO Auto-generated method stub
		
	}


}

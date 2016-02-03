package Behaviors;



import HAL.IHAL;
import State.SharedState;
import State.State;

public class DisplayTestStateBehavior extends StateBehavior {
	public DisplayTestStateBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed =  false;

	@Override
	public void action() {
		if(!this.surpressed){
			for(int i=1;i<=5;i++){
				this.hal.printOnDisplay("Counter: "+i, 0, 1000l);
			}
			this.sharedState.reset(true);
			Thread.yield();
		}
	}

	@Override
	State getTargetState() {
		return State.TestState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}

}

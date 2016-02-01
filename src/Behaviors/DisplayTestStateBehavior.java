package Behaviors;


import State.SharedState;
import State.State;
import common.Output;
import com.google.common.base.Optional;

public class DisplayTestStateBehavior extends StateBehavior {
	private boolean surpressed =  false;
	public DisplayTestStateBehavior(SharedState sharedState) {
		super(sharedState);
	}

	@Override
	public void action() {
		if(!this.surpressed){
			for(int i=1;i<=5;i++){
				Output.printOnDisplay("Counter: "+i, 0, 0, Optional.of(1000l));
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

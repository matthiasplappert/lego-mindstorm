package Behaviors;

import State.SharedState;
import State.State;
import lejos.robotics.subsumption.Behavior;

abstract public class StateBehavior implements Behavior {
	protected SharedState sharedState;
	
	public StateBehavior(SharedState sharedState) {
		this.sharedState = sharedState;
	}
	
	@Override
	public boolean takeControl() {
		return this.sharedState.getState().equals(this.getTargetState());
	}
//	@Override
//	public void suppress() {
//		this.sharedState.reset();
//	}
	abstract State getTargetState();
}

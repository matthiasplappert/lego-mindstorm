package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.MyState;
import lejos.robotics.subsumption.Behavior;

abstract public class StateBehavior implements Behavior {
	protected SharedState sharedState;
	protected IHAL hal;
	
	public StateBehavior(SharedState sharedState, IHAL hal) {
		this.sharedState = sharedState;
		this.hal = hal;
	}
	
	@Override
	public boolean takeControl() {
		return this.sharedState.getState().equals(this.getTargetState());
	}
//	@Override
//	public void suppress() {
//		this.sharedState.reset();
//	}
	abstract MyState getTargetState();
}

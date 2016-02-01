package common;

import lejos.robotics.subsumption.Behavior;

abstract public class StateBehavior implements Behavior {
	protected SharedState sharedState;
	
	public StateBehavior(SharedState sharedState) {
		this.sharedState = sharedState;
	}
	
	@Override
	public boolean takeControl() {
		return this.sharedState.getState() == this.getTargetState();
	}
	
	abstract State getTargetState();
}

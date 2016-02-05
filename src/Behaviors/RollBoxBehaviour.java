package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.utility.Delay;

public class RollBoxBehaviour extends StateBehavior {

	public RollBoxBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed = false;
	private boolean finished = false;

	@Override
	public void action() {
		this.hal.printOnDisplay("RollBoxBehaviour started", 0, 0);
		while (!this.suppressed && !this.finished) {

			this.hal.resetGyro();
			this.hal.setSpeed(Speed.VeryFast);
			this.hal.setCourseFollowingAngle((int) this.hal.getMeanGyro());
			while (!this.suppressed && this.hal.getLineType() != LineType.LINE) {
				this.hal.performCourseFollowingStep();
				Delay.msDelay(10);

			}
			this.hal.stop();
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
		suppressed = true;
	}
}

package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
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
			float diff = 0;
			int steps = 0;
			boolean enough = false;
			float difference;
			
			this.hal.resetGyro();
			this.hal.setSpeed(Speed.VeryFast);
			this.hal.forward();
			Delay.msDelay(1000);
			float distance = this.hal.getMeanDistance();

			while (!enough && this.hal.getCurrentDistance() < 15.f && !suppressed)
			{
				steps++;
				this.hal.forward();
				Delay.msDelay(450);
				difference = this.hal.getMeanDistance() - distance;
				diff += difference;
				if (steps > 5 && diff / 5 < 0.5f) {
					enough = true;
				}
				if (Math.abs(difference) > 0.3f) {
					this.hal.turn((int) Math.signum(difference));
					Delay.msDelay(150);
				}
			}
			
			
			this.hal.stop();
			Sound.beep();
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

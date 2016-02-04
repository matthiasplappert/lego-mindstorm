package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.utility.Delay;

public class DrivebyBehaviour extends StateBehavior {

	private boolean suppressed;
	private DrivebyReturnType returnType;
	private final int min_dist;

	public DrivebyReturnType getReturnType() {
		return returnType;
	}

	private static final Speed DefaultSpeed = Speed.Slow;
	private int maxTurnAngle;
	private int offset;

	public DrivebyBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState, hal, 10, 5, 90);
	}

	public DrivebyBehaviour(SharedState sharedState, IHAL hal, int min_dist, int offset, int maxTurnAngle) {
		super(sharedState, hal);
		this.suppressed = false;
		this.min_dist = min_dist;
		this.offset = offset;
		this.maxTurnAngle = maxTurnAngle;
	}

	@Override
	public void action() {
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP, false);
		Delay.msDelay(250);

		while (!this.suppressed) {
			// Get (filtered) distance
			float distance = this.hal.getMeanDistance();

			// Robot control.
			if (isTooClose(distance)) {
				this.hal.turn(-this.maxTurnAngle, false, true);

				while (!this.suppressed && this.hal.isRotating()) {
					if (!this.isTooClose(this.hal.getMeanDistance())) {
						break;
					}
					Delay.msDelay(10);
				}

			} else if (isTooFar(distance)) {
				this.hal.turn(this.maxTurnAngle, false, true);

				while (!this.suppressed && this.hal.isRotating()) {
					if (!this.isTooFar(this.hal.getMeanDistance())) {
						break;
					}
					Delay.msDelay(10);
				}
			} else {
				this.hal.forward(DefaultSpeed);
			}

		}
		Delay.msDelay(10);
	}

	private boolean isTooFar(float distance) {
		return distance > this.min_dist + this.offset;
	}

	private boolean isTooClose(float distance) {
		return distance < this.min_dist;
	}

	@Override
	public void suppress() {
		this.suppressed = true;

	}

	@Override
	State getTargetState() {
		return State.DriveByState;
	}

}

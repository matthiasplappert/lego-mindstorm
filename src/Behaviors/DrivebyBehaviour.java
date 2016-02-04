package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class DrivebyBehaviour extends StateBehavior {

	private boolean suppressed;
	private DrivebyReturnType returnType;
	private final int min_dist;

	public DrivebyReturnType getReturnType() {
		return returnType;
	}

	private static final Speed DefaultSpeed = Speed.Medium;
	private static final int DELAY = 5;

	private int maxTurnAngle;
	private int offset;
	//TODO: MeanFilter smaller
	public DrivebyBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState, hal, 5, 3, 160);
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
		LCD.drawString("DriveByBehaviour", 0, 0);
		while (!this.suppressed) {
			// Get (filtered) distance
			float distance = this.hal.getMeanDistance();
			LCD.drawString("dist to wall: " + distance, 0, 1);
			if (isButtonPressed())
			{
				this.hal.stop();
			} else {
				// Robot control.
				if (isTooClose(distance)) {
					this.hal.turn(-this.maxTurnAngle, false, true);

					while (!this.suppressed && this.hal.isRotating()) {
						if (!this.isTooClose(this.hal.getMeanDistance()) && !this.isButtonPressed()) {
							break;
						}
						Delay.msDelay(DELAY);
					}

				} else if (isTooFar(distance) && !this.isButtonPressed()) {
					this.hal.turn(this.maxTurnAngle, false, true);

					while (!this.suppressed && this.hal.isRotating()) {
						if (!this.isTooFar(this.hal.getMeanDistance())) {
							break;
						}
						Delay.msDelay(DELAY);
					}
				} else {
					this.hal.forward(DefaultSpeed);
				}
			}

		}
		Delay.msDelay(10);
	}

	private boolean isButtonPressed() {
		return this.hal.isTouchButtonPressed();
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

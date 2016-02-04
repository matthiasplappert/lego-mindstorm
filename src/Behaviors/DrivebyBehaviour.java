package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class DrivebyBehaviour extends StateBehavior {

	private boolean suppressed;
	private DrivebyReturnType returnType;
	private final int min_dist;

	public DrivebyReturnType getReturnType() {
		return returnType;
	}

	private static final Speed DefaultSpeed = Speed.Labyrinth;
	private static final int DELAY = 5;

	private int maxTurnAngle;
	private int offset;
	//TODO: MeanFilter smaller
	public DrivebyBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState, hal, 5, 3, 80);
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
		LCD.drawString("DriveByBehaviour", 0, 0);
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.Labyrinth);
		this.hal.getMeanDistance();
		Sound.beep();
		Delay.msDelay(5000);
		Sound.beep();
		this.hal.setSpeed(DefaultSpeed);
		while (!this.suppressed) {
			// Get (filtered) distance
			float distance = this.hal.getMeanDistance();
			LCD.drawString("dist to wall: " + distance, 0, 1);
			if (isButtonPressed())
			{
				this.hal.stop();
				moveBackAndTurnLeft();
			} else {
				// Robot control.
				if (isTooClose(distance)) {
					this.hal.turn(-this.maxTurnAngle);

					while (!this.suppressed && this.hal.isRotating()) {
						if (!this.isTooClose(this.hal.getMeanDistance()) && !this.isButtonPressed()) {
							break;
						}
						Delay.msDelay(DELAY);
					}

				} else if (isTooFar(distance) && !this.isButtonPressed()) {
					this.hal.turn(this.maxTurnAngle);

					while (!this.suppressed && this.hal.isRotating()) {
						if (!this.isTooFar(this.hal.getMeanDistance())) {
							break;
						}
						Delay.msDelay(DELAY);
					}
				} else {
					this.hal.forward();
				}
			}
			Delay.msDelay(10);

		}
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		this.sharedState.reset(true);
	}
	private void moveBackAndTurnLeft(){
//		this.hal.setSpeed(Speed.Medium);
		this.hal.backward();
		Delay.msDelay(500);
		this.hal.stop();
//		this.hal.setSpeed(DefaultSpeed);
		this.hal.rotate(maxTurnAngle);
		Delay.msDelay(1000);

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

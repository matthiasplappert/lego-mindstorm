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
	private final int target_dist;

	public DrivebyReturnType getReturnType() {
		return returnType;
	}

	private static final Speed DefaultSpeed = Speed.Labyrinth;
	private static final int DELAY = 5;

	final private int maxTurnAngle;
	final private int offset;
	final private int holeOffset;
	final private int initDistance;

	// TODO: MeanFilter smaller
	public DrivebyBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState, hal, 5, 2, 80, 15, 15);
	}

	public DrivebyBehaviour(SharedState sharedState, IHAL hal, int target_dist, int offset, int maxTurnAngle,
			int holeOffset, int initDistance) {
		super(sharedState, hal);
		this.suppressed = false;
		this.target_dist = target_dist;
		this.offset = offset;
		this.maxTurnAngle = maxTurnAngle;
		this.holeOffset = holeOffset;
		this.initDistance = initDistance;
	}

	@Override
	public void action() {
		LCD.drawString("DriveByBehaviour", 0, 0);
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.Labyrinth);
		this.hal.getMeanDistance();
		Sound.beep();
		Delay.msDelay(3000);
		Sound.twoBeeps();
		this.startupSequence();
		this.hal.setSpeed(DefaultSpeed);
		while (!this.suppressed && this.hal.getLineType() != LineType.LINE) {

			// Get (filtered) distance
			float distance = this.hal.getMeanDistance();
			LCD.drawString("dist to wall: " + distance, 0, 1);
			if (isButtonPressed()) {
				if (this.hal.getMeanDistance() > this.holeOffset) {
					moveBackAndTurn(Direction.RIGHT);
				} else {
					moveBackAndTurn(Direction.LEFT);
				}
				this.hal.stop();
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
		if (this.hal.getLineType() == LineType.LINE) {
			Sound.beepSequence();
			targetLineFound();
		}
		this.hal.stop();
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		this.sharedState.reset(true);
	}

	private void targetLineFound() {
		this.hal.stop();
		this.hal.setSpeed(Speed.Slow);
		this.hal.backward();
		Delay.msDelay(500);
	}

	private void startupSequence() {
		this.hal.setSpeed(Speed.Medium);
		this.hal.setCourseFollowingAngle((int) this.hal.getMeanGyro() - 2);
		int distance_counter = 0;
		while (!this.suppressed && distance_counter < 3) {
			this.hal.performCourseFollowingStep();
			if (this.hal.getMeanDistance() < this.initDistance) {
				distance_counter++;
			} else {
				distance_counter = 0;
			}
			Delay.msDelay(10);
		}

	}

	private void moveBackAndTurn(Direction dir) {
		this.hal.setSpeed(Speed.Slow);
		this.hal.backward();
		Delay.msDelay(1000);
		this.hal.stop();
		this.hal.setSpeed(DefaultSpeed);

		this.hal.rotate(dir.getMultiplierForDirection() * maxTurnAngle);
		while (this.hal.isRotating()) {
			Delay.msDelay(10);
		}
		this.hal.stop();

	}

	private boolean isButtonPressed() {
		return this.hal.isTouchButtonPressed();
	}

	private boolean isTooFar(float distance) {
		return distance > this.target_dist + this.offset;
	}

	private boolean isTooClose(float distance) {
		return distance < this.target_dist;
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

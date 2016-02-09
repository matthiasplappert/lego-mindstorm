package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class MazeBehaviour extends StateBehavior {
	
	private static final int START_ANGLE = 10;
	
	private static final float DISTANCE_THRESHOLD = 5.f;
	
	private static final float DISTANCE_TOLERANCE = 2.f;
	
	private static final int TURN_ANGLE = 5;
	
	private static final int LOOP_DELAY = 10;
	
	private static final float BACKUP_DISTANCE = 10.f;
	
	private static final int ROTATION_ANGLE = -90;

	private static final Speed DefaultSpeed = Speed.Labyrinth;
	
	private boolean suppressed;
	
	public MazeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	@Override
	public void action() {
		this.suppressed = false;
		
		LCD.drawString("MazeBehaviour", 0, 0);
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.Labyrinth);
		
		// Set course slightly to the right.
		this.hal.resetGyro();
		this.hal.setCourseFollowingAngle(START_ANGLE);
		while (!this.suppressed && this.hal.getMeanDistance() > DISTANCE_THRESHOLD) {
			this.hal.performCourseFollowingStep();
		}
		this.hal.stop();
		Sound.twoBeeps();
		
		// We have reached the wall, start navigating.
		while (!this.suppressed && this.hal.getLineType() != LineType.LINE) {
			// Get (filtered) distance
			float distance = this.hal.getMeanDistance();
			this.hal.printOnDisplay("dist to wall: " + distance, 1, 0);
			if (isButtonPressed()) {
				moveBackAndTurn();
				this.hal.stop();
			} else {
				// Keep distance to wall.
				if (isTooClose(distance)) {
					// Too close means that we need to turn left.
					this.hal.turn(-TURN_ANGLE);
				} else if (isTooFar(distance)) {
					this.hal.turn(TURN_ANGLE);
				} else {
					this.hal.forward();
				}
				Delay.msDelay(LOOP_DELAY);
			}
		}
		this.hal.stop();
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		this.sharedState.reset(true);
	}

	private void moveBackAndTurn() {
		this.hal.setSpeed(Speed.Fast);
		this.hal.resetLeftTachoCount();
		this.hal.backward();
		while (!this.suppressed && -this.hal.getLeftTachoDistance() < BACKUP_DISTANCE) {
			Delay.msDelay(LOOP_DELAY);
		}
		this.hal.stop();
		
		this.hal.setSpeed(DefaultSpeed);
		this.hal.rotate(ROTATION_ANGLE);
		while (!this.suppressed && this.hal.isRotating()) {
			Delay.msDelay(LOOP_DELAY);
		}
		this.hal.stop();
	}

	private boolean isButtonPressed() {
		return this.hal.isTouchButtonPressed();
	}

	private boolean isTooFar(float distance) {
		return distance > DISTANCE_THRESHOLD + DISTANCE_TOLERANCE;
	}

	private boolean isTooClose(float distance) {
		return distance < DISTANCE_THRESHOLD;
	}

	@Override
	public void suppress() {
		this.suppressed = true;

	}

	@Override
	MyState getTargetState() {
		return MyState.MazeState;
	}
}

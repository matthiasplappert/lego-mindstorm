package Behaviors;

import java.util.Random;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import State.MyState;

public class FreeTrackBehaviour extends StateBehavior {

	private static final int START_ANGLE = 20;
	
	private static final float DISTANCE_THRESHOLD = 10.f;
	
	private static final float DISTANCE_TOLERANCE = 5.f;
	
	private static final int TURN_ANGLE = 5;
	
	private static final int LOOP_DELAY = 10;
	
	private static final float BACKUP_DISTANCE = 10.f;
	
	private static final int ROTATION_ANGLE = -90;

	private static final Speed DefaultSpeed = Speed.Labyrinth;
	
	private boolean suppressed;
	private boolean hasDoneLeftTurn;
	public FreeTrackBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		hasDoneLeftTurn = false;
	}

	@Override
	public void action() {
		this.suppressed = false;
		
		LCD.drawString("FreeTrackBehaviour", 0, 0);
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.Labyrinth);
		
		// We use the same speed throughout the maze
		this.hal.setSpeed(Speed.Labyrinth);
		
		// Set course slightly to the right.
		this.hal.resetGyro();
		this.hal.setCourseFollowingAngle(START_ANGLE);
		while (!this.suppressed && this.hal.getMeanDistance() > DISTANCE_THRESHOLD) {
			this.hal.performCourseFollowingStep();
		}
		this.hal.stop();
		Sound.twoBeeps();
		
		// We have reached the wall, start navigating.
		while (!this.suppressed && !this.hasDoneLeftTurn) {
			// Get (filtered) distance
			if (isButtonPressed()) {
				moveBackAndTurn();
				this.hal.stop();
				this.hasDoneLeftTurn = true;
				this.hal.resetGyro();
			} else {
				// Keep distance to wall.
				followWall();
				Delay.msDelay(LOOP_DELAY);
			}
		}
				
		while(this.hal.getCurrentGyro() < 90 && !this.suppressed){
			this.followWall();
			Delay.msDelay(10);
		}
		
		//we are around the last corner		
		this.sharedState.setState(MyState.BossState);
	}

	private void followWall() {
		float distance = this.hal.getMeanDistance();
		this.hal.printOnDisplay("dist to wall: " + distance, 1, 0);

		if (isTooClose(distance)) {
			// Too close means that we need to turn left.
			this.hal.turn(-TURN_ANGLE);
		} else if (isTooFar(distance)) {
			this.hal.turn(TURN_ANGLE);
		} else {
			this.hal.forward();
		}
	}

	private void moveBackAndTurn() {
		this.hal.resetLeftTachoCount();
		this.hal.backward();
		while (!this.suppressed && -this.hal.getLeftTachoDistance() < BACKUP_DISTANCE) {
			Delay.msDelay(LOOP_DELAY);
		}
		this.hal.stop();
		
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
		return MyState.FreeTrackState;
	}
}

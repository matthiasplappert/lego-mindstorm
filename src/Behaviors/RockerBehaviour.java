package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class RockerBehaviour extends StateBehavior {
	public RockerBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}
	
	// Shared constants
	public static final int LOOP_DELAY = 10;
	private static final float DROPOFF_DISTANCE_THRESHOLD = 8.0f; // in cm
	
	// The initial offset angle. Should be in the direction of the sensor.
	// This avoids that we fall of the other side before reaching the edge.
	private static final int OFFSET_ANGLE = 45;
	private static final int MAX_TURN_ANGLE = 45;
	
	private static final Speed FORWARD_SPEED = Speed.VeryFast;
	private static final Speed EDGE_SEARCH_SPEED = Speed.Fast;
	private static final Speed EDGE_FOLLOW_SPEED = Speed.VeryFast;
	
	private boolean suppressed = false;
	
	@Override
	public void action() {
		this.suppressed = false;
		
		this.hal.setSpeed(FORWARD_SPEED);
		this.hal.resetLeftTachoCount();
		this.hal.forward();
		while (!suppressed && this.hal.getLeftTachoDistance() < 10.0f) {
			Delay.msDelay(10);
		}

		// RELEASE THE KRAKEN (and wait for it)
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.DOWN);

		// Wait until we have a stable signal. We at least wait for 10
		// iterations and ensure
		// that we initially cannot see the dropoff.
		//LCD.drawString("Mode: initializing", 0, 3);
		while (!this.suppressed && this.canSeeDropoff(this.getDistance())) {
			Delay.msDelay(LOOP_DELAY);
		}

		// Configure the follow angle. We use this initially before we have
		// found the edge
		// for the first time.
		//LCD.drawString("Mode: finding edge", 0, 3);
		this.hal.resetGyro();
		this.hal.setCourseFollowingAngle(OFFSET_ANGLE);
		this.hal.setSpeed(EDGE_SEARCH_SPEED);
		while (!this.suppressed) {
			this.hal.performCourseFollowingStep();
			if (this.canSeeDropoff(this.getDistance())) {
				break;
			}
			Delay.msDelay(LOOP_DELAY);
		}

		// Turn to the left until we can barely see the edge anymore and go go
		// go.
		//Sound.beep();
		this.hal.setSpeed(EDGE_SEARCH_SPEED);
		this.hal.rotate(-MAX_TURN_ANGLE);
		while (!this.suppressed && this.canSeeDropoff(this.getDistance())) {
			Delay.msDelay(LOOP_DELAY);
		}
		this.hal.stop();

		// Our strategy is the following: We just keep going until we reach the
		// drop-off.
		// We then start to regulate the motors such that we keep a safe
		// distance to the drop-off.
		//Sound.buzz();
		//LCD.clear(3);
		//LCD.drawString("Mode: following edge", 0, 3);
		this.hal.setSpeed(EDGE_FOLLOW_SPEED);
		int numberOfStepsWithoutDropoff = 0;
		boolean finished = false;
		while (!this.suppressed && !finished) {
			// Get (filtered) distance
			float distance = this.getDistance();
			boolean canSeeDropoff = this.canSeeDropoff(distance);

			// Robot control.
			if (canSeeDropoff) {
				// Turn slightly to the left until we do not see the dropoff
				// anymore.
				numberOfStepsWithoutDropoff = 0;
				this.hal.turn(-MAX_TURN_ANGLE);
				while (!this.suppressed && this.hal.isRotating() && !this.hal.getLineType().equals(LineType.LINE)) {
					if (!this.canSeeDropoff(this.getDistance())) {
						break;
					}
					Delay.msDelay(LOOP_DELAY);
				}
			} else {
				// Turn slightly to the right until we do not see the dropoff
				// anymore.
				this.hal.turn(MAX_TURN_ANGLE);
				while (!this.suppressed && this.hal.isRotating() && !this.hal.getLineType().equals(LineType.LINE)) {
					if (this.canSeeDropoff(this.getDistance())) {
						break;
					}
					if (numberOfStepsWithoutDropoff > 50) {
						finished = true;
						break;
					}
					numberOfStepsWithoutDropoff++;
					Delay.msDelay(LOOP_DELAY);
				}
			}
			Delay.msDelay(LOOP_DELAY);
		}
		this.hal.stop();
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		
		// Move forward until we meet the line.
		this.hal.resetGyro();
		this.hal.setCourseFollowingAngle(-5);
		while (!this.suppressed && !this.hal.getLineType().equals(LineType.LINE)) {
			this.hal.performCourseFollowingStep();
			Delay.msDelay(LOOP_DELAY);
		}
		this.hal.stop();
		this.sharedState.setLineFollowHint(Direction.RIGHT); // first look to the right
		this.sharedState.setState(MyState.LineFollowState);
	}
	
	private float getDistance() {
		float distance = this.hal.getMeanDistance();
		//this.hal.printOnDisplay("Distance: " + Float.toString(distance), 3, 0);
		return distance;
	}

	private boolean canSeeDropoff(float distance) {
		boolean canSeeDropoff = (distance > DROPOFF_DISTANCE_THRESHOLD);
		//this.hal.printOnDisplay(canSeeDropoff ? "Dropoff detected" : "No dropoff detected", 3, 0);
		return canSeeDropoff;
	}

	@Override
	MyState getTargetState() {
		return MyState.RockerState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}

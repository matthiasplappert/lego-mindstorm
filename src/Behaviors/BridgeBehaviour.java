package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class BridgeBehaviour extends StateBehavior {
	private static final float DROPOFF_DISTANCE_THRESHOLD = 10.0f; // in cm

	private static final int STEP_DELAY_MS = 10;

	private static final int INITIAL_FORWARD_DISTANCE = 30;
	private static final int ELEVATOR_FORWARD_DISTANCE = 15;
	private static final int MAX_TURN_ANGLE = 45;

	// The initial offset angle. Should be in the direction of the sensor.
	// This avoids that we fall of the other side before reaching the edge.
	private static final int OFFSET_ANGLE = 45;

	private boolean suppressed = false;

	public BridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	@Override
	public void action() {
		this.suppressed = false;

		LCD.drawString("BridgeBehavior", 0, 0);

		this.hal.setSpeed(Speed.VeryFast);
		this.hal.resetLeftTachoCount();
		this.hal.forward();
		while (!suppressed && this.hal.getLeftTachoDistance() < INITIAL_FORWARD_DISTANCE) {
			Delay.msDelay(10);
		}

		// RELEASE THE KRAKEN (and wait for it)
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.DOWN);

		// Wait until we have a stable signal. We at least wait for 10
		// iterations and ensure
		// that we initially cannot see the dropoff.
		LCD.drawString("Mode: initializing", 0, 3);
		while (!this.suppressed && this.canSeeDropoff(this.getDistance())) {
			Delay.msDelay(STEP_DELAY_MS);
		}

		// Configure the follow angle. We use this initially before we have
		// found the edge
		// for the first time.
		LCD.drawString("Mode: finding edge", 0, 3);
		this.hal.resetGyro();
		this.hal.setCourseFollowingAngle(OFFSET_ANGLE);
		this.hal.setSpeed(Speed.Medium);
		while (!this.suppressed) {
			this.hal.performCourseFollowingStep();
			if (this.canSeeDropoff(this.getDistance())) {
				break;
			}
			Delay.msDelay(STEP_DELAY_MS);
		}

		// Turn to the left until we can barely see the edge anymore and go go
		// go.
		Sound.beep();
		this.hal.setSpeed(Speed.Medium);
		this.hal.rotate(-MAX_TURN_ANGLE);
		while (!this.suppressed && this.canSeeDropoff(this.getDistance())) {
			Delay.msDelay(STEP_DELAY_MS);
		}
		this.hal.stop();

		// Our strategy is the following: We just keep going until we reach the
		// drop-off.
		// We then start to regulate the motors such that we keep a safe
		// distance to the drop-off.
		Sound.buzz();
		LCD.clear(3);
		LCD.drawString("Mode: following edge", 0, 3);
		this.hal.setSpeed(Speed.Fast);
		while (!this.suppressed && !this.hal.getLineType().equals(LineType.LINE)) {
			// Get (filtered) distance
			float distance = this.getDistance();
			boolean canSeeDropoff = this.canSeeDropoff(distance);

			// Robot control.
			if (canSeeDropoff) {
				// Turn slightly to the left until we do not see the dropoff
				// anymore.
				this.hal.turn(-MAX_TURN_ANGLE);
				while (!this.suppressed && this.hal.isRotating() && !this.hal.getLineType().equals(LineType.LINE)) {
					if (!this.canSeeDropoff(this.getDistance())) {
						break;
					}
					Delay.msDelay(STEP_DELAY_MS);
				}
			} else {
				// Turn slightly to the right until we do not see the dropoff
				// anymore.
				this.hal.turn(MAX_TURN_ANGLE);
				while (!this.suppressed && this.hal.isRotating() && !this.hal.getLineType().equals(LineType.LINE)) {
					if (this.canSeeDropoff(this.getDistance())) {
						break;
					}
					Delay.msDelay(STEP_DELAY_MS);
				}
			}
			Delay.msDelay(STEP_DELAY_MS);
		}
		this.hal.resetGyro();
		this.hal.stop();
		Sound.buzz();
		this.hal.rotate(-45);
		while (!suppressed && this.hal.isRotating()) {
			Delay.msDelay(10);
		}
		this.hal.resetLeftTachoCount();
		this.hal.forward();
		while (!suppressed && this.hal.getLeftTachoDistance() < ELEVATOR_FORWARD_DISTANCE) {
			Delay.msDelay(10);
		}
		this.hal.rotate(45);
		while (!suppressed && this.hal.isRotating()) {
			Delay.msDelay(10);
		}
		
		this.hal.forward();
		this.hal.resetLeftTachoCount();
		while (!suppressed && this.hal.getLeftTachoDistance() < ELEVATOR_FORWARD_DISTANCE) {
			Delay.msDelay(10);
		}
		this.hal.stop();
		// TODO: detect the end of the bridge

		Sound.beep();
		Sound.beep();
		Sound.beep();
		Sound.beep();
		
		// Restore state
		LCD.clear();
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		this.sharedState.reset(true);
	}

	private float getDistance() {
		float distance = this.hal.getMeanDistance();

		// Debugging
		LCD.clear(1);
		LCD.drawString("Distance: " + Float.toString(distance), 0, 1);

		return distance;
	}

	private boolean canSeeDropoff(float distance) {
		boolean canSeeDropoff = (distance > DROPOFF_DISTANCE_THRESHOLD);

		// Debugging
		LCD.clear(2);
		LCD.drawString(canSeeDropoff ? "Dropoff detected" : "No dropoff detected", 0, 2);

		return canSeeDropoff;
	}

	@Override
	MyState getTargetState() {
		return MyState.BridgeState;
	}

	@Override
	public void suppress() {
		this.suppressed = true;
	}
}

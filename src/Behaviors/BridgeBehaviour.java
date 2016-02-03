package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;

public class BridgeBehaviour extends StateBehavior {	
	private static final float DROPOFF_DISTANCE_THRESHOLD = 20.0f;  // in cm
	
	private static final int SLIDING_WINDOW = 3;
	
	private static final int MAX_TURN_ANGLE = 45;
	
	public BridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed = false;
	
	@Override
	public void action() {
		LCD.drawString("BridgeBehavior", 0, 0);
		
		// RELEASE THE KRAKEN (and wait for it)
		//this.hal.moveDistanceSensorToPosition(30, false);
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.DOWN, false);
		
		// We use a slight mean filter to avoid reacting nervously
		EV3UltrasonicSensor sensor = this.hal.getUltrasonicSensor();
		sensor.enable();
		MeanFilter meanFilter = new MeanFilter(sensor, BridgeBehaviour.SLIDING_WINDOW);
		
		// Warm up for a couple of steps.
		for (int i = 0; i < 10; i++) {
			this.getDistance(meanFilter);
			Delay.msDelay(10);
		}
		
		// TODO: we need to avoid falling off the other side somehow
		
		// Our strategy is the following: We just keep going until we reach the drop-off.
		// We then start to regulate the motors such that we keep a safe distance to the drop-off.
		boolean hasSeenDropoff = false;
		while (!this.surpressed) {
			// Get (filtered) distance
			float distance = this.getDistance(meanFilter);
			boolean canSeeDropoff = this.canSeeDropoff(distance);
			if (distance == Float.POSITIVE_INFINITY && !hasSeenDropoff) {
				// Work around a problem where the sensor sometimes reports infinity for small distances.
				// In case we haven't yet seen the dropoff, assume that we are safe.
				canSeeDropoff = false;
			}
			
			// Robot control.
			if (canSeeDropoff) {
				// Turn slightly to the left until we do not see the dropoff anymore.
				this.hal.turn(-MAX_TURN_ANGLE, false, true);
				while (!this.surpressed && this.hal.isRotating()) {
					if (!this.canSeeDropoff(this.getDistance(meanFilter))) {
						break;
					}
					Delay.msDelay(10);
				}
				hasSeenDropoff = true;
			} else {
				if (!hasSeenDropoff) {
					// We haven't found the dropoff yet, so just keep going.
					// TODO: we should have a slight bias to the right here to avoid falling of the other side
					this.hal.forward();
				} else {
					// We have seen the dropoff before, but can't see it anymore. Correct by
					// turning slighty to the right until we see the dropoff again.
					this.hal.turn(MAX_TURN_ANGLE, false, true);
					while (!this.surpressed && this.hal.isRotating()) {
						if (this.canSeeDropoff(this.getDistance(meanFilter))) {
							break;
						}
						Delay.msDelay(10);
					}
				}
			}
			
			// We sample with 100Hz.
			Delay.msDelay(10);
		}
		
		// TODO: detect the end of the bridge
		
		// Restore state
		LCD.clear();
		sensor.disable();
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP, false);
		this.sharedState.reset(true);
	}
	
	private float getDistance(MeanFilter meanFilter) {
		float[] buffer = new float[meanFilter.sampleSize()];
		meanFilter.fetchSample(buffer, 0);
		float distance = buffer[0] * 100.0f;  // in cm
		
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
	State getTargetState() {
		return State.BridgeState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}
}

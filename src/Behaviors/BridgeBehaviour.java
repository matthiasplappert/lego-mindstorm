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
	private static final float DROPOFF_DISTANCE_THRESHOLD = 50.0f;  // in cm
	
	private static final int SLIDING_WINDOW = 3;
	
	private static final int TURN_ANGLE = 2;
	
	public BridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed = false;
	
	@Override
	public void action() {
		LCD.drawString("BridgeBehavior", 0, 0);
		
		// RELEASE THE KRAKEN (and wait for it)
		//this.hal.moveDistanceSensorToPosition(30, false);
		//this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.LEFT_DOWN, true);
		
		// We use a slight mean filter to avoid reacting nervously
		EV3UltrasonicSensor sensor = this.hal.getUltrasonicSensor();
		sensor.enable();
		MeanFilter meanFilter = new MeanFilter(sensor, BridgeBehaviour.SLIDING_WINDOW);
		float[] buffer = new float[meanFilter.sampleSize()];
		
		// TODO: we need to avoid falling off the other side somehow
		
		// Our strategy is the following: We just keep going until we reach the left drop-off.
		// We then start to regulate the motors such that we keep a safe distance to the drop-off.
		while (!this.surpressed) {
			// Get (filtered) distance
			sensor.fetchSample(buffer, 0);
			meanFilter.fetchSample(buffer, 0);
			boolean isValid = (buffer[0] != Float.POSITIVE_INFINITY);
			float distance = buffer[0] * 100.0f;  // in cm
			
			LCD.clear(2);
			LCD.clear(1);
			if (isValid && distance > BridgeBehaviour.DROPOFF_DISTANCE_THRESHOLD) {
				LCD.drawString("Dropoff detected", 0, 2);
				this.hal.turn(BridgeBehaviour.TURN_ANGLE, false, false);
			} else {
				LCD.drawString("No dropoff detected", 0, 2);
				this.hal.forward();
			}
			LCD.drawString("Distance: " + Float.toString(distance), 0, 1);
			Delay.msDelay(100);
		}
		
		// TODO: detect the end of the bridge
		
		// Restore state
		LCD.clear();
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.LEFT, false);
		this.sharedState.reset(true);
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

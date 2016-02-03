package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.filter.MeanFilter;

public class BridgeBehaviour extends StateBehavior {	
	private static final float DISTANCE_THRESHOLD = 1.0f;  // in cm
	
	private static final int MEAN_WINDOW = 3;
	
	private static final int TURN_ANGLE = 1;
	
	public BridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed = false;
	
	@Override
	public void action() {
		LCD.drawString("BridgeBehavior", 0, 0);
		
		// RELEASE THE KRAKEN (and wait for it)
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.LEFT_DOWN, false);
		System.exit(0);
		
		// We use a slight mean filter to avoid reacting nervously
		EV3UltrasonicSensor sensor = this.hal.getUltrasonicSensor();
		sensor.enable();
		MeanFilter meanFilter = new MeanFilter(sensor, BridgeBehaviour.MEAN_WINDOW);
		float[] buffer = new float[meanFilter.sampleSize()];
		
		// Our strategy is the following: We just keep going until we reach the left drop-off.
		// We then start to regulate the motors such that we keep a safe distance to the drop-off.
		while (!this.surpressed) {
			// Get (filtered) distance
			meanFilter.fetchSample(buffer, 0);
			float distance = buffer[0] * 100.0f;  // in cm
			
			LCD.clear(2);
			if (distance > DISTANCE_THRESHOLD) {
				LCD.drawString("Dropoff detected", 0, 2);
				this.hal.turn(BridgeBehaviour.TURN_ANGLE, false, false);
			} else {
				LCD.drawString("No dropoff detected", 0, 2);
				this.hal.forward();
			}
			LCD.drawString(Float.toString(distance), 0, 1);
		}
		
		// TODO: detect the end of the bridge
		
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

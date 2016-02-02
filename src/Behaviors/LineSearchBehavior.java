package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;

public class LineSearchBehavior extends StateBehavior {
	public static final float THRESHOLD = 0.2f;
	
	public static final int MEAN_WINDOW = 5;
	
	public static final int EXPLORATION_ANGLE_DIFF = 5;
	
	private Port port;

	private boolean suppressed;
	
	public LineSearchBehavior(SharedState sharedState, IHAL hal, Port port) {
		super(sharedState, hal);
		this.port = port;
		this.suppressed = false;
	}

	@Override
	public void action() {
		EV3ColorSensor sensor = new EV3ColorSensor(this.port);
		SampleProvider sampleProvider = sensor.getRedMode();
		MeanFilter meanFilter = new MeanFilter(sampleProvider, LineSearchBehavior.MEAN_WINDOW);
		float[] meanBuffer = new float[meanFilter.sampleSize()];
		float[] valueBuffer = new float[sampleProvider.sampleSize()];
		
		// TODO: implement handling the barcode
		this.hal.forward();
		int counter = 1;
		Direction direction = Direction.LEFT;
		while (!this.suppressed) {
			if (this.isOnLine(meanFilter, meanBuffer, sampleProvider, valueBuffer)) {
				// We're already driving forward, just keep doing that. Reset search strategy values.
				counter = 1;
				direction = Direction.LEFT;
				continue;
			}
			
			// Okay, we're not on the line anymore. Start search strategy. We first 
			// turn EXPLORATION_ANGLE_DIFF to the left, then the same amount to the right
			// (relative to the start position), then 2 *  EXPLORATION_ANGLE_DIFF to the
			// left, ... and so on until we find the line.
			int turn_angle = counter * LineSearchBehavior.EXPLORATION_ANGLE_DIFF;
			if (direction.equals(Direction.RIGHT)) {
				turn_angle = -turn_angle;
			}
			this.hal.rotate(turn_angle, false);
			direction = Direction.changeDirection(direction);
			counter++;
		}
		
		sensor.close();
	}

	private boolean isOnLine(MeanFilter meanFilter, float[] meanBuffer, SampleProvider sampleProvider, float[] valueBuffer) {
		meanFilter.fetchSample(meanBuffer, 0);
		float currentMean = meanBuffer[0];
		
		return currentMean > LineSearchBehavior.THRESHOLD;
		
		/*sampleProvider.fetchSample(valueBuffer, 0);
		float currentValue = valueBuffer[0];
		float delta = currentMean - currentValue;
		return Math.abs(delta) > LineSearchBehavior.THRESHOLD;*/
	}

	@Override
	State getTargetState() {
		return State.LineSearch;
	}

	@Override
	public void suppress() {
		this.suppressed = true;
	}
}

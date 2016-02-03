package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;

//TODO:
//Change to three level detection: white, border, line. Behaviour: increasing rotation angle as closer the measurements comes to the border
//TODO: Redo analysis on larger data set
public class LineSearchBehavior extends StateBehavior {
	public static final float THRESHOLD = 0.1f;
	public static final int MEAN_WINDOW = 5;

	// TODO: update this as soon as we have proper handling in the HAL
	public static final int EXPLORATION_ANGLE_DIFF = 15;

	public static final int LOOP_DELAY = 100;


	private boolean suppressed;
	private MeanFilter meanFilter;
	private SampleProvider sampleProvider;
	private float[] meanBuffer;
	
	public LineSearchBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		this.suppressed = false;
		this.halInit();
	}
	private void halInit(){
		EV3ColorSensor sensor = this.hal.getColorSensor();
		sampleProvider = sensor.getRedMode();
		this.meanFilter = new MeanFilter(sampleProvider, LineSearchBehavior.MEAN_WINDOW);
		this.meanBuffer = new float[meanFilter.sampleSize()];

	}
	@Override
	public void action() {
		float[] valueBuffer = new float[sampleProvider.sampleSize()];
		// float[] fileSample = new float[sampleProvider.sampleSize()];
		// TODO: implement handling the barcode
			int counter = 1;
			Direction direction = Direction.LEFT;

			while (!this.suppressed) {
				// Do not sample too often.
				Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
				if (this.isOnLine()) {
					// Drive forward and reset search strategy values for
					// potential later use.
					counter = 1;
//					direction = Direction.LEFT;
					direction = Utils.drawDirection();
					this.hal.forward();

//					continue;
				}
				else{
	
					// Okay, we're not on the line anymore. Start search strategy.
					// We first
					// turn EXPLORATION_ANGLE_DIFF to the left, then the same amount
					// to the right
					// (relative to the start position), then 2 *
					// EXPLORATION_ANGLE_DIFF to the
					// left, ... and so on until we find the line.

					//compute the angle to rotate about
					final int angle_val = counter * LineSearchBehavior.EXPLORATION_ANGLE_DIFF;
					//Get the right direction for the turn
					final int turn_angle = direction.equals(Direction.RIGHT)? angle_val : -angle_val;
					//rotate
					this.hal.rotate(turn_angle, true);
					//Rotate until Until we have seen the line again

					while (!this.suppressed && this.hal.motorsAreMoving()) {
						if (this.isOnLine()) {
							//Overdrive
							this.hal.rotate(turn_angle, true);
							Delay.msDelay(LineSearchBehavior.LOOP_DELAY / 10);
							// We've found the line, stop moving.
							this.hal.stop();
							break;
						}

						// Again, do not sample too often here.
						Delay.msDelay(LineSearchBehavior.LOOP_DELAY / 10);
					}
					//invert direction and increase counter: In the next step explore the other direction
					direction = Direction.changeDirection(direction);
					counter++;

				}

			}

	}

	private boolean isOnLine() {

		float currentMean = getMeanSensorValue();
		boolean isOnLine = currentMean > LineSearchBehavior.THRESHOLD;
		LCD.drawString("isOnLine: " + isOnLine, 0, 0);
		LCD.drawString("currentMean: " + currentMean, 0, 1);
		return isOnLine;
	}
	private float getMeanSensorValue(){
		this.meanFilter.fetchSample(meanBuffer, 0);
		float currentMean = this.meanBuffer[0];
		return currentMean;
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

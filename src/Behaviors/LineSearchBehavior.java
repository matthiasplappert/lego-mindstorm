package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

//TODO:
//Change to three level detection: white, border, line. Behaviour: increasing rotation angle as closer the measurements comes to the border
//TODO: Redo analysis on larger data set
public class LineSearchBehavior extends StateBehavior {

	// TODO: update this as soon as we have proper handling in the HAL
	public static final int DEFAULT_EXPLORATION_ANGLE_DIFF = 5;
	public static final int CORRECTION_ANGLE = 2;

	public static final int UPPER_TRESHOLD_ANGLE = 160;
	public static final int LOOP_DELAY = 10;
	public static final long TIMEDIFF_LAST_LINE_FINDING = 2 * 1000 * 1000 * 1000;

	private boolean suppressed;

	public LineSearchBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		this.suppressed = false;
	}

	@Override
	public void action() {
		// TODO: implement handling the barcode
		int line_search_angle_diff = DEFAULT_EXPLORATION_ANGLE_DIFF;
		int counter = 1;
		Direction direction = Direction.LEFT;
		Direction overdrive_direction = Direction.LEFT;
		hal.enableRedMode();
		LCD.clear();

		while (!this.suppressed) {
			// Do not sample too often.
			Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
			LineType line_state = this.getLineState();

			long timestamp_for_correction = 0;
			switch (line_state) {
			case LINE:
				// clear some variables
				direction = Utils.drawDirection();
				counter = 1;
				line_search_angle_diff = DEFAULT_EXPLORATION_ANGLE_DIFF;
				// TODO: Move medium forward
				this.hal.forward();
				break;
			case BORDER:

				final long time_diff = Math.abs(System.nanoTime() - timestamp_for_correction);
				if (time_diff < TIMEDIFF_LAST_LINE_FINDING && timestamp_for_correction > 0) {
					this.driveAndCorrectToDirection(overdrive_direction);
				}
				// filter for time

				break;
			case BLACK:
				int angle_val = counter * line_search_angle_diff;

				// Get the right direction for the turn
				final int turn_angle = direction.getMultiplierForDirection() * angle_val;

				// rotate
				this.hal.rotate(turn_angle, true);
				// Rotate until Until we have seen the line again

				while (!this.suppressed && this.hal.motorsAreMoving()) {
					if (this.getLineState() == LineType.LINE) {

						// Overdrive

						// here choose the other direction than in line search
						// strategy
						overdrive_direction = direction.getOppositeDirection();
						driveAndCorrectToDirection(overdrive_direction);
						this.hal.stop();
						timestamp_for_correction = System.nanoTime();
						break;
					}
					// Again, do not sample too often here.
					Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
				}
				// invert direction and increase counter: In the next step
				// explore the other direction
				direction = direction.getOppositeDirection();
				counter++;
				break;
			case UNDEFINED:
				this.hal.stop();
				Sound.beepSequence();
				break;
			}

			// else {
			//
			// // Okay, we're not on the line anymore. Start search strategy.
			// // We first
			// // turn EXPLORATION_ANGLE_DIFF to the left, then the same amount
			// // to the right
			// // (relative to the start position), then 2 *
			// // EXPLORATION_ANGLE_DIFF to the
			// // left, ... and so on until we find the line.
			//
			// //compute the angle to rotate about
			//
			//
			// }

		}

	}

	private void driveAndCorrectToDirection(Direction OverrideDirection) {
		int overdrive_angle = Utils.considerDirectionForRotation(CORRECTION_ANGLE, OverrideDirection);
		this.hal.turn(overdrive_angle, false, false);
	}

	private LineType getLineState() {

		final float currentValue = hal.getRedColorSensorValue();
		LineType line_state = hal.getLineType();
		String message = null;
		switch (line_state) {
		case BLACK:
			message = "offtrack";
			break;
		case BORDER:
			message = "border";
			break;
		case LINE:
			message = "online";
		case UNDEFINED:
			throw new IllegalStateException("undefined behaviour");
		}
		LCD.drawString("Message: " + message, 0, 0);
		LCD.drawString("currentMean: " + currentValue, 0, 1);
		return line_state;
	}
	// private float getMeanSensorValue(){
	// this.meanFilter.fetchSample(meanBuffer, 0);
	// float currentMean = this.meanBuffer[0];
	// return currentMean;
	// }

	@Override
	State getTargetState() {
		return State.LineSearch;
	}

	@Override
	public void suppress() {
		this.suppressed = true;
	}
}

package Behaviors;

import HAL.ColorMode;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

//TODO:
//Change to three level detection: white, border, line. 
//Behaviour: increasing rotation angle as closer the measurements comes to the border

public class LineSearchBehavior extends StateBehavior {

	// TODO: update this as soon as we have proper handling in the HAL
	public static final int DEFAULT_EXPLORATION_ANGLE_DIFF = 5;
	public static final int CORRECTION_ANGLE = 2;

	public static final int UPPER_TRESHOLD_ANGLE = 160;
	public static final int LOOP_DELAY = 10;
	public static final long TIMEDIFF_LAST_LINE_FINDING = 2 * 1000 * 1000 * 1000;

	private boolean suppressed;
	private FindLineBehaviour findLineBehav;

	public LineSearchBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		this.suppressed = false;
		this.findLineBehav = new FindLineBehaviour(sharedState, hal, 170, 5);
	}

	@Override
	public void action() {

		Direction overdrive_direction = Direction.LEFT;
		if(!hal.isRedColorMode())
			this.hal.setColorMode(ColorMode.RED);
		LCD.clear();

		while (!this.suppressed) {
			// Do not sample too often.
			Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
			LineType line_state = this.hal.getLineType();

			long timestamp_for_correction = 0;
			switch (line_state) {
			case LINE:
				// clear some variables
				this.hal.forward(Speed.Medium);
				break;
			case BORDER:
				// filter for time
				final long time_diff = Math.abs(System.nanoTime() - timestamp_for_correction);
				if (time_diff < TIMEDIFF_LAST_LINE_FINDING && timestamp_for_correction > 0) {
					this.driveAndCorrectToDirection(overdrive_direction);
				}

				break;
			case BLACK:
				findLineBehav.action();
				switch(findLineBehav.returnState()){
				case Line_FOUND:
					overdrive_direction = findLineBehav.getLastUsedDirection();
					break;
				case ROTATION_LIMIT_ERROR:
					this.hal.stop();
					LCD.drawString("Overotation. Please manually set this robot on a better position", 0, 0);
					
					break;
				default:
					break;
				
				}
			case UNDEFINED:
				break;
			default:
				break;
				}
				break;
			}

	}

	private void driveAndCorrectToDirection(Direction OverrideDirection) {
		int overdrive_angle = Utils.considerDirectionForRotation(CORRECTION_ANGLE, OverrideDirection);
		this.hal.turn(overdrive_angle, false, false);
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

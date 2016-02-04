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

	// 0: small rotations, 1: check for barcode, 2: large rotations
	private int searchStage = 0;

	public LineSearchBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		this.suppressed = false;
	}

	@Override
	public void action() {

		Direction overdrive_direction = Direction.LEFT;
		if (!hal.isRedColorMode())
			this.hal.setColorMode(ColorMode.RED);
		LCD.clear();

		this.hal.setSpeed(Speed.Fast);
		this.hal.resetGyro();

		while (!this.suppressed) {
			// Do not sample too often.
			Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
			LineType line_state = this.hal.getLineType();

			long timestamp_for_correction = 0;
			switch (line_state) {
			case LINE:
				// clear some variables
				this.hal.forward();
				break;
			case BORDER:
				// filter for time
				/*final long time_diff = Math.abs(System.nanoTime() - timestamp_for_correction);
				if (time_diff < TIMEDIFF_LAST_LINE_FINDING && timestamp_for_correction > 0) {
					this.driveAndCorrectToDirection(overdrive_direction.getOppositeDirection());
				}*/
				break;
			case BLACK:
				switch (searchStage) {
				case 0:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 20, Direction.LEFT);
					this.findLineBehav.action();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 1:
					// TODO start BarCode Behaviour 
					this.searchStage++; //remove this line if todo is finished, only increase searchStage on failed behaviour
					break;
				case 2:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 100, Direction.LEFT);
					this.findLineBehav.action();
					reactToFindLine(findLineBehav.returnState());
					break;
				}
				break;
			}
		}
	}

	private void driveAndCorrectToDirection(Direction OverrideDirection) {
		int overdrive_angle = Utils.considerDirectionForRotation(CORRECTION_ANGLE, OverrideDirection);
		this.hal.turn(overdrive_angle);

		while (this.hal.isRotating() && !this.suppressed) {
			Delay.msDelay(10);
		}
	}

	private void reactToFindLine(FindLineReturnState state) {
		switch(state){
		case LINE_FOUND:
			this.hal.forward();
			this.searchStage = 0;
			break;
		case LINE_NOT_FOUND:	
			this.searchStage++;
			break;
		}
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

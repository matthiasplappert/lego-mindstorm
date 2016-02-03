package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

/**
 * This class rotates the robot (in place) until it finds the line to follow.
 * 
 * @author chris
 *
 */
public class FindLineBehaviour extends StateBehavior {
	private boolean suppressed;
	private FindLineReturnState returnState;
	private final int default_exploration_angle;
	private final int maxAngle;
	private Direction direction;
	public static final int LOOP_DELAY = 100;

	public FindLineBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState, hal, 170, 5);

	}

	public FindLineBehaviour(SharedState sharedState, IHAL hal, int maxAngle, int defaultExplorationAngleDiff) {
		super(sharedState, hal);
		this.suppressed = false;
		this.default_exploration_angle = defaultExplorationAngleDiff;
		this.maxAngle = maxAngle;
		this.suppressed = false;
	}

	public Direction getLastUsedDirection() {
		return this.direction;
	}

	@Override
	public void action() {
		// this.hal.resetGyro();
		// final float initGyro = this.hal.getGyroValue();
		this.direction = Utils.drawDirection();
		int line_search_angle_diff = this.default_exploration_angle;
		int counter = 1;

		if (!this.hal.isRedMode())
			this.hal.enableRedMode();
		LCD.drawString("Start Line Search", 0, 0);
		while (!this.suppressed) {
			// get Gyro Value
			// check if we have rotate for more than 180 degree
			LineType line_state = this.hal.getLineType();
			LCD.drawString("LineType: " + line_state.toString(), 1, 0);
			if (line_state == LineType.LINE) {
				this.returnState = FindLineReturnState.Line_FOUND;
				return;
			}

			int angle_val = counter * line_search_angle_diff;
			if (Math.abs(angle_val) > this.maxAngle) {
				// then abort line search. It is the callers responsibility
				// to fix this situation
				this.hal.stop();
				LCD.drawString("Angle overflow", 0, 1);
				Sound.beepSequence();
				Delay.msDelay(1000);

				this.returnState = FindLineReturnState.ROTATION_LIMIT_ERROR;
				return;
			}
			// Get the right direction for the turn
			final int turn_angle = direction.getMultiplierForDirection() * angle_val;

			// rotate
			this.hal.rotate(turn_angle, false);
			// Rotate until we have seen the line again

//			this.hal.rotate(turn_angle, true);
			// Rotate until Until we have seen the line again

			while (!this.suppressed && this.hal.motorsAreMoving()) {
				if (this.hal.getLineType() == LineType.LINE) {

					// Overdrive

					// here choose the other direction than in line search
					// strategy
					this.hal.stop();
					break;
				}
				// Again, do not sample too often here.
				Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
			}
			// invert direction and increase counter: In the next step explore
			// the other direction
			direction = direction.getOppositeDirection();
			counter++;
			Delay.msDelay(FindLineBehaviour.LOOP_DELAY);

		}
		// invert direction and increase counter: In the next step
		// explore the other direction

	}

	

	public FindLineReturnState returnState() {
		return this.returnState;
	}

	@Override
	public void suppress() {
		this.suppressed = true;

	}

	@Override
	State getTargetState() {
		return State.FindLineState;
	}

}

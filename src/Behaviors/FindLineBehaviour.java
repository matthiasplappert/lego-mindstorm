package Behaviors;

import HAL.ColorMode;
import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
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
	private final int searchAngle;
	private Direction lastUsedDirection;

	public static final int LOOP_DELAY = 100;

	public FindLineBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState, hal, 20, Direction.RIGHT);

	}

	public FindLineBehaviour(SharedState sharedState, IHAL hal, int searchAngle, Direction initialDirection) {
		super(sharedState, hal);
		this.suppressed = false;
		this.searchAngle = searchAngle;
		this.lastUsedDirection = initialDirection;
	}

	public Direction getLastUsedDirection() {
		return this.lastUsedDirection;
	}

	@Override
	public void action() {
		this.hal.resetGyro();
		boolean bothDirectionsChecked = false;
		int sign;

		if (!this.hal.isRedColorMode())
			this.hal.setColorMode(ColorMode.RED);

		while (!this.suppressed) {
			// check if we have found a line
			LineType line_state = this.hal.getLineType();
			this.hal.printOnDisplay("FindLine: " + line_state.toString(), 4, 0);
			if (line_state == LineType.LINE) {
				this.hal.stop();
				this.returnState = FindLineReturnState.LINE_FOUND;
				this.hal.printOnDisplay("FindLine: LINE_FOUND", 5, 0);
				return;
			}

			// if right sign = 1, if left sign = -1
			if(lastUsedDirection == Direction.RIGHT){				
				sign = 1;
				this.hal.printOnDisplay("TURN RIGHT", 6, 0);
			}else{
				sign = -1;
				this.hal.printOnDisplay("TURN LEFT", 6, 0);
			}
			// rotate to angle			
			this.hal.rotateTo(sign * searchAngle);
			// Rotate until we have seen the line again or we reached the
			// searchAngle
			while (!this.suppressed && this.hal.isRotating()) {
				if (this.hal.getLineType() == LineType.LINE) {
					this.hal.stop();
					this.returnState = FindLineReturnState.LINE_FOUND;
					this.hal.printOnDisplay("FindLine: LINE_FOUND", 5, 0);
					return;
				}
				// Again, do not sample too often here.
				Delay.msDelay(LineSearchBehavior.LOOP_DELAY / 2);
			}
			// invert direction and increase counter: In the next step explore
			// the other direction
			lastUsedDirection = lastUsedDirection.getOppositeDirection();

			// we turned left and right and did not found a line
			if (bothDirectionsChecked) {
				this.returnState = FindLineReturnState.LINE_NOT_FOUND;
				this.hal.printOnDisplay("FindLine: bothChecked LINE_NOT_FOUND", 5, 0);
				// we restore the Direction we were looking before
				Sound.buzz();
				this.hal.rotateTo(0);
				while (!this.suppressed && this.hal.isRotating()) {
					Delay.msDelay(LineSearchBehavior.LOOP_DELAY / 2);
				}
				return;
			}
			bothDirectionsChecked = true;
		}
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

package Behaviors;

import HAL.ColorMode;
import HAL.IHAL;
import State.SharedState;
import State.MyState;
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
	private final boolean useRotation; // if true rotate, if false turn

	public static final int LOOP_DELAY = 2;

	public FindLineBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState, hal, 20, Direction.RIGHT, true);

	}

	public FindLineBehaviour(SharedState sharedState, IHAL hal, int searchAngle, Direction initialDirection) {
		this(sharedState, hal, searchAngle, initialDirection, true);
	}

	public FindLineBehaviour(SharedState sharedState, IHAL hal, int searchAngle, Direction initialDirection,
			boolean useRotation) {
		super(sharedState, hal);
		this.suppressed = false;
		this.searchAngle = searchAngle;
		this.lastUsedDirection = initialDirection;
		this.useRotation = useRotation;
		this.returnState = FindLineReturnState.LINE_NOT_FOUND;
	}

	public Direction getLastUsedDirection() {
		return this.lastUsedDirection;
	}

	@Override
	public void action() {
		this.suppressed = false;
		
		this.hal.resetGyro();
		boolean bothDirectionsChecked = false;
		int sign;

		if (!this.hal.isRedColorMode())
			this.hal.setColorMode(ColorMode.RED);

		while (!this.suppressed) {
			// check if we have found a line
			LineType line_state = this.hal.getLineType();
			//this.hal.printOnDisplay("FindLine: " + line_state.toString(), 4, 0);
			if (line_state == LineType.LINE) {
				this.hal.stop();
				this.returnState = FindLineReturnState.LINE_FOUND;
				//this.hal.printOnDisplay("FindLine: LINE_FOUND", 5, 0);
				return;
			}

			// if right sign = 1, if left sign = -1
			if (lastUsedDirection == Direction.RIGHT) {
				sign = 1;
				//this.hal.printOnDisplay("TURN RIGHT", 6, 0);
			} else {
				sign = -1;
				//this.hal.printOnDisplay("TURN LEFT", 6, 0);
			}
			// rotate/turn to angle
			if (this.useRotation) {
				this.hal.rotateTo(sign * searchAngle, false);
			} else { // use turn
				this.hal.turnTo(sign * searchAngle, false);
			}
			// Rotate until we have seen the line again or we reached the
			// searchAngle
			while (!this.suppressed && this.hal.isRotating()) {
				if (this.hal.getLineType() == LineType.LINE) {
					this.hal.stop();
					this.returnState = FindLineReturnState.LINE_FOUND;
					//this.hal.printOnDisplay("FindLine: LINE_FOUND", 5, 0);
					return;
				}
				// Again, do not sample too often here.
				Delay.msDelay(LineFollowBehavior.LOOP_DELAY);
			}

			// we turned left and right and did not found a line
			if (bothDirectionsChecked) {
				this.returnState = FindLineReturnState.LINE_NOT_FOUND;
				//this.hal.printOnDisplay("FindLine: bothChecked LINE_NOT_FOUND", 5, 0);
				// we restore the Direction we were looking before
				//Sound.buzz();

				if (this.useRotation) {
					this.hal.rotateTo(0, true);
				} else { // use turn
					this.hal.turnTo(0, true);
				}

				while (!this.suppressed && this.hal.isRotating()) {
					Delay.msDelay(LineFollowBehavior.LOOP_DELAY);
				}
				return;
			}

			// invert direction and increase counter: In the next step explore
			// the other direction
			lastUsedDirection = lastUsedDirection.getOppositeDirection();
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
	MyState getTargetState() {
		return MyState.FindLineState;
	}

}

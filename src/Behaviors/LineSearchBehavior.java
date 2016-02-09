package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class LineSearchBehavior extends StateBehavior {
	private boolean suppressed = false;

	private static final int STEP_DELAY_MS = 10;

	private static final float FORWARD_DISTANCE = 10.0f;

	private static final float FORWARD_SEARCH_DISTANCE = 40.0f;

	private static final float DISTANCE_THRESHOLD = 10.0f;

	private static final int TURN_ANGLE = 60;

	private static final int ROTATE_TO_ANGLE = -70;

	private static final Speed TURN_SPEED = Speed.Medium;

	public LineSearchBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	@Override
	public void action() {
		this.suppressed = false;
		boolean hasFoundLine = false;
		// Move forward a bit and then start search sequence.
		this.hal.resetGyro();
		this.hal.resetLeftTachoCount();
		this.hal.resetRightTachoCount();
		while (!this.suppressed && this.hal.getLeftTachoDistance() < FORWARD_DISTANCE && !hasFoundLine) {
			this.hal.performCourseFollowingStep();
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			Delay.msDelay(STEP_DELAY_MS);
		}
		if (hasFoundLine) {
			this.didFindLine();
			return;
		}
		this.hal.stop();

		// Turn to the right.
		//this.hal.printOnDisplay("TURN: right", 1, 0);
		this.hal.setSpeed(TURN_SPEED);
		this.hal.rotate(TURN_ANGLE);
		while (!this.suppressed && this.hal.isRotating() && !this.hal.isTouchButtonPressed()
				&& this.hal.getMeanDistance() > DISTANCE_THRESHOLD && !hasFoundLine) {
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			Delay.msDelay(STEP_DELAY_MS);
		}
		if (hasFoundLine) {
			this.didFindLine();
			return;
		}
		this.hal.stop();

		this.hal.resetLeftTachoCount();
		this.hal.forward();
		while (!suppressed && this.hal.getLeftTachoDistance() < FORWARD_SEARCH_DISTANCE
				&& !this.hal.isTouchButtonPressed() && this.hal.getMeanDistance() > DISTANCE_THRESHOLD
				&& !hasFoundLine) {
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			Delay.msDelay(10);
		}
		if (hasFoundLine) {
			this.didFindLine();
			return;
		}
		this.hal.stop();

		// Turn to the left.
		//this.hal.printOnDisplay("TURN: left", 1, 0);
		this.hal.rotateTo(ROTATE_TO_ANGLE, true);
		while (!this.suppressed && this.hal.isRotating()&& !hasFoundLine) {
			//this.hal.printOnDisplay(Float.toString(this.hal.getCurrentGyro()), 5, 0);
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			Delay.msDelay(STEP_DELAY_MS);
		}
		if (hasFoundLine) {
			this.didFindLine();
			return;
		}
		this.hal.stop();
		
		this.hal.resetLeftTachoCount();
		this.hal.forward();
		while (!suppressed && this.hal.getLeftTachoDistance() < FORWARD_SEARCH_DISTANCE
				&& !this.hal.isTouchButtonPressed() && this.hal.getMeanDistance() > DISTANCE_THRESHOLD
				&& !hasFoundLine) {
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			Delay.msDelay(10);
		}
		
		
		//this.hal.turn(-TURN_ANGLE);
//		while (!this.suppressed && this.hal.isRotating() && !this.hal.isTouchButtonPressed() && !hasFoundLine) {
//			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
//			Delay.msDelay(STEP_DELAY_MS);
//		}
		if (hasFoundLine) {
			this.didFindLine();
			return;
		}
		this.hal.stop();

		// Just keep going, maybe we'll find it eventually...
		Sound.buzz();
		this.hal.forward();
		while (!this.suppressed && !this.hal.isTouchButtonPressed() && !hasFoundLine) {
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			Delay.msDelay(STEP_DELAY_MS);
		}
		this.didFindLine();
	}

	private void didFindLine() {
		//this.hal.printOnDisplay("did find line", 2, 0);
		this.hal.stop();
		Sound.beepSequenceUp();
		this.sharedState.setState(MyState.LineFollowState);
	}

	@Override
	public void suppress() {
		this.suppressed = true;

	}

	@Override
	MyState getTargetState() {
		return MyState.LineSearchState;
	}
}

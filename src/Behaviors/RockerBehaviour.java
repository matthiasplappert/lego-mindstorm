package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class RockerBehaviour extends StateBehavior {
	public RockerBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	//public static final int DEFAULT_EXPLORATION_ANGLE_DIFF = 5;
	public static final int LOOP_DELAY = 10;
	public static final int SEARCH_COURSE_ANGLE = -45;
	public static final int STRAIGHT_LINE_THRESHOLD = 1000; // in ms

	private boolean suppressed = false;

	private FindLineBehaviour findLineBehav;
	private Direction lastDirection = Direction.LEFT;

	@Override
	public void action() {
		this.suppressed = false;
		
		this.hal.resetLeftTachoCount();
		this.hal.resetRightTachoCount();

		this.hal.printOnDisplay("RockerBehaviour started", 0, 0);
		this.hal.printOnDisplay("Searching for line", 1, 0);
		this.hal.resetGyro();
		this.hal.setCourseFollowingAngle(SEARCH_COURSE_ANGLE);
		while (!this.suppressed && !this.hal.getLineType().equals(LineType.LINE)) {
			this.hal.performCourseFollowingStep();
			Delay.msDelay(LOOP_DELAY);
		}
		Sound.beep();
		this.hal.stop();
		Delay.msDelay(5000);
		
		// At this point, we are on the line.
		long currentTime;
		long lastTimeLineFound = Long.MAX_VALUE;
		// 0 = search line, 1 = full speed, but did not leave line yet, 2 = left
		// line, next is barcode
		int fullSpeedMode = 0;
		this.hal.printOnDisplay("Line found", 1, 0);
		this.hal.setSpeed(Speed.Rocker);
		this.hal.resetGyro();
		boolean lineFollowDone = false;
		while (!this.suppressed && !lineFollowDone) {
			switch (this.hal.getLineType()) {
			case LINE:
				if (fullSpeedMode == 2) {
					lineFollowDone = true;
					this.hal.stop();
				} else {
					currentTime = System.currentTimeMillis();
					lastTimeLineFound = Math.min(lastTimeLineFound, currentTime);

					if (currentTime - lastTimeLineFound >= STRAIGHT_LINE_THRESHOLD) {
						// engage full speed mode
						Sound.beep();
						this.hal.setSpeed(Speed.VeryFast);
						fullSpeedMode = 1;
					}
					this.hal.forward();
				}
				break;
			case BORDER:
			case BLACK:
				if (fullSpeedMode == 1) {
					// roboter left the line, next is barcode
					Sound.beepSequenceUp();
					fullSpeedMode = 2;
				} else {
					// still searching a straight way ahead.
					lastTimeLineFound = Long.MAX_VALUE;
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 12,
							this.lastDirection.getOppositeDirection(), false);
					this.findLineBehav.action();
					this.lastDirection = this.findLineBehav.getLastUsedDirection();
					if (findLineBehav.returnState().equals(LineType.LINE)) {
						this.hal.forward();
					}
				}
				break;
			default:
				break;
			}
			Delay.msDelay(RockerBehaviour.LOOP_DELAY);
		}
		
		// Just keep going straight until the end of the bridge. We ignore the barcode.
		this.hal.resetGyro();
		this.hal.setCourseFollowingAngle(0);
		while (!this.suppressed && this.hal.getLeftTachoDistance() < 120.0f) {
			this.hal.performCourseFollowingStep();
		}
		this.hal.stop();
		
		// Find the line again.
		this.sharedState.setState(MyState.FindLineState);
	}

	@Override
	MyState getTargetState() {
		return MyState.RockerState;
	}

	@Override
	public void suppress() {
		if (this.findLineBehav != null) {
			this.findLineBehav.suppress();
		}
		suppressed = true;
	}
}

package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class RockerBehaviour extends StateBehavior {
	public RockerBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	//public static final int DEFAULT_EXPLORATION_ANGLE_DIFF = 5;
	public static final int LOOP_DELAY = 10;
	public static final int SEARCH_COURSE_ANGLE = -45;
	public static final int STRAIGHT_LINE_THRESHOLD = 1000; // in ms
	public static final float FORWARD_DISTANCE = 130.0f; // in cm

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
		//Delay.msDelay(5000);
		
		// At this point, we are on the line. Follow it until we are mostly straight.
		long currentTime;
		long lastTimeLineFound = Long.MAX_VALUE;
		this.hal.printOnDisplay("Line found", 1, 0);
		this.hal.setSpeed(Speed.Rocker);
		this.hal.resetGyro();
		boolean lineFollowDone = false;
		while (!this.suppressed && !lineFollowDone) {
			switch (this.hal.getLineType()) {
			case LINE:
				currentTime = System.currentTimeMillis();
				lastTimeLineFound = Math.min(lastTimeLineFound, currentTime);

				if (currentTime - lastTimeLineFound >= STRAIGHT_LINE_THRESHOLD) {
					// engage full speed mode
					lineFollowDone = true;
					this.hal.stop();
				} else {
					this.hal.forward();
				}
				break;
			case BORDER:
			case BLACK:
				// still searching a straight way ahead.
				lastTimeLineFound = Long.MAX_VALUE;
				this.findLineBehav = new FindLineBehaviour(sharedState, hal, 12,
						this.lastDirection.getOppositeDirection(), false);
				this.findLineBehav.action();
				this.lastDirection = this.findLineBehav.getLastUsedDirection();
				if (findLineBehav.returnState().equals(LineType.LINE)) {
					this.hal.forward();
				}
				break;
			default:
				break;
			}
			Delay.msDelay(RockerBehaviour.LOOP_DELAY);
		}
		
		this.hal.stop();
		LCD.clear();
		this.hal.printOnDisplay("RockerBehaviour", 0, 0);
		this.hal.printOnDisplay("going straight ahead", 1, 0);
		
		// Just keep going straight until the end of the bridge. We ignore the barcode.
		this.hal.resetGyro();
		this.hal.setCourseFollowingAngle(0);
		this.hal.setSpeed(Speed.VeryFast);
		while (!this.suppressed && this.hal.getLeftTachoDistance() < FORWARD_DISTANCE) {
			this.hal.performCourseFollowingStep();
			this.hal.printOnDisplay("distance: " + this.hal.getLeftTachoCount(), 3, 0);
			Delay.msDelay(LOOP_DELAY);
		}
		this.hal.stop();
		
		// Find the line again.
		this.sharedState.setState(MyState.LineSearchState);
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

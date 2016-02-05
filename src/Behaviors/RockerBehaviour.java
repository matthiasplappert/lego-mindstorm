package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class RockerBehaviour extends StateBehavior {

	public RockerBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	public static final int DEFAULT_EXPLORATION_ANGLE_DIFF = 5;
	public static final int LOOP_DELAY = 10;

	private boolean suppressed = false;

	// 0 = search line, 1 = full speed, but did not leave line yet, 2 = left
	// line, next is barcode
	private int fullSpeedMode = 0;
	private long lastTimeLineFound = Long.MAX_VALUE;

	private FindLineBehaviour findLineBehav;
	private Direction lastDirection = Direction.RIGHT; // opposite of first
														// direction

	// 0: small rotation, 1: line not found --> barcode
	private int searchStage = 0;

	@Override
	public void action() {
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 0);
		long currentTime;

		Sound.beep();

		while (!this.suppressed) {

			this.hal.setSpeed(Speed.Rocker);
			this.hal.resetGyro();
			Delay.msDelay(RockerBehaviour.LOOP_DELAY);
			LineType line_state = this.hal.getLineType();

			switch (line_state) {
			case LINE:
				if (fullSpeedMode == 2) {
					// TODO check for barcode
					Sound.buzz();
					this.hal.stop();
				} else {
					currentTime = System.currentTimeMillis();
					lastTimeLineFound = Math.min(lastTimeLineFound, currentTime);

					if (currentTime - lastTimeLineFound >= 40) {
						// engage full speed mode
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
					fullSpeedMode = 2;
				} else {
					// still searching a straight way ahead

					lastTimeLineFound = Long.MAX_VALUE;
					switch (searchStage) {
					case 0:
						this.findLineBehav = new FindLineBehaviour(sharedState, hal, 12,
								this.lastDirection.getOppositeDirection(), false);
						this.findLineBehav.action();
						this.lastDirection = this.findLineBehav.getLastUsedDirection();
						reactToFindLine(findLineBehav.returnState());
						break;
					case 2:

						break;
					}
				}
				break;
			default:
				break;
			}

		}

		this.sharedState.reset(true);
		Thread.yield();
	}

	private void reactToFindLine(FindLineReturnState state) {
		switch (state) {
		case LINE_FOUND:
			this.hal.printOnDisplay("Result is LINE_FOUND at " + this.searchStage, 2, 0);
			this.hal.forward();
			this.searchStage = 0; // reset search stage
			break;
		case LINE_NOT_FOUND:
			this.hal.printOnDisplay("Result is LINE_NOT_FOUND at " + this.searchStage, 2, 0);
			this.searchStage++;
			break;
		}
	}

	@Override
	State getTargetState() {
		return State.RockerState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}

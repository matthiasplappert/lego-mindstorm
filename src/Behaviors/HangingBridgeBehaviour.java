package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import HAL.Speed;

public class HangingBridgeBehaviour extends StateBehavior {

	public HangingBridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed = false;
	private float target_dist;
	private float offset = 0f;
	private static final int DELAY = 5;

	private int searchStage = 0;

	private int maxTurnAngle = 1;

	private FindLineBehaviour findLineBehav;
	private Direction lastDirection = Direction.LEFT;

	@Override
	public void action() {
		float difference;
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 0);

		// Follow line
		// After loosing the line

		float distance = this.hal.getMeanDistance();
		while (distance > 15.f) {
			// Linesearch
			Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
			LineType line_state = this.hal.getLineType();

			switch (line_state) {
			case LINE:
				// clear some variables
				this.hal.printOnDisplay("Search found LINE", 1, 0);
				this.hal.forward();
				Delay.msDelay(100);
				break;
			case BLACK:
				this.hal.printOnDisplay("Search found BLACK at " + searchStage, 1, 0);
				switch (searchStage) {
				case 0:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 30, this.lastDirection);
					this.findLineBehav.action();
					this.lastDirection = this.findLineBehav.getLastUsedDirection();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 2:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 100, this.lastDirection);
					this.findLineBehav.action();
					this.lastDirection = this.findLineBehav.getLastUsedDirection();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 3:
					// Error nothing ever found
					Sound.buzz();
					searchStage = 0; // TODO remove this here
				}
				break;
			default:
				break;
			// this.hal.testMotor();
			// End Linesearch
			}
			distance = this.hal.getMeanDistance();
		}

		this.hal.resetGyro();
		this.hal.setSpeed(Speed.Medium);
		this.hal.forward();
		float diff = 0;
		int steps = 0;
		int i = 0;
		float[] last_diff = new float[3];
		last_diff[0] = 200;
		last_diff[1] = 200;
		last_diff[2] = 200;
		boolean enough = false;
		while (!enough && this.hal.getCurrentDistance() < 15.f && !suppressed) {
			this.hal.forward();
			Delay.msDelay(400);
			difference = this.hal.getMeanDistance() - distance;
			last_diff[i] = difference;
			diff = last_diff[0] + last_diff[1] + last_diff[2];
			if (steps > 15 && Math.abs(diff / 3) <= 0.2f) {
				enough = true;
			} else if (Math.abs(difference) > 0.2f) {
				this.hal.turn((int) Math.signum(difference));
			}
			// Maybe even higher
			Delay.msDelay(200);
			this.hal.stop();
			steps++;
			i++;
			if (i > 2)
				i = 0;
			// }
		}
		this.hal.stop();
		//
		// this.target_dist = this.hal.getMeanDistance();
		//
		// for (int i = 0; i < 250; i++) {
		// if (this.suppressed) {
		// break;
		// }
		// if (isTooClose(this.hal.getMeanDistance())) {
		// // Sound.beep();
		// this.hal.turn(-this.maxTurnAngle);
		//
		// while (!this.suppressed && this.hal.isRotating()) {
		// if (!this.isTooClose(this.hal.getMeanDistance())) {
		// break;
		// }
		// // Delay.msDelay(DELAY);
		// }
		//
		// } else if (isTooFar(this.hal.getMeanDistance())) {
		// // Sound.buzz();
		// this.hal.turn(this.maxTurnAngle);
		//
		// while (!this.suppressed && this.hal.isRotating()) {
		// if (!this.isTooFar(this.hal.getMeanDistance())) {
		// break;
		// }
		// // Delay.msDelay(DELAY);
		// }
		// }
		// this.hal.forward();
		//
		// Delay.msDelay(DELAY);
		// this.hal.printOnDisplay("i = " + i, 2, 0);
		// }
		// this.hal.stop();
		// this.hal.setSpeed(Speed.Medium);
		Sound.beep();
		// this.hal.resetGyro();
		this.hal.setSpeed(Speed.VeryFast);
		Delay.msDelay(100);
		this.hal.setCourseFollowingAngle((int) this.hal.getMeanGyro());
		while (!this.suppressed && this.hal.getLineType() != LineType.LINE)

		{
			this.hal.performCourseFollowingStep();
			Delay.msDelay(10);
		}
		this.hal.stop();
		this.sharedState.reset(true);
		Thread.yield();

	}

	@Override
	State getTargetState() {
		return State.HangingBridgeState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}

	// private boolean isTooFar(float distance) {
	// return distance > this.target_dist + this.offset;
	// }
	//
	// private boolean isTooClose(float distance) {
	// return distance < this.target_dist;
	// }

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
}

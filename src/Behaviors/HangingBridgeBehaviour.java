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
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 1000);
		float distance = this.hal.getMeanDistance();
		while (distance > 15.f) {
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
			}
			distance = this.hal.getMeanDistance();
		}

		this.hal.resetGyro();
		// // Follow line
		// // After loosing the line

		// // if (distance > 10) {
		// // search line
		// // }
		// this.hal.setSpeed(Speed.Slow);
		this.hal.setSpeed(Speed.Medium);
		this.hal.forward();
		// Delay.msDelay(1000);
		float diff = 0;
		int steps = 0;
		boolean enough = false;
		while (!enough && this.hal.getCurrentDistance() < 15.f && !suppressed)

		{
			steps++;
			this.hal.forward();
			Delay.msDelay(500);
			difference = this.hal.getMeanDistance() - distance;
			diff += difference;
			if (steps > 5 && diff / 5 < 0.1) {
				enough = true;
			}
			if (Math.abs(difference) > 0.3f) {
				this.hal.turn((int) Math.signum(difference));
				Delay.msDelay(200);
			}
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
		Sound.buzz();
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

	private boolean isTooFar(float distance) {
		return distance > this.target_dist + this.offset;
	}

	private boolean isTooClose(float distance) {
		return distance < this.target_dist;
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
}

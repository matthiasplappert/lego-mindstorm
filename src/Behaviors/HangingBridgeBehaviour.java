package Behaviors;

import org.junit.rules.DisableOnDebug;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import HAL.Speed;

public class HangingBridgeBehaviour extends StateBehavior {

	public HangingBridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed = false;

	private static final int DELAY = 10;

	private int searchStage = 0;

	private FindLineBehaviour findLineBehav;

	private LineSearchBehavior linesearch;

	@Override
	public void action() {
		this.suppressed = false;

		float difference;
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 0);
		this.hal.printOnDisplay("Search Line", 7, 0);
		if (!this.hal.getLineType().equals(LineType.LINE)) {
			//this.hal.rotate(40);
			//while(!suppressed && this.hal.isRotating()){
			//	Delay.msDelay(5);
			//}
			//this.hal.forward();
			//Delay.msDelay(50);
			
			linesearch = new LineSearchBehavior(sharedState, hal);
			linesearch.action();
			sharedState.setState(this.getTargetState());
		}
		// Follow line
		// After loosing the line
		this.hal.printOnDisplay("Follow Line", 7, 0);
		this.hal.setSpeed(Speed.FollowLine);

		float distance = this.hal.getMeanDistance();
		while (distance > 15.f) {
			// Linesearch
			Delay.msDelay(HangingBridgeBehaviour.DELAY);
			LineType line_state = this.hal.getLineType();

			switch (line_state) {
			case LINE:
				// clear some variables
				// this.hal.printOnDisplay("Search found LINE", 1, 0);
				this.hal.forward();
				Delay.msDelay(20);
				break;
			case BLACK:
				// this.hal.printOnDisplay("Search found BLACK at " +
				// searchStage, 1, 0);
				switch (searchStage) {
				case 0:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 45, Direction.RIGHT);
					this.findLineBehav.action();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 2:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 100, Direction.RIGHT);
					this.findLineBehav.action();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 3:
					// Error nothing ever found
					Sound.buzz();
					Sound.buzz();
					Sound.buzz();
					Sound.buzz();
					Sound.buzz();
					Sound.buzz();
					searchStage = 0; // TODO remove this here
				}
				break;
			default:
				break;
			}
			distance = this.hal.getMeanDistance();
		}
		
		this.hal.stop();
		Sound.buzz();
		Sound.beep();
		Sound.buzz();
		this.hal.setSpeed(Speed.Fast);
		this.hal.printOnDisplay("Get Direction", 7, 0);
		this.hal.resetGyro();
		this.hal.forward();
		float current_dist = 0;
		float diff = 0;
		float avg = 0;
		// int steps = 0;
		int i = 0;
		float[] last_dist = new float[3];
		for (int k = 0; k < last_dist.length; k++) {
			last_dist[k] = this.hal.getMeanDistance();
			Delay.msDelay(5);
		}
		boolean enough = false;
		float minimum_distance = 20;
		this.hal.resetLeftTachoCount();
		int count = 0;
		int gyro_follow = 0;

		while (!enough && !suppressed) {//&&  this.hal.getCurrentDistance() < 15.f 
			count++;

			
			this.hal.forward();
			Delay.msDelay(100);
			current_dist = this.hal.getMeanDistance();
			
			
			avg = 0;
			for (int k = 0; k < last_dist.length; k++) {
				avg += last_dist[k];
			}
			avg /= last_dist.length;
			diff = current_dist - avg;
			
			last_dist[i] = current_dist;
			//if (this.hal.getLeftTachoDistance() >= minimum_distance && diff <= 0.02f) {
			if ( count > 10 && diff <= 0.01f) {
				enough = true;
				gyro_follow = (int)this.hal.getMeanGyro();
				Sound.beep();
				Sound.beep();
				Sound.beep();
				Sound.beep();

			} else {// else if (Math.abs(current_dist) > 0.1f) {
				this.hal.turn((int) Math.signum(diff));
			}

			Delay.msDelay(80);
			this.hal.stop();
			i = (i + 1) % last_dist.length;
			
			
			
			this.hal.printOnDisplay("Count: " + count, 2, 0);
			this.hal.printOnDisplay("Array_1:" + last_dist[0], 3, 0);
			this.hal.printOnDisplay("Array_2:" + last_dist[1], 4, 0);
			this.hal.printOnDisplay("Array_3:" + last_dist[2], 5, 0);
			this.hal.printOnDisplay("Current:" + current_dist , 6, 0);
		}
		this.hal.stop();
		this.hal.printOnDisplay("Move Forward", 7, 0);
		//Sound.beep();
		//Delay.msDelay(5000);
		// this.hal.printOnDisplay("In Gas", 3, 0);

		/*
		 * this.hal.resetGyro(); this.hal.forward(); float diff = 0; // int
		 * steps = 0; int i = 0; float[] last_diff = new float[3]; for (int k =
		 * 0; k < last_diff.length; k++) { last_diff[k] = 200; } boolean enough
		 * = false; float minimum_distance = 5; this.hal.resetLeftTachoCount();
		 * int count = 0; while (!enough && this.hal.getCurrentDistance() < 15.f
		 * && !suppressed) { count++; this.hal.printOnDisplay("Count: " + count,
		 * 2, 0); this.hal.forward(); Delay.msDelay(100); difference =
		 * this.hal.getMeanDistance() - distance; last_diff[i] = difference;
		 * diff = 0; for (int k = 0; k < last_diff.length; k++) { diff +=
		 * last_diff[k]; } if (this.hal.getLeftTachoDistance() >=
		 * minimum_distance && Math.abs(diff / last_diff.length) <= 0.2f) {
		 * enough = true; Sound.beep(); Sound.beep(); Sound.beep();
		 * Sound.beep();
		 * 
		 * } else if (Math.abs(difference) > 0.2f) { this.hal.turn((int)
		 * Math.signum(difference)); }
		 * 
		 * Delay.msDelay(80); this.hal.stop(); i = (i + 1) % last_diff.length; }
		 * this.hal.stop(); Sound.beep(); Delay.msDelay(5000);
		 * this.hal.printOnDisplay("In Gas", 3, 0);
		 */

		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.SAFE);
		//this.hal.setSpeed(Speed.VeryFast);
		Delay.msDelay(100);
		this.hal.setCourseFollowingAngle(gyro_follow);
		
		while (!this.suppressed && this.hal.getLineType() != LineType.LINE)

		{
			this.hal.performCourseFollowingStep();
			Delay.msDelay(10);
		}
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		this.hal.stop();
		this.hal.backward();
		this.hal.resetLeftTachoCount();
		while (!suppressed && this.hal.getLeftTachoDistance() > -10) {
			Delay.msDelay(10);
		}
		this.hal.printOnDisplay("END!", 7, 0);
		this.hal.stop();
		this.sharedState.reset(true);
		Thread.yield();

	}

	@Override
	MyState getTargetState() {
		return MyState.HangingBridgeState;
	}

	@Override
	public void suppress() {
		if (linesearch != null) {
			this.linesearch.suppress();
		}
		if (findLineBehav != null) {
			this.findLineBehav.suppress();
		}
		suppressed = true;
	}

	private void reactToFindLine(FindLineReturnState state) {
		switch (state) {
		case LINE_FOUND:
			// this.hal.printOnDisplay("Result is LINE_FOUND at " +
			// this.searchStage, 2, 0);
			this.hal.forward();
			this.searchStage = 0; // reset search stage
			break;
		case LINE_NOT_FOUND:
			// this.hal.printOnDisplay("Result is LINE_NOT_FOUND at " +
			// this.searchStage, 2, 0);
			this.searchStage++;
			break;
		}
	}
}

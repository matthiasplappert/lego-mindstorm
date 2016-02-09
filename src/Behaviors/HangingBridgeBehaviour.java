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

		
		linesearch = new LineSearchBehavior(sharedState, hal);
		linesearch.action();
		
		Sound.beep();
		Delay.msDelay(5000);
		
		// Follow line
		// After loosing the line

		this.hal.setSpeed(Speed.Fast);
		
		float distance = this.hal.getMeanDistance();
		while (distance > 15.f) {
			// Linesearch
			Delay.msDelay(HangingBridgeBehaviour.DELAY);
			LineType line_state = this.hal.getLineType();

			switch (line_state) {
			case LINE:
				// clear some variables
				//this.hal.printOnDisplay("Search found LINE", 1, 0);
				this.hal.forward();
				Delay.msDelay(20);
				break;
			case BLACK:
				//this.hal.printOnDisplay("Search found BLACK at " + searchStage, 1, 0);
				switch (searchStage) {
				case 0:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 45, Direction.RIGHT);
					this.findLineBehav.action();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 2:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 100,  Direction.RIGHT);
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
		
		Sound.buzz();
		Delay.msDelay(5000);

		this.hal.printOnDisplay("In HangingBridgeBehaviour", 3, 0);
		
		this.hal.resetGyro();
		this.hal.setSpeed(Speed.HangingBridge);
		this.hal.forward();
		float diff = 0;
		//int steps = 0;
		int i = 0;
		float[] last_diff = new float[5];
		for (int k = 0; k < last_diff.length; k++) {
			last_diff[k] = 200;
		}
		boolean enough = false;
		float minimum_distance = 5;
		this.hal.resetLeftTachoCount();
		while (!enough && this.hal.getCurrentDistance() < 15.f && !suppressed) {
			this.hal.forward();
			Delay.msDelay(100);
			difference = this.hal.getMeanDistance() - distance;
			last_diff[i] = difference;
			diff = 0;
			for (int k = 0; k < last_diff.length; k++) {
				diff += last_diff[k];
			}
			if (this.hal.getLeftTachoDistance() >= minimum_distance && Math.abs(diff / last_diff.length) <= 0.2f) {
				enough = true;
				Sound.beep();
				Sound.beep();
				Sound.beep();
				Sound.beep();
				
			} else if (Math.abs(difference) > 0.2f) {
				this.hal.turn((int) Math.signum(difference));
			}
			// Maybe even higher
			Delay.msDelay(50);
			//this.hal.stop();
			i = (i + 1)%last_diff.length;
		}
		this.hal.stop();
		Sound.beep();
		Delay.msDelay(5000);
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.SAFE);
		this.hal.setSpeed(Speed.VeryFast);
		Delay.msDelay(100);
		this.hal.setCourseFollowingAngle((int) this.hal.getMeanGyro());
		while (!this.suppressed && this.hal.getLineType() != LineType.LINE)

		{
			this.hal.performCourseFollowingStep();
			Delay.msDelay(10);
		}
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		this.hal.stop();
		this.hal.backward();
		this.hal.resetLeftTachoCount();
		while (!suppressed && this.hal.getLeftTachoDistance() > -5) {
			Delay.msDelay(10);
		}
		this.sharedState.reset(true);
		Thread.yield();

	}

	@Override
	MyState getTargetState() {
		return MyState.HangingBridgeState;
	}

	@Override
	public void suppress() {
		if(linesearch != null){
			this.linesearch.suppress();
		}
		if(findLineBehav != null){
			this.findLineBehav.suppress();
		}
		suppressed = true;
	}


	private void reactToFindLine(FindLineReturnState state) {
		switch (state) {
		case LINE_FOUND:
			//this.hal.printOnDisplay("Result is LINE_FOUND at " + this.searchStage, 2, 0);
			this.hal.forward();
			this.searchStage = 0; // reset search stage
			break;
		case LINE_NOT_FOUND:
			//this.hal.printOnDisplay("Result is LINE_NOT_FOUND at " + this.searchStage, 2, 0);
			this.searchStage++;
			break;
		}
	}
}

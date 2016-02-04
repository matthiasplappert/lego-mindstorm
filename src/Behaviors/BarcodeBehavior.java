package Behaviors;

import HAL.ColorMode;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class BarcodeBehavior extends StateBehavior {
	private static final int STEP_DELAY_MS = 10;
	
	private static final float MAX_DISTANCE_CM = 5.0f;
	
	private boolean suppressed = false;
	
	public BarcodeBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	@Override
	public void action() {
		LCD.clear();
		LCD.drawString("BarcodeBehavior", 0, 0);
		
		// Configure state.
		this.hal.setSpeed(Speed.Fast);
		this.hal.resetGyro();
		this.hal.setColorMode(ColorMode.RED);
		this.hal.resetLeftTachoCount();
		int numberOfChangesFromLineToNoneLine = 0;
		float distanceWithoutChange = 0.0f;
		boolean hasFoundLineOnce = false;
		
		// Move forward and ensure that we are actually moving forward.
		this.hal.setCourseFollowingAngle(0);
		boolean wasOnLine = this.isOnLine();
		while (!this.suppressed && (distanceWithoutChange < MAX_DISTANCE_CM || !hasFoundLineOnce)) {
			boolean isOnLine = this.isOnLine();
			if (isOnLine) {
				hasFoundLineOnce = true;
			}
			
			// Keep track of number of steps without change.
			if (isOnLine != wasOnLine) {
				distanceWithoutChange = 0.0f;
			} else {
				LCD.clear(5);
				LCD.drawString(Float.toString(this.hal.getLeftTachoCount()), 0, 5);
				distanceWithoutChange += this.hal.convertTachoCountToDistance(this.hal.getLeftTachoCount());
				LCD.clear(6);
				LCD.drawString(Float.toString(distanceWithoutChange), 0, 6);
				this.hal.resetLeftTachoCount();
			}
			
			// Count changes from line to not on line.
			if (wasOnLine && !isOnLine) {
				numberOfChangesFromLineToNoneLine++;
				Sound.beep();
			}
			
			// Keep going and update remaining state.
			this.hal.performCourseFollowingStep();
			wasOnLine = isOnLine;
			
			// Debugging
			LCD.clear(2);
			LCD.drawString(Integer.toString(numberOfChangesFromLineToNoneLine), 0, 2);
			Delay.msDelay(10);
		}
		
		Sound.buzz();
		Delay.msDelay(5000);
		this.sharedState.reset(true);
	}
	
	private boolean isOnLine() {
		LineType lineType = this.hal.getLineType();
		
		// Debugging
		LCD.clear(1);
		LCD.drawString("Line type: " + lineType, 0, 1);
		
		switch (this.hal.getLineType()) {
		case LINE:
			return true;
		default:
			return false;
		}
	}

	@Override
	public void suppress() {
		this.suppressed = true;
	}

	@Override
	State getTargetState() {
		return State.BarcodeState;
	}
}

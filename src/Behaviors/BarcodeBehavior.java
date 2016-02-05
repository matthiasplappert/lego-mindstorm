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
	
	private static final float MAX_DISTANCE_CM = 10.0f;
	
	private boolean suppressed = false;
	
	public int scannedBarcode = -1;
	
	public BarcodeBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	@Override
	public void action() {
		LCD.clear();
		LCD.drawString("BarcodeBehavior", 0, 0);
		
		// WARNING: ONLY ENABLE FOR DEBUGGING, WILL EXIT THE PROGRAM
		//this.testTachoDistance();
		
		// Configure state.
		this.hal.setSpeed(Speed.Fast);
		this.hal.resetGyro();
		this.hal.setColorMode(ColorMode.RED);
		this.hal.resetLeftTachoCount();
		this.hal.resetRightTachoCount();
		int barcode = 0;
		
		// Move forward and ensure that we are actually moving forward.
		this.hal.setCourseFollowingAngle(0);
		boolean wasOnLine = this.isOnLine();
		while (!this.suppressed && this.hal.getLeftTachoDistance() < MAX_DISTANCE_CM) {
			boolean isOnLine = this.isOnLine();
			
			// Keep track of number of steps without change.
			if (isOnLine != wasOnLine) {
				this.hal.resetLeftTachoCount();
				this.hal.resetRightTachoCount();
			}
			LCD.clear(5);
			LCD.drawString(Float.toString(this.hal.getLeftTachoCount()), 0, 5);
			LCD.clear(6);
			LCD.drawString(Float.toString(this.hal.getLeftTachoDistance()), 0, 6);
			
			// Count changes from line to not on line.
			if (wasOnLine && !isOnLine) {
				barcode++;
				Sound.beep();
			}
			
			// Keep going and update remaining state.
			this.hal.performCourseFollowingStep();
			wasOnLine = isOnLine;
			
			// Debugging
			LCD.clear(2);
			LCD.drawString(Integer.toString(barcode), 0, 2);
			Delay.msDelay(STEP_DELAY_MS);
		}
		this.hal.stop();
		
		// Handle barcode.
		if (barcode == 0) {
			Sound.buzz();
			
			// Did not find barcode, back up to initial pose.
			this.hal.resetGyro();
			this.hal.setCourseFollowingAngle(0);
			this.hal.resetLeftTachoCount();
			this.hal.resetRightTachoCount();
			while (!this.suppressed && -this.hal.getLeftTachoDistance() < MAX_DISTANCE_CM) {
				LCD.drawString(Float.toString(this.hal.getLeftTachoDistance()), 0, 6);
				this.hal.performCourseFollowingStep(true);
				Delay.msDelay(STEP_DELAY_MS);
			}
			this.hal.stop();
		} else {
			Sound.beepSequenceUp();
		}
		this.scannedBarcode = barcode;
		
		// In case this is used directly.
		if (this.sharedState != null) {
			this.sharedState.reset(true);
		}
	}
	
	// Helper method for debugging (disabled in production).
	private void testTachoDistance() {
		this.hal.setSpeed(Speed.Fast);
		this.hal.resetGyro();
		this.hal.resetLeftTachoCount();
		this.hal.resetRightTachoCount();
		this.hal.setCourseFollowingAngle(0);
		while (!this.suppressed && this.hal.getLeftTachoDistance() < 100.0f) {
			this.hal.performCourseFollowingStep();
			LCD.clear(5);
			LCD.drawString(Float.toString(this.hal.getLeftTachoCount()), 0, 5);
			LCD.clear(6);
			LCD.drawString(Float.toString(this.hal.getLeftTachoDistance()), 0, 6);
		}
		this.hal.stop();
		Sound.buzz();
		LCD.clear(5);
		LCD.drawString(Float.toString(this.hal.getLeftTachoCount()), 0, 5);
		LCD.clear(6);
		LCD.drawString(Float.toString(this.hal.getLeftTachoDistance()), 0, 6);
		Delay.msDelay(10000);
		System.exit(0);
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

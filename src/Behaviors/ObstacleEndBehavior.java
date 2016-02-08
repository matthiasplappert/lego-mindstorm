package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class ObstacleEndBehavior extends StateBehavior {
	private boolean suppressed = false;

	private BarcodeBehavior barcodeBehavior;
	
	private static final int STEP_DELAY_MS = 10;
	
	private static final float BACKUP_DISTANCE = 3.0f;
	
	private static final float FORWARD_DISTANCE = 5.0f;
	
	private static final float DISTANCE_THRESHOLD = 10.0f;
	
	private static final int TURN_ANGLE = 45;
	
	private static final Speed TURN_SPEED = Speed.VerySlow;
	
	public ObstacleEndBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}
	
	@Override
	public void action() {
		this.suppressed = false;
		
		// This method starts with the following assumption: The previous running
		// code drove the robot until it can barely see the line and then immediately stops.
		// So what we do now is back up a bit, then perform a barcode scan and hopefully see two lines.
		this.hal.stop();
		this.hal.resetGyro();
		this.hal.resetLeftTachoCount();
		this.hal.resetRightTachoCount();
		this.hal.setCourseFollowingAngle(0);
		this.hal.setSpeed(Speed.Fast);
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		while (!this.suppressed && -this.hal.getLeftTachoDistance() < BACKUP_DISTANCE) {
			this.hal.performCourseFollowingStep(true);
			Delay.msDelay(STEP_DELAY_MS);
		}
		
		// Okay, now perform the barcode scan.
		this.barcodeBehavior = new BarcodeBehavior(this.sharedState, this.hal);
		this.barcodeBehavior.action();
		this.sharedState.setState(this.getTargetState());
		if (this.barcodeBehavior.scannedBarcode != 2) {
			// TODO: what do we do now?!
			Button.LEDPattern(1);
			for (int i = 0; i < 5; i++) {
				Sound.beepSequence();
				Delay.msDelay(100);
				return;
			}
		}
		LCD.clear();
		this.hal.printOnDisplay("ObstacleEndBehavior", 0, 0);
		
		// Move forward a bit and then start search sequence.
		this.hal.resetGyro();
		this.hal.resetLeftTachoCount();
		this.hal.resetRightTachoCount();
		while (!this.suppressed && this.hal.getLeftTachoDistance() < FORWARD_DISTANCE) {
			this.hal.performCourseFollowingStep();
			Delay.msDelay(STEP_DELAY_MS);
		}
		
		boolean hasFoundLine = false;
		
		// Turn to the right.
		this.hal.printOnDisplay("TURN: right", 1, 1000);
		this.hal.setSpeed(TURN_SPEED);
		this.hal.turn(TURN_ANGLE);
		while (!this.suppressed && this.hal.isRotating() && !this.hal.isTouchButtonPressed() &&
			   this.hal.getMeanDistance() > DISTANCE_THRESHOLD && !hasFoundLine) {
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			Delay.msDelay(STEP_DELAY_MS);
		}
		if (hasFoundLine) {
			this.didFindLine();
			return;
		}
		this.hal.stop();
		
		// Turn to the left.
		this.hal.printOnDisplay("TURN: left", 1, 1000);
		this.hal.rotateTo(0, true);
		while (!this.suppressed && this.hal.isRotating()) {
			this.hal.printOnDisplay(Float.toString(this.hal.getCurrentGyro()), 5, 0);
			Delay.msDelay(STEP_DELAY_MS);
		}
		this.hal.stop();
		this.hal.turn(-TURN_ANGLE);
		while (!this.suppressed && this.hal.isRotating() && !this.hal.isTouchButtonPressed() &&
			   !hasFoundLine) {
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			Delay.msDelay(STEP_DELAY_MS);
		}
		if (hasFoundLine) {
			this.didFindLine();
			return;
		}
		
		// Just keep going, maybe we'll find it eventually...
		Sound.buzz();
		this.didFindLine();
	}
	
	private void didFindLine() {
		this.hal.printOnDisplay("did find line", 2, 0);
		this.hal.stop();
		Sound.beepSequenceUp();
		Delay.msDelay(5000);
		this.sharedState.setState(State.LineSearchState);
	}

	@Override
	public void suppress() {
		this.barcodeBehavior.suppress();
		this.suppressed = true;
		
	}

	@Override
	State getTargetState() {
		return State.ObstacleEndState;
	}
}

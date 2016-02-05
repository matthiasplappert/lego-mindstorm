package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class ObstacleEndBehavior extends StateBehavior {
	private boolean suppressed = false;

	private BarcodeBehavior barcodeBehavior;
	
	private static final int STEP_DELAY_MS = 10;
	
	private static final float BACKUP_DISTANCE = 3.0f;
	
	private static final float FORWARD_DISTANCE = 5.0f;
	
	private static final float DISTANCE_THRESHOLD = 10.0f;
	
	private static final int TURN_ANGLE = 45;
	
	public ObstacleEndBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}
	
	@Override
	public void action() {
		// This method starts with the following assumption: The previous running
		// code drove the robot until it can barely see the line and then immediately stops.
		// So what we do now is back up a bit, then perform a barcode scan and hopefully see two lines.
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
		this.barcodeBehavior = new BarcodeBehavior(null, this.hal);
		this.barcodeBehavior.action();
		if (this.barcodeBehavior.scannedBarcode != 2) {
			// TODO: what do we do now?!
			Button.LEDPattern(1);
			for (int i = 0; i < 5; i++) {
				Sound.beepSequence();
				return;
			}
		}
		
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
		this.hal.turn(TURN_ANGLE);
		while (!this.suppressed && this.hal.isRotating()) {
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			if (this.hal.isTouchButtonPressed() || hasFoundLine || this.hal.getMeanDistance() > DISTANCE_THRESHOLD) {
				break;
			}
			Delay.msDelay(STEP_DELAY_MS);
		}
		if (hasFoundLine) {
			this.didFindLine();
			return;
		}
		
		// Turn to the left.
		this.hal.turn(-2 * TURN_ANGLE);
		while (!this.suppressed && this.hal.isRotating()) {
			hasFoundLine = this.hal.getLineType().equals(LineType.LINE);
			if (this.hal.isTouchButtonPressed() || hasFoundLine) {
				break;
			}
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
		Sound.beepSequenceUp();
		this.hal.stop();
		this.sharedState.setState(State.LineSearch);
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

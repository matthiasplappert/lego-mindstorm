package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.MyState;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class ObstacleEndBehavior extends StateBehavior {
	private boolean suppressed = false;

	private BarcodeBehavior barcodeBehavior;
	
	private static final int STEP_DELAY_MS = 10;
	
	private static final float BACKUP_DISTANCE = 3.0f;
	
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
			// Wrong barcode, but keep going anyway
			Sound.buzz();
		}
		LCD.clear();
		
		// Next step: search the line
		this.sharedState.setState(MyState.LineSearchState);
	}
	
	@Override
	public void suppress() {
		if (this.barcodeBehavior != null) {
			this.barcodeBehavior.suppress();
		}
		this.suppressed = true;
	}

	@Override
	MyState getTargetState() {
		return MyState.ObstacleEndState;
	}
}

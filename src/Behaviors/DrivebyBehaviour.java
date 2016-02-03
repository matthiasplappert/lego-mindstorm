package Behaviors;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class DrivebyBehaviour extends StateBehavior {

	private boolean suppressed;
	private DrivebyReturnType returnType;
	private final int min_dist;
	private final int dist_for_whole;
	private final int correctionAngle;
	private final int allowed_dist_offset;
	public DrivebyReturnType getReturnType() {
		return returnType;
	}

	private static final Direction SensorDirection = Direction.LEFT;

	public DrivebyBehaviour(SharedState sharedState, IHAL hal) {
		this(sharedState,hal,15, 5, 50,5 );
	}
	public DrivebyBehaviour(SharedState sharedState, IHAL hal, int min_dist, int min_dist_offset, int dist_for_whole, int correctionAngle) {
		super(sharedState, hal);
		this.suppressed = false;
		this.min_dist = min_dist;
		this.dist_for_whole = dist_for_whole;
		this.correctionAngle = correctionAngle;
		this.allowed_dist_offset = min_dist_offset;
	}

	@Override
	public void action() {
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP, false);
		Delay.msDelay(250);
		if(!this.checkDrivingConditions(this.hal.getMeanDistance())){
			this.returnType = DrivebyReturnType.InitialStateFailed;
			Sound.beepSequence();
			LCD.drawString("Driving Conditions are not met", 0, 0);

		}
		float dist = this.hal.getMeanDistance();

			while(!this.suppressed && checkDrivingConditions(dist)){
				Direction turnDirection;
				if(dist > this.min_dist && dist < this.min_dist + this.allowed_dist_offset ) {
					//Drive Forward
					hal.forward(Speed.Slow);

				}
				else{
					
						turnDirection = dist > this.min_dist + this.allowed_dist_offset?
								DrivebyBehaviour.SensorDirection :
									DrivebyBehaviour.SensorDirection.getOppositeDirection();
						final int angle = Utils.considerDirectionForRotation(this.correctionAngle,
								turnDirection);
						this.hal.turn(angle, false, true);
				}
				Delay.msDelay(100);
				dist = this.hal.getMeanDistance();

			}
			if(this.hal.isTouchButtonPressed()){
				this.returnType = DrivebyReturnType.WALLINFRONT;
				LCD.drawString("Wall at Front", 0, 0);
			}
			else{
				LCD.drawString("No wall at the sensor side", 0, 0);
				this.returnType = DrivebyReturnType.NOWALL;
			}
			return;
		

	}

	/***
	 * This method returns true until:
	 * 1) The front button is pressed
	 * 2) There is a whole in the wall
	 * @return true when driving is safe (and no condition to consider)
	 */
	private boolean checkDrivingConditions(float dist){
		return dist < Float.POSITIVE_INFINITY &&  dist > Float.NEGATIVE_INFINITY 
				&& dist < dist_for_whole
				&& !this.hal.isTouchButtonPressed();
		
	}
	@Override
	public void suppress() {
		this.suppressed = true;

	}

	@Override
	State getTargetState() {
		return State.DriveByState;
	}

}

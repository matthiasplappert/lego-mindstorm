package Behaviors;

import java.util.Random;

import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import lejos.utility.Delay;
import State.MyState;

public class BossBehaviour extends StateBehavior {

	private final int evasion_angle = 80;
	private final int no_change_angle = 15;
	
	public BossBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed = false;

	@Override
	public void action() {
		this.suppressed = false;
		
		this.hal.resetGyro();

		this.hal.printOnDisplay("BossBehaviour started", 0, 0);
		Random r = new Random();
		int course_last_part = (r.nextInt(11) + 15);// [0,10[ + 15 = [15,25]

		// we are around the last corner
		this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.Labyrinth);

		this.hal.setCourseFollowingAngle(course_last_part);// move slightly to
															// the right
		this.hal.setSpeed(Speed.VeryFast);

		int count = 0;
		
		while (!this.suppressed) {
			this.hal.performCourseFollowingStep();
			Delay.msDelay(5);
			
			if(count > 500){
				this.hal.backward();
				Delay.msDelay(200);
				
				if(r.nextBoolean()){ //random left or right
					this.hal.rotate(no_change_angle);
				}else{
					this.hal.rotate(-no_change_angle);
				}
				while(!suppressed && this.hal.isRotating()){
					Delay.msDelay(5);
				}
				count = 0;
			}
			
			//when we hit boss or wall --> rotate to left or right randomly
			//then continue with direction
			if(this.hal.isTouchButtonPressed()){
				count = 0;
				if(this.hal.getCurrentGyro() + 90 > 120){
					//turn left if we are about to go backwards
					this.hal.rotate(-evasion_angle);
				}else if(this.hal.getCurrentGyro() - 90 < -120){
					//turn right if we are about to go backwards
					this.hal.rotate(evasion_angle);
				}else if(r.nextBoolean()){ //random left or right
					this.hal.rotate(evasion_angle);
				}else{
					this.hal.rotate(-evasion_angle);
				}
				while(!suppressed && this.hal.isRotating()){
					Delay.msDelay(5);
				}								
			}
			
			//if the wall is to close --> turn left
			if(this.hal.getCurrentDistance() < 5.0){
				count = 0;
				this.hal.rotate(-10);
				while(!suppressed && this.hal.isRotating()){
					Delay.msDelay(5);
				}
			}
			count++;
		}

		this.hal.stop();
		// this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);
		this.sharedState.reset(true);
	}

	@Override
	MyState getTargetState() {
		return MyState.BossState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}
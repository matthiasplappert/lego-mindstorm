package Behaviors;

import java.io.IOException;

import HAL.ColorMode;
import HAL.DistanceSensorPosition;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import communication.ComModule;
import communication.Communication;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import State.MyState;

public class ElevatorBehaviour extends StateBehavior {

	private static final int ELEVATOR_MOVING_DURATION = 10;
	private static final Speed forwardSpeed = Speed.Fast;
	private ComModule comm;

	public ElevatorBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		this.comm = Communication.getModule();
	}

	private boolean suppressed = false;
	private boolean finished = false;

	@Override
	public void action() {
		this.suppressed = false;
		try {
			LCD.clear();
			this.hal.printOnDisplay("ElevatorBehaviour started", 0, 0);
			
			
			while (!this.suppressed && !this.finished) {//HERE IS outer loop!
				//wait for status=true
				boolean status = false;
				this.hal.moveDistanceSensorToPosition(DistanceSensorPosition.UP);//Here we do not need Distance Sensor
				
				do{
					status = this.comm.requestStatus();
					LCD.drawString("status is false       " , 1, 0);
					Delay.msDelay(100);

				}
				while (status == false && !this.suppressed ); 

				//reserve elevator
				this.hal.setColorMode(ColorMode.AMBIENT_LIGHT);
				LCD.drawString("Request Elevator       " , 1, 0);

				if(this.comm.requestElevator()){
					//wait for safe signal
					this.wait_for_ambient_light_on();
					//enter elevator
					LCD.drawString("Ambient Light off      " , 1, 0);
					this.move_forward_till_button();
					this.hal.setColorMode(ColorMode.RED);
					//request elevator to move
					LCD.drawString("Request Elevator       " , 1, 0);
					LCD.drawString("to go down       " , 2, 0);

					if(this.comm.moveElevatorDown()){
						Delay.msDelay(ELEVATOR_MOVING_DURATION*1000);
						LCD.drawString("Try to leave Elevator      " , 1, 0);
						LCD.drawString("                           " ,2, 0);
						
						this.move_until_line();
						finished = true;
						this.sharedState.reset(true);
//						new ObstacleEndBehavior(this.sharedState, this.hal).action();
					}
					else{
						//TODO: Move back to safe position and go to outer loop
						throw new IllegalStateException("we hang in elevator!");
//						break;//goto outer loop
					}
				}
				else{
					break;//go to outer loop
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

//		Thread.yield();

	}

	private void move_until_line() {
		this.hal.setSpeed(forwardSpeed);
		while(this.hal.getLineType() != LineType.LINE){
			this.hal.forward();
			LCD.drawString("No Line Found", 1, 0);
			Delay.msDelay(10);
		}
		this.hal.stop();
		
	}

	private void move_forward_till_button() {
		this.hal.resetGyro();
		this.hal.setSpeed(forwardSpeed);
		this.hal.setCourseFollowingAngle(-5);
		while(this.hal.getCurrentDistance() > 10){
			this.hal.performCourseFollowingStep();
			Delay.msDelay(10);
		}
		this.hal.rotate(10);

		
		while (!this.hal.isTouchButtonPressed() && !suppressed) {
			
			
			
			
			this.hal.forward();
			Delay.msDelay(10);

		}
		go_back();

	}

	private void go_back() {
		this.hal.stop();
		this.hal.setSpeed(Speed.Medium);
		this.hal.resetLeftTachoCount();
		
		this.hal.backward();
		while(this.hal.getLeftTachoDistance() > -2){
			Delay.msDelay(10);		
		}
		this.hal.stop();	
		this.hal.setSpeed(forwardSpeed);
	}

	private void wait_for_ambient_light_on() {
		while(!this.hal.isAmbientLightOn()){
			LCD.drawString("Ambient Light is off       " , 1, 0);
			Delay.msDelay(10);
		}
		
	}

	@Override
	MyState getTargetState() {
		return MyState.ElevatorState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}

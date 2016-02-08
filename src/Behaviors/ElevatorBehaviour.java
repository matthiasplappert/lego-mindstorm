package Behaviors;

import java.io.IOException;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import communication.ComModule;
import communication.Communication;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import State.MyState;

public class ElevatorBehaviour extends StateBehavior {

	private static final int ELEVATOR_MOVING_DURATION = 40;
	private static final Speed forwardSpeed = Speed.Medium;
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
			this.hal.printOnDisplay("ElevatorBehaviour started", 0, 0);
			while (!this.suppressed && !this.finished) {//HERE IS outer loop!s
				//wait for status=true
				boolean status = false;
				do{
					status = this.comm.requestStatus();
					LCD.drawString("status is false       " , 1, 0);
					Delay.msDelay(100);

				}
				while (status == false && !this.suppressed ); 
				//reserve elevator
				
				LCD.drawString("Request Elevator       " , 1, 0);

				if(this.comm.requestElevator()){
					//wait for safe signal
					this.wait_for_ambient_light_off();
					//enter elevator
					LCD.drawString("Ambient Light off      " , 1, 0);
					this.move_forward_till_button();
					//request elevator to move
					LCD.drawString("Request Elevator       " , 1, 0);
					LCD.drawString("to go down       " , 2, 0);

					if(this.comm.moveElevatorDown()){
						Delay.msDelay(ELEVATOR_MOVING_DURATION*1000);
						LCD.drawString("Try to leave Elevator      " , 1, 0);
						LCD.drawString("                           " ,2, 0);
						
						this.move_until_line();
						new ObstacleEndBehavior(this.sharedState, this.hal).action();
					}
					else{
						break;//goto outer loop
					}
				}
				else{
					break;//go to outer loop
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finished = true;

		this.sharedState.reset(true);
		Thread.yield();

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
		this.hal.setSpeed(forwardSpeed);
		while(this.hal.isTouchButtonPressed()){
			this.hal.forward();
			Delay.msDelay(10);
		}
		this.hal.stop();		
	}

	private void wait_for_ambient_light_off() {
		while(this.hal.isAmbientLightOn()){
			LCD.drawString("Ambient Light is on       " , 1, 0);
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

package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

public class HangingBridgeBehaviour extends StateBehavior {	
	
	public HangingBridgeBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean surpressed =  false;
	private boolean finished = false;
	
	@Override
	public void action() {
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 1000);
		while(!this.surpressed && !this.finished){
			LCD.clear();
			String s;
			/*
			this.hal.resetGyro();	
			s = String.valueOf(this.hal.getGyroValue());
			this.hal.printOnDisplay(s, 3, 10);
			
			this.hal.rotate(180, false);
			s = String.valueOf(this.hal.getGyroValue());
			this.hal.printOnDisplay(s, 4, 10);
			this.hal.rotate(-180, false);
			s = String.valueOf(this.hal.getGyroValue());
			this.hal.printOnDisplay(s, 5, 10);
			
			
			
			Sound.beep();
			this.hal.rotate(180, true);
			while(this.hal.isRotating() && !this.surpressed){
				s = String.valueOf(this.hal.getGyroValue());
				this.hal.printOnDisplay(s, 6, 10);
			}
			s = String.valueOf(this.hal.getGyroValue());
			this.hal.printOnDisplay(s, 6, 10);
			Sound.beep();
			
			this.hal.stop();
			
			this.hal.rotate(-180, true);
			while(this.hal.isRotating() && !this.surpressed){
				s = String.valueOf(this.hal.getGyroValue());
				this.hal.printOnDisplay(s, 7, 10);
			}
			s = String.valueOf(this.hal.getGyroValue());
			this.hal.printOnDisplay(s, 7, 10);
			this.hal.stop();
			s = String.valueOf(this.hal.getGyroValue());										
			this.hal.printOnDisplay(s, 8, 500);
			
			Sound.beep();
			Delay.msDelay(2000);
			Sound.beep();
			
			finished = true;*/
			
			//hal.moveDistanceSensorToPosition(1);
			//hal.moveDistanceSensorToPosition(0);
			
			hal.forward();
			Delay.msDelay(500);
			hal.turn(45, true, false);
			Delay.msDelay(100);			
			hal.turn(-45, true, false);
			Delay.msDelay(500);
						
			hal.stop();
			Delay.msDelay(2000);
			Sound.beep();			
			
			hal.forward();
			Delay.msDelay(100);
			hal.turn(-45, false, false);
			Delay.msDelay(100);			
			hal.turn(45, false, false);
			Delay.msDelay(500);
			
			hal.stop();
			Delay.msDelay(2000);
			Sound.beep();
			

			hal.forward(Speed.VerySlow);
			Delay.msDelay(2000);
			Sound.beep();
			hal.forward(Speed.Slow);
			Delay.msDelay(2000);
			Sound.beep();
			hal.forward(Speed.Medium);
			Delay.msDelay(2000);
			Sound.beep();
			hal.forward(Speed.Fast);
			Delay.msDelay(2000);
			Sound.beep();
			hal.forward(Speed.VeryFast);
			Delay.msDelay(2000);
			Sound.beep();
			hal.stop();
			Delay.msDelay(500);
			
			/*Sound.beep();
			hal.forward();
			Delay.msDelay(100);
			
			this.hal.turn(20, false, true);
			while(this.hal.isRotating() && !this.surpressed){
				Delay.msDelay(10);
			}
			hal.stop();
			
			this.hal.turn(-20, false, true);
			while(this.hal.isRotating() && !this.surpressed){
				Delay.msDelay(10);
			}*/
			hal.stop();
			Sound.beep();
			Delay.msDelay(2000);
			Sound.buzz();
		}
		
		this.sharedState.reset(true);
		Thread.yield();
	}

	@Override
	State getTargetState() {
		return State.HangingBridgeState;
	}

	@Override
	public void suppress() {
		surpressed = true;
	}
}

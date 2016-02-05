package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;
import lejos.utility.TextMenu;
import main.Main;

public class ShutdownBehavior extends StateBehavior {
	public ShutdownBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	@Override
	public boolean takeControl() {
		return Button.ESCAPE.isDown();
	}

	@Override
	public void action() {
		boolean exit = false;
		LCD.clear();
		
		LCD.drawString("Press UP for Menu", 0, 0);
		LCD.drawString("Press DOWN for EXIT", 0, 1);
		this.hal.stop();
		
		while(!exit){
			Delay.msDelay(50);			
			
			if(Button.UP.isDown()){
				Sound.beep();
				
				//this lets the arbitrator exit and the main while-loop continues
				sharedState.setState(State.ExitState);
				
				exit = true;
			}else if(Button.DOWN.isDown()){
				System.exit(0);				
			}			
		}			
	}

	@Override
	public void suppress() {
		// Nothing to do here.
	}

	@Override
	State getTargetState() {
		// TODO Auto-generated method stub
		return State.ShutDownState;
	}
}

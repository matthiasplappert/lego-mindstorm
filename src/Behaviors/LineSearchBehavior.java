package Behaviors;

import HAL.ColorMode;
import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

//TODO:
//Change to three level detection: white, border, line. 
//Behaviour: increasing rotation angle as closer the measurements comes to the border

public class LineSearchBehavior extends StateBehavior {

	// TODO: update this as soon as we have proper handling in the HAL
	public static final int DEFAULT_EXPLORATION_ANGLE_DIFF = 5;
	public static final int CORRECTION_ANGLE = 2;

	public static final int UPPER_TRESHOLD_ANGLE = 160;
	public static final int LOOP_DELAY = 10;
	public static final long TIMEDIFF_LAST_LINE_FINDING = 2 * 1000 * 1000 * 1000;

	private boolean suppressed;
	private FindLineBehaviour findLineBehav;
	private Direction lastDirection = Direction.LEFT;

	// 0: small rotations, 1: check for barcode, 2: large rotations
	private int searchStage = 0;
	private BarcodeBehavior barcodeBehav;

	public LineSearchBehavior(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
		this.suppressed = false;
	}

	@Override
	public void action() {
		this.suppressed = false;
		
		if (!hal.isRedColorMode())
			this.hal.setColorMode(ColorMode.RED);
		LCD.clear();
		this.hal.printOnDisplay("LineSearchBehavior called", 0, 5000);

		Sound.beepSequence();
		this.hal.setSpeed(Speed.Fast);
		this.hal.resetGyro();
		this.searchStage = 0;
		
		/*while(!suppressed){
			this.hal.resetGyro();
			this.hal.rotateTo(90);
			
			while (this.hal.isRotating() && !this.suppressed) {
				Delay.msDelay(10);
				this.hal.printOnDisplay("Gyro: " + this.hal.getMeanGyro(), 2, 10);
			}
			
			this.hal.stop();
			Sound.beep();	
			this.hal.printOnDisplay("Gyro: " + this.hal.getMeanGyro(), 2, 10);
			
			Delay.msDelay(1000);
		}*/
		

		boolean done = false;
		while (!this.suppressed && !done) {
			// Do not sample too often.
			Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
			LineType line_state = this.hal.getLineType();
			
			switch (line_state) {
			case LINE:
				// clear some variables
				this.hal.printOnDisplay("Search found LINE", 1, 0);
				this.hal.forward();
				Delay.msDelay(100);
				break;
			case BORDER:
				/*Sound.beep();
				this.hal.printOnDisplay("Search found BORDER", 1, 0);
				this.hal.forward();
				overdrive_angle = Utils.considerDirectionForRotation(CORRECTION_ANGLE, this.lastDirection);
				this.hal.turn(overdrive_angle);

				while (this.hal.isRotating() && !this.suppressed) {
					Delay.msDelay(10);
				}
				break;*/
			case BLACK:
				this.hal.printOnDisplay("Search found BLACK at " + this.searchStage, 1, 0);
				switch (this.searchStage) {
				case 0:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 30,  this.lastDirection);
					this.findLineBehav.action();
					this.lastDirection = this.findLineBehav.getLastUsedDirection();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 1:
					/*this.barcodeBehav = new BarcodeBehavior(sharedState, hal);
					this.barcodeBehav.action();
					
					if (this.barcodeBehav.scannedBarcode > 0) {
						// We have a valid barcode, switch behavior and stop line search.
						State newState = State.getFromBarcode(this.barcodeBehav.scannedBarcode);
						this.sharedState.setState(newState);
						this.searchStage = 0;
						done = true;
					} else {
						// Keep looking for line
						this.searchStage++;
					}
					break;*/
				case 2:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 100, this.lastDirection);
					this.findLineBehav.action();
					this.lastDirection = this.findLineBehav.getLastUsedDirection();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 3: 
					//Error nothing ever found
					Sound.buzz(); 
					this.searchStage = 0; //TODO remove this here
				}			
				break;
			default:
				break;
			}
		}
	}

	private void reactToFindLine(FindLineReturnState state) {				
		switch(state){
		case LINE_FOUND:
			this.hal.printOnDisplay("Result is LINE_FOUND at " + this.searchStage, 2, 0);
			this.hal.forward();
			this.searchStage = 0; //reset search stage
			break;
		case LINE_NOT_FOUND:	
			this.hal.printOnDisplay("Result is LINE_NOT_FOUND at " + this.searchStage, 2, 0);
			this.searchStage++;
			break;
		}
	}

	@Override
	State getTargetState() {
		return State.LineSearchState;
	}

	@Override
	public void suppress() {
		if (this.findLineBehav != null)
			this.findLineBehav.suppress();
		if (this.barcodeBehav != null)
			this.barcodeBehav.suppress();
		this.suppressed = true;
	}
}

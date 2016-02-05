package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import State.State;
import lejos.hardware.Sound;
import lejos.utility.Delay;

public class RockerBehaviour extends StateBehavior {	
	
	public RockerBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}
	
	public static final int DEFAULT_EXPLORATION_ANGLE_DIFF = 5;
	public static final int LOOP_DELAY = 10;

	private boolean suppressed =  false;
	private boolean finished = false;
	
	private FindLineBehaviour findLineBehav;
	private Direction lastDirection = Direction.LEFT;

	// 0: small rotation, 1: line not found --> barcode
	private int searchStage = 0;
	
	@Override
	public void action() {
		this.hal.printOnDisplay("HangingBridgeBehaviour started", 0, 1000);
		while(!this.suppressed && !this.finished){
			
			this.hal.setSpeed(Speed.Rocker);
			this.hal.resetGyro();
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
			case BLACK:
				this.hal.printOnDisplay("Search found BLACK at " + searchStage, 1, 0);
				switch (searchStage) {
				case 0:
					this.findLineBehav = new FindLineBehaviour(sharedState, hal, 12,  this.lastDirection.getOppositeDirection(), false);
					this.findLineBehav.action();
					this.lastDirection = this.findLineBehav.getLastUsedDirection();
					reactToFindLine(findLineBehav.returnState());
					break;
				case 2:
					//TODO check for barcode
					Sound.buzz();
					
					searchStage = 0; //TODO remove since only debug
					this.hal.stop();
					break;
				}			
				break;
			default:
				break;
			}
			
						
		}
		
		this.sharedState.reset(true);
		Thread.yield();
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
		return State.RockerState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}

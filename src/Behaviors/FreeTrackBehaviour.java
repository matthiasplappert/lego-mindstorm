package Behaviors;

import HAL.IHAL;
import HAL.Speed;
import State.SharedState;
import lejos.utility.Delay;
import State.MyState;

public class FreeTrackBehaviour extends StateBehavior {

	public FreeTrackBehaviour(SharedState sharedState, IHAL hal) {
		super(sharedState, hal);
	}

	private boolean suppressed = false;

	@Override
	public void action() {
		this.suppressed = false;		
		this.hal.printOnDisplay("FreeTrackBehaviour started", 0, 0);
		
		this.hal.resetGyro();
		this.hal.setSpeed(Speed.VeryFast);
		this.hal.setCourseFollowingAngle(5);
		int count = 0;
		while (!this.suppressed && !this.hal.isTouchButtonPressed()
				&& this.hal.getMeanDistance() > 5){
			this.hal.performCourseFollowingStep();
			Delay.msDelay(5);
			if(count > 200){
				count = 0;
				this.hal.setCourseFollowingAngle((int) this.hal.getMeanGyro() + 5); 
			}
		}
		this.hal.stop();		
		
		this.sharedState.setState(MyState.MazeState);
	}

	@Override
	MyState getTargetState() {
		return MyState.FreeTrackState;
	}

	@Override
	public void suppress() {
		suppressed = true;
	}
}

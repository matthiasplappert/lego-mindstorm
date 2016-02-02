package main;

import java.util.ArrayList;

import Behaviors.DisplayTestStateBehavior;
import Behaviors.LineSearchBehavior;
import HAL.DefaultHAL;
import HAL.HALHelper;
import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

//class StateChangeRecorder{
//	  @Subscribe public void recordCustomerChange(StateChangeEvent e) {
//		    System.out.println("Transition from " + e.getOldState().toString() + " to "+e.getNewState().toString());
//		  }
//}
public class Main {
	public static void main(String[] args) {
//		LCD.drawString("Running ...", 0, 0);
//		Output.printOnDisplay("Running...");
//		HAL hal = new HAL();
		IHAL hal = new DefaultHAL(){
			@Override
			public void printOnDisplay(String text, long waitDuration) {
				System.out.println(text);
				if(waitDuration>0)
					HALHelper.sleep(waitDuration);
			}
		};

		ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
		SharedState sharedState = new SharedState(State.LineSearch);
//		sharedState.register(new StateChangeRecorder());
		
		behaviors.add(new LineSearchBehavior(sharedState, hal));
		behaviors.add(new DisplayTestStateBehavior(sharedState, hal));
		Behavior[] behavs = new Behavior[behaviors.size()];
		for(int i=0;i<behavs.length;i++)
			behavs[i] = behaviors.get(i);
		Arbitrator a = new Arbitrator(behavs);
		a.start();
	}
}

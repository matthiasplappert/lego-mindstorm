package main;

import java.util.ArrayList;

import Behaviors.DisplayTestStateBehavior;
import Behaviors.LineSearchBehavior;
import HAL.DefaultHAL;
import HAL.HAL;
import HAL.HALHelper;
import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;


public class Main {
	public static void main(String[] args) {

		IHAL hal = new HAL();
//		IHAL hal = new DefaultHAL(){
//			@Override
//			public void printOnDisplay(String text, long waitDuration) {
//				System.out.println(text);
//				if(waitDuration>0)
//					HALHelper.sleep(waitDuration);
//			}
//		};

		ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
		SharedState sharedState = new SharedState(State.LineSearch);
		
		behaviors.add(new LineSearchBehavior(sharedState, hal));
		behaviors.add(new DisplayTestStateBehavior(sharedState, hal));
		Behavior[] behavs = new Behavior[behaviors.size()];
		for(int i=0;i<behavs.length;i++)
			behavs[i] = behaviors.get(i);
		Arbitrator a = new Arbitrator(behavs, false);
		a.start();
	}
}

package main;

import java.util.ArrayList;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;

import Behaviors.DisplayTestStateBehavior;
import Behaviors.LineSearchBehavior;
import HAL.DefaultHAL;
import HAL.HALHelper;
import HAL.IHAL;
import State.SharedState;
import State.State;
import State.StateChangeEvent;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

class StateChangeRecorder{
	  @Subscribe public void recordCustomerChange(StateChangeEvent e) {
		    System.out.println("Transition from " + e.getOldState().toString() + " to "+e.getNewState().toString());
		  }
}
public class Main {
	public static void main(String[] args) {
//		LCD.drawString("Running ...", 0, 0);
//		Output.printOnDisplay("Running...");
//		HAL hal = new HAL();
		IHAL hal = new DefaultHAL(){
			@Override
			public void printOnDisplay(String text, Optional<Long> waitDuration) {
				System.out.println(text);
				if(waitDuration.isPresent())
					HALHelper.sleep(waitDuration.get());
			}
		};

		ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
		SharedState sharedState = new SharedState(State.LineSearch);
		sharedState.register(new StateChangeRecorder());
		
		behaviors.add(new LineSearchBehavior(sharedState, hal));
		behaviors.add(new DisplayTestStateBehavior(sharedState, hal));
		Arbitrator a = new Arbitrator(Iterables.toArray(behaviors, Behavior.class));
		a.start();
	}
}

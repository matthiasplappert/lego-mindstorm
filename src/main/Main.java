package main;

import java.util.ArrayList;

import com.google.common.collect.Iterables;
import com.google.common.eventbus.Subscribe;

import Behaviors.DisplayTestStateBehavior;
import Behaviors.LineSearchBehavior;
import State.SharedState;
import State.State;
import State.StateChangeEvent;
import common.Output;
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
		Output.printOnDisplay("Running...");
		ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
		SharedState sharedState = new SharedState(State.LineSearch);
		sharedState.register(new StateChangeRecorder());
		
		behaviors.add(new LineSearchBehavior(sharedState));
		behaviors.add(new DisplayTestStateBehavior(sharedState));
		Arbitrator a = new Arbitrator(Iterables.toArray(behaviors, Behavior.class));
		a.go();
	}
}

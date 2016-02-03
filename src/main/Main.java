package main;

import java.util.ArrayList;

import Behaviors.DisplayTestStateBehavior;
import Behaviors.HangingBridgeBehaviour;
import Behaviors.LineSearchBehavior;
import Behaviors.ShutdownBehavior;
import HAL.DefaultHAL;
import HAL.HAL;
import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.port.SensorPort;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.TextMenu;


public class Main {
	private static TextMenu createMenu(State[] states) {
		// Allow to pick initial state using the GUI. This code ensures that the default
		// state is always at the top of the list, hence the somewhat lengthy code.
		states[0] = State.getInitState();
		int j = 1;
		for (State state: State.values()) {
			if (state.equals(State.getInitState())) {
				continue;
			}
			states[j] = state;
			j++;
		}
		String[] stateStrings = new String[states.length];
		for (int i = 0; i < states.length; i++) {
			stateStrings[i] = states[i] + "";
		}
		TextMenu menu = new TextMenu(stateStrings);
		return menu;
	}
	
	public static void main(String[] args) {
		// Create initial shared state.
		State[] states = new State[State.values().length];
		TextMenu menu = Main.createMenu(states);
		int initialStateIndex = menu.select();
		SharedState sharedState = new SharedState(states[initialStateIndex]);

		IHAL hal = new HAL();
		hal.resetGyro();
//		IHAL hal = new DefaultHAL(){
//			@Override
//			public void printOnDisplay(String text, long waitDuration) {
//				System.out.println(text);
//				if(waitDuration>0)
//					HALHelper.sleep(waitDuration);
//			}
//		};
		
		// Create behaviors.
		ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
		
		// Task-specific behaviors		
		behaviors.add(new LineSearchBehavior(sharedState, hal, SensorPort.S1));
		behaviors.add(new DisplayTestStateBehavior(sharedState, hal));
		behaviors.add(new HangingBridgeBehaviour(sharedState, hal));
		
		
		// WARNING: always keep this as the last element since it allows us to exit from the program. 
		behaviors.add(new ShutdownBehavior());
		
		Behavior[] behavs = new Behavior[behaviors.size()];
		for(int i=0;i<behavs.length;i++)
			behavs[i] = behaviors.get(i);
		Arbitrator a = new Arbitrator(behavs, false);
		a.start();
	}
}

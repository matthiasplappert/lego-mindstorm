package main;

import java.util.ArrayList;
import java.util.List;

import Behaviors.BossBehaviour;
import Behaviors.BridgeBehaviour;
import Behaviors.DrivebyBehaviour;
import Behaviors.ElevatorBehaviour;
import Behaviors.FreeTrackBehaviour;
import Behaviors.HangingBridgeBehaviour;
import Behaviors.LineSearchBehavior;
import Behaviors.MazeBehaviour;
import Behaviors.SensorDataBehaviour;
import Behaviors.RollBoxBehaviour;
import Behaviors.SensorDataBehaviour;
import Behaviors.ShutdownBehavior;
import HAL.HAL;
import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;
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
		LCD.drawString("Frank the Tank is getting ready ...", 0, 0);
		IHAL hal = new HAL();
		LCD.clear();
		
		// Create initial shared state.		
		State[] states = new State[State.values().length];
		TextMenu menu = Main.createMenu(states);
		int initialStateIndex = menu.select();
		if (initialStateIndex < 0) {
			return;
		}
		LCD.clear();
		
		SharedState sharedState = new SharedState(states[initialStateIndex]);	

		ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
		behaviors.add(new SensorDataBehaviour(sharedState, hal));

		behaviors.add(new LineSearchBehavior(sharedState, hal));
		behaviors.add(new HangingBridgeBehaviour(sharedState, hal));
		behaviors.add(new BridgeBehaviour(sharedState, hal));
		behaviors.add(new BossBehaviour(sharedState, hal));
		behaviors.add(new ElevatorBehaviour(sharedState, hal));
		behaviors.add(new FreeTrackBehaviour(sharedState, hal));
		behaviors.add(new MazeBehaviour(sharedState, hal));
		behaviors.add(new SensorDataBehaviour(sharedState, hal));
		behaviors.add(new RollBoxBehaviour(sharedState, hal));
		behaviors.add(new DrivebyBehaviour(sharedState, hal));
		
		// WARNING: always keep this as the last element since it allows us to exit from the program. 
		behaviors.add(new ShutdownBehavior());
		
		Arbitrator a = new Arbitrator(Main.getArrayForList(behaviors), false);
		a.start();
	}
	
	public static Behavior[] getArrayForList(List<Behavior> behaviors){
		Behavior[] behavs = new Behavior[behaviors.size()];
		for(int i=0;i<behavs.length;i++)
			behavs[i] = behaviors.get(i);
		return behavs;
	}
}

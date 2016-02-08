package main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import Behaviors.BarcodeBehavior;
import Behaviors.BossBehaviour;
import Behaviors.BridgeBehaviour;
//import Behaviors.DrivebyBehaviour;
import Behaviors.ElevatorBehaviour;
import Behaviors.FreeTrackBehaviour;
import Behaviors.HangingBridgeBehaviour;
import Behaviors.LineSearchBehavior;
import Behaviors.MazeBehaviour;
import Behaviors.ObstacleEndBehavior;
import Behaviors.RockerBehaviour;
import Behaviors.SensorDataBehaviour;
import Behaviors.RollBoxBehaviour;
import Behaviors.ShutdownBehavior;
import HAL.HAL;
import HAL.IHAL;
import State.SharedState;
import State.StateArbitrator;
import State.MyState;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Behavior;
import lejos.utility.TextMenu;

public class Main {
	public static TextMenu createMenu(MyState[] states) {
		// Allow to pick initial state using the GUI. This code ensures that the
		// default
		// state is always at the top of the list, hence the somewhat lengthy
		// code.
		states[0] = MyState.getInitState();
		int j = 1;
		for (MyState state : MyState.values()) {
			if (state.equals(MyState.getInitState())) {
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
	
	public static void main(String[] args) throws IOException {
		LCD.drawString("Frank the Tank", 0, 2);
		LCD.drawString("is getting ready", 0, 3);
		Sound.beep();
		IHAL hal = new HAL();
		LCD.clear();

		// Create initial shared state.
		MyState[] states = new MyState[MyState.values().length];
		int initialStateIndex = 0;
		if (initialStateIndex < 0) {
			System.exit(0);
		}
		LCD.clear();

		while (true) {
			SharedState sharedState = new SharedState(states[initialStateIndex]);

			ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
			
			// Just for testing/debugging
			behaviors.add(new SensorDataBehaviour(sharedState, hal));
			behaviors.add(new BarcodeBehavior(sharedState, hal));
			
			// Behaviors for each station in the parcour.
			behaviors.add(new HangingBridgeBehaviour(sharedState, hal));
			behaviors.add(new BridgeBehaviour(sharedState, hal));
			behaviors.add(new BossBehaviour(sharedState, hal));
			behaviors.add(new ElevatorBehaviour(sharedState, hal));
			behaviors.add(new FreeTrackBehaviour(sharedState, hal));
			behaviors.add(new MazeBehaviour(sharedState, hal));
			behaviors.add(new RollBoxBehaviour(sharedState, hal));
			behaviors.add(new RockerBehaviour(sharedState, hal));
			
			// Shared behaviors.
			behaviors.add(new LineSearchBehavior(sharedState, hal));
			behaviors.add(new ObstacleEndBehavior(sharedState, hal));

			// WARNING: always keep this as the last element since it allows us to
			// exit from the program.
			behaviors.add(new ShutdownBehavior(sharedState, hal));
			
			// call menu
			Sound.twoBeeps();
			TextMenu menu = Main.createMenu(states);
			initialStateIndex = menu.select();
			if (initialStateIndex < 0) {
				System.exit(0);
			}

			try {
				StateArbitrator a = new StateArbitrator(Main.getArrayForList(behaviors), sharedState);
				sharedState.setState(states[initialStateIndex]);
				a.start();
				a.stop();
				a = null;
				System.gc();
			} catch (Exception e) {
				FileWriter fw = new FileWriter("/home/lejos/latest_exception.txt", false);
				PrintWriter pw = new PrintWriter(fw);
				e.printStackTrace(pw);
				fw.close();
				System.exit(0);
			}
		}
	}

	public static Behavior[] getArrayForList(List<Behavior> behaviors) {
		Behavior[] behavs = new Behavior[behaviors.size()];
		for (int i = 0; i < behavs.length; i++)
			behavs[i] = behaviors.get(i);
		return behavs;
	}
}

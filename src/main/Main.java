package main;

import java.util.ArrayList;

import common.LineSearchBehavior;
import common.SharedState;
import common.State;

import lejos.hardware.lcd.LCD;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Main {
	public static void main(String[] args) {
		LCD.drawString("Running ...", 0, 0);
		
		ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
		SharedState sharedState = new SharedState(State.LineSearch);
		behaviors.add(new LineSearchBehavior(sharedState));
		Arbitrator a = new Arbitrator((Behavior[]) behaviors.toArray());
		a.start();
	}
}

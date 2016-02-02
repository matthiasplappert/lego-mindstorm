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
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.Motor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.MindSensorsPressureSensor;
import lejos.internal.ev3.EV3UARTPort;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;


public class Main {
	public static void main(String[] args) throws InterruptedException {

		Motor.C.setSpeed(50);
		Motor.C.rotateTo(0);
		Thread.sleep (1000);
		Motor.C.rotateTo(-45);
		Thread.sleep (1000);
		Motor.C.rotateTo(-90);
		Thread.sleep (1000);
		Motor.C.rotateTo(-135);
		Thread.sleep (1000);
		Motor.C.rotateTo(0);
		Thread.sleep (1000);
		Motor.C.stop();
		
		Motor.A.forward();
		Motor.B.forward();
		
		EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S2);
		boolean wait = true;
		float[] sample = new float[1];
		while(wait){
			touchSensor.getTouchMode().fetchSample(sample, 0); //get current sample
			if(sample[0] == 0){ //not pressed
				Thread.sleep(10);
			}else{ //pressed
				wait = false;				
			}
		}
		
		Motor.A.backward();
		Motor.B.backward();
		Thread.sleep(1000);
		Motor.A.stop();
		Motor.B.stop();
	   //Motor.C.forward();
	   //Thread.sleep (100);
	   //Motor.C.stop();
	   
	   //Motor.A.rotate(-720,true);
	   //while(Motor.A.isMoving() :Thread.yield();
	   //int angle = Motor.A.getTachoCount(); // should be -360

		//IHAL hal = new HAL();
//		IHAL hal = new DefaultHAL(){
//			@Override
//			public void printOnDisplay(String text, long waitDuration) {
//				System.out.println(text);
//				if(waitDuration>0)
//					HALHelper.sleep(waitDuration);
//			}
//		};

		/*ArrayList<Behavior> behaviors = new ArrayList<Behavior>();
		SharedState sharedState = new SharedState(State.LineSearch);
		
		behaviors.add(new LineSearchBehavior(sharedState, hal));
		behaviors.add(new DisplayTestStateBehavior(sharedState, hal));
		Behavior[] behavs = new Behavior[behaviors.size()];
		for(int i=0;i<behavs.length;i++)
			behavs[i] = behaviors.get(i);
		Arbitrator a = new Arbitrator(behavs, false);
		a.start();*/
	}
}

package Behaviors;

import HAL.IHAL;
import State.SharedState;
import State.State;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;
import lejos.utility.Delay;
import java.io.*;

//TODO:
//Change to three level detection: white, border, line. Behaviour: increasing rotation angle as closer the measurements comes to the border
//TODO: Redo analysis on larger data set
public class LineSearchBehavior extends StateBehavior {
	public static final float THRESHOLD = 0.1f;
	public static final int MEAN_WINDOW = 5;
	public static final boolean KEEP_MEASUREMENTS = false;

	// TODO: update this as soon as we have proper handling in the HAL
	public static final int EXPLORATION_ANGLE_DIFF = 25;

	public static final int LOOP_DELAY = 100;

	private Port port;

	private boolean suppressed;

	public LineSearchBehavior(SharedState sharedState, IHAL hal, Port port) {
		super(sharedState, hal);
		this.port = port;
		this.suppressed = false;
	}
	private BufferedWriter openWriter(String filename) throws IOException{
		if(KEEP_MEASUREMENTS)
			filename+= System.currentTimeMillis()+".bak";
		File file = new File(filename);

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
	      return out;
	}
	@Override
	public void action() {
		EV3ColorSensor sensor = this.hal.getColorSensor();
		SampleProvider sampleProvider = sensor.getRedMode();
		MeanFilter meanFilter = new MeanFilter(sampleProvider, LineSearchBehavior.MEAN_WINDOW);
		float[] meanBuffer = new float[meanFilter.sampleSize()];
		float[] valueBuffer = new float[sampleProvider.sampleSize()];
		// float[] fileSample = new float[sampleProvider.sampleSize()];
		// TODO: implement handling the barcode
		BufferedWriter out; // declare outside the try block

		try {
			out = openWriter("sensor.log");

			int counter = 1;
			Direction direction = Direction.LEFT;
			while (!this.suppressed) {
				// Do not sample too often.
				Delay.msDelay(LineSearchBehavior.LOOP_DELAY);
//				this.writeToLogFile(valueBuffer[0], out);

				if (this.isOnLine(meanFilter, meanBuffer, sampleProvider, valueBuffer, out)) {
					// Drive forward and reset search strategy values for
					// potential later use.
					counter = 1;
					direction = Direction.LEFT;
					this.hal.forward();

					continue;
				}

				//Random directory
				direction = Utils.drawBoolean() ? Direction.LEFT: Direction.RIGHT;
				// Okay, we're not on the line anymore. Start search strategy.
				// We first
				// turn EXPLORATION_ANGLE_DIFF to the left, then the same amount
				// to the right
				// (relative to the start position), then 2 *
				// EXPLORATION_ANGLE_DIFF to the
				// left, ... and so on until we find the line.

				int turn_angle = counter * LineSearchBehavior.EXPLORATION_ANGLE_DIFF;
				if (direction.equals(Direction.RIGHT)) {
					turn_angle = -turn_angle;
				}
				this.hal.rotate(turn_angle, true);
				while (!this.suppressed && this.hal.motorsAreMoving()) {
					if (this.isOnLine(meanFilter, meanBuffer, sampleProvider, valueBuffer, out)) {
						//Overdrive
						this.hal.rotate(turn_angle, true);
						// We've found the line, stop moving.
						this.hal.stop();
						break;
					}

					// Again, do not sample too often here.
					Delay.msDelay(LineSearchBehavior.LOOP_DELAY / 10);
				}
				
				direction = Direction.changeDirection(direction);
				counter++;

			}
//			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    finally{
		sensor.close();
	      }
	}

	private boolean isOnLine(MeanFilter meanFilter, float[] meanBuffer, SampleProvider sampleProvider,
			float[] valueBuffer, BufferedWriter out) throws IOException {
		meanFilter.fetchSample(meanBuffer, 0);
		float currentMean = meanBuffer[0];
//		float currentValue = valueBuffer[0];
		boolean isOnLine = currentMean > LineSearchBehavior.THRESHOLD;
		LCD.drawString("isOnLine: " + isOnLine, 0, 0);
		LCD.drawString("currentMean: " + currentMean, 0, 1);
		return isOnLine;
	}

	private void writeToLogFile(float sample, BufferedWriter out) throws IOException {
	    out.write(Float.toString(sample));
	    out.newLine();
	}
	@Override
	State getTargetState() {
		return State.LineSearch;
	}

	@Override
	public void suppress() {
		this.suppressed = true;
	}
}

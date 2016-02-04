package HAL;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.filter.MeanFilter;

public class SensorMeanFilter extends Thread{

	private float[] meanBufferGyro;
	private float[] meanBufferUltrasonic;
	private float[] meanBufferColor;

	private MeanFilter meanFilterGyro;
	private MeanFilter meanFilterUltrasonic;
	private MeanFilter meanFilterColor;
	private EV3ColorSensor colorSensor;
	private ColorMode colorMode;







	public SensorMeanFilter(EV3GyroSensor gyro, EV3UltrasonicSensor ultrasonic, EV3ColorSensor color) {
		int sampleCount = 10;
		meanFilterGyro = new MeanFilter(gyro.getAngleMode(), sampleCount);
		meanFilterUltrasonic = new MeanFilter(ultrasonic.getDistanceMode(), sampleCount);	
		
		meanBufferGyro = new float[meanFilterGyro.sampleSize()];
		meanBufferUltrasonic = new float[meanFilterUltrasonic.sampleSize()];
		this.colorSensor = color;
		this.enableRedMode();
		
	}
	
	
	
	
	@Override
	public void run(){
		while(true){
			meanFilterGyro.fetchSample(meanBufferGyro, 0);
			meanFilterUltrasonic.fetchSample(meanBufferUltrasonic, 0);
			meanFilterColor.fetchSample(meanBufferColor, 0);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	public float getMeanGyro(){
		return meanBufferGyro[0];
	}
	
	public float getMeanUltrasonic(){
		if(!Float.isNaN(meanBufferUltrasonic[0]))
			return meanBufferUltrasonic[0];
		else
			return Float.POSITIVE_INFINITY;
						
	}
	
	public float getMeanColorValue(){
		return meanBufferColor[0];
	}
	
	public void enableRedMode(){
		
		this.meanFilterColor = new MeanFilter(this.colorSensor.getRedMode(), 5);
		meanBufferColor = new float[meanFilterColor.sampleSize()];

		this.colorMode = ColorMode.RED;
	}

	public void enableRGBMode(){
		
		this.meanFilterColor = new MeanFilter(this.colorSensor.getRGBMode(), 5);
		meanBufferColor = new float[meanFilterColor.sampleSize()];

		this.colorMode = ColorMode.RED;
	}
	
	public void enableColorIDMode(){
		
		this.meanFilterColor = new MeanFilter(this.colorSensor.getColorIDMode(), 5);
		meanBufferColor = new float[meanFilterColor.sampleSize()];
		this.colorMode = ColorMode.RED;
	}

	
	public ColorMode getColorMode() {
		return colorMode;
	}
}

package HAL;

//import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MeanFilter;

public class SensorSampler extends Thread{
	private static final int GYRO_WINDOW_LENGTH = 5;
	
	private EV3GyroSensor gyro;
	private SampleProvider gyroSampleProvider;
	
	private float[] meanBufferGyro;
	private float[] currentBufferGyro;
	private float[] meanBufferUltrasonic;
	private float[] meanBufferColor;

	private MeanFilter meanFilterGyro;
	private MeanFilter meanFilterUltrasonic;
	private MeanFilter meanFilterColor;
	private EV3ColorSensor colorSensor;
	private ColorMode colorMode;

	private Lock lock;

	public SensorSampler(EV3GyroSensor gyro, EV3UltrasonicSensor ultrasonic, EV3ColorSensor color) {
		this.lock = new ReentrantLock();
		
		this.gyro = gyro;
		this.gyroSampleProvider = this.gyro.getAngleMode();
		this.resetGyro(); // this also re-creates all necessary buffers and filters
		
		meanFilterUltrasonic = new MeanFilter(ultrasonic.getDistanceMode(), 5);	
		meanBufferUltrasonic = new float[meanFilterUltrasonic.sampleSize()];
		
		this.colorSensor = color;
		this.enableRedMode();
	}
	
	@Override
	public void run(){
		while(true){
			lock.lock();
				meanFilterGyro.fetchSample(meanBufferGyro, 0);
				gyroSampleProvider.fetchSample(currentBufferGyro, 0);
				meanFilterUltrasonic.fetchSample(meanBufferUltrasonic, 0);
				meanFilterColor.fetchSample(meanBufferColor, 0);
			lock.unlock();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
	
	public void resetGyro() {
		lock.lock();
			gyro.reset();
			meanFilterGyro = new MeanFilter(gyroSampleProvider, GYRO_WINDOW_LENGTH);
			meanBufferGyro = new float[meanFilterGyro.sampleSize()];
			currentBufferGyro = new float[gyro.sampleSize()];
		lock.unlock();
	}
	
	public float getMeanGyro(){
		lock.lock();
		final float value = meanBufferGyro[0];
		lock.unlock();
		return - value;
	}
	
	public float getCurrentGyro() {
		lock.lock();
		final float value = currentBufferGyro[0];
		lock.unlock();
		return - value;
	}
	
	public float getMeanUltrasonic(){
		lock.lock();
		final float value = meanBufferUltrasonic[0];
		lock.unlock();
		if(!Float.isNaN(value))
			return value;
		else
			return 100;
	}
	
	public float getMeanColorValue(){
		lock.lock();
		final float val = meanBufferColor[0];
		lock.unlock();
		return val;
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

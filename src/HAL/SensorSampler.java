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
	private float[] meanBufferAmbient;

	
	private float[] currentBufferUltrasonic;
	private EV3UltrasonicSensor ultrasonic;
	private SampleProvider ultrasonicSampleProvider;
	
	private MeanFilter meanFilterGyro;
	private MeanFilter meanFilterUltrasonic;
	private MeanFilter meanFilterColor;
	private MeanFilter meanFilterAmbient;

	private EV3ColorSensor colorSensor;
	private ColorMode colorMode;

	private boolean suppressed = false;
	
	private Lock lock;

	public SensorSampler(EV3GyroSensor gyro, EV3UltrasonicSensor ultrasonic, EV3ColorSensor color) {
		this.lock = new ReentrantLock();
		
		this.gyro = gyro;
		this.gyroSampleProvider = this.gyro.getAngleMode();
		this.resetGyro(); // this also re-creates all necessary buffers and filters
		
		meanFilterUltrasonic = new MeanFilter(ultrasonic.getDistanceMode(), 5);	
		meanBufferUltrasonic = new float[meanFilterUltrasonic.sampleSize()];
		
		this.ultrasonic = ultrasonic;
		this.ultrasonicSampleProvider = this.ultrasonic.getDistanceMode();
		currentBufferUltrasonic = new float [ultrasonic.sampleSize()];
		
		this.colorSensor = color;
		this.enableRedMode();
		this.lock = new ReentrantLock();
	}
	
	@Override
	public void run(){
		suppressed = false;
		while(!suppressed){
			lock.lock();
				meanFilterGyro.fetchSample(meanBufferGyro, 0);
				gyroSampleProvider.fetchSample(currentBufferGyro, 0);
				meanFilterUltrasonic.fetchSample(meanBufferUltrasonic, 0);
				ultrasonicSampleProvider.fetchSample(currentBufferUltrasonic, 0);
				switch(this.colorMode){
				case AMBIENT_LIGHT:
					meanFilterAmbient.fetchSample(meanBufferAmbient, 0);
					break;
				case RED:
					meanFilterColor.fetchSample(meanBufferColor, 0);
					break;
				default:
					break;
				
				}
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
	 
	
	public void suppress(){
		this.suppressed = true;
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
		this.lock.lock();
			this.meanFilterColor = new MeanFilter(this.colorSensor.getRedMode(), 5);
			meanBufferColor = new float[meanFilterColor.sampleSize()];
			this.colorMode = ColorMode.RED;
		this.lock.unlock();
	}
	public void enableAmbientMode() {
		this.lock.lock();
			this.meanFilterAmbient= new MeanFilter(this.colorSensor.getAmbientMode(), 10);
			meanBufferAmbient = new float[this.meanFilterAmbient.sampleSize()];
			this.colorMode = ColorMode.AMBIENT_LIGHT;
		this.lock.unlock();
	}
	
	public ColorMode getColorMode() {
		return colorMode;
	}

	public float getCurrentUltrasonic() {
		lock.lock();
		final float value = currentBufferUltrasonic[0];
		lock.unlock();
		return value;
	}
	
	public float getMeanAmbientLight() {
		lock.lock();
		final float value = meanBufferAmbient[0];
		lock.unlock();
		return value;
	}

}

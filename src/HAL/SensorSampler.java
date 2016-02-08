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

	
	private float[] currentBufferUltrasonic;
	private EV3UltrasonicSensor ultrasonic;
	private SampleProvider ultrasonicSampleProvider;
	
	private MeanFilter meanFilterGyro;
	private MeanFilter meanFilterUltrasonic;
	private MeanFilter meanFilterColor;
	private EV3ColorSensor colorSensor;
	private ColorMode colorMode;

	private Lock lock;

	private MyColorID colorID;

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
		while(true){
			lock.lock();
				meanFilterGyro.fetchSample(meanBufferGyro, 0);
				gyroSampleProvider.fetchSample(currentBufferGyro, 0);
				meanFilterUltrasonic.fetchSample(meanBufferUltrasonic, 0);
				
				switch(this.colorMode){
				case RED:
					meanFilterColor.fetchSample(meanBufferColor, 0);
					break;
				case COLORID:
					final int colorId = colorSensor.getColorID();
		            switch (colorId){
		                //RED
		                case 0:
		                    this.colorID = MyColorID.RED;
		                    break;
		                //GREEN
		                case 1:
		                    this.colorID = MyColorID.GREEN;
		                    break;
		                //BLUE
		                case 2:
		                    this.colorID = MyColorID.BLUE;
		                default:
		                    this.colorID = MyColorID.UNDEF;
		            }
				case RGB:
					break;
				default:
					break;
				}
				ultrasonicSampleProvider.fetchSample(currentBufferUltrasonic, 0);
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
		this.lock.lock();

		this.meanFilterColor = new MeanFilter(this.colorSensor.getRedMode(), 5);
		meanBufferColor = new float[meanFilterColor.sampleSize()];
		this.colorMode = ColorMode.RED;
		this.lock.unlock();

	}

	public void enableRGBMode(){
		this.lock.lock();

		this.meanFilterColor = new MeanFilter(this.colorSensor.getRGBMode(), 5);
		meanBufferColor = new float[meanFilterColor.sampleSize()];
		this.colorMode = ColorMode.RGB;
		this.lock.unlock();

	}
	
	public void enableColorIDMode(){
		this.lock.lock();
		this.meanFilterColor = new MeanFilter(this.colorSensor.getColorIDMode(), 1);
		meanBufferColor = new float[meanFilterColor.sampleSize()];
		this.colorSensor.setFloodlight(false);
		this.colorMode = ColorMode.COLORID;
		this.lock.unlock();
	}

	
	public ColorMode getColorMode() {
		this.lock.lock();
		final ColorMode cm = this.colorMode;
		this.lock.unlock();
		return cm;
	}

	public float getCurrentUltrasonic() {
		lock.lock();
		final float value = currentBufferUltrasonic[0];
		lock.unlock();
		return value;
	}
	
	public MyColorID getColorID(){
		this.lock.lock();
		final MyColorID color = this.colorID;
		this.lock.unlock();
		return color;
	}
	
}

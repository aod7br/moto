package com.moto;


// http://pastebin.com/c1MNT5x7

import android.app.Activity;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class MotoActivity extends Activity {
	/** Called when the activity is first created. */
	private TextView text_marcha,text_velocidade,text_rpm;
	private Handler mHandler;
	private SensorManager sm = null;

	float []m_lastMagFields;
	float []m_lastAccels;
	private float[] m_rotationMatrix = new float[16];
	private float[] m_remappedR = new float[16];
	private float[] m_orientation = new float[4];   
	/* fix random noise by averaging tilt values */
	final static int AVERAGE_BUFFER = 30;
	float []m_prevPitch = new float[AVERAGE_BUFFER];
	float m_lastPitch = 0.f;
	float m_lastYaw = 0.f;
	/* current index int m_prevEasts */
	int m_pitchIndex = 0;

	float []m_prevRoll = new float[AVERAGE_BUFFER];
	float m_lastRoll = 0.f;
	/* current index into m_prevTilts */
	int m_rollIndex = 0;

	/* center of the rotation */
	private float m_tiltCentreX = 0.f;
	private float m_tiltCentreY = 0.f;
	private float m_tiltCentreZ = 0.f;  
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		sm = (SensorManager) getSystemService(SENSOR_SERVICE);

		text_marcha=(TextView)findViewById(R.id.marcha);     
		text_velocidade=(TextView)findViewById(R.id.velocidade);     
		text_rpm=(TextView)findViewById(R.id.rpm);     

		mHandler = new Handler();
		mHandler.post(loop);
	}

	//@Override
	private Runnable loop = new Runnable() {
		int marcha=1;
		int velocidade=0;
		int rpm=1000;
		float aceleracao=50;
		double[] cambio ={0,3.083,2.062,1.545,1.272,1.130};


		public void run() {
			String cor_marcha;
			String cor_rpm;
			String cor_velocidade;

			SensorManager.getOrientation(outR, values); 

			rpm+=(int) aceleracao;

			if (marcha<5){
				if (rpm>8500) {
					rpm-=3000;
					marcha++;
				}
			}
			rpm=(rpm>8500?8500:rpm);

			velocidade=(int) ( Math.sqrt( rpm/100*( 9/(cambio[marcha]*cambio[marcha]) )  )*Math.sqrt(85) );

			cor_marcha=(marcha<5?"#FFB0B0B0":"#FFFF0000");
			cor_velocidade=(velocidade<200?"#FFFFFF00":"#FFFF0000");
			cor_rpm=(rpm<5500?"#FFB0B0B0":"#FFFFFF00");
			cor_rpm=(rpm<7500?cor_rpm:"#FFFF0000");

			text_marcha.setText(""+marcha);
			text_marcha.setTextColor(Color.parseColor(cor_marcha));
			text_velocidade.setText(""+velocidade);
			text_velocidade.setTextColor(Color.parseColor(cor_velocidade));
			text_rpm.setText(""+rpm);
			text_rpm.setTextColor(Color.parseColor(cor_rpm));

			mHandler.postDelayed(this, 1);
		}//run

	};// runnable 


	Filter [] m_filters = { new Filter(), new Filter(), new Filter() };

	private class Filter {
		static final int AVERAGE_BUFFER = 10;
		float []m_arr = new float[AVERAGE_BUFFER];
		int m_idx = 0;

		public float append(float val) {
			m_arr[m_idx] = val;
			m_idx++;
			if (m_idx == AVERAGE_BUFFER)
				m_idx = 0;
			return avg();
		}
		public float avg() {
			float sum = 0;
			for (float x: m_arr)
				sum += x;
			return sum / AVERAGE_BUFFER;
		}
	}

	private void computeOrientation() {
		if (SensorManager.getRotationMatrix(m_rotationMatrix, null, m_lastMagFields, m_lastAccels)) {
			SensorManager.getOrientation(m_rotationMatrix, m_orientation);

			/* 1 radian = 57.2957795 degrees */
			/* [0] : yaw, rotation around z axis
			 * [1] : pitch, rotation around x axis
			 * [2] : roll, rotation around y axis */
			float yaw = m_orientation[0] * 57.2957795f;
			float pitch = m_orientation[1] * 57.2957795f;
			float roll = m_orientation[2] * 57.2957795f;

			m_lastYaw = m_filters[0].append(yaw);
			m_lastPitch = m_filters[1].append(pitch);
			m_lastRoll = m_filters[2].append(roll);
			//yt.setText("azi z: " + m_lastYaw);
			//pt.setText("pitch x: " + m_lastPitch);
			//rt.setText("roll y: " + m_lastRoll);
		}
	}


}// activity


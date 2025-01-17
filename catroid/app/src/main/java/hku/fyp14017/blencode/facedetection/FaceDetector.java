/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2014 The Catrobat Team
 *  (<http://developer.fyp14017.hku/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.fyp14017.hku/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.hku/licenses/>.
 */
package hku.fyp14017.blencode.facedetection;

import android.graphics.Point;

import hku.fyp14017.blencode.common.ScreenValues;
import hku.fyp14017.blencode.formulaeditor.SensorCustomEvent;
import hku.fyp14017.blencode.formulaeditor.SensorCustomEventListener;
import hku.fyp14017.blencode.formulaeditor.Sensors;

import java.util.LinkedList;
import java.util.List;

import hku.fyp14017.blencode.formulaeditor.SensorCustomEvent;
import hku.fyp14017.blencode.formulaeditor.Sensors;

public abstract class FaceDetector {

	private List<SensorCustomEventListener> faceDetectedListeners = new LinkedList<SensorCustomEventListener>();
	private List<SensorCustomEventListener> faceDetectionStatusListeners = new LinkedList<SensorCustomEventListener>();

	private boolean faceDetected = false;

	public abstract boolean startFaceDetection();

	public abstract void stopFaceDetection();

	public void addOnFaceDetectedListener(SensorCustomEventListener listener) {
		if (listener == null) {
			return;
		}
		faceDetectedListeners.add(listener);
	}

	public void removeOnFaceDetectedListener(SensorCustomEventListener listener) {
		faceDetectedListeners.remove(listener);
	}

	public void addOnFaceDetectionStatusListener(SensorCustomEventListener listener) {
		if (listener == null) {
			return;
		}
		faceDetectionStatusListeners.add(listener);
	}

	public void removeOnFaceDetectionStatusListener(SensorCustomEventListener listener) {
		faceDetectionStatusListeners.remove(listener);
	}

	protected void onFaceDetected(Point position, int size) {
		float[] positionXFloatValue = new float[] { position.x };
		float[] positionYFloatValue = new float[] { position.y };
		float[] sizeFloatValue = new float[] { size };
		SensorCustomEvent xPositionEvent = new SensorCustomEvent(Sensors.FACE_X_POSITION, positionXFloatValue);
		SensorCustomEvent yPositionEvent = new SensorCustomEvent(Sensors.FACE_Y_POSITION, positionYFloatValue);
		SensorCustomEvent sizeEvent = new SensorCustomEvent(Sensors.FACE_SIZE, sizeFloatValue);
		for (SensorCustomEventListener faceDetectedListener : faceDetectedListeners) {
			faceDetectedListener.onCustomSensorChanged(xPositionEvent);
			faceDetectedListener.onCustomSensorChanged(yPositionEvent);
			faceDetectedListener.onCustomSensorChanged(sizeEvent);
		}
	}

	protected void onFaceDetected(boolean faceDetected) {
		if (this.faceDetected != faceDetected) {
			this.faceDetected = faceDetected;
			float[] detectedFloatValue = new float[] { faceDetected ? 1 : 0 };
			SensorCustomEvent event = new SensorCustomEvent(Sensors.FACE_DETECTED, detectedFloatValue);
			for (SensorCustomEventListener listener : faceDetectionStatusListeners) {
				listener.onCustomSensorChanged(event);
			}
		}
	}

	protected Point getRelationForFacePosition() {
		return new Point(-ScreenValues.SCREEN_WIDTH, -ScreenValues.SCREEN_HEIGHT);
	}
}

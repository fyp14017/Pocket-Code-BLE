/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
 * (<http://developer.fyp14017.hku/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.fyp14017.hku/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.hku/licenses/>.
 */
package hku.fyp14017.blencode.stage;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.common.ScreenValues;
import hku.fyp14017.blencode.drone.DroneInitializer;
import hku.fyp14017.blencode.formulaeditor.SensorHandler;
import hku.fyp14017.blencode.io.StageAudioFocus;
import hku.fyp14017.blencode.ui.dialogs.StageDialog;
import hku.fyp14017.blencode.utils.LedUtil;
import hku.fyp14017.blencode.utils.VibratorUtil;
import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.common.ScreenValues;
import hku.fyp14017.blencode.drone.DroneInitializer;
import hku.fyp14017.blencode.formulaeditor.SensorHandler;
import hku.fyp14017.blencode.io.StageAudioFocus;
import hku.fyp14017.blencode.utils.LedUtil;
import hku.fyp14017.blencode.utils.VibratorUtil;

public class StageActivity extends AndroidApplication {
	public static final String TAG = StageActivity.class.getSimpleName();
	public static StageListener stageListener;
	private boolean resizePossible;
	private StageDialog stageDialog;
    public static Context ctx;
	private DroneConnection droneConnection = null;

	public static final int STAGE_ACTIVITY_FINISH = 7777;

	private StageAudioFocus stageAudioFocus;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ctx = StageActivity.this;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (getIntent().getBooleanExtra(DroneInitializer.INIT_DRONE_STRING_EXTRA, false)) {
			droneConnection = new DroneConnection(this);
		}
		stageListener = new StageListener();
		stageDialog = new StageDialog(this, stageListener, hku.fyp14017.blencode.R.style.stage_dialog);
		calculateScreenSizes();

		initialize(stageListener, true);
		if (droneConnection != null) {
			try {
				droneConnection.initialise();
			} catch (RuntimeException runtimeException) {
				Log.e(TAG, "Failure during drone service startup", runtimeException);
				Toast.makeText(this, hku.fyp14017.blencode.R.string.error_no_drone_connected, Toast.LENGTH_LONG).show();
				this.finish();
			}
		}

		stageAudioFocus = new StageAudioFocus(this);
	}

	@Override
	public void onBackPressed() {
		pause();
		stageDialog.show();
	}

	public void manageLoadAndFinish() {
		stageListener.pause();
		stageListener.finish();

		PreStageActivity.shutdownResources();
	}

	@Override
	public void onPause() {
		SensorHandler.stopSensorListeners();
		stageListener.activityPause();
		stageAudioFocus.releaseAudioFocus();
		LedUtil.pauseLed();
		VibratorUtil.pauseVibrator();
		super.onPause();

		if (droneConnection != null) {
			droneConnection.pause();
		}
	}

	@Override
	public void onResume() {
		SensorHandler.startSensorListener(this);
		stageListener.activityResume();
		stageAudioFocus.requestAudioFocus();
		LedUtil.resumeLed();
		VibratorUtil.resumeVibrator();
		super.onResume();

		if (droneConnection != null) {
			droneConnection.start();
		}
	}

	public void pause() {
		SensorHandler.stopSensorListeners();
		stageListener.menuPause();
		LedUtil.pauseLed();
		VibratorUtil.pauseVibrator();
	}

	public void resume() {
		stageListener.menuResume();
		LedUtil.resumeLed();
		VibratorUtil.resumeVibrator();
		SensorHandler.startSensorListener(this);
	}

	public boolean getResizePossible() {
		return resizePossible;
	}

	private void calculateScreenSizes() {
		ifLandscapeSwitchWidthAndHeight();
		int virtualScreenWidth = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenWidth;
		int virtualScreenHeight = ProjectManager.getInstance().getCurrentProject().getXmlHeader().virtualScreenHeight;
		float aspectRatio = (float) virtualScreenWidth / (float) virtualScreenHeight;
		float screenAspectRatio = ScreenValues.getAspectRatio();

		if ((virtualScreenWidth == ScreenValues.SCREEN_WIDTH && virtualScreenHeight == ScreenValues.SCREEN_HEIGHT)
				|| Float.compare(screenAspectRatio, aspectRatio) == 0) {
			resizePossible = false;
			stageListener.maximizeViewPortWidth = ScreenValues.SCREEN_WIDTH;
			stageListener.maximizeViewPortHeight = ScreenValues.SCREEN_HEIGHT;
			return;
		}

		resizePossible = true;

		float scale = 1f;
		float ratioHeight = (float) ScreenValues.SCREEN_HEIGHT / (float) virtualScreenHeight;
		float ratioWidth = (float) ScreenValues.SCREEN_WIDTH / (float) virtualScreenWidth;

		if (aspectRatio < screenAspectRatio) {
			scale = ratioHeight / ratioWidth;
			stageListener.maximizeViewPortWidth = (int) (ScreenValues.SCREEN_WIDTH * scale);
			stageListener.maximizeViewPortX = (int) ((ScreenValues.SCREEN_WIDTH - stageListener.maximizeViewPortWidth) / 2f);
			stageListener.maximizeViewPortHeight = ScreenValues.SCREEN_HEIGHT;

		} else if (aspectRatio > screenAspectRatio) {
			scale = ratioWidth / ratioHeight;
			stageListener.maximizeViewPortHeight = (int) (ScreenValues.SCREEN_HEIGHT * scale);
			stageListener.maximizeViewPortY = (int) ((ScreenValues.SCREEN_HEIGHT - stageListener.maximizeViewPortHeight) / 2f);
			stageListener.maximizeViewPortWidth = ScreenValues.SCREEN_WIDTH;
		}
	}

	private void ifLandscapeSwitchWidthAndHeight() {
		if (ScreenValues.SCREEN_WIDTH > ScreenValues.SCREEN_HEIGHT) {
			int tmp = ScreenValues.SCREEN_HEIGHT;
			ScreenValues.SCREEN_HEIGHT = ScreenValues.SCREEN_WIDTH;
			ScreenValues.SCREEN_WIDTH = tmp;
		}
	}

	@SuppressLint("NewApi")
    @Override
	protected void onDestroy() {
		if (droneConnection != null) {
			droneConnection.destroy();
		}
		Log.d(TAG, "Destroy");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
        for(int a =0 ; a < PreStageActivity.SensorTagCounter; a++){
            PreStageActivity.bgs[a].disconnect();
            PreStageActivity.bgs[a] = null;
        }
        PreStageActivity.SensorTagCounter=0;
        PreStageActivity.CardCounter=0;
		LedUtil.destroy();
		VibratorUtil.destroy();
		super.onDestroy();
	}

}

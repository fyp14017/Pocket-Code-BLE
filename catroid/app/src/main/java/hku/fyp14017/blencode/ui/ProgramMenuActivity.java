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
package hku.fyp14017.blencode.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.ActionBar;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.drone.DroneInitializer;
import hku.fyp14017.blencode.stage.PreStageActivity;
import hku.fyp14017.blencode.stage.StageActivity;

import java.util.concurrent.locks.Lock;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.stage.PreStageActivity;

public class ProgramMenuActivity extends BaseActivity {

	public static final String FORWARD_TO_SCRIPT_ACTIVITY = "forwardToScriptActivity";
	private static final String TAG = ProgramMenuActivity.class.getSimpleName();
	private Lock viewSwitchLock = new ViewSwitchLock();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey(FORWARD_TO_SCRIPT_ACTIVITY)) {
			Intent intent = new Intent(this, ScriptActivity.class);
			intent.putExtra(ScriptActivity.EXTRA_FRAGMENT_POSITION, bundle.getInt(FORWARD_TO_SCRIPT_ACTIVITY));
			startActivity(intent);
		}

		setContentView(hku.fyp14017.blencode.R.layout.activity_program_menu);

		BottomBar.hideAddButton(this);

		final ActionBar actionBar = getSupportActionBar();

		//The try-catch block is a fix for this bug: https://github.com/Catrobat/Catroid/issues/618
		try {
			String title = ProjectManager.getInstance().getCurrentSprite().getName();
			actionBar.setTitle(title);
			actionBar.setHomeButtonEnabled(true);
		} catch (NullPointerException nullPointerException) {
			Log.e(TAG, "onCreate: NPE -> finishing", nullPointerException);
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (ProjectManager.getInstance().getCurrentSpritePosition() == 0) {
			((Button) findViewById(hku.fyp14017.blencode.R.id.program_menu_button_looks)).setText(hku.fyp14017.blencode.R.string.backgrounds);
		} else {
			((Button) findViewById(hku.fyp14017.blencode.R.id.program_menu_button_looks)).setText(hku.fyp14017.blencode.R.string.looks);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PreStageActivity.REQUEST_RESOURCES_INIT && resultCode == RESULT_OK) {
			Intent intent = new Intent(ProgramMenuActivity.this, StageActivity.class);
			DroneInitializer.addDroneSupportExtraToNewIntentIfPresentInOldIntent(data, intent);
			startActivity(intent);
		}
	}

	public void handleScriptsButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		startScriptActivity(ScriptActivity.FRAGMENT_SCRIPTS);
	}

	public void handleLooksButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		startScriptActivity(ScriptActivity.FRAGMENT_LOOKS);
	}

	public void handleSoundsButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		startScriptActivity(ScriptActivity.FRAGMENT_SOUNDS);
	}

	public void handlePlayButton(View view) {
		if (!viewSwitchLock.tryLock()) {
			return;
		}
		ProjectManager.getInstance().getCurrentProject().getUserVariables().resetAllUserVariables();
		Intent intent = new Intent(this, PreStageActivity.class);
		startActivityForResult(intent, PreStageActivity.REQUEST_RESOURCES_INIT);
	}

	private void startScriptActivity(int fragmentPosition) {
		Intent intent = new Intent(this, ScriptActivity.class);
		intent.putExtra(ScriptActivity.EXTRA_FRAGMENT_POSITION, fragmentPosition);
		startActivity(intent);
	}
}

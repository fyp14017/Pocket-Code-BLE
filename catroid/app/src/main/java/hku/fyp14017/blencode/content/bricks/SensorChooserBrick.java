/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
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
package hku.fyp14017.blencode.content.bricks;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.content.Sprite;
import hku.fyp14017.blencode.content.actions.ExtendedActions;
import hku.fyp14017.blencode.stage.PreStageActivity;

import java.util.List;

import hku.fyp14017.blencode.stage.PreStageActivity;

public class SensorChooserBrick extends BrickBaseType implements OnItemSelectedListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private transient AdapterView<?> adapterView;

	public SensorChooserBrick(Sprite sprite) {
		this.sprite = sprite;
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}
		if (view == null) {
			alphaValue = 255;
		}
		view = View.inflate(context, hku.fyp14017.blencode.R.layout.brick_ble_temperature, null);
		view = getViewWithAlpha(alphaValue);

		setCheckboxView(hku.fyp14017.blencode.R.id.brick_ble_temperature_checkbox);
		final Brick brickInstance = this;
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});
		return view;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		adapterView = parent;
	}

	@Override
	public Brick clone() {
		return new SensorChooserBrick(getSprite());
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {

		if (view != null) {

			View layout = view.findViewById(hku.fyp14017.blencode.R.id.brick_ble_temperature_layout);
			Drawable background = layout.getBackground();
			background.setAlpha(alphaValue);

			TextView TempLabel = (TextView) view.findViewById(hku.fyp14017.blencode.R.id.ble_temp_label);
			TempLabel.setTextColor(TempLabel.getTextColors().withAlpha(alphaValue));
			ColorStateList color = TempLabel.getTextColors().withAlpha(alphaValue);
			if (adapterView != null) {
				((TextView) adapterView.getChildAt(0)).setTextColor(color);
			}
			this.alphaValue = (alphaValue);

		}

		return view;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite) {
		SensorChooserBrick copyBrick = (SensorChooserBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	@Override
	public View getPrototypeView(Context context) {
		View prototypeView = View.inflate(context, hku.fyp14017.blencode.R.layout.brick_ble_temperature, null);
		return prototypeView;
	}

	@Override
	public int getRequiredResources() {
        PreStageActivity.SensorTagCounter ++;
        return BLUETOOTH_BLE_SENSORS;
	}

	@Override
	public List<SequenceAction> addActionToSequence(Sprite s, SequenceAction sequence) {
		sequence.addAction(ExtendedActions.connectSensorTagAction());
		return null;
	}

	@Override
	public String brickTutorial() {
		return "Allows apps to connect to the SensorTag and discover all sensors on the device.\n\n"
				+ "Uses Bluetooth 4.0 and Bluetooth Low-Energy frameworks to connect to SensorTag devices.";

	}
}

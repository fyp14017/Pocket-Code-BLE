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
package hku.fyp14017.blencode.content.bricks;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.BaseAdapter;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.content.Script;
import hku.fyp14017.blencode.content.Sprite;
import hku.fyp14017.blencode.content.StartScript;

import java.util.List;

import hku.fyp14017.blencode.content.Script;
import hku.fyp14017.blencode.content.StartScript;

public class WhenStartedBrick extends ScriptBrick {
	private static final long serialVersionUID = 1L;

	private Script script;

	public WhenStartedBrick(Script script) {
		this.script = script;
	}

	public WhenStartedBrick() {

	}

	@Override
	public int getRequiredResources() {
		return NO_RESOURCES;
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite) {
		WhenStartedBrick copyBrick = (WhenStartedBrick) clone();
		copyBrick.script = script;
		return copyBrick;
	}

	@Override
	public View getView(Context context, int brickId, final BaseAdapter baseAdapter) {
		if (animationState) {
			return view;

		}
		view = View.inflate(context, hku.fyp14017.blencode.R.layout.brick_when_started, null);

		setCheckboxView(hku.fyp14017.blencode.R.id.brick_when_started_checkbox);

		//method moved to to DragAndDropListView since it is not working on 2.x
		/*
		 * checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		 * 
		 * @Override
		 * public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		 * checked = isChecked;
		 * if (!checked) {
		 * for (Brick currentBrick : adapter.getCheckedBricks()) {
		 * currentBrick.setCheckedBoolean(false);
		 * }
		 * }
		 * adapter.handleCheck(brickInstance, checked);
		 * 
		 * }
		 * });
		 */

		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		return View.inflate(context, hku.fyp14017.blencode.R.layout.brick_when_started, null);
	}

	@Override
	public Brick clone() {
		return new WhenStartedBrick(null);
	}

	@Override
	public Script getScriptSafe() {
		if (script == null) {
			script = new StartScript();
		}

		return script;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {

		if (view != null) {

			View layout = view.findViewById(hku.fyp14017.blencode.R.id.brick_when_started_layout);
			Drawable background = layout.getBackground();
			background.setAlpha(alphaValue);
			this.alphaValue = (alphaValue);

		}

		return view;
	}

    @Override
    public String brickTutorial(){
        return "This brick marks what should happen when the app is just launched.";
    }

	@Override
	public List<SequenceAction> addActionToSequence(Sprite sprite, SequenceAction sequence) {
		return null;

	}
}

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
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import hku.fyp14017.blencode.R;

import hku.fyp14017.blencode.common.BrickValues;

import hku.fyp14017.blencode.content.Sprite;
import hku.fyp14017.blencode.content.actions.ExtendedActions;
import hku.fyp14017.blencode.formulaeditor.Formula;
import hku.fyp14017.blencode.ui.fragment.FormulaEditorFragment;

import java.util.List;

import hku.fyp14017.blencode.common.BrickValues;
import hku.fyp14017.blencode.formulaeditor.Formula;
import hku.fyp14017.blencode.ui.fragment.FormulaEditorFragment;

public class SetSizeToBrick extends FormulaBrick implements OnClickListener {
	private static final long serialVersionUID = 1L;

	private transient View prototypeView;

	public SetSizeToBrick() {
		addAllowedBrickField(BrickField.SIZE);
	}

	public SetSizeToBrick(double sizeValue) {
		initializeBrickFields(new Formula(sizeValue));
	}

	public SetSizeToBrick(Formula size) {
		initializeBrickFields(size);
	}

	private void initializeBrickFields(Formula size) {
		addAllowedBrickField(BrickField.SIZE);
		setFormulaWithBrickField(BrickField.SIZE, size);
	}

    @Override
    public String brickTutorial(){
        return "Used to set the size of the object.";
    }

	@Override
	public int getRequiredResources() {
		return getFormulaWithBrickField(BrickField.SIZE).getRequiredResources();
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}

		view = View.inflate(context, hku.fyp14017.blencode.R.layout.brick_set_size_to, null);
		view = getViewWithAlpha(alphaValue);

		setCheckboxView(hku.fyp14017.blencode.R.id.brick_set_size_to_checkbox);

		final Brick brickInstance = this;
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});
		TextView text = (TextView) view.findViewById(hku.fyp14017.blencode.R.id.brick_set_size_to_prototype_text_view);
		TextView edit = (TextView) view.findViewById(hku.fyp14017.blencode.R.id.brick_set_size_to_edit_text);
		getFormulaWithBrickField(BrickField.SIZE).setTextFieldId(hku.fyp14017.blencode.R.id.brick_set_size_to_edit_text);
		getFormulaWithBrickField(BrickField.SIZE).refreshTextField(view);
		text.setVisibility(View.GONE);
		edit.setVisibility(View.VISIBLE);
		edit.setOnClickListener(this);
		return view;
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, hku.fyp14017.blencode.R.layout.brick_set_size_to, null);
		TextView textSetSizeTo = (TextView) prototypeView.findViewById(hku.fyp14017.blencode.R.id.brick_set_size_to_prototype_text_view);
		textSetSizeTo.setText(String.valueOf(BrickValues.SET_SIZE_TO));
		return prototypeView;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {

		if (view != null) {

			View layout = view.findViewById(hku.fyp14017.blencode.R.id.brick_set_size_to_layout);
			Drawable background = layout.getBackground();
			background.setAlpha(alphaValue);

			TextView textSize = (TextView) view.findViewById(hku.fyp14017.blencode.R.id.brick_set_size_to_label);
			TextView textPercent = (TextView) view.findViewById(hku.fyp14017.blencode.R.id.brick_set_size_to_percent);
			TextView editSize = (TextView) view.findViewById(hku.fyp14017.blencode.R.id.brick_set_size_to_edit_text);
			textSize.setTextColor(textSize.getTextColors().withAlpha(alphaValue));
			textPercent.setTextColor(textPercent.getTextColors().withAlpha(alphaValue));
			editSize.setTextColor(editSize.getTextColors().withAlpha(alphaValue));
			editSize.getBackground().setAlpha(alphaValue);

			this.alphaValue = (alphaValue);

		}

		return view;
	}

	@Override
	public void onClick(View view) {
		if (checkbox.getVisibility() == View.VISIBLE) {
			return;
		}
		FormulaEditorFragment.showFragment(view, this, getFormulaWithBrickField(BrickField.SIZE));
	}

	@Override
	public List<SequenceAction> addActionToSequence(Sprite sprite, SequenceAction sequence) {
		sequence.addAction(ExtendedActions.setSizeTo(sprite, getFormulaWithBrickField(BrickField.SIZE)));
		return null;
	}
}

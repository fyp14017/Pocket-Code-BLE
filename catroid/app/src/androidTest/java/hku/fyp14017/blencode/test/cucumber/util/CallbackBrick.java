/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2014 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hku.fyp14017.blencode.test.cucumber.util;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.fyp14017.blencode.content.Sprite;
import hku.fyp14017.blencode.content.bricks.ShowBrick;

import java.util.List;

import hku.fyp14017.blencode.content.bricks.ShowBrick;

public final class CallbackBrick extends ShowBrick {
	private final transient BrickCallback callback;

	public CallbackBrick(Sprite sprite, BrickCallback callback) {
		CallbackBrick.this.sprite = sprite;
		this.callback = callback;
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		sequence.addAction(new CallbackAction(callback));
		return null;
	}

	public interface BrickCallback {
		public void onCallback();
	}
}

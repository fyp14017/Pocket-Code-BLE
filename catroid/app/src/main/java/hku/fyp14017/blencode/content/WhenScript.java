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
package hku.fyp14017.blencode.content;

import hku.fyp14017.blencode.content.bricks.ScriptBrick;
import hku.fyp14017.blencode.content.bricks.UserBrick;
import hku.fyp14017.blencode.content.bricks.WhenBrick;

import java.util.List;

import hku.fyp14017.blencode.content.bricks.ScriptBrick;
import hku.fyp14017.blencode.content.bricks.UserBrick;
import hku.fyp14017.blencode.content.bricks.WhenBrick;

public class WhenScript extends Script {

	private static final long serialVersionUID = 1L;
	private static final String LONGPRESSED = "Long Pressed";
	private static final String TAPPED = "Tapped";
	private static final String DOUBLETAPPED = "Double Tapped";
	private static final String SWIPELEFT = "Swipe Left";
	private static final String SWIPERIGHT = "Swipe Right";
	private static final String SWIPEUP = "Swipe Up";
	private static final String SWIPEDOWN = "Swipe Down";
	private static final String[] ACTIONS = { TAPPED, DOUBLETAPPED, LONGPRESSED, SWIPEUP, SWIPEDOWN, SWIPELEFT,
			SWIPERIGHT };
	private String action;
	private transient int position;

	public WhenScript() {
		super();
		this.position = 0;
		this.action = TAPPED;
	}

	public WhenScript(WhenBrick brick) {
		this.brick = brick;
	}

	@Override
	protected Object readResolve() {
		super.readResolve();
		return this;
	}

	public void setAction(int position) {
		this.position = position;
		this.action = ACTIONS[position];
	}

    public void setAction(String action) {
        //this.position = position;
        this.action = action;
    }

	public String getAction() {
		return action;
	}

	public int getPosition() {
		return position;
	}

	@Override
	public ScriptBrick getScriptBrick() {
		if (brick == null) {
            brick = new WhenBrick(this);
		}

		return brick;
	}

	@Override
	public Script copyScriptForSprite(Sprite copySprite, List<UserBrick> preCopiedUserBricks) {
		WhenScript cloneScript = new WhenScript();
		doCopy(copySprite, cloneScript, preCopiedUserBricks);

		return cloneScript;
	}
}

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

import hku.fyp14017.blencode.common.MessageContainer;
import hku.fyp14017.blencode.content.bricks.BroadcastReceiverBrick;
import hku.fyp14017.blencode.content.bricks.ScriptBrick;
import hku.fyp14017.blencode.content.bricks.UserBrick;

import java.util.List;

import hku.fyp14017.blencode.common.MessageContainer;
import hku.fyp14017.blencode.content.bricks.BroadcastReceiverBrick;
import hku.fyp14017.blencode.content.bricks.ScriptBrick;

public class BroadcastScript extends Script implements BroadcastMessage {

	private static final long serialVersionUID = 1L;
	private String receivedMessage;

	public BroadcastScript(String broadcastMessage) {
		super();
		setBroadcastMessage(broadcastMessage);
	}

	@Override
	public ScriptBrick getScriptBrick() {
		if (brick == null) {
			brick = new BroadcastReceiverBrick(this);
		}

		return brick;
	}

	@Override
	protected Object readResolve() {
		MessageContainer.addMessage(receivedMessage, this);
		super.readResolve();
		return this;
	}

	@Override
	public String getBroadcastMessage() {
		return receivedMessage;
	}

	public void setBroadcastMessage(String broadcastMessage) {
		MessageContainer.removeReceiverScript(this.receivedMessage, this);
		this.receivedMessage = broadcastMessage;
		MessageContainer.addMessage(this.receivedMessage, this);

	}

	@Override
	public Script copyScriptForSprite(Sprite copySprite, List<UserBrick> preCopiedUserBricks) {
		BroadcastScript cloneScript = new BroadcastScript(receivedMessage);

		doCopy(copySprite, cloneScript, preCopiedUserBricks);
		return cloneScript;
	}
}

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
package hku.fyp14017.blencode.formulaeditor;

import android.util.Log;

public enum Operators {
	LOGICAL_AND(2, true), LOGICAL_OR(1, true), EQUAL(3, true), NOT_EQUAL(4, true), SMALLER_OR_EQUAL(4, true), GREATER_OR_EQUAL(
			4, true), SMALLER_THAN(4, true), GREATER_THAN(4, true), PLUS(5), MINUS(5), MULT(6), DIVIDE(6), MOD(6), POW(
			7), LOGICAL_NOT(4, true);

	private static final String TAG = Operators.class.getSimpleName();

	private final Integer priority;
	public final boolean isLogicalOperator;

	Operators(Integer priority) {
		this.priority = priority;
		isLogicalOperator = false;
	}

	Operators(Integer priority, boolean isLogical) {
		this.priority = priority;
		isLogicalOperator = isLogical;
	}

	public int compareOperatorTo(Operators operator) {
		int returnValue = 0;
		if (priority > operator.priority) {
			returnValue = 1;
		} else if (priority == operator.priority) {
			returnValue = 0;
		} else if (priority < operator.priority) {
			returnValue = -1;
		}

		return returnValue;
	}

	public static Operators getOperatorByValue(String value) {
		try {
			return valueOf(value);
		} catch (IllegalArgumentException illegalArgumentException) {
			Log.e(TAG, Log.getStackTraceString(illegalArgumentException));
		}
		return null;
	}

	public static boolean isOperator(String value) {
		if (getOperatorByValue(value) == null) {
			return false;
		}

		return true;
	}

}

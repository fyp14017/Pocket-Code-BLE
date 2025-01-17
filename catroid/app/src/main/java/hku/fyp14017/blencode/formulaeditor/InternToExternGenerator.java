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

import android.content.Context;
import android.util.Log;

import hku.fyp14017.blencode.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InternToExternGenerator {

	private String generatedExternFormulaString;
	private ExternInternRepresentationMapping generatedExternInternRepresentationMapping;
	private Context context;

	private static final HashMap<String, Integer> INTERN_EXTERN_LANGUAGE_CONVERTER_MAP = new HashMap<String, Integer>();
	static {
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.DIVIDE.name(), hku.fyp14017.blencode.R.string.formula_editor_operator_divide);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.MULT.name(), hku.fyp14017.blencode.R.string.formula_editor_operator_mult);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.MINUS.name(), hku.fyp14017.blencode.R.string.formula_editor_operator_minus);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.PLUS.name(), hku.fyp14017.blencode.R.string.formula_editor_operator_plus);

		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(".", hku.fyp14017.blencode.R.string.formula_editor_decimal_mark);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.SIN.name(), hku.fyp14017.blencode.R.string.formula_editor_function_sin);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.COS.name(), hku.fyp14017.blencode.R.string.formula_editor_function_cos);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.TAN.name(), hku.fyp14017.blencode.R.string.formula_editor_function_tan);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.LN.name(), hku.fyp14017.blencode.R.string.formula_editor_function_ln);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.LOG.name(), hku.fyp14017.blencode.R.string.formula_editor_function_log);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.PI.name(), hku.fyp14017.blencode.R.string.formula_editor_function_pi);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.SQRT.name(), hku.fyp14017.blencode.R.string.formula_editor_function_sqrt);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.RAND.name(), hku.fyp14017.blencode.R.string.formula_editor_function_rand);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.ABS.name(), hku.fyp14017.blencode.R.string.formula_editor_function_abs);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.ROUND.name(), hku.fyp14017.blencode.R.string.formula_editor_function_round);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.MOD.name(), hku.fyp14017.blencode.R.string.formula_editor_function_mod);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.ARCSIN.name(), hku.fyp14017.blencode.R.string.formula_editor_function_arcsin);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.ARCCOS.name(), hku.fyp14017.blencode.R.string.formula_editor_function_arccos);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.ARCTAN.name(), hku.fyp14017.blencode.R.string.formula_editor_function_arctan);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.EXP.name(), hku.fyp14017.blencode.R.string.formula_editor_function_exp);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.MAX.name(), hku.fyp14017.blencode.R.string.formula_editor_function_max);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.MIN.name(), hku.fyp14017.blencode.R.string.formula_editor_function_min);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.TRUE.name(), hku.fyp14017.blencode.R.string.formula_editor_function_true);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.FALSE.name(), hku.fyp14017.blencode.R.string.formula_editor_function_false);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.LENGTH.name(), hku.fyp14017.blencode.R.string.formula_editor_function_length);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.LETTER.name(), hku.fyp14017.blencode.R.string.formula_editor_function_letter);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Functions.JOIN.name(), hku.fyp14017.blencode.R.string.formula_editor_function_join);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.X_ACCELERATION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_x_acceleration);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.Y_ACCELERATION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_y_acceleration);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.Z_ACCELERATION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_z_acceleration);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.COMPASS_DIRECTION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_compass_direction);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.X_INCLINATION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_x_inclination);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.Y_INCLINATION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_y_inclination);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.FACE_DETECTED.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_face_detected);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.FACE_SIZE.name(), hku.fyp14017.blencode.R.string.formula_editor_sensor_face_size);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.FACE_X_POSITION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_face_x_position);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.FACE_Y_POSITION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_sensor_face_y_position);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.LOUDNESS.name(), hku.fyp14017.blencode.R.string.formula_editor_sensor_loudness);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.IR_TEMPERATURE.name(), hku.fyp14017.blencode.R.string.ir_temperature);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.SENSOR_TAG_TEMPERATURE.name(), hku.fyp14017.blencode.R.string.sensor_temperature);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.ACCELEROMETER_ABS.name(), hku.fyp14017.blencode.R.string.sensor_accelerometer_abs);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.ACCELEROMETER_X.name(), hku.fyp14017.blencode.R.string.sensor_accelerometer_x);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.ACCELEROMETER_Y.name(), hku.fyp14017.blencode.R.string.sensor_accelerometer_y);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.ACCELEROMETER_Z.name(), hku.fyp14017.blencode.R.string.sensor_accelerometer_z);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.GYROSCOPE_X.name(), hku.fyp14017.blencode.R.string.sensor_gyroscope_x);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.GYROSCOPE_Y.name(), hku.fyp14017.blencode.R.string.sensor_gyroscope_y);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.GYROSCOPE_Z.name(), hku.fyp14017.blencode.R.string.sensor_gyroscope_z);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.MAGNETOMETER_ABS.name(), hku.fyp14017.blencode.R.string.sensor_magnetometer_abs);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.MAGNETOMETER_X.name(), hku.fyp14017.blencode.R.string.sensor_magnetometer_x);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.MAGNETOMETER_Y.name(), hku.fyp14017.blencode.R.string.sensor_magnetometer_y);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.MAGNETOMETER_Z.name(), hku.fyp14017.blencode.R.string.sensor_magnetometer_z);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.MAGNETOMETER_Z.name(), hku.fyp14017.blencode.R.string.sensor_magnetometer_z);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.PRESSURE.name(), hku.fyp14017.blencode.R.string.sensor_pressure);
        INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.HUMIDITY.name(), hku.fyp14017.blencode.R.string.sensor_humidity);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.OBJECT_X.name(), hku.fyp14017.blencode.R.string.formula_editor_object_x);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.OBJECT_Y.name(), hku.fyp14017.blencode.R.string.formula_editor_object_y);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.OBJECT_GHOSTEFFECT.name(),
				hku.fyp14017.blencode.R.string.formula_editor_object_ghosteffect);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.OBJECT_BRIGHTNESS.name(),
				hku.fyp14017.blencode.R.string.formula_editor_object_brightness);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.OBJECT_SIZE.name(), hku.fyp14017.blencode.R.string.formula_editor_object_size);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.OBJECT_ROTATION.name(),
				hku.fyp14017.blencode.R.string.formula_editor_object_rotation);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Sensors.OBJECT_LAYER.name(), hku.fyp14017.blencode.R.string.formula_editor_object_layer);

		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.LOGICAL_NOT.name(), hku.fyp14017.blencode.R.string.formula_editor_logic_not);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.NOT_EQUAL.name(), hku.fyp14017.blencode.R.string.formula_editor_logic_notequal);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.EQUAL.name(), hku.fyp14017.blencode.R.string.formula_editor_logic_equal);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.GREATER_OR_EQUAL.name(),
				hku.fyp14017.blencode.R.string.formula_editor_logic_greaterequal);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.GREATER_THAN.name(),
				hku.fyp14017.blencode.R.string.formula_editor_logic_greaterthan);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.LOGICAL_AND.name(), hku.fyp14017.blencode.R.string.formula_editor_logic_and);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.LOGICAL_OR.name(), hku.fyp14017.blencode.R.string.formula_editor_logic_or);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.SMALLER_OR_EQUAL.name(),
				hku.fyp14017.blencode.R.string.formula_editor_logic_leserequal);
		INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.put(Operators.SMALLER_THAN.name(),
				hku.fyp14017.blencode.R.string.formula_editor_logic_lesserthan);

	}

	public InternToExternGenerator(Context context) {
		this.context = context;
		generatedExternFormulaString = "";
		generatedExternInternRepresentationMapping = new ExternInternRepresentationMapping();
	}

	public void generateExternStringAndMapping(List<InternToken> internTokenFormula) {
		Log.i("info", "generateExternStringAndMapping:enter");

		List<InternToken> internTokenList = new LinkedList<InternToken>();

		for (InternToken internToken : internTokenFormula) {
			internTokenList.add(internToken);
		}

		generatedExternInternRepresentationMapping = new ExternInternRepresentationMapping();

		generatedExternFormulaString = "";
		InternToken currentToken = null;
		InternToken nextToken = null;
		String externTokenString;
		int externStringStartIndex;
		int externStringEndIndex;

		int internTokenListIndex = 0;

		while (internTokenList.isEmpty() == false) {
			if (appendWhiteSpace(currentToken, nextToken)) {
				generatedExternFormulaString += " ";
			}
			externStringStartIndex = generatedExternFormulaString.length();
			currentToken = internTokenList.get(0);

			if (internTokenList.size() < 2) {
				nextToken = null;
			} else {
				nextToken = internTokenList.get(1);
			}

			externTokenString = generateExternStringFromToken(currentToken);
			generatedExternFormulaString += externTokenString;
			externStringEndIndex = generatedExternFormulaString.length();

			generatedExternInternRepresentationMapping.putMapping(externStringStartIndex, externStringEndIndex,
					internTokenListIndex);

			internTokenList.remove(0);
			internTokenListIndex++;

		}

		generatedExternFormulaString += " ";

	}

	private String generateExternStringFromToken(InternToken internToken) {
		switch (internToken.getInternTokenType()) {
			case NUMBER:
				String number = internToken.getTokenStringValue();

				if (!number.contains(".")) {
					return number;
				}

				String left = number.substring(0, number.indexOf('.'));
				String right = number.substring(number.indexOf('.') + 1);

				return left + getExternStringForInternTokenValue(".", context) + right;

			case OPERATOR:

				String returnvalue = internToken.getTokenStringValue();
				String mappingValue = getExternStringForInternTokenValue(internToken.getTokenStringValue(), context);

				return mappingValue == null ? returnvalue : mappingValue;

			case BRACKET_OPEN:
			case FUNCTION_PARAMETERS_BRACKET_OPEN:
				return "(";
			case BRACKET_CLOSE:
			case FUNCTION_PARAMETERS_BRACKET_CLOSE:
				return ")";
			case FUNCTION_PARAMETER_DELIMITER:
				return ",";
			case USER_VARIABLE:
				return "\"" + internToken.getTokenStringValue() + "\"";
			case STRING:
				return "\'" + internToken.getTokenStringValue() + "\'";
            case SENSOR:
                Log.d("dev" , "Sensor name is " + internToken.getTokenStringValue());
                if(internToken.getTokenStringValue().startsWith("SensorTag")){
                    return internToken.getTokenStringValue();
                }
                else{
                    return getExternStringForInternTokenValue(internToken.getTokenStringValue(), context);
                }
			default:
				return getExternStringForInternTokenValue(internToken.getTokenStringValue(), context);

		}
	}

	private boolean appendWhiteSpace(InternToken currentToken, InternToken nextToken) {
		if (currentToken == null) {
			return false;
		}
		if (nextToken == null) {
			return true;
		}

		switch (nextToken.getInternTokenType()) {
			case FUNCTION_PARAMETERS_BRACKET_OPEN:
				return false;
		}
		return true;

	}

	public String getGeneratedExternFormulaString() {
		return generatedExternFormulaString;
	}

	public ExternInternRepresentationMapping getGeneratedExternInternRepresentationMapping() {
		return generatedExternInternRepresentationMapping;
	}

	private String getExternStringForInternTokenValue(String internTokenValue, Context context) {
		Integer stringResourceID = INTERN_EXTERN_LANGUAGE_CONVERTER_MAP.get(internTokenValue);
		if (stringResourceID == null) {
			return null;
		}
		return context.getString(stringResourceID);
	}
}

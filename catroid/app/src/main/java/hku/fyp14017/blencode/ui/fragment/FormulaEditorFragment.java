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
package hku.fyp14017.blencode.ui.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.content.bricks.Brick;
import hku.fyp14017.blencode.formulaeditor.Formula;
import hku.fyp14017.blencode.formulaeditor.FormulaEditorEditText;
import hku.fyp14017.blencode.formulaeditor.FormulaElement;
import hku.fyp14017.blencode.formulaeditor.InternFormulaParser;
import hku.fyp14017.blencode.ui.BottomBar;
import hku.fyp14017.blencode.ui.ScriptActivity;
import hku.fyp14017.blencode.ui.dialogs.CustomAlertDialogBuilder;
import hku.fyp14017.blencode.ui.dialogs.FormulaEditorComputeDialog;
import hku.fyp14017.blencode.ui.dialogs.NewStringDialog;
import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.content.bricks.Brick;
import hku.fyp14017.blencode.formulaeditor.Formula;
import hku.fyp14017.blencode.formulaeditor.FormulaEditorEditText;
import hku.fyp14017.blencode.ui.BottomBar;
import hku.fyp14017.blencode.ui.ScriptActivity;
import hku.fyp14017.blencode.ui.dialogs.CustomAlertDialogBuilder;
import hku.fyp14017.blencode.ui.dialogs.FormulaEditorComputeDialog;
import hku.fyp14017.blencode.ui.dialogs.NewStringDialog;

public class FormulaEditorFragment extends SherlockFragment implements OnKeyListener,
		ViewTreeObserver.OnGlobalLayoutListener {

	private static final int PARSER_OK = -1;
	private static final int PARSER_STACK_OVERFLOW = -2;
	private static final int PARSER_INPUT_SYNTAX_ERROR = -3;

	private static final int SET_FORMULA_ON_CREATE_VIEW = 0;
	private static final int SET_FORMULA_ON_SWITCH_EDIT_TEXT = 1;
	private static final int TIME_WINDOW = 2000;

	public static final String FORMULA_EDITOR_FRAGMENT_TAG = "formula_editor_fragment";
	public static final String BRICK_BUNDLE_ARGUMENT = "brick";
	public static final String FORMULA_BUNDLE_ARGUMENT = "formula";

	private Context context;
	private Brick currentBrick;
	private Formula currentFormula;
	private FormulaEditorEditText formulaEditorEditText;
	private LinearLayout formulaEditorKeyboard;
	private ImageButton formularEditorFieldDeleteButton;
	private LinearLayout formulaEditorBrick;
	private Toast toast;
	private View brickView;
	private long[] confirmSwitchEditTextTimeStamp = { 0, 0 };
	private int confirmSwitchEditTextCounter = 0;
	private CharSequence previousActionBarTitle;
	private View fragmentView;
	private VariableDeletedReceiver variableDeletedReceiver;

	public FormulaEditorFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setUpActionBar();
		currentBrick = (Brick) getArguments().getSerializable(BRICK_BUNDLE_ARGUMENT);
		currentFormula = (Formula) getArguments().getSerializable(FORMULA_BUNDLE_ARGUMENT);
	}

	private void setUpActionBar() {
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		previousActionBarTitle = ProjectManager.getInstance().getCurrentSprite().getName();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(hku.fyp14017.blencode.R.string.formula_editor_title);
	}

	private void resetActionBar() {
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setTitle(previousActionBarTitle);
	}

	public static void showFragment(View view, Brick brick, Formula formula) {

		SherlockFragmentActivity activity = null;
		activity = (SherlockFragmentActivity) view.getContext();

		FormulaEditorFragment formulaEditorFragment = (FormulaEditorFragment) activity.getSupportFragmentManager()
				.findFragmentByTag(FORMULA_EDITOR_FRAGMENT_TAG);

		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();

		if (formulaEditorFragment == null) {
			formulaEditorFragment = new FormulaEditorFragment();
			Bundle bundle = new Bundle();
			bundle.putSerializable(BRICK_BUNDLE_ARGUMENT, brick);
			bundle.putSerializable(FORMULA_BUNDLE_ARGUMENT, formula);
			formulaEditorFragment.setArguments(bundle);

			fragTransaction.add(hku.fyp14017.blencode.R.id.script_fragment_container, formulaEditorFragment, FORMULA_EDITOR_FRAGMENT_TAG);
			fragTransaction.hide(fragmentManager.findFragmentByTag(ScriptFragment.TAG));
			fragTransaction.show(formulaEditorFragment);
			BottomBar.hideBottomBar(activity);
		} else if (formulaEditorFragment.isHidden()) {
			formulaEditorFragment.updateBrickViewAndFormula(brick, formula);
			fragTransaction.hide(fragmentManager.findFragmentByTag(ScriptFragment.TAG));
			fragTransaction.show(formulaEditorFragment);
			BottomBar.hideBottomBar(activity);
		} else {
			formulaEditorFragment.setInputFormula(formula, SET_FORMULA_ON_SWITCH_EDIT_TEXT);
		}
		fragTransaction.commit();
	}

	public void updateBrickView() {
		updateBrickView(currentBrick);
	}

	private void updateBrickView(Brick newBrick) {
		currentBrick = newBrick;
		formulaEditorBrick.removeAllViews();
		View newBrickView = newBrick.getView(context, 0, null);
		formulaEditorBrick.addView(newBrickView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		brickView = newBrickView;
		fragmentView.getViewTreeObserver().addOnGlobalLayoutListener(this);

	}

	private void updateBrickViewAndFormula(Brick newBrick, Formula newFormula) {
		updateBrickView(newBrick);
		currentFormula = newFormula;
		setInputFormula(newFormula, SET_FORMULA_ON_CREATE_VIEW);
		fragmentView.getViewTreeObserver().addOnGlobalLayoutListener(this);
		updateButtonViewOnKeyboard();
	}

	private void onUserDismiss() {
		formulaEditorEditText.endEdit();
		currentFormula.prepareToRemove();

		SherlockFragmentActivity activity = getSherlockActivity();
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
		fragTransaction.hide(this);
		fragTransaction.show(fragmentManager.findFragmentByTag(ScriptFragment.TAG));
		fragTransaction.commit();

		resetActionBar();

		BottomBar.showBottomBar(activity);
		BottomBar.showPlayButton(activity);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentView = inflater.inflate(hku.fyp14017.blencode.R.layout.fragment_formula_editor, container, false);
		fragmentView.setFocusableInTouchMode(true);
		fragmentView.requestFocus();

		formularEditorFieldDeleteButton = (ImageButton) fragmentView.findViewById(hku.fyp14017.blencode.R.id.formula_editor_edit_field_clear);

		context = getActivity();
		brickView = currentBrick.getView(context, 0, null);

		formulaEditorBrick = (LinearLayout) fragmentView.findViewById(hku.fyp14017.blencode.R.id.formula_editor_brick_space);

		formulaEditorBrick.addView(brickView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));

		formulaEditorEditText = (FormulaEditorEditText) fragmentView.findViewById(hku.fyp14017.blencode.R.id.formula_editor_edit_field);

		formulaEditorKeyboard = (LinearLayout) fragmentView.findViewById(hku.fyp14017.blencode.R.id.formula_editor_keyboardview);
		formulaEditorEditText.init(this);

		fragmentView.getViewTreeObserver().addOnGlobalLayoutListener(this);

		setInputFormula(currentFormula, SET_FORMULA_ON_CREATE_VIEW);

		return fragmentView;
	}

	@Override
	public void onStart() {
		formulaEditorKeyboard.setClickable(true);
		formularEditorFieldDeleteButton.setClickable(true);

		getView().requestFocus();
		View.OnTouchListener touchListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				Log.i("info", "viewId: " + view.getId());
				if (event.getAction() == MotionEvent.ACTION_UP) {
					updateButtonViewOnKeyboard();
					view.setPressed(false);
					return true;
				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					view.setPressed(true);

					switch (view.getId()) {
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_compute:
							InternFormulaParser internFormulaParser = formulaEditorEditText.getFormulaParser();
							FormulaElement formulaElement = internFormulaParser.parseFormula();
							if (formulaElement == null) {
								if (internFormulaParser.getErrorTokenIndex() >= 0) {
									formulaEditorEditText.setParseErrorCursorAndSelection();
								}
								return false;
							}
							Formula formulaToCompute = new Formula(formulaElement);
							FormulaEditorComputeDialog computeDialog = new FormulaEditorComputeDialog(context);
							computeDialog.setFormula(formulaToCompute);
							computeDialog.show();
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_undo:
							formulaEditorEditText.undo();
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_redo:
							formulaEditorEditText.redo();
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_function:
							showFormulaEditorListFragment(FormulaEditorListFragment.FUNCTION_TAG,
									hku.fyp14017.blencode.R.string.formula_editor_function);
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_logic:
							showFormulaEditorListFragment(FormulaEditorListFragment.LOGIC_TAG,
									hku.fyp14017.blencode.R.string.formula_editor_logic);
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_object:
							showFormulaEditorListFragment(FormulaEditorListFragment.OBJECT_TAG,
									hku.fyp14017.blencode.R.string.formula_editor_choose_object_variable);
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_sensors:
							showFormulaEditorListFragment(FormulaEditorListFragment.SENSOR_TAG,
									hku.fyp14017.blencode.R.string.formula_editor_sensors);
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_variables:
							showFormulaEditorVariableListFragment(FormulaEditorVariableListFragment.VARIABLE_TAG,
									hku.fyp14017.blencode.R.string.formula_editor_variables);
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_ok:
							endFormulaEditor();
							return true;
						case hku.fyp14017.blencode.R.id.formula_editor_keyboard_string:
							FragmentManager fragmentManager = ((SherlockFragmentActivity) context)
									.getSupportFragmentManager();
							Fragment dialogFragment = fragmentManager
									.findFragmentByTag(NewStringDialog.DIALOG_FRAGMENT_TAG);

							if (dialogFragment == null) {
								dialogFragment = NewStringDialog.newInstance();
							}

							((NewStringDialog) dialogFragment).show(fragmentManager,
									NewStringDialog.DIALOG_FRAGMENT_TAG);
							return true;
						default:
							formulaEditorEditText.handleKeyEvent(view.getId(), "");
							return true;
					}

				}
				return false;
			}
		};

		for (int index = 0; index < formulaEditorKeyboard.getChildCount(); index++) {
			LinearLayout child = (LinearLayout) formulaEditorKeyboard.getChildAt(index);
			for (int nestedIndex = 0; nestedIndex < child.getChildCount(); nestedIndex++) {
				View view = child.getChildAt(nestedIndex);
				view.setOnTouchListener(touchListener);
			}
		}
		formularEditorFieldDeleteButton.setOnTouchListener(touchListener);

		updateButtonViewOnKeyboard();

		super.onStart();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		for (int index = 0; index < menu.size(); index++) {
			menu.getItem(index).setVisible(false);
		}

		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(getString(hku.fyp14017.blencode.R.string.formula_editor_title));

		super.onPrepareOptionsMenu(menu);
	}

	private void setInputFormula(Formula newFormula, int mode) {

		int orientation = getResources().getConfiguration().orientation;

		switch (mode) {
			case SET_FORMULA_ON_CREATE_VIEW:
				formulaEditorEditText.enterNewFormula(currentFormula.getInternFormulaState());
				currentFormula.highlightTextField(brickView, orientation);
				refreshFormulaPreviewString();
				break;
			case SET_FORMULA_ON_SWITCH_EDIT_TEXT:

				if (currentFormula == newFormula && formulaEditorEditText.hasChanges()) {
					formulaEditorEditText.quickSelect();
					break;
				}
				if (formulaEditorEditText.hasChanges()) {
					confirmSwitchEditTextTimeStamp[0] = confirmSwitchEditTextTimeStamp[1];
					confirmSwitchEditTextTimeStamp[1] = System.currentTimeMillis();
					confirmSwitchEditTextCounter++;
					if (!saveFormulaIfPossible()) {
						return;
					}
				}

				formulaEditorEditText.endEdit();

				currentFormula = newFormula;
				formulaEditorEditText.enterNewFormula(newFormula.getInternFormulaState());

				refreshFormulaPreviewString();

				break;
			default:
				break;
		}
	}

	public boolean saveFormulaIfPossible() {
		InternFormulaParser formulaToParse = formulaEditorEditText.getFormulaParser();
		FormulaElement formulaParseTree = formulaToParse.parseFormula();
		int err = formulaToParse.getErrorTokenIndex();
		switch (err) {
			case PARSER_OK:
				currentFormula.setRoot(formulaParseTree);
				if (formulaEditorBrick != null) {
					currentFormula.refreshTextField(brickView);
				}
				formulaEditorEditText.formulaSaved();
				showToast(hku.fyp14017.blencode.R.string.formula_editor_changes_saved);
				return true;
			case PARSER_STACK_OVERFLOW:
				return checkReturnWithoutSaving(PARSER_STACK_OVERFLOW);
			default:
				formulaEditorEditText.setParseErrorCursorAndSelection();
				return checkReturnWithoutSaving(PARSER_INPUT_SYNTAX_ERROR);
		}
	}

	private boolean checkReturnWithoutSaving(int errorType) {
		Log.i("info",
				"confirmSwitchEditTextCounter=" + confirmSwitchEditTextCounter + " "
						+ (System.currentTimeMillis() <= confirmSwitchEditTextTimeStamp[0] + TIME_WINDOW));

		if ((System.currentTimeMillis() <= confirmSwitchEditTextTimeStamp[0] + TIME_WINDOW)
				&& (confirmSwitchEditTextCounter > 1)) {
			confirmSwitchEditTextTimeStamp[0] = 0;
			confirmSwitchEditTextTimeStamp[1] = 0;
			confirmSwitchEditTextCounter = 0;
			currentFormula.setDisplayText(null);
			showToast(hku.fyp14017.blencode.R.string.formula_editor_changes_discarded);
			return true;
		} else {
			switch (errorType) {
				case PARSER_INPUT_SYNTAX_ERROR:
					showToast(hku.fyp14017.blencode.R.string.formula_editor_parse_fail);
					break;
				case PARSER_STACK_OVERFLOW:
					showToast(hku.fyp14017.blencode.R.string.formula_editor_parse_fail_formula_too_long);
					break;
			}
			return false;
		}

	}

	/*
	 * TODO Remove Toasts from this class and replace them with something useful
	 * This is a hack more than anything else. We shouldn't use Toasts if we're going to change the message all the time
	 */
	private void showToast(int resourceId) {
		if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
			toast = Toast.makeText(getActivity().getApplicationContext(), resourceId, Toast.LENGTH_SHORT);
		} else {
			toast.setText(resourceId);
		}
		toast.show();
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		Log.i("info", "onKey() in FE-Fragment! keyCode: " + keyCode);
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (formulaEditorEditText.hasChanges()) {
					AlertDialog.Builder builder = new CustomAlertDialogBuilder(getActivity());
					builder.setTitle(hku.fyp14017.blencode.R.string.formula_editor_discard_changes_dialog_title)
							.setMessage(hku.fyp14017.blencode.R.string.formula_editor_discard_changes_dialog_message)
							.setNegativeButton(hku.fyp14017.blencode.R.string.no, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {

									showToast(hku.fyp14017.blencode.R.string.formula_editor_changes_discarded);
									currentFormula.setDisplayText(null);
									onUserDismiss();
								}
							}).setPositiveButton(hku.fyp14017.blencode.R.string.yes, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (saveFormulaIfPossible()) {
										onUserDismiss();
									}
								}
							}).create().show();

				} else {
					onUserDismiss();
				}

				return true;
		}
		return false;
	}

	private void endFormulaEditor() {
		if (formulaEditorEditText.hasChanges()) {
			if (saveFormulaIfPossible()) {
				onUserDismiss();
			}
		} else {
			onUserDismiss();
		}
	}

	public void refreshFormulaPreviewString() {
		refreshFormulaPreviewString(formulaEditorEditText.getStringFromInternFormula());
	}

	public void refreshFormulaPreviewString(String newString) {
		currentFormula.setDisplayText(newString);

		updateBrickView();

		currentFormula.refreshTextField(brickView, newString);

		int orientation = getResources().getConfiguration().orientation;
		currentFormula.highlightTextField(brickView, orientation);
	}

	private void showFormulaEditorListFragment(String tag, int actionbarResId) {
		FragmentManager fragmentManager = ((SherlockFragmentActivity) context).getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(tag);

		if (fragment == null) {
			fragment = new FormulaEditorListFragment();
			Bundle bundle = new Bundle();
			bundle.putString(FormulaEditorListFragment.ACTION_BAR_TITLE_BUNDLE_ARGUMENT,
					context.getString(actionbarResId));
			bundle.putString(FormulaEditorListFragment.FRAGMENT_TAG_BUNDLE_ARGUMENT, tag);
			fragment.setArguments(bundle);
			fragmentManager.beginTransaction().add(hku.fyp14017.blencode.R.id.script_fragment_container, fragment, tag).commit();
		}
		((FormulaEditorListFragment) fragment).showFragment(context);
	}

	private void showFormulaEditorVariableListFragment(String tag, int actionbarResId) {
		FragmentManager fragmentManager = ((SherlockFragmentActivity) context).getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(tag);

		if (fragment == null) {
			if (getActivity().getClass().equals(ScriptActivity.class)) {
				fragment = new FormulaEditorVariableListFragment(false);
			}
			else {
				fragment = new FormulaEditorVariableListFragment(true);
			}
			Bundle bundle = new Bundle();
			bundle.putString(FormulaEditorVariableListFragment.ACTION_BAR_TITLE_BUNDLE_ARGUMENT,
					context.getString(actionbarResId));
			bundle.putString(FormulaEditorVariableListFragment.FRAGMENT_TAG_BUNDLE_ARGUMENT, tag);
			fragment.setArguments(bundle);
			fragmentManager.beginTransaction().add(hku.fyp14017.blencode.R.id.script_fragment_container, fragment, tag).commit();
		}
		((FormulaEditorVariableListFragment) fragment).setAddButtonListener(getSherlockActivity());
		((FormulaEditorVariableListFragment) fragment).showFragment(context);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onGlobalLayout() {
		fragmentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

		Rect brickRect = new Rect();
		Rect keyboardRec = new Rect();
		formulaEditorBrick.getGlobalVisibleRect(brickRect);
		formulaEditorKeyboard.getGlobalVisibleRect(keyboardRec);

		formulaEditorEditText.setMaxHeight(keyboardRec.top - brickRect.bottom);

	}

    public void addResourceToActiveFormula(int resource, String name) {
        formulaEditorEditText.handleKeyEvent(resource, name);
    }

	public void addResourceToActiveFormula(int resource) {
		formulaEditorEditText.handleKeyEvent(resource, "");
	}

	public void addUserVariableToActiveFormula(String userVariableName) {
		formulaEditorEditText.handleKeyEvent(0, userVariableName);
	}

	public void addStringToActiveFormula(String string) {
		formulaEditorEditText.handleKeyEvent(hku.fyp14017.blencode.R.id.formula_editor_keyboard_string, string);
	}

	private class VariableDeletedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScriptActivity.ACTION_VARIABLE_DELETED)) {
				updateBrickView(currentBrick);
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (variableDeletedReceiver != null) {
			getActivity().unregisterReceiver(variableDeletedReceiver);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (variableDeletedReceiver == null) {
			variableDeletedReceiver = new VariableDeletedReceiver();
		}

		IntentFilter filterVariableDeleted = new IntentFilter(ScriptActivity.ACTION_VARIABLE_DELETED);
		getActivity().registerReceiver(variableDeletedReceiver, filterVariableDeleted);
		BottomBar.hideBottomBar(getSherlockActivity());
	}

	public void updateButtonViewOnKeyboard() {

		ImageButton undo = (ImageButton) getSherlockActivity().findViewById(hku.fyp14017.blencode.R.id.formula_editor_keyboard_undo);
		if (!formulaEditorEditText.getHistory().undoIsPossible()) {
			undo.setImageResource(hku.fyp14017.blencode.R.drawable.icon_undo_disabled);
			undo.setEnabled(false);
		} else {
			undo.setImageResource(hku.fyp14017.blencode.R.drawable.icon_undo);
			undo.setEnabled(true);
		}

		ImageButton redo = (ImageButton) getSherlockActivity().findViewById(hku.fyp14017.blencode.R.id.formula_editor_keyboard_redo);
		if (!formulaEditorEditText.getHistory().redoIsPossible()) {
			redo.setImageResource(hku.fyp14017.blencode.R.drawable.icon_redo_disabled);
			redo.setEnabled(false);
		} else {
			redo.setImageResource(hku.fyp14017.blencode.R.drawable.icon_redo);
			redo.setEnabled(true);
		}

		ImageButton backspace = (ImageButton) getSherlockActivity().findViewById(hku.fyp14017.blencode.R.id.formula_editor_edit_field_clear);
		if (!formulaEditorEditText.isThereSomethingToDelete()) {
			backspace.setImageResource(hku.fyp14017.blencode.R.drawable.icon_backspace_disabled);
			backspace.setEnabled(false);
		} else {
			backspace.setImageResource(hku.fyp14017.blencode.R.drawable.icon_backspace);
			backspace.setEnabled(true);
		}

	}
}

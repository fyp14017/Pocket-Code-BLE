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
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.common.Constants;
import hku.fyp14017.blencode.content.Script;
import hku.fyp14017.blencode.content.Sprite;
import hku.fyp14017.blencode.content.bricks.AllowedAfterDeadEndBrick;
import hku.fyp14017.blencode.content.bricks.Brick;
import hku.fyp14017.blencode.content.bricks.DeadEndBrick;
import hku.fyp14017.blencode.content.bricks.NestingBrick;
import hku.fyp14017.blencode.content.bricks.ScriptBrick;
import hku.fyp14017.blencode.content.bricks.UserBrick;
import hku.fyp14017.blencode.ui.BottomBar;
import hku.fyp14017.blencode.ui.ScriptActivity;
import hku.fyp14017.blencode.ui.ViewSwitchLock;
import hku.fyp14017.blencode.ui.adapter.BrickAdapter;
import hku.fyp14017.blencode.ui.adapter.BrickAdapter.OnBrickCheckedListener;
import hku.fyp14017.blencode.ui.dialogs.CustomAlertDialogBuilder;
import hku.fyp14017.blencode.ui.dialogs.DeleteLookDialog;
import hku.fyp14017.blencode.ui.dragndrop.DragAndDropListView;
import hku.fyp14017.blencode.ui.fragment.BrickCategoryFragment.OnCategorySelectedListener;
import hku.fyp14017.blencode.utils.Utils;

import java.util.List;
import java.util.concurrent.locks.Lock;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.common.Constants;
import hku.fyp14017.blencode.content.Script;
import hku.fyp14017.blencode.content.bricks.AllowedAfterDeadEndBrick;
import hku.fyp14017.blencode.content.bricks.Brick;
import hku.fyp14017.blencode.content.bricks.ScriptBrick;
import hku.fyp14017.blencode.content.bricks.UserBrick;
import hku.fyp14017.blencode.ui.BottomBar;
import hku.fyp14017.blencode.ui.ScriptActivity;
import hku.fyp14017.blencode.ui.ViewSwitchLock;
import hku.fyp14017.blencode.ui.dialogs.CustomAlertDialogBuilder;
import hku.fyp14017.blencode.ui.dialogs.DeleteLookDialog;

public class ScriptFragment extends ScriptActivityFragment implements OnCategorySelectedListener,
		OnBrickCheckedListener {

	public static final String TAG = ScriptFragment.class.getSimpleName();

	private static final int ACTION_MODE_COPY = 0;
	private static final int ACTION_MODE_DELETE = 1;

	private static int selectedBrickPosition = Constants.NO_POSITION;

	private ActionMode actionMode;
	private View selectAllActionModeButton;

	private BrickAdapter adapter;
	private DragAndDropListView listView;

	private Sprite sprite;
	private Script scriptToEdit;

	private BrickListChangedReceiver brickListChangedReceiver;

	private Lock viewSwitchLock = new ViewSwitchLock();

	private boolean deleteScriptFromContextMenu = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(hku.fyp14017.blencode.R.layout.fragment_script, null);

		listView = (DragAndDropListView) rootView.findViewById(android.R.id.list);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initListeners();
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		menu.findItem(hku.fyp14017.blencode.R.id.show_details).setVisible(false);
		menu.findItem(hku.fyp14017.blencode.R.id.rename).setVisible(false);
		menu.findItem(hku.fyp14017.blencode.R.id.backpack).setVisible(false);
		menu.findItem(hku.fyp14017.blencode.R.id.unpacking).setVisible(false);

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.findItem(hku.fyp14017.blencode.R.id.delete).setVisible(true);
		menu.findItem(hku.fyp14017.blencode.R.id.copy).setVisible(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onStart() {
		super.onStart();
		BottomBar.showBottomBar(getActivity());
		initListeners();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!Utils.checkForExternalStorageAvailableAndDisplayErrorIfNot(getActivity())) {
			return;
		}

		if (brickListChangedReceiver == null) {
			brickListChangedReceiver = new BrickListChangedReceiver();
		}

		IntentFilter filterBrickListChanged = new IntentFilter(ScriptActivity.ACTION_BRICK_LIST_CHANGED);
		getActivity().registerReceiver(brickListChangedReceiver, filterBrickListChanged);

		BottomBar.showBottomBar(getActivity());
		BottomBar.showPlayButton(getActivity());
		BottomBar.showAddButton(getActivity());
		initListeners();
		adapter.resetAlphas();
	}

	@Override
	public void onPause() {
		super.onPause();
		ProjectManager projectManager = ProjectManager.getInstance();

		if (brickListChangedReceiver != null) {
			getActivity().unregisterReceiver(brickListChangedReceiver);
		}
		if (projectManager.getCurrentProject() != null) {
			projectManager.saveProject();
			projectManager.getCurrentProject().removeUnusedBroadcastMessages(); // TODO: Find better place
		}
	}

	public BrickAdapter getAdapter() {
		return adapter;
	}

	@Override
	public DragAndDropListView getListView() {
		return listView;
	}

	@Override
	public void onCategorySelected(String category) {
		AddBrickFragment addBrickFragment = AddBrickFragment.newInstance(category, this);
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(hku.fyp14017.blencode.R.id.script_fragment_container, addBrickFragment,
				AddBrickFragment.ADD_BRICK_FRAGMENT_TAG);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
		adapter.notifyDataSetChanged();
	}

	public void updateAdapterAfterAddNewBrick(Brick brickToBeAdded) {
		int firstVisibleBrick = listView.getFirstVisiblePosition();
		int lastVisibleBrick = listView.getLastVisiblePosition();
		int position = ((1 + lastVisibleBrick - firstVisibleBrick) / 2);
		position += firstVisibleBrick;

		//TODO: allow recursive userbricks if its possible
		if (adapter.getUserBrick() != null && brickToBeAdded instanceof UserBrick) {// && ((UserBrick) brickToBeAdded).getDefinitionBrick().equals(ProjectManager.getInstance().getCurrentUserBrick().getDefinitionBrick())) {
			Toast toast = null;
			if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
				toast = Toast.makeText(getActivity().getApplicationContext(), hku.fyp14017.blencode.R.string.recursive_user_brick_forbidden, Toast.LENGTH_LONG);
			} else {
				toast.setText(hku.fyp14017.blencode.R.string.recursive_user_brick_forbidden);
			}
			toast.show();
		}
		else {
			adapter.addNewBrick(position, brickToBeAdded, true);
			adapter.notifyDataSetChanged();
		}
	}

	private void initListeners() {
		sprite = ProjectManager.getInstance().getCurrentSprite();
		if (sprite == null) {
			return;
		}

		getSherlockActivity().findViewById(hku.fyp14017.blencode.R.id.button_add).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				handleAddButton();
			}
		});

		adapter = new BrickAdapter(getActivity(), sprite, listView);
		adapter.setOnBrickCheckedListener(this);
		ScriptActivity activity = (ScriptActivity) getActivity();
		activity.setupBrickAdapter(adapter);

		if (ProjectManager.getInstance().getCurrentSprite().getNumberOfScripts() > 0) {
			ProjectManager.getInstance().setCurrentScript(((ScriptBrick) adapter.getItem(0)).getScriptSafe());
		}

		listView.setOnCreateContextMenuListener(this);
		listView.setOnDragAndDropListener(adapter);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);
	}

	private void showCategoryFragment() {
		BrickCategoryFragment brickCategoryFragment = new BrickCategoryFragment();
		brickCategoryFragment.setBrickAdapter(adapter);
		brickCategoryFragment.setOnCategorySelectedListener(this);
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		fragmentTransaction.add(hku.fyp14017.blencode.R.id.script_fragment_container, brickCategoryFragment,
				BrickCategoryFragment.BRICK_CATEGORY_FRAGMENT_TAG);

		fragmentTransaction.addToBackStack(BrickCategoryFragment.BRICK_CATEGORY_FRAGMENT_TAG);
		fragmentTransaction.commit();

		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean getShowDetails() {
		//Currently no showDetails option
		return false;
	}

	@Override
	public void setShowDetails(boolean showDetails) {
		//Currently no showDetails option
	}

	@Override
	protected void showRenameDialog() {
		//Rename not supported
	}

	@Override
	public void startRenameActionMode() {
		//Rename not supported
	}

	@Override
	public void startCopyActionMode() {
		startActionMode(copyModeCallBack);
	}

	@Override
	public void startDeleteActionMode() {
		startActionMode(deleteModeCallBack);
	}

	private void startActionMode(ActionMode.Callback actionModeCallback) {
		actionMode = getSherlockActivity().startActionMode(actionModeCallback);

		for (int i = adapter.listItemCount; i < adapter.getBrickList().size(); i++) {
			adapter.getView(i, null, getListView());
		}

		unregisterForContextMenu(listView);
		BottomBar.hideBottomBar(getActivity());
		adapter.setCheckboxVisibility(View.VISIBLE);
		adapter.setActionMode(true);
		updateActionModeTitle();
	}

	@Override
	public void handleAddButton() {
		if (!viewSwitchLock.tryLock()) {
			return;
		}

		// addButtonHandler != null when the user brick category is open in the AddBrickFragment
		if (AddBrickFragment.addButtonHandler != null) {
			AddBrickFragment.addButtonHandler.handleAddButton();
			return;
		}

		if (listView.isCurrentlyDragging()) {
			listView.animateHoveringBrick();
			return;
		}

		showCategoryFragment();
	}

	@Override
	public boolean getActionModeActive() {
		return actionModeActive;
	}

	@Override
	public int getSelectMode() {
		return adapter.getSelectMode();
	}

	@Override
	public void setSelectMode(int selectMode) {
		adapter.setSelectMode(selectMode);
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void showDeleteDialog() {

		DeleteLookDialog deleteLookDialog = DeleteLookDialog.newInstance(selectedBrickPosition);
		deleteLookDialog.show(getFragmentManager(), DeleteLookDialog.DIALOG_FRAGMENT_TAG);
	}

	private class BrickListChangedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScriptActivity.ACTION_BRICK_LIST_CHANGED)) {
				adapter.updateProjectBrickList();
			}
		}
	}

	private void addSelectAllActionModeButton(ActionMode mode, Menu menu) {
		selectAllActionModeButton = Utils.addSelectAllActionModeButton(getLayoutInflater(null), mode, menu);
		selectAllActionModeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				adapter.checkAllItems();
			}
		});
	}

	private ActionMode.Callback deleteModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);
			setActionModeActive(true);

			mode.setTag(ACTION_MODE_DELETE);
			addSelectAllActionModeButton(mode, menu);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {

			if (adapter.getAmountOfCheckedItems() == 0) {
				clearCheckedBricksAndEnableButtons();
			} else {
				showConfirmDeleteDialog(false);
			}
		}
	};

	private ActionMode.Callback copyModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);
			setActionModeActive(true);

			mode.setTag(ACTION_MODE_COPY);
			addSelectAllActionModeButton(mode, menu);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			List<Brick> checkedBricks = adapter.getCheckedBricks();

			for (Brick brick : checkedBricks) {
				copyBrick(brick);
				if (brick instanceof ScriptBrick) {
					break;
				}
			}

			clearCheckedBricksAndEnableButtons();
		}
	};

	private void copyBrick(Brick brick) {
		if (brick instanceof NestingBrick
				&& (brick instanceof AllowedAfterDeadEndBrick || brick instanceof DeadEndBrick)) {
			return;
		}

		if (brick instanceof ScriptBrick) {
			scriptToEdit = ((ScriptBrick) brick).getScriptSafe();

			Script clonedScript = scriptToEdit.copyScriptForSprite(sprite, sprite.getUserBrickList());

			sprite.addScript(clonedScript);
			adapter.initBrickList();
			adapter.notifyDataSetChanged();

			return;
		}

		int brickId = adapter.getBrickList().indexOf(brick);
		if (brickId == -1) {
			return;
		}

		int newPosition = adapter.getCount();

		try {
			Brick copiedBrick = brick.clone();

			Script scriptList = null;
			if (adapter.getUserBrick() != null) {
				scriptList = ProjectManager.getInstance().getCurrentUserBrick().getDefinitionBrick().getUserScript();
			}
			else {
				scriptList = ProjectManager.getInstance().getCurrentScript();
			}
			if (brick instanceof NestingBrick) {
				NestingBrick nestingBrickCopy = (NestingBrick) copiedBrick;
				nestingBrickCopy.initialize();

				for (NestingBrick nestingBrick : nestingBrickCopy.getAllNestingBrickParts(true)) {
					scriptList.addBrick((Brick) nestingBrick);
				}
			} else {
				scriptList.addBrick(copiedBrick);
			}

			adapter.addNewBrick(newPosition, copiedBrick, false);
			adapter.initBrickList();

			ProjectManager.getInstance().saveProject();
			adapter.notifyDataSetChanged();
		} catch (CloneNotSupportedException exception) {
			Log.e(getTag(), "Copying a Brick failed", exception);
			Toast.makeText(getActivity(), hku.fyp14017.blencode.R.string.error_copying_brick, Toast.LENGTH_SHORT).show();
		}
	}

	private void deleteBrick(Brick brick) {

		if (brick instanceof ScriptBrick) {
			scriptToEdit = ((ScriptBrick) brick).getScriptSafe();
			adapter.handleScriptDelete(sprite, scriptToEdit);
			return;
		}
		int brickId = adapter.getBrickList().indexOf(brick);
		if (brickId == -1) {
			return;
		}
		adapter.removeFromBrickListAndProject(brickId, true);
	}

	private void deleteCheckedBricks() {
		List<Brick> checkedBricks = adapter.getReversedCheckedBrickList();

		for (Brick brick : checkedBricks) {
			deleteBrick(brick);
		}
	}

	private void showConfirmDeleteDialog(boolean fromContextMenu) {
		this.deleteScriptFromContextMenu = fromContextMenu;
		int titleId;
		if ((deleteScriptFromContextMenu && scriptToEdit.getBrickList().size() == 0)
				|| adapter.getAmountOfCheckedItems() == 1) {
			titleId = hku.fyp14017.blencode.R.string.dialog_confirm_delete_brick_title;
		} else {
			titleId = hku.fyp14017.blencode.R.string.dialog_confirm_delete_multiple_bricks_title;
		}

		AlertDialog.Builder builder = new CustomAlertDialogBuilder(getActivity());
		builder.setTitle(titleId);
		builder.setMessage(hku.fyp14017.blencode.R.string.dialog_confirm_delete_brick_message);
		builder.setPositiveButton(hku.fyp14017.blencode.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				if (deleteScriptFromContextMenu) {
					adapter.handleScriptDelete(sprite, scriptToEdit);
				} else {
					deleteCheckedBricks();
					clearCheckedBricksAndEnableButtons();
				}
			}
		});
		builder.setNegativeButton(hku.fyp14017.blencode.R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();

			}
		});

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if (!deleteScriptFromContextMenu) {
					clearCheckedBricksAndEnableButtons();
				}
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	private void clearCheckedBricksAndEnableButtons() {
		setSelectMode(ListView.CHOICE_MODE_NONE);
		adapter.clearCheckedItems();

		setActionModeActive(false);

		registerForContextMenu(listView);
		BottomBar.showBottomBar(getActivity());
		adapter.setActionMode(false);
	}

	@Override
	public void onBrickChecked() {
		updateActionModeTitle();
		Utils.setSelectAllActionModeButtonVisibility(selectAllActionModeButton,
				adapter.getCount() > 0 && adapter.getAmountOfCheckedItems() != adapter.getCount());
	}

	private void updateActionModeTitle() {
		int numberOfSelectedItems = adapter.getAmountOfCheckedItems();

		String completeTitle;
		switch ((Integer) actionMode.getTag()) {
			case ACTION_MODE_COPY:
				completeTitle = getResources().getQuantityString(hku.fyp14017.blencode.R.plurals.number_of_bricks_to_copy,
						numberOfSelectedItems, numberOfSelectedItems);
				break;
			case ACTION_MODE_DELETE:
				completeTitle = getResources().getQuantityString(hku.fyp14017.blencode.R.plurals.number_of_bricks_to_delete,
						numberOfSelectedItems, numberOfSelectedItems);
				break;
			default:
				throw new IllegalArgumentException("Wrong or unhandled tag in ActionMode.");
		}

		int indexOfNumber = completeTitle.indexOf(' ') + 1;
		Spannable completeSpannedTitle = new SpannableString(completeTitle);
		completeSpannedTitle.setSpan(new ForegroundColorSpan(getResources().getColor(hku.fyp14017.blencode.R.color.actionbar_title_color)),
				indexOfNumber, indexOfNumber + String.valueOf(numberOfSelectedItems).length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		actionMode.setTitle(completeSpannedTitle);
	}

	@Override
	public void startBackPackActionMode() {

	}

}

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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.common.Constants;
import hku.fyp14017.blencode.common.SoundInfo;
import hku.fyp14017.blencode.io.StorageHandler;
import hku.fyp14017.blencode.ui.BackPackActivity;
import hku.fyp14017.blencode.ui.BottomBar;
import hku.fyp14017.blencode.ui.ScriptActivity;
import hku.fyp14017.blencode.ui.SoundViewHolder;
import hku.fyp14017.blencode.ui.adapter.SoundAdapter;
import hku.fyp14017.blencode.ui.adapter.SoundBaseAdapter;
import hku.fyp14017.blencode.ui.controller.BackPackListManager;
import hku.fyp14017.blencode.ui.controller.SoundController;
import hku.fyp14017.blencode.ui.dialogs.CustomAlertDialogBuilder;
import hku.fyp14017.blencode.ui.dialogs.DeleteSoundDialog;
import hku.fyp14017.blencode.ui.dialogs.RenameSoundDialog;
import hku.fyp14017.blencode.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.common.Constants;
import hku.fyp14017.blencode.common.SoundInfo;
import hku.fyp14017.blencode.ui.BackPackActivity;
import hku.fyp14017.blencode.ui.BottomBar;
import hku.fyp14017.blencode.ui.ScriptActivity;
import hku.fyp14017.blencode.ui.SoundViewHolder;
import hku.fyp14017.blencode.ui.adapter.SoundAdapter;
import hku.fyp14017.blencode.ui.adapter.SoundBaseAdapter;
import hku.fyp14017.blencode.ui.controller.BackPackListManager;
import hku.fyp14017.blencode.ui.dialogs.CustomAlertDialogBuilder;
import hku.fyp14017.blencode.ui.dialogs.DeleteSoundDialog;

public class SoundFragment extends ScriptActivityFragment implements SoundBaseAdapter.OnSoundEditListener,
		LoaderManager.LoaderCallbacks<Cursor>, Dialog.OnKeyListener {

	public static final String TAG = SoundFragment.class.getSimpleName();

	private static int selectedSoundPosition = Constants.NO_POSITION;

	private static String actionModeTitle;

	private static String singleItemAppendixDeleteActionMode;
	private static String multipleItemAppendixDeleteActionMode;

	private MediaPlayer mediaPlayer;
	private SoundBaseAdapter adapter;
	private ArrayList<SoundInfo> soundInfoList;
	private SoundInfo selectedSoundInfo;

	private ListView listView;

	private SoundDeletedReceiver soundDeletedReceiver;
	private SoundRenamedReceiver soundRenamedReceiver;
	private SoundCopiedReceiver soundCopiedReceiver;

	private SoundsListInitReceiver soundsListInitReceiver;

	private ActionMode actionMode;
	private View selectAllActionModeButton;

	private boolean isRenameActionMode;
	private boolean isResultHandled = false;

	private OnSoundInfoListChangedAfterNewListener soundInfoListChangedAfterNewListener;

	public void setOnSoundInfoListChangedAfterNewListener(OnSoundInfoListChangedAfterNewListener listener) {
		soundInfoListChangedAfterNewListener = listener;
	}

	private void setHandleAddbutton() {
		ImageButton addButton = (ImageButton) getSherlockActivity().findViewById(hku.fyp14017.blencode.R.id.button_add);
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				handleAddButton();
			}
		});

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(hku.fyp14017.blencode.R.layout.fragment_sounds, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		listView = getListView();
		registerForContextMenu(listView);

		if (savedInstanceState != null) {
			selectedSoundInfo = (SoundInfo) savedInstanceState
					.getSerializable(SoundController.BUNDLE_ARGUMENTS_SELECTED_SOUND);
		}
		soundInfoList = ProjectManager.getInstance().getCurrentSprite().getSoundList();

		adapter = new SoundAdapter(getActivity(), hku.fyp14017.blencode.R.layout.fragment_sound_soundlist_item,
				hku.fyp14017.blencode.R.id.fragment_sound_item_title_text_view, soundInfoList, false);

		adapter.setOnSoundEditListener(this);
		setListAdapter(adapter);
		((SoundAdapter) adapter).setSoundFragment(this);

		Utils.loadProjectIfNeeded(getActivity());
		setHandleAddbutton();

		// set adapter and soundInfoList for ev. unpacking
		BackPackListManager.getInstance().setCurrentSoundInfoList(soundInfoList);
		BackPackListManager.getInstance().setCurrentSoundAdapter(adapter);

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(hku.fyp14017.blencode.R.id.copy).setVisible(true);

		boolean visibility = false;
		if (false) {
			visibility = true;
		}
		menu.findItem(hku.fyp14017.blencode.R.id.backpack).setVisible(visibility);
		menu.findItem(hku.fyp14017.blencode.R.id.cut).setVisible(false);

		if (false && BackPackListManager.getInstance().getSoundInfoArrayList().size() > 0) {
			menu.findItem(hku.fyp14017.blencode.R.id.unpacking).setVisible(true);
		} else {
			menu.findItem(hku.fyp14017.blencode.R.id.unpacking).setVisible(false);

			StorageHandler.getInstance().clearBackPackSoundDirectory();
		}

		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(SoundController.BUNDLE_ARGUMENTS_SELECTED_SOUND, selectedSoundInfo);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		mediaPlayer = new MediaPlayer();
		initClickListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		setHandleAddbutton();

		if (!Utils.checkForExternalStorageAvailableAndDisplayErrorIfNot(getActivity())) {
			return;
		}

		if (soundRenamedReceiver == null) {
			soundRenamedReceiver = new SoundRenamedReceiver();
		}

		if (soundDeletedReceiver == null) {
			soundDeletedReceiver = new SoundDeletedReceiver();
		}

		if (soundCopiedReceiver == null) {
			soundCopiedReceiver = new SoundCopiedReceiver();
		}

		if (soundsListInitReceiver == null) {
			soundsListInitReceiver = new SoundsListInitReceiver();
		}

		IntentFilter intentFilterRenameSound = new IntentFilter(ScriptActivity.ACTION_SOUND_RENAMED);
		getActivity().registerReceiver(soundRenamedReceiver, intentFilterRenameSound);

		IntentFilter intentFilterDeleteSound = new IntentFilter(ScriptActivity.ACTION_SOUND_DELETED);
		getActivity().registerReceiver(soundDeletedReceiver, intentFilterDeleteSound);

		IntentFilter intentFilterCopySound = new IntentFilter(ScriptActivity.ACTION_SOUND_COPIED);
		getActivity().registerReceiver(soundCopiedReceiver, intentFilterCopySound);

		IntentFilter intentFilterSoundsListInit = new IntentFilter(ScriptActivity.ACTION_SOUNDS_LIST_INIT);
		getActivity().registerReceiver(soundsListInitReceiver, intentFilterSoundsListInit);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity()
				.getApplicationContext());

		setShowDetails(settings.getBoolean(SoundController.SHARED_PREFERENCE_NAME, false));

		SoundController.getInstance().handleAddButtonFromNew(this);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			SoundController.getInstance().handleAddButtonFromNew(this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		ProjectManager projectManager = ProjectManager.getInstance();
		if (projectManager.getCurrentProject() != null) {
			projectManager.saveProject();
		}
		SoundController.getInstance().stopSound(mediaPlayer, soundInfoList);
		adapter.notifyDataSetChanged();

		if (soundRenamedReceiver != null) {
			getActivity().unregisterReceiver(soundRenamedReceiver);
		}

		if (soundDeletedReceiver != null) {
			getActivity().unregisterReceiver(soundDeletedReceiver);
		}

		if (soundCopiedReceiver != null) {
			getActivity().unregisterReceiver(soundCopiedReceiver);
		}

		if (soundsListInitReceiver != null) {
			getActivity().unregisterReceiver(soundsListInitReceiver);
		}

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity()
				.getApplicationContext());
		SharedPreferences.Editor editor = settings.edit();

		editor.putBoolean(SoundController.SHARED_PREFERENCE_NAME, getShowDetails());
		editor.commit();

	}

	@Override
	public void onStop() {
		super.onStop();
		mediaPlayer.reset();
		mediaPlayer.release();
		mediaPlayer = null;
	}

	@Override
	public void startCopyActionMode() {
		if (actionMode == null) {
			SoundController.getInstance().stopSoundAndUpdateList(mediaPlayer, soundInfoList, adapter);
			actionMode = getSherlockActivity().startActionMode(copyModeCallBack);
			unregisterForContextMenu(listView);
			BottomBar.hideBottomBar(getActivity());
			isRenameActionMode = false;
		}

	}

	@Override
	public void startBackPackActionMode() {
		Log.d("TAG", "startBackPackActionMode");
		if (actionMode == null) {
			SoundController.getInstance().stopSoundAndUpdateList(mediaPlayer, soundInfoList, adapter);
			actionMode = getSherlockActivity().startActionMode(backPackModeCallBack);
			unregisterForContextMenu(listView);
			BottomBar.hideBottomBar(getActivity());
			isRenameActionMode = false;
		}

	}

	@Override
	public void startRenameActionMode() {
		if (actionMode == null) {
			SoundController.getInstance().stopSoundAndUpdateList(mediaPlayer, soundInfoList, adapter);
			actionMode = getSherlockActivity().startActionMode(renameModeCallBack);
			unregisterForContextMenu(listView);
			BottomBar.hideBottomBar(getActivity());
			isRenameActionMode = true;
		}
	}

	@Override
	public void startDeleteActionMode() {
		if (actionMode == null) {
			SoundController.getInstance().stopSoundAndUpdateList(mediaPlayer, soundInfoList, adapter);
			actionMode = getSherlockActivity().startActionMode(deleteModeCallBack);
			unregisterForContextMenu(listView);
			BottomBar.hideBottomBar(getActivity());
			isRenameActionMode = false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		//when new sound title is selected and ready to be added to the catroid project
		if (resultCode == Activity.RESULT_OK && requestCode == SoundController.REQUEST_SELECT_MUSIC && data != null) {
			Bundle arguments = new Bundle();
			arguments.putParcelable(SoundController.BUNDLE_ARGUMENTS_SELECTED_SOUND, data.getData());

			if (getLoaderManager().getLoader(SoundController.ID_LOADER_MEDIA_IMAGE) == null) {
				getLoaderManager().initLoader(SoundController.ID_LOADER_MEDIA_IMAGE, arguments, this);
			} else {
				getLoaderManager().restartLoader(SoundController.ID_LOADER_MEDIA_IMAGE, arguments, this);
			}
		}
		if (requestCode == SoundController.REQUEST_SELECT_MUSIC) {
			Log.d("SoundFragment", "onActivityResult RequestMusic");
			setHandleAddbutton();

		}

	}

	@Override
	public void onSoundPlay(View view) {
		SoundController.getInstance().handlePlaySoundButton(view, soundInfoList, mediaPlayer, adapter);
	}

	@Override
	public void onSoundPause(View view) {
		handlePauseSoundButton(view);
	}

	@Override
	public void onSoundChecked() {
		if (isRenameActionMode || actionMode == null) {
			return;
		}

		updateActionModeTitle();
		Utils.setSelectAllActionModeButtonVisibility(selectAllActionModeButton,
				adapter.getCount() > 0 && adapter.getAmountOfCheckedItems() != adapter.getCount());
	}

	private void updateActionModeTitle() {
		int numberOfSelectedItems = adapter.getAmountOfCheckedItems();

		if (numberOfSelectedItems == 0) {
			actionMode.setTitle(actionModeTitle);
		} else {
			String appendix = multipleItemAppendixDeleteActionMode;

			if (numberOfSelectedItems == 1) {
				appendix = singleItemAppendixDeleteActionMode;
			}


			String numberOfItems = Integer.toString(numberOfSelectedItems);
			String completeTitle = actionModeTitle + " " + numberOfItems + " " + appendix;

			int titleLength = actionModeTitle.length();

			Spannable completeSpannedTitle = new SpannableString(completeTitle);
			completeSpannedTitle.setSpan(
					new ForegroundColorSpan(getResources().getColor(hku.fyp14017.blencode.R.color.actionbar_title_color)), titleLength + 1,
					titleLength + (1 + numberOfItems.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			actionMode.setTitle(completeSpannedTitle);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arguments) {
		return SoundController.getInstance().onCreateLoader(id, arguments, getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		CopyAudioFilesTask task = new CopyAudioFilesTask();
		String audioPath = SoundController.getInstance().onLoadFinished(loader, data, getActivity());
		if (!audioPath.isEmpty()) {
			task.execute(audioPath);
			getLoaderManager().destroyLoader(SoundController.ID_LOADER_MEDIA_IMAGE);
		}

		getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_BRICK_LIST_CHANGED));

		isResultHandled = true;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	public void handlePauseSoundButton(View view) {
		final int position = (Integer) view.getTag();
		pauseSound(soundInfoList.get(position));
		adapter.notifyDataSetChanged();
	}

	public void pauseSound(SoundInfo soundInfo) {
		mediaPlayer.pause();
		soundInfo.isPlaying = false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);

		if (SoundController.getInstance().isSoundPlaying(mediaPlayer)) {
			SoundController.getInstance().stopSoundAndUpdateList(mediaPlayer, soundInfoList, adapter);
		}
		selectedSoundInfo = adapter.getItem(selectedSoundPosition);
		menu.setHeaderTitle(selectedSoundInfo.getTitle());
		adapter.addCheckedItem(((AdapterContextMenuInfo) menuInfo).position);

		getSherlockActivity().getMenuInflater().inflate(hku.fyp14017.blencode.R.menu.context_menu_default, menu);
		menu.findItem(hku.fyp14017.blencode.R.id.context_menu_copy).setVisible(true);
		menu.findItem(hku.fyp14017.blencode.R.id.context_menu_unpacking).setVisible(false);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case hku.fyp14017.blencode.R.id.context_menu_backpack:
				Intent intent = new Intent(getActivity(), BackPackActivity.class);
				intent.putExtra(BackPackActivity.EXTRA_FRAGMENT_POSITION, 2);
				intent.putExtra(BackPackActivity.BACKPACK_ITEM, true);
				BackPackListManager.setCurrentSoundInfo(selectedSoundInfo);
				BackPackListManager.getInstance().addSoundToActionBarSoundInfoArrayList(selectedSoundInfo);
				startActivity(intent);
				break;

			case hku.fyp14017.blencode.R.id.context_menu_copy:
				SoundInfo newSoundInfo = SoundController.getInstance().copySound(selectedSoundInfo, soundInfoList,
						adapter);
				updateSoundAdapter(newSoundInfo);

				break;

			case hku.fyp14017.blencode.R.id.context_menu_cut:
				break;

			case hku.fyp14017.blencode.R.id.context_menu_insert_below:
				break;

			case hku.fyp14017.blencode.R.id.context_menu_move:
				break;

			case hku.fyp14017.blencode.R.id.context_menu_rename:
				showRenameDialog();
				break;

			case hku.fyp14017.blencode.R.id.context_menu_delete:
				showConfirmDeleteDialog();
				break;
		}
		return super.onContextItemSelected(item);
	}

	private void updateSoundAdapter(SoundInfo newSoundInfo) {

		if (soundInfoListChangedAfterNewListener != null) {
			soundInfoListChangedAfterNewListener.onSoundInfoListChangedAfterNew(newSoundInfo);
		}

		//scroll down the list to the new item:
		final ListView listView = getListView();
		listView.post(new Runnable() {
			@Override
			public void run() {
				listView.setSelection(listView.getCount() - 1);
			}
		});

		if (isResultHandled) {
			isResultHandled = false;

			ScriptActivity scriptActivity = (ScriptActivity) getActivity();
			if (scriptActivity.getIsSoundFragmentFromPlaySoundBrickNew()
					&& scriptActivity.getIsSoundFragmentHandleAddButtonHandled()) {
				SoundController.getInstance().switchToScriptFragment(this);
			}
		}
	}

	@Override
	public void setSelectMode(int selectMode) {
		adapter.setSelectMode(selectMode);
		adapter.notifyDataSetChanged();
	}

	@Override
	public int getSelectMode() {
		return adapter.getSelectMode();
	}

	@Override
	public void setShowDetails(boolean showDetails) {
		// TODO CHANGE THIS!!! (was just a quick fix)
		if (adapter != null) {
			adapter.setShowDetails(showDetails);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public boolean getShowDetails() {
		// TODO CHANGE THIS!!! (was just a quick fix)
		return adapter != null && adapter.getShowDetails();
	}

	@Override
	public void handleAddButton() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("audio/*");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)	{
			disableGoogleDrive(intent);
		}
		startActivityForResult(Intent.createChooser(intent, getString(hku.fyp14017.blencode.R.string.sound_select_source)),
				SoundController.REQUEST_SELECT_MUSIC);
	}

	@TargetApi(19)
	private void disableGoogleDrive(Intent intent) {
		intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
	}

	@Override
	public void showRenameDialog() {
		RenameSoundDialog renameSoundDialog = RenameSoundDialog.newInstance(selectedSoundInfo.getTitle());
		renameSoundDialog.show(getFragmentManager(), RenameSoundDialog.DIALOG_FRAGMENT_TAG);
	}

	@Override
	protected void showDeleteDialog() {
		DeleteSoundDialog deleteSoundDialog = DeleteSoundDialog.newInstance(selectedSoundPosition);
		deleteSoundDialog.show(getFragmentManager(), DeleteSoundDialog.DIALOG_FRAGMENT_TAG);
	}

	private class SoundRenamedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScriptActivity.ACTION_SOUND_RENAMED)) {
				String newSoundTitle = intent.getExtras().getString(RenameSoundDialog.EXTRA_NEW_SOUND_TITLE);

				if (newSoundTitle != null && !newSoundTitle.equalsIgnoreCase("")) {
					selectedSoundInfo.setTitle(newSoundTitle);
					adapter.notifyDataSetChanged();
				}
			}
		}
	}

	private class SoundDeletedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScriptActivity.ACTION_SOUND_DELETED)) {
				adapter.notifyDataSetChanged();
				getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_BRICK_LIST_CHANGED));
			}
		}
	}

	private class SoundCopiedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ScriptActivity.ACTION_SOUND_COPIED)) {
				adapter.notifyDataSetChanged();
				getActivity().sendBroadcast(new Intent(ScriptActivity.ACTION_BRICK_LIST_CHANGED));
			}
		}
	}

	private void addSelectAllActionModeButton(ActionMode mode, Menu menu) {
		selectAllActionModeButton = Utils.addSelectAllActionModeButton(getLayoutInflater(null), mode, menu);
		selectAllActionModeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				for (int position = 0; position < soundInfoList.size(); position++) {
					adapter.addCheckedItem(position);
				}
				adapter.notifyDataSetChanged();
				onSoundChecked();
			}

		});
	}

	private ActionMode.Callback renameModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_SINGLE);
			mode.setTitle(hku.fyp14017.blencode.R.string.rename);

			setActionModeActive(true);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			((SoundAdapter) adapter).onDestroyActionModeRename(mode, listView);
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

			actionModeTitle = getString(hku.fyp14017.blencode.R.string.copy);
			singleItemAppendixDeleteActionMode = getString(hku.fyp14017.blencode.R.string.category_sound);
			multipleItemAppendixDeleteActionMode = getString(hku.fyp14017.blencode.R.string.sounds);

			mode.setTitle(actionModeTitle);
			addSelectAllActionModeButton(mode, menu);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {

			((SoundAdapter) adapter).onDestroyActionModeCopy(mode);

		}

	};

	private ActionMode.Callback backPackModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {

			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);
			setActionModeActive(true);

			actionModeTitle = getString(hku.fyp14017.blencode.R.string.backpack);
			singleItemAppendixDeleteActionMode = getString(hku.fyp14017.blencode.R.string.category_sound);
			multipleItemAppendixDeleteActionMode = getString(hku.fyp14017.blencode.R.string.sounds);

			mode.setTitle(actionModeTitle);
			addSelectAllActionModeButton(mode, menu);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {

			((SoundAdapter) adapter).onDestroyActionModeBackPack(mode);

		}

	};

	private ActionMode.Callback deleteModeCallBack = new ActionMode.Callback() {

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			setSelectMode(ListView.CHOICE_MODE_MULTIPLE);
			setActionModeActive(true);

			actionModeTitle = getString(hku.fyp14017.blencode.R.string.delete);
			singleItemAppendixDeleteActionMode = getString(hku.fyp14017.blencode.R.string.category_sound);
			multipleItemAppendixDeleteActionMode = getString(hku.fyp14017.blencode.R.string.sounds);

			mode.setTitle(hku.fyp14017.blencode.R.string.delete);
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
				clearCheckedSoundsAndEnableButtons();
			} else {
				showConfirmDeleteDialog();
			}
		}
	};

	private void initClickListener() {
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				selectedSoundPosition = position;
				return false;
			}
		});
	}

	private void showConfirmDeleteDialog() {
		int titleId;
		if (adapter.getAmountOfCheckedItems() == 1) {
			titleId = hku.fyp14017.blencode.R.string.dialog_confirm_delete_sound_title;
		} else {
			titleId = hku.fyp14017.blencode.R.string.dialog_confirm_delete_multiple_sounds_title;
		}

		AlertDialog.Builder builder = new CustomAlertDialogBuilder(getActivity());
		builder.setTitle(titleId);
		builder.setMessage(hku.fyp14017.blencode.R.string.dialog_confirm_delete_sound_message);
		builder.setPositiveButton(hku.fyp14017.blencode.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				SoundController.getInstance().deleteCheckedSounds(getActivity(), adapter, soundInfoList, mediaPlayer);
				clearCheckedSoundsAndEnableButtons();
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
				clearCheckedSoundsAndEnableButtons();
			}
		});

		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	public void clearCheckedSoundsAndEnableButtons() {
		setSelectMode(ListView.CHOICE_MODE_NONE);
		adapter.clearCheckedItems();

		actionMode = null;
		setActionModeActive(false);

		registerForContextMenu(listView);
		BottomBar.showBottomBar(getActivity());
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				ScriptActivity scriptActivity = (ScriptActivity) getActivity();
				if (scriptActivity.getIsSoundFragmentFromPlaySoundBrickNew()) {
					SoundController.getInstance().switchToScriptFragment(this);

					return true;
				}
			default:
				break;
		}
		return false;
	}

	public View getView(int position, View convertView) {
		SoundViewHolder holder;

		if (convertView == null) {
			convertView = View.inflate(getActivity(), hku.fyp14017.blencode.R.layout.fragment_sound_soundlist_item, null);

			holder = new SoundViewHolder();
			holder.playAndStopButton = (ImageButton) convertView.findViewById(hku.fyp14017.blencode.R.id.fragment_sound_item_image_button);
			holder.playAndStopButton.setImageResource(hku.fyp14017.blencode.R.drawable.ic_media_play);
			holder.playAndStopButton.setContentDescription(getString(hku.fyp14017.blencode.R.string.sound_play));

			holder.soundFragmentButtonLayout = (LinearLayout) convertView
					.findViewById(hku.fyp14017.blencode.R.id.fragment_sound_item_main_linear_layout);
			holder.checkbox = (CheckBox) convertView.findViewById(hku.fyp14017.blencode.R.id.fragment_sound_item_checkbox);
			holder.titleTextView = (TextView) convertView.findViewById(hku.fyp14017.blencode.R.id.fragment_sound_item_title_text_view);
			holder.timeSeparatorTextView = (TextView) convertView
					.findViewById(hku.fyp14017.blencode.R.id.fragment_sound_item_time_seperator_text_view);
			holder.soundFileSizePrefixTextView = (TextView) convertView
					.findViewById(hku.fyp14017.blencode.R.id.fragment_sound_item_size_prefix_text_view);
			holder.soundFileSizeTextView = (TextView) convertView.findViewById(hku.fyp14017.blencode.R.id.fragment_sound_item_size_text_view);

			holder.timePlayedChronometer = (Chronometer) convertView
					.findViewById(hku.fyp14017.blencode.R.id.fragment_sound_item_time_played_chronometer);

			convertView.setTag(holder);
		} else {
			holder = (SoundViewHolder) convertView.getTag();
		}
		SoundController controller = SoundController.getInstance();
		controller.updateSoundLogic(getActivity(), position, holder, adapter);

		return convertView;
	}

	public interface OnSoundInfoListChangedAfterNewListener {

		void onSoundInfoListChangedAfterNew(SoundInfo soundInfo);

	}

	public class CopyAudioFilesTask extends AsyncTask<String, Void, File> {
		private ProgressDialog progressDialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle(hku.fyp14017.blencode.R.string.loading);
			progressDialog.show();
		}

		@Override
		protected File doInBackground(String... path) {
			File file = null;
			try {
				file = StorageHandler.getInstance().copySoundFile(path[0]);
			} catch (IOException e) {
				Log.e("CATROID", "Cannot load sound.", e);
			}
			return file;
		}

		@Override
		protected void onPostExecute(File file) {
			progressDialog.dismiss();

			if (file != null) {
				String fileName = file.getName();
				String soundTitle = fileName.substring(fileName.indexOf('_') + 1, fileName.lastIndexOf('.'));
				SoundInfo newSoundInfo = SoundController.getInstance().updateSoundAdapter(soundTitle, fileName,
						soundInfoList, adapter);

				if (soundInfoListChangedAfterNewListener != null) {
					soundInfoListChangedAfterNewListener.onSoundInfoListChangedAfterNew(newSoundInfo);
				}

				//scroll down the list to the new item:
				final ListView listView = getListView();
				listView.post(new Runnable() {
					@Override
					public void run() {
						listView.setSelection(listView.getCount() - 1);
					}
				});

				if (isResultHandled) {
					isResultHandled = false;

					ScriptActivity scriptActivity = (ScriptActivity) getActivity();
					if (scriptActivity.getIsSoundFragmentFromPlaySoundBrickNew()
							&& scriptActivity.getIsSoundFragmentHandleAddButtonHandled()) {
						SoundController.getInstance().switchToScriptFragment(SoundFragment.this);
					}
				}
			} else {
				Utils.showErrorDialog(getActivity(), hku.fyp14017.blencode.R.string.error_load_sound);
			}
		}

	}

	public void setSelectedSoundInfo(SoundInfo selectedSoundInfo) {
		this.selectedSoundInfo = selectedSoundInfo;
	}

	public ArrayList<SoundInfo> getSoundInfoList() {
		return soundInfoList;
	}

	private class SoundsListInitReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ScriptActivity.ACTION_SOUNDS_LIST_INIT)) {
				adapter.notifyDataSetChanged();
			}
		}
	}
}

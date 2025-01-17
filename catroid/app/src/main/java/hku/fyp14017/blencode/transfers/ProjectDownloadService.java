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
package hku.fyp14017.blencode.transfers;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.common.Constants;
import hku.fyp14017.blencode.utils.DownloadUtil;
import hku.fyp14017.blencode.utils.UtilZip;
import hku.fyp14017.blencode.utils.Utils;
import hku.fyp14017.blencode.web.ConnectionWrapper;
import hku.fyp14017.blencode.web.ServerCalls;
import hku.fyp14017.blencode.web.WebconnectionException;
import hku.fyp14017.blencode.common.Constants;
import hku.fyp14017.blencode.utils.DownloadUtil;
import hku.fyp14017.blencode.utils.UtilZip;
import hku.fyp14017.blencode.web.ConnectionWrapper;
import hku.fyp14017.blencode.web.ServerCalls;
import hku.fyp14017.blencode.web.WebconnectionException;

public class ProjectDownloadService extends IntentService {

	public static final String TAG = ProjectDownloadService.class.getSimpleName();

	public static final String RECEIVER_TAG = "receiver";
	public static final String DOWNLOAD_NAME_TAG = "downloadName";
	public static final String URL_TAG = "url";
	public static final String ID_TAG = "notificationId";

	private static final String DOWNLOAD_FILE_NAME = "down" + Constants.CATROBAT_EXTENSION;

	private String projectName;
	private String zipFileString;
	private String url;
	Notification downloadNotification;
	PendingIntent pendingDownload;
	private Integer notificationId;
	public ResultReceiver receiver;
	private Handler handler;

	// mock object testing
	protected ConnectionWrapper createConnection() {
		return new ConnectionWrapper();
	}

	public ProjectDownloadService() {
		super(ProjectDownloadService.class.getSimpleName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean result = false;

		this.projectName = intent.getStringExtra(DOWNLOAD_NAME_TAG);
		this.zipFileString = Utils.buildPath(Constants.TMP_PATH, DOWNLOAD_FILE_NAME);
		this.url = intent.getStringExtra(URL_TAG);
		this.notificationId = intent.getIntExtra(ID_TAG, -1);

		receiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER_TAG);
		try {
			ServerCalls.getInstance().downloadProject(url, zipFileString, receiver, notificationId);
			result = UtilZip.unZipFile(zipFileString, Utils.buildProjectPath(projectName));
			Log.v(TAG, "url: " + url + ", zip-file: " + zipFileString + ", notificationId: " + notificationId);
		} catch (WebconnectionException webconnectionException) {
			Log.e(TAG, Log.getStackTraceString(webconnectionException));
		} finally {
			DownloadUtil.getInstance().downloadFinished(projectName);
		}

		if (!result) {
			showToast(hku.fyp14017.blencode.R.string.error_project_download);
			return;
		}

		showToast(hku.fyp14017.blencode.R.string.notification_download_finished);
	}

	private void showToast(final int messageId) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_SHORT).show();
			}
		});
	}
}

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
package hku.fyp14017.blencode;

import android.app.Application;
import android.util.Log;

import com.parrot.freeflight.settings.ApplicationSettings;
import com.parse.Parse;

public class CatroidApplication extends Application {

	private static final String TAG = CatroidApplication.class.getSimpleName();;

	private ApplicationSettings settings;
	public static final String OS_ARCH = System.getProperty("os.arch");;

	private static boolean parrotLibrariesLoaded = false;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "CatroidApplication onCreate");
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "eLVYhF8lasRdIv2iaUbYiu7g5yczFEEndwYw8ECM", "ekJo8KK0tivuPNT4O8O3nlfvxt2nFjO3wRi3LlWg");
		settings = new ApplicationSettings(this);
	}

	public ApplicationSettings getParrotApplicationSettings() {
		return settings;
	}

	public static synchronized boolean parrotNativeLibsAlreadyLoadedOrLoadingWasSucessful() {
		if (parrotLibrariesLoaded) {
			return parrotLibrariesLoaded;
		}

		if (!parrotLibrariesLoaded) { //Drone is deactivated in release builds for now 04.2014
			Log.d(TAG, "Current platform = \"" + OS_ARCH + "\"");
			if (OS_ARCH.startsWith("arm")) {
				Log.d(TAG, "We are on an arm platform load parrot native libs");
				try {
					System.loadLibrary("avutil");
					System.loadLibrary("swscale");
					System.loadLibrary("avcodec");
					System.loadLibrary("avfilter");
					System.loadLibrary("avformat");
					System.loadLibrary("avdevice");
					System.loadLibrary("adfreeflight");
				} catch (UnsatisfiedLinkError e) {
					Log.e(TAG, Log.getStackTraceString(e));
					return parrotLibrariesLoaded;
				}
				parrotLibrariesLoaded = true;
			} else {
				Log.d(TAG, "We are not on an arm based device, dont load libs");
			}
		}
		return parrotLibrariesLoaded;
	}
}

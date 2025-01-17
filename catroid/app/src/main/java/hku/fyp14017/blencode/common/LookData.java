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
package hku.fyp14017.blencode.common;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.utils.ImageEditing;
import hku.fyp14017.blencode.utils.Utils;

import java.io.FileNotFoundException;
import java.io.Serializable;

import hku.fyp14017.blencode.ProjectManager;
import hku.fyp14017.blencode.utils.ImageEditing;

public class LookData implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = LookData.class.getSimpleName();

	@XStreamAsAttribute
	private String name;
	private String fileName;
	private transient Bitmap thumbnailBitmap;
	private transient Integer width;
	private transient Integer height;
	private static final transient int THUMBNAIL_WIDTH = 150;
	private static final transient int THUMBNAIL_HEIGHT = 150;
	private transient Pixmap pixmap = null;
	private transient Pixmap originalPixmap = null;
	private transient TextureRegion region = null;

	@Override
	public LookData clone() {
		LookData cloneLookData = new LookData();

		cloneLookData.name = this.name;
		cloneLookData.fileName = this.fileName;
		String filePath = getPathToImageDirectory() + "/" + fileName;
		try {
			ProjectManager.getInstance().getFileChecksumContainer().incrementUsage(filePath);
		} catch (FileNotFoundException fileNotFoundexception) {
			Log.e(TAG, Log.getStackTraceString(fileNotFoundexception));
		}

		return cloneLookData;
	}

	public void resetLookData() {
		pixmap = null;
		originalPixmap = null;
		region = null;
	}

	public TextureRegion getTextureRegion() {
		if (region == null) {
			region = new TextureRegion(new Texture(getPixmap()));
		}
		return region;
	}

	public void setTextureRegion() {
		this.region = new TextureRegion(new Texture(getPixmap()));
	}

	public Pixmap getPixmap() {
		if (pixmap == null) {
			pixmap = new Pixmap(Gdx.files.absolute(getAbsolutePath()));
		}
		return pixmap;
	}

	public void setPixmap(Pixmap pixmap) {
		this.pixmap = pixmap;
	}

	public Pixmap getOriginalPixmap() {
		if (originalPixmap == null) {
			originalPixmap = new Pixmap(Gdx.files.absolute(getAbsolutePath()));
		}
		return originalPixmap;

	}

	public LookData() {
	}

	public String getAbsolutePath() {
		if (fileName != null) {
			return Utils.buildPath(getPathToImageDirectory(), fileName);
		} else {
			return null;
		}
	}

	public String getLookName() {
		return name;
	}

	public void setLookName(String name) {
		this.name = name;
	}

	public void setLookFilename(String fileName) {
		this.fileName = fileName;
	}

	public String getLookFileName() {
		return fileName;
	}

	public String getChecksum() {
		if (fileName == null) {
			return null;
		}
		return fileName.substring(0, 32);
	}

	private String getPathToImageDirectory() {
		return Utils.buildPath(Utils.buildProjectPath(ProjectManager.getInstance().getCurrentProject().getName()),
				Constants.IMAGE_DIRECTORY);
	}

	public Bitmap getThumbnailBitmap() {
		if (thumbnailBitmap == null) {
			thumbnailBitmap = ImageEditing.getScaledBitmapFromPath(getAbsolutePath(), THUMBNAIL_HEIGHT,
                    THUMBNAIL_WIDTH, ImageEditing.ResizeType.STAY_IN_RECTANGLE_WITH_SAME_ASPECT_RATIO, false);
		}
		return thumbnailBitmap;
	}

	public void resetThumbnailBitmap() {
		thumbnailBitmap = null;
	}

	public int[] getMeasure() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(getAbsolutePath(), options);
		width = options.outWidth;
		height = options.outHeight;

		return new int[] { width, height };
	}

	@Override
	public String toString() {
		return name;
	}
}

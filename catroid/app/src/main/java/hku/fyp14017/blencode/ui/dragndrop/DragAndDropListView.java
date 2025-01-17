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
package hku.fyp14017.blencode.ui.dragndrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

import hku.fyp14017.blencode.R;
import hku.fyp14017.blencode.common.ScreenValues;
import hku.fyp14017.blencode.content.bricks.Brick;
import hku.fyp14017.blencode.content.bricks.ScriptBrick;
import hku.fyp14017.blencode.ui.adapter.BrickAdapter;
import hku.fyp14017.blencode.utils.Utils;
import hku.fyp14017.blencode.common.ScreenValues;
import hku.fyp14017.blencode.content.bricks.Brick;
import hku.fyp14017.blencode.content.bricks.ScriptBrick;
import hku.fyp14017.blencode.utils.Utils;

public class DragAndDropListView extends ListView implements OnLongClickListener {

	private static final int SCROLL_SPEED = 25;
	public static final int WIDTH_OF_BRICK_PREVIEW_IMAGE = 400;
	private static final int DRAG_BACKGROUND_COLOR = Color.TRANSPARENT;

	private int maximumDragViewHeight;

	private int previousItemPosition;
	private int touchPointY;

	private int upperScrollBound;
	private int lowerScrollBound;
	private int upperDragBound;
	private int lowerDragBound;

	private ImageView dragView;
	private int position;
	private boolean newView;
	private int touchedListPosition;

	private boolean dimBackground;
	private boolean dragNewBrick;
	private boolean isScrolling;

	private long blinkAnimationTimestamp;

	private DragAndDropListener dragAndDropListener;

	public DragAndDropListView(Context context) {
		super(context);
	}

	public DragAndDropListView(Context context, AttributeSet attributes) {
		super(context, attributes);
	}

	public DragAndDropListView(Context context, AttributeSet attributes, int defStyle) {
		super(context, attributes, defStyle);
	}

	public void setOnDragAndDropListener(DragAndDropListener listener) {
		dragAndDropListener = listener;
	}

	public int getTouchedListPosition() {
		return touchedListPosition;
	}

	public void setInsertedBrick(int pos) {
		this.position = pos;
		newView = true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		//hack: on Android 2.x getView() is not always called when checkbox is checked.
		//Therefore the action is catched here and does exactly the same as otherwise the
		//onCheckedChangeListener would do
		if (event.getAction() == MotionEvent.ACTION_UP) {
			int x = (int) event.getX();
			int y = (int) event.getY();
			if (y < 0) {
				y = 0;
			} else if (y > getHeight()) {
				y = getHeight();
			}
			BrickAdapter adapter = ((BrickAdapter) dragAndDropListener);
			int itemPosition = pointToPosition(x, y);
			itemPosition = itemPosition < 0 ? adapter.getCount() - 1 : itemPosition;
			final Brick brick = (Brick) adapter.getItem(itemPosition);
			if (adapter.isActionMode() && brick instanceof ScriptBrick) {
				boolean checked = !brick.isChecked();
				brick.setCheckedBoolean(checked);
				brick.getCheckBox().setChecked(checked);

				if (!checked) {
					for (Brick currentBrick : adapter.getCheckedBricksFromScriptBrick((ScriptBrick) brick)) {
						currentBrick.setCheckedBoolean(false);
					}
				}
				adapter.handleCheck(brick, checked);
				brick.getView(adapter.getContext(), itemPosition, adapter);
				return true;
			}
		}

		if (dragAndDropListener != null && dragView != null) {
			onTouchEvent(event);
		}

		return super.onInterceptTouchEvent(event);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);

		if (dimBackground) {
			Rect rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStyle(Style.FILL);
			paint.setAlpha(128);

			canvas.drawRect(rect, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int x = (int) event.getX();
		int y = (int) event.getY();

		if (y < 0) {
			y = 0;
		} else if (y > getHeight()) {
			y = getHeight();
		}

		int itemPosition = pointToPosition(x, y);
		itemPosition = itemPosition < 0 ? ((BrickAdapter) dragAndDropListener).getCount() - 1 : itemPosition;

		if (touchedListPosition != itemPosition) {
			touchedListPosition = itemPosition;
			if (dragAndDropListener != null) {
				dragAndDropListener.setTouchedScript(touchedListPosition);
			}
		}

		if (dragAndDropListener != null && dragView != null) {
			int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					setDragViewAnimation(0);
					dragAndDropListener.drop();

					stopDragging();

					dimBackground = false;
					dragNewBrick = false;
					break;

				case MotionEvent.ACTION_MOVE:

					scrollListWithDraggedItem(y);

					dragTouchedListItem((int) event.getRawY());
					dragItemInList(y, itemPosition);

					dimBackground = true;
					break;
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {

		super.onSizeChanged(width, height, oldWidth, oldHeight);
		upperScrollBound = height / 6;
		lowerScrollBound = height * 5 / 6;
		maximumDragViewHeight = height / 2;
	}

	@Override
	public boolean onLongClick(View view) {
		int itemPosition = calculateItemPositionAndTouchPointY(view);

		boolean drawingCacheEnabled = view.isDrawingCacheEnabled();

		view.setDrawingCacheEnabled(true);

		view.measure(MeasureSpec.makeMeasureSpec(ScreenValues.SCREEN_WIDTH, MeasureSpec.EXACTLY), MeasureSpec
				.makeMeasureSpec(Utils.getPhysicalPixels(WIDTH_OF_BRICK_PREVIEW_IMAGE, getContext()),
						MeasureSpec.AT_MOST));
		view.layout(0, 0, ScreenValues.SCREEN_WIDTH, view.getMeasuredHeight());

		view.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
		view.buildDrawingCache(true);

		if (view.getDrawingCache() == null) {
			view.setDrawingCacheEnabled(drawingCacheEnabled);
			return false;
		}

		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(drawingCacheEnabled);

		startDragging(bitmap, touchPointY);

		if (!dragNewBrick) {
			setDragViewAnimation(0);
			dragNewBrick = false;
		}

		dragAndDropListener.drag(itemPosition, itemPosition);
		dimBackground = true;

		previousItemPosition = itemPosition;

		return true;
	}

	private void startDragging(Bitmap bitmap, int y) {
		stopDragging();

		if (bitmap.getHeight() > maximumDragViewHeight) {
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), maximumDragViewHeight);
		}

		ImageView imageView = getGlowingBorder(bitmap);

		WindowManager.LayoutParams dragViewParameters = createLayoutParameters();
		if (isScrolling) {
			isScrolling = false;

			dragViewParameters.y = getHeight() / 2 - bitmap.getHeight() / 2;
		} else {
			dragViewParameters.y = y - bitmap.getHeight() / 2;
		}
		dragViewParameters.windowAnimations = hku.fyp14017.blencode.R.style.brick_new;

		WindowManager windowManager = getWindowManager();

		windowManager.addView(imageView, dragViewParameters);

		dragView = imageView;
	}

	public ImageView getGlowingBorder(Bitmap bitmap) {
		ImageView imageView = new ImageView(getContext());
		imageView.setBackgroundColor(DRAG_BACKGROUND_COLOR);
		imageView.setId(hku.fyp14017.blencode.R.id.drag_and_drop_list_view_image_view);

		Bitmap glowingBitmap = Bitmap.createBitmap(bitmap.getWidth() + 30, bitmap.getHeight() + 30,
				Bitmap.Config.ARGB_8888);
		Canvas glowingCanvas = new Canvas(glowingBitmap);
		Bitmap alpha = bitmap.extractAlpha();
		Paint paintBlur = new Paint();
		paintBlur.setColor(Color.WHITE);
		glowingCanvas.drawBitmap(alpha, 15, 15, paintBlur);
		BlurMaskFilter blurMaskFilter = new BlurMaskFilter(15.0f, BlurMaskFilter.Blur.OUTER);
		paintBlur.setMaskFilter(blurMaskFilter);
		glowingCanvas.drawBitmap(alpha, 15, 15, paintBlur);
		paintBlur.setMaskFilter(null);
		glowingCanvas.drawBitmap(bitmap, 15, 15, paintBlur);

		imageView.setImageBitmap(glowingBitmap);

		return imageView;
	}

	private void dragTouchedListItem(int y) {
		WindowManager.LayoutParams dragViewParameters = (WindowManager.LayoutParams) dragView.getLayoutParams();
		dragViewParameters.y = y - dragView.getHeight() / 2;

		WindowManager windowManager = getWindowManager();
		try {
			windowManager.updateViewLayout(dragView, dragViewParameters);
		} catch (IllegalArgumentException e) {
			windowManager.addView(dragView, dragViewParameters);
		}
	}

	private void stopDragging() {
		if (dragView != null) {
			dragView.setVisibility(GONE);
			WindowManager windowManager = getWindowManager();
			windowManager.removeView(dragView);
			dragView.setImageDrawable(null);
			dragView = null;
		}
	}

	public void resetDraggingScreen() {
		stopDragging();

		dimBackground = false;
		dragNewBrick = false;

		invalidate();
	}

	private WindowManager.LayoutParams createLayoutParameters() {

		WindowManager.LayoutParams windowParameters = new WindowManager.LayoutParams();
		windowParameters.gravity = Gravity.TOP | Gravity.LEFT;

		windowParameters.height = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParameters.width = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParameters.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		windowParameters.format = PixelFormat.TRANSLUCENT;

		return windowParameters;
	}

	private WindowManager getWindowManager() {
		return (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
	}

	private int calculateItemPositionAndTouchPointY(View view) {
		int itemPosition = -1;
		int[] location = new int[2];

		if (newView) {
			itemPosition = this.position;
			View tempView = (getChildAt(getChildCount() - 1));
			if (tempView != null) {
				tempView.getLocationOnScreen(location);
				touchPointY = location[1] + (getChildAt(getChildCount() - 1)).getHeight();
			}
			newView = false;

		} else {
			itemPosition = pointToPosition(view.getLeft(), view.getTop());
			int visiblePosition = itemPosition - getFirstVisiblePosition();
			(getChildAt(visiblePosition)).getLocationOnScreen(location);
			touchPointY = location[1] + (getChildAt(visiblePosition)).getHeight() / 2;
		}

		return itemPosition;
	}

	private void scrollListWithDraggedItem(int y) {
		if (y > lowerScrollBound) {
			smoothScrollBy(SCROLL_SPEED, 0);
		} else if (y < upperScrollBound) {
			smoothScrollBy(-SCROLL_SPEED, 0);
		}
	}

	private void dragItemInList(int y, int itemPosition) {
		int index = previousItemPosition - getFirstVisiblePosition();

		if (index > 0) {
			View upperChild = getChildAt(index - 1);
			upperDragBound = upperChild.getBottom() - upperChild.getHeight() / 2;
		} else {
			upperDragBound = 0;
		}

		if (index < getChildCount() - 1) {
			View lowerChild = getChildAt(index + 1);
			lowerDragBound = lowerChild.getTop() + lowerChild.getHeight() / 2;
		} else {
			lowerDragBound = getHeight();
		}

		if ((y > lowerDragBound || y < upperDragBound)) {
			if (previousItemPosition != itemPosition) {
				dragAndDropListener.drag(previousItemPosition, itemPosition);
			}
			previousItemPosition = itemPosition;
		}
	}

	public void animateHoveringBrick() {
		if (dragView == null) {
			return;
		}

		WindowManager.LayoutParams dragViewParameters = (WindowManager.LayoutParams) dragView.getLayoutParams();

		long now = System.currentTimeMillis();
		if (blinkAnimationTimestamp < now) {
			dragViewParameters.windowAnimations = hku.fyp14017.blencode.R.style.brick_blink;
			getWindowManager().removeView(dragView);
			getWindowManager().addView(dragView, dragViewParameters);
			blinkAnimationTimestamp = now + 800;
		}
	}

	public boolean isCurrentlyDragging() {
		if (dragView == null) {
			return false;
		}

		return true;
	}

	private void setDragViewAnimation(int styleId) {
		WindowManager.LayoutParams dragViewParameters = (WindowManager.LayoutParams) dragView.getLayoutParams();
		dragViewParameters.windowAnimations = styleId;
		try {
			getWindowManager().updateViewLayout(dragView, dragViewParameters);
		} catch (IllegalArgumentException e) {
			getWindowManager().addView(dragView, dragViewParameters);
		}
	}

	public void setDraggingNewBrick() {
		dragNewBrick = true;
	}

	public void setIsScrolling() {
		isScrolling = true;
	}

}

package com.example.vigi.hotwordswall;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by Vigi on 2015/5/28.<br>
 * It shows a jumbled but no overlap word list wall.<br>
 * It has no cross pixel in vertical direction.
 */
public class HotWordsWall extends View {
	private static final String TAG = "HotWordsWall";
	private static final int MAX_WORD_LENGTH = 18;          // the max length of every word
	private static final int MAX_WORDS_NUM = 10;               //the max nums of words
	private static final float MAX_WORD_SP_SIZE = 35.0f;           //the max size of textSize
	private static final float MIN_WORD_SP_SIZE = 15.0f;        //the min ...
	private static final int[] COLOR_COLLECTION = { Color.BLACK,
													Color.DKGRAY,
													Color.GRAY,
//													Color.LTGRAY,
//													Color.WHITE,
													Color.RED,
													Color.GREEN,
													Color.BLUE,
													Color.YELLOW,
													Color.CYAN,
													Color.MAGENTA};             //choose color from this collection randomly for every word

	private List<String> hotWordsData;
	private List<WordInfo> wordsInfo;

	private Random randomUtil;
	private GestureDetector onClickDetector;
	private Paint painter;
	//	private Paint rectPainter;
	private int height;
	private int width;
	private boolean drawFinished = false;

	private OnWordClickListener listener;

	public HotWordsWall(Context context) {
		super(context);init();
	}

	public HotWordsWall(Context context, AttributeSet attrs) {
		super(context, attrs);init();
	}

	public HotWordsWall(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);init();
	}

	private void init() {
		if (isInEditMode()) return;
		randomUtil = new Random();
		onClickDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if (wordsInfo == null || listener == null || !drawFinished) return false;
				for (WordInfo wordInfo : wordsInfo) {
					if (wordInfo.r.contains((int)e.getX(), (int)e.getY())) {
						listener.onClick(wordInfo.data);
						return true;
					}
				}
				return false;
			}
		});

		painter = new Paint(Paint.ANTI_ALIAS_FLAG);
		painter.setShadowLayer(10, 5, 5, Color.GRAY);
//		rectPainter = new Paint(Paint.ANTI_ALIAS_FLAG);
//		rectPainter.setStyle(Paint.Style.STROKE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (wordsInfo == null) return;
		for (WordInfo wordInfo : wordsInfo) {
			drawEachWord(canvas, wordInfo);
		}
	}

	private void drawEachWord(Canvas canvas, WordInfo wi) {
		painter.setColor(wi.color);
		painter.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, wi.size, getResources().getDisplayMetrics()));
		canvas.drawText(wi.data, wi.r.left, wi.r.bottom, painter);

//		canvas.drawRect(wi.r, rectPainter);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
		width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
	}

	public void setOnWordClickListener(OnWordClickListener l) {
		listener = l;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean b = super.onTouchEvent(event);
		if (b) return true;
		return onClickDetector.onTouchEvent(event);
	}

	public void hangWordsTo(@NonNull List<String> h) {
		if (h.size() == 0 || h.size() > MAX_WORDS_NUM)
			throw new RuntimeException("wrong list size! It must be in (0, " + MAX_WORDS_NUM + "]");
		drawFinished = false;
		hotWordsData = h;
		generateData();
		invalidate();
		drawFinished = true;
	}


	/**
	 * the core method of this class.<br>
	 * but this one has some problem.<br>
	 * especially the {@link #evaluateRandArrayFirst} method.<br>
	 * who can help me improve it ~~   TAT
	 */
	private void generateData() {
//		Log.e(TAG, "container-->width=" + width + ", height=" + height);
		List<WordInfo> c = new ArrayList<>(MAX_WORDS_NUM);
		int totalHeight = 0;
		for (String s : hotWordsData) {
			String trimStr = s.trim();
			if (trimStr.length() <= 0)
				throw new RuntimeException("list can not contain empty string");
			if (trimStr.length() > MAX_WORD_LENGTH)
				throw new RuntimeException("\"" + trimStr + "\" is too long to " + MAX_WORD_LENGTH);

			WordInfo wi = new WordInfo();
			//generate each color and set data
			wi.color = COLOR_COLLECTION[randomUtil.nextInt(COLOR_COLLECTION.length)];
			wi.data = trimStr;
			//generate each size
			float maxSize = calculateCroppedTextSize(trimStr, width, height / hotWordsData.size() * 2);
			if (maxSize < MIN_WORD_SP_SIZE)
				Log.w(TAG, "can not be here. reduce the MIN_WORD_SP_SIZE value!");
			wi.size = MIN_WORD_SP_SIZE + randomUtil.nextFloat() * (maxSize - MIN_WORD_SP_SIZE);
			//calculate width and height
			painter.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, wi.size, Resources.getSystem().getDisplayMetrics()));
			painter.getTextBounds(trimStr, 0, trimStr.length(), wi.r);
			totalHeight += wi.r.height();

			c.add(wi);
//			Log.e(TAG, "no location-->" + wi.toString());
		}
		//randomize order
		Collections.sort(c);
		//evaluate each top and left
		int totalSpaceHeight = height - totalHeight;
		if (totalSpaceHeight < 0)
			Log.w(TAG, "can not be here. reduce the MAX_WORD_SP_SIZE value!");
		int[] splitSpaceArray = new int[c.size() + 1];
		evaluateRandArrayFirst(splitSpaceArray, 0, splitSpaceArray.length - 1, totalSpaceHeight, 2, totalSpaceHeight / (c.size() + 1) * 2);
		int thisTop = splitSpaceArray[0] + getPaddingTop();
		for (int i = 0; i < c.size(); ++i) {
			WordInfo wi = c.get(i);
			int offsetX = getPaddingLeft() + randomUtil.nextInt(width - wi.r.width()) - wi.r.left;
			int offsetY = thisTop - wi.r.top;
			wi.r.offset(offsetX, offsetY);
			thisTop += wi.r.height() + splitSpaceArray[i + 1];

//			Log.e(TAG, wi.toString());
		}

		wordsInfo = c;
	}

	private void evaluateRandArrayFirst(int[] array, int start, int end, int total, int each_min, int each_max) {
		int loopCount = 0;
		while (loopCount++ < 10) {
			if (evaluateRandArray(array, start, end, total, each_min, each_max))
				return;
			Log.e(TAG, "Calculate again!");
		}
		throw new RuntimeException("It might be some error!");
	}

	private boolean evaluateRandArray(int[] array, int start, int end, int total, int each_min, int each_max) {
//		Log.e(TAG, "start=" + start + ", end=" + end + ", total=" + total + ", each_min=" + each_min + ", each_max=" + each_max);
		if (total < each_min) return false;
		if (end == start) {   //last one
			if (total > each_max) return false;
			array[start] = total;
//			Log.e(TAG, "array[" + start + "]=" + total);
			return true;
		}
		int value = (int) (each_min + randomUtil.nextFloat() * (each_max - each_min + 1));    // value = [each_min, each_max]
		if (!evaluateRandArray(array, start + 1, end, total - value, each_min, each_max)) return false;

		array[start] = value;
//		Log.e(TAG, "array[" + start + "]=" + value);
		return true;
	}

	/**
	 * calculate the max text size to fit in the region(maxWidth, maxHeight)
	 */
	private float calculateCroppedTextSize(String text, int maxWidth, int maxHeight) {
		float result = MAX_WORD_SP_SIZE + 1;
		boolean isFit = false;
		Rect r = new Rect();
		while (!isFit) {
			result = result -1;
			painter.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, result, Resources.getSystem().getDisplayMetrics()));
			painter.getTextBounds(text, 0, text.length(), r);
			if (r.width() > maxWidth) continue;
			if (r.height() > maxHeight) continue;
			isFit = true;
		}
		return result;
	}


	private class WordInfo implements Comparable {
		Rect r = new Rect();
		float size;
		int color;
		String data;

		@Override
		public String toString() {
			return "WordInfo{" +
					"r=" + r +
					", width=" + r.width() +
					", height=" + r.height() +
					", size=" + size +
					", color=" + color +
					", data='" + data + '\'' +
					'}';
		}

		@Override
		public int compareTo(Object another) {
			return randomUtil.nextBoolean() ? 1 : -1;
		}
	}

	public interface OnWordClickListener {
		void onClick(String word);
	}
}

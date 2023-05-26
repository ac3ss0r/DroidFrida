package acessor.droidfrida;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeView extends EditText {

	private Paint lineNumberPaint;

	public CodeView(Context context) {
		super(context);
		init();
	}

	public CodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CodeView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		lineNumberPaint = new Paint();
		lineNumberPaint.setColor(Color.WHITE);
		lineNumberPaint.setTextSize(getTextSize());
		lineNumberPaint.setAntiAlias(true);
		addTextChangedListener(new TextWatcher() {

				@Override
				public void beforeTextChanged(CharSequence text, int start, int count, int after) { }

				@Override
				public void onTextChanged(CharSequence text, int start, int before, int count) {}


				@Override
				public void afterTextChanged(Editable s) {
					removeSpans(s);
					highlightJavaScript(s);
				}

			});
	}

	void removeSpans(Editable e) {
		for (ForegroundColorSpan old : e.getSpans(0, e.length(), ForegroundColorSpan.class))
			e.removeSpan(old);
	}

	private int getTextWidth(String text) {
		Paint paint = new Paint();
		paint.setTypeface(getTypeface());
		paint.setTextSize(getTextSize());
		Rect rect = new Rect();
		paint.getTextBounds(text, 0, text.length(), rect);
		int widthPixels = rect.width();
		return widthPixels;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int lineCount = getLineCount();
		int lineHeight = getLineHeight();
		int scrollX = getScrollX();
		float lineNumberX = 10; // draw start
		float lineNumberY;

		int borderX = getTextWidth(Integer.toString(lineCount)) 
			+ (int)lineNumberX + 20;
		for (int i = 0; i < lineCount; i++) {
			lineNumberY = lineHeight * (i + 1) - (lineHeight / 2) 
				+ lineHeight;
			canvas.drawText(String.valueOf(i + 1), lineNumberX + scrollX, lineNumberY, lineNumberPaint);
		}
		setPaddingRelative(borderX + 10, getPaddingTop(), 0, 0);	
		canvas.drawLine(borderX + scrollX, 0, borderX + scrollX, 
						getHeight() + getScrollY(), getPaint());
		super.onDraw(canvas);
	}

	private void highlightJavaScript(Editable s) {
		highlightKeywords(s, "var|let|const|function|return|if|else|for|while|do|switch|case|break|new|this|class", "#78A8E0");
		highlightKeywords(s, "true|false|null|undefined", "#4080B8");
		highlightKeywords(s, "/\\*(?:.|[\\n\\r])*?\\*/|//.*", "#9e9e9e");
		highlightKeywords(s, "\\b([0-9]+)\\b", "#4080B8");
		highlightKeywords(s, "\"(.*?)\"|'(.*?)", "#507840");
	}

	private void highlightKeywords(Editable spannable, String pattern, String color) {
		Pattern keywordPattern = Pattern.compile(pattern);
		Matcher keywordMatcher = keywordPattern.matcher(spannable);
		while (keywordMatcher.find()) {
			spannable.setSpan(new ForegroundColorSpan(Color.parseColor(color)), keywordMatcher.start(), keywordMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
}

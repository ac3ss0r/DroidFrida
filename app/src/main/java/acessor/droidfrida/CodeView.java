package acessor.droidfrida;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.Toast;
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

  /* a custom hashsum method is used to avoid
   reallocating large strings which may cause lag
   */
  public long hashsum(String str) {
    final int prime = 31;
    long hash = 0;
    long multiplier = 1;
    for (int i = 0; i < str.length(); i++) {
	  hash = hash + (str.charAt(i) - 'a' + 1) * multiplier;
	  multiplier = multiplier * prime;
    }
    return hash;
  }

  private void init() {
	lineNumberPaint = new Paint();
	lineNumberPaint.setColor(Color.WHITE);
	lineNumberPaint.setTextSize(getTextSize());
	lineNumberPaint.setAntiAlias(true);
	addTextChangedListener(new TextWatcher() {

		private long beforeHashsum = -1;

		@Override
		public void beforeTextChanged(CharSequence text, int start, int count, int after) {
		  this.beforeHashsum = hashsum(getText().toString());
		}

		@Override
		public void onTextChanged(CharSequence text, int start, int before, int count) {}

		@Override
		public void afterTextChanged(Editable s) {
		  if (beforeHashsum != hashsum(getText().toString())) {
			int cpos = getSelectionStart(),
			  scrollY = getScrollY(),
			  scrollX = getScrollX();
			highlightJavaScript(s.toString());
			setSelection(cpos);
			scrollTo(scrollX, scrollY);
		  }
		}
	  });
  }

  private int getTextWidth(String text) {
    Paint paint = new Paint();
    paint.setTypeface(getTypeface());
    paint.setTextSize(getTextSize());
    Rect rect = new Rect();
    paint.getTextBounds(text, 0, text.length(), rect);
    int widthPixels = rect.width();
    //DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
    int widthDp = widthPixels;
    return widthDp; 
  }

  @Override
  protected void onDraw(Canvas canvas) {
	int lineCount = getLineCount();
	int lineHeight = getLineHeight();
	float lineNumberX = 10; // draw start
	float lineNumberY;

	int borderX = getTextWidth(Integer.toString(getLineCount())) 
	  + (int)lineNumberX + 20;

	for (int i = 0; i < lineCount; i++) {
	  lineNumberY = lineHeight * (i + 1) - (lineHeight / 2) + getScrollY() 
		+ lineHeight;
	  canvas.drawText(String.valueOf(i + 1), lineNumberX, lineNumberY, lineNumberPaint);
	}

	setPaddingRelative(borderX + 10, 
					   getPaddingTop(), 0, 0);

	canvas.drawLine(borderX, 0, borderX, getHeight(), getPaint());

	super.onDraw(canvas);
  }

  private void highlightJavaScript(String text) {
	Spannable spannable = new SpannableString(text);
	/*for (ForegroundColorSpan old : spannable.getSpans(0, spannable.length(), ForegroundColorSpan.class))
	  spannable.removeSpan(old);*/
	highlightKeywords(spannable, "var|let|const|function|return|if|else|for|while|do|switch|case|break");
	highlightKeywords(spannable, "true|false|null|undefined");
	highlightKeywords(spannable, "new|this|class");
	highlightStrings(spannable);
	highlightNumbers(spannable);

	setText(spannable);
  }

  private void highlightKeywords(Spannable spannable, String pattern) {
	Pattern keywordPattern = Pattern.compile("\\b(" + pattern + ")\\b");
	Matcher keywordMatcher = keywordPattern.matcher(spannable);
	while (keywordMatcher.find()) {
	  spannable.setSpan(new ForegroundColorSpan(Color.BLUE), keywordMatcher.start(), keywordMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
  }

  private void highlightStrings(Spannable spannable) {
	Pattern stringPattern = Pattern.compile("(\".*\")|('.*')");
	Matcher stringMatcher = stringPattern.matcher(spannable);
	while (stringMatcher.find()) {
	  spannable.setSpan(new ForegroundColorSpan(Color.MAGENTA), stringMatcher.start(), stringMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
  }

  private void highlightNumbers(Spannable spannable) {
	Pattern numberPattern = Pattern.compile("\\b[0-9]+\\b");
	Matcher numberMatcher = numberPattern.matcher(spannable);
	while (numberMatcher.find()) {
	  spannable.setSpan(new ForegroundColorSpan(Color.RED), numberMatcher.start(), numberMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}
  }
}

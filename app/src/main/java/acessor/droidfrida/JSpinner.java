package acessor.droidfrida;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class JSpinner extends Spinner {
  OnItemSelectedListener listener;

  public JSpinner(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void setSelection(int position) {
    super.setSelection(position);
    if (listener != null)
	  listener.onItemSelected(null, null, position, 0);
  }

  public void setOnItemSelectedEvenIfUnchangedListener(
	OnItemSelectedListener listener) {
    this.listener = listener;
  }
}

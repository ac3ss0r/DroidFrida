package acessor.droidfrida;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

/* The default spinner doesnt fire its events 
when the selection is made, thats a bug in 
standard android library. To fix that this
class was made
*/

public class FixedSpinner extends Spinner {
  OnItemSelectedListener listener;

  public FixedSpinner(Context context, AttributeSet attrs) {
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

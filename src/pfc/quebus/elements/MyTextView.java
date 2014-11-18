package pfc.quebus.elements;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/* Clase texto con sus constructores*/
public class MyTextView extends TextView{
	
	public MyTextView(Context context) {
		super(context);
	}
	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/*Este método desactiva los eventos de accesibilidad*/
	@Override
	public void sendAccessibilityEvent(int eventType) {
	}
	

	
	
}
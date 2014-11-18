package pfc.quebus.elements;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/* Clase lista con sus constructores*/
public class MyListView extends ListView{
	
	public MyListView(Context context) {
		super(context);
	}
	
	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/*Este método desactiva los eventos de accesibilidad*/
	@Override
	   public void sendAccessibilityEvent(int eventType) {
	}	
	
}
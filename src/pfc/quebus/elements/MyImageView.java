package pfc.quebus.elements;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/* Clase imagen con sus constructores*/
public class MyImageView extends ImageView{
	
	public MyImageView(Context context) {
		super(context);
	}
	public MyImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	   /*Este método desactiva los eventos de accesibilidad*/
	@Override
	public void sendAccessibilityEvent(int eventType) {

	}
	

	
	
}
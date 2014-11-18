package pfc.quebus.elements;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/* Clase bot�n con sus constructores*/
public class MyButton extends Button{

	    public MyButton(Context context) {
	        super(context);
	    }

	   public MyButton(Context context, AttributeSet attrs) {
	      super(context, attrs);
	   }

	   public MyButton(Context context, AttributeSet attrs, int defStyle) {
	      super(context, attrs, defStyle);
	   }

	   /*Este m�todo desactiva los eventos de accesibilidad*/
	   @Override
	   public void sendAccessibilityEvent(int eventType) {
		   
	   } 
	   
}
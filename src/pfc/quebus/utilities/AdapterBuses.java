package pfc.quebus.utilities;

import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.quebus.R;
import pfc.quebus.elements.*;


public class AdapterBuses extends BaseAdapter{
	
	protected Activity activity;
    protected ArrayList<Bus> buses;
    
    /* Constructor al que se le pasa la Activity y el array de autobuses*/
    public AdapterBuses(Activity activity, ArrayList<Bus> buses) {
        this.activity = activity;
        this.buses = buses;
      }
    
    /* M�todo que devuelve el n�mero de elementos */
	@Override
	public int getCount() {
		return buses.size();
	}
	
	/* M�todo que devuelve un elemento*/
	@Override
	public Object getItem(int arg0) {
		return buses.get(arg0);
	}
	
	/* M�todo que devuelve el id de un elemento */
	@Override
	public long getItemId(int position) {
		return buses.get(position).getId();
	}
	
	/* Aqu� se define el adapter */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Generamos una convertView por motivos de eficiencia
        View v = convertView;
 
        //Asociamos el layout de la lista que hemos creado
        if(convertView == null){
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.listitem, null);
        }
 
        // Creamos un objeto directivo
        Bus bus = buses.get(position);
        //Rellenamos las caracter�sticas
        MyImageView img = (MyImageView) v.findViewById(R.id.busImg);
        img.setBackgroundColor(Color.parseColor(bus.getColor()));
        MyTextView line = (MyTextView) v.findViewById(R.id.linea);
        line.setText("L�nea "+bus.getIdLinea()+" - "+bus.getMin()+" minutos");

 
        // Retornamos la vista
        return v;
	}
	
}
package pfc.quebus.activities;

import com.example.quebus.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Help extends Activity{
	
	ListView lv;
	
	/* M�todo que se ejecuta cuando la activity est� creada*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
		lv = (ListView)findViewById(R.id.listView1);
		
		//Creamos los Items de la lista
		final String[] list_data = new String[]{"La aplicaci�n se compone de 4 pantallas, en todas habr� que dar un click para fijar la pantalla y luego habr� dos simples gestos, doble click y doble click manteniendo pulsado.","La primera es una imagen con el nombre de la aplicaci�n. Con el doble click iniciaremos la b�squeda.","La segunda es una lista con las l�neas que pasar�n pr�ximamente por la parada en la que estamos. Con doble click volveremos a buscar y con doble click manteniendo pulsado se abrir� el reconocedor de voz y diremos la l�nea deseada.","La tercera nos informa del tiempo que queda para que la l�nea elegida llegue a la parada. Se actualizar� autom�ticamente. Con doble click volveremos a empezar y con doble click manteniendo pulsado actualizaremos manualmente el tiempo.","La cuarta pantalla se abrir� cuando el autob�s haya llegado a nuestra parada y nos informar� de cu�nto queda para la pr�xima parada y cu�l es. Se actualizar� autom�ticamente. Como siempre, con doble click volveremos a empezar y con doble click manteniendo pulsado actualizaremos manualmente.", "Si hay problemas con la voz o el reconocedor, en ajustes hay una opci�n que lleva al Play Store para instalar el paquete necesario", "Informaci�n proporcionada por el Ayuntamiento de Gij�n"};
	
		// Creamos el adaptador asignando tambien el dise�o grafico
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.helplistitem, list_data);
		 
		// Asignamos el adaptador al control lista
		lv.setAdapter(adapter);
	
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
}
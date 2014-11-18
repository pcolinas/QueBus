package pfc.quebus.activities;

import com.example.quebus.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Help extends Activity{
	
	ListView lv;
	
	/* Método que se ejecuta cuando la activity está creada*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
		lv = (ListView)findViewById(R.id.listView1);
		
		//Creamos los Items de la lista
		final String[] list_data = new String[]{"La aplicación se compone de 4 pantallas, en todas habrá que dar un click para fijar la pantalla y luego habrá dos simples gestos, doble click y doble click manteniendo pulsado.","La primera es una imagen con el nombre de la aplicación. Con el doble click iniciaremos la búsqueda.","La segunda es una lista con las líneas que pasarán próximamente por la parada en la que estamos. Con doble click volveremos a buscar y con doble click manteniendo pulsado se abrirá el reconocedor de voz y diremos la línea deseada.","La tercera nos informa del tiempo que queda para que la línea elegida llegue a la parada. Se actualizará automáticamente. Con doble click volveremos a empezar y con doble click manteniendo pulsado actualizaremos manualmente el tiempo.","La cuarta pantalla se abrirá cuando el autobús haya llegado a nuestra parada y nos informará de cuánto queda para la próxima parada y cuál es. Se actualizará automáticamente. Como siempre, con doble click volveremos a empezar y con doble click manteniendo pulsado actualizaremos manualmente.", "Si hay problemas con la voz o el reconocedor, en ajustes hay una opción que lleva al Play Store para instalar el paquete necesario", "Información proporcionada por el Ayuntamiento de Gijón"};
	
		// Creamos el adaptador asignando tambien el diseño grafico
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.helplistitem, list_data);
		 
		// Asignamos el adaptador al control lista
		lv.setAdapter(adapter);
	
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
}
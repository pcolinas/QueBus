package pfc.quebus.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.example.quebus.R;
import pfc.quebus.elements.*;
import pfc.quebus.utilities.*;


public class GetInfo extends Activity implements OnInitListener{
	
	/*Declaración de variables*/
	protected static final int RESULT_SPEECH = 1;
	
	static int stop = -1; //Inicialización de la variable en la que se guardará el índice de la parada más cercana
	private double lat, lng; //Variables para almacenar la latitud y la longitud de la posición del terminal 
	
	
	String liveInfoURL = "http://datos.gijon.es/doc/transporte/busgijontr.json";
	static String nearStop;
	String noBuses = "No hay autobuses próximamente";
	String stopName;
	
	ArrayList<Bus> buses;
	static ArrayList<String> lines;
	
	static TextToSpeech tts;
	private Locale loc = new Locale("es", "ES"); //Idioma español de España para el tts
	
	MyListView list;
	TextView text;
	RelativeLayout rl;
	MyTextView noBus;
	
	String langPackageName = "com.svox.langpack.installer";
	
	boolean hoverMove = false;
	int hoverCheck = 0;
	SpeechRecognizer sr;
	
	boolean backDestroy = true;
	
	
	/* Método que se ejecuta cuando la activity está creada*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.getinfo);
		
		rl = (RelativeLayout)findViewById(R.id.layout);
		text = (TextView)findViewById(R.id.parada);
		list = (MyListView)findViewById(R.id.listView1);
		noBus = (MyTextView)findViewById(R.id.nobus);
		
		buses = new ArrayList<Bus>();	//Array para almacenar los próximos autobuses
		lines  = new ArrayList<String>();	//Array para almacenar los String a mostrar
		
		CheckConnection conn = new CheckConnection(this);
		GetLocation gps = new GetLocation(this); //Creación del objeto gps para la localización
		//Se comprueba que haya conexión GPS
		if(!gps.canGetLocation()) {
            //Mensaje informativo
			nearStop = "Se ha perdido la conexión GPS, inténtelo más tarde";
			tts = new TextToSpeech(GetInfo.this, GetInfo.this); //Informamos de ello
			/* Ponemos el mensaje en la pantalla */
			text.setText("Parada");
			text.setContentDescription("Parada");
			noBus.setText(nearStop);
			activateGestures();	
        }else{
							       
	        lat = gps.getLatitude();
	        lng = gps.getLongitude();
	        
	        gps.stopUsingGPS();
	        
	        try {
	        		stop = getCloserStop(lat, lng); //Búsqueda del índice de la parada más cercana
	
	 			
	 			if(MainActivity.busInfo != null){
	 				/*Si hemos podido descargar la información obtenemos el nombre de la parada más cercana*/
	 				stopName = MainActivity.busInfo.getJSONObject(stop).getString("descripcion");
	 				text.setText(stopName);
	 				text.setContentDescription(stopName);
	 				nearStop = "Parada: "+stopName+"\n";
	 				
	 				if(!conn.isConnectedToInternet()) {
	 		            //Mensaje informativo
	 					nearStop = "Se ha perdido la conexión a internet, inténtelo más tarde";
		 				tts = new TextToSpeech(GetInfo.this, GetInfo.this); //Informamos de ello
		 				activateGestures();	
	 		        }else{
		 				/*Hacemos la petición de la información en vivo*/
		 	 			URLReader liveInfo = new URLReader();
		 	 			liveInfo.execute(liveInfoURL/*, "llegada"*/);
	 		        }
	 	 			
	 			}else{
	 				/*Si no, lo indicamos*/
	 				text.setText("La información no está disponible");
	 				nearStop = "La información no está disponible";
	 				tts = new TextToSpeech(GetInfo.this, GetInfo.this); //Informamos de ello
	 				activateGestures();	
	 			}
	 			
	 			
	        } catch (JSONException e) {
	 			e.printStackTrace();
	 		}
        
        }	  
		
	}
	
	/*Método que reabre la activity para nueva búsqueda*/	
	public void restart(){
		
		backDestroy = false;
		stopTTS();
		Intent i = new Intent();
		i.setClass(GetInfo.this, GetInfo.class);
		startActivity(i);
		finish(); //Se cierra la pantalla actual
	}
	
	/*Clase asíncrona que hace la petición JSON*/
	public class URLReader extends AsyncTask<String, Void, JSONObject>{
		String prueba = null;
		JSONArray liveInfo;
		
		@Override
		protected void onPreExecute() {
		 }
		
		@Override
		protected JSONObject doInBackground(String... str) {
			return GetLiveInfo.getLiveInfo(str[0]);
	        
	    }
		
		/*onPostExecute se ejecuta cuando ya se tiene el resultado de la petición*/
		protected void onPostExecute(JSONObject result) {
			JSONObject jObj;
			try {
				jObj = new JSONObject(result.getString("llegadas"));
				liveInfo = new JSONArray(jObj.getString("llegada"));

				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			try {
				/*Si tenemos toda la información buscamos los autobuses que pasan por la parada*/
				if(liveInfo != null && MainActivity.busInfo != null){
					long j = 0;
					/* Se buscan todos los autobuses que pasan por la parada y se guardan en un array de buses*/
					for (int i = 0; i < liveInfo.length(); i++) {
						if(liveInfo.getJSONObject(i).getString("idparada").equals(MainActivity.busInfo.getJSONObject(stop).getString("idparada"))){
							Bus bus = new Bus(liveInfo.getJSONObject(i).getString("idlinea"), liveInfo.getJSONObject(i).getString("minutos"), liveInfo.getJSONObject(i).getString("idparada"), liveInfo.getJSONObject(i).getString("idtrayecto"), liveInfo.getJSONObject(i).getString("matricula"),j);
							buses.add(bus);
							j++;
						} 
					}
				}
				
				/* Se busca cada bus en la información de las líneas para hallar el color de cada una*/
				if(liveInfo != null && MainActivity.lineInfo != null){
					for (int i = 0; i < MainActivity.lineInfo.length(); i++) {
						for(Bus j : buses) //Se recorre el array 
						if(MainActivity.lineInfo.getJSONObject(i).getString("idlinea").equals(j.getIdLinea())){
							j.setColor(MainActivity.lineInfo.getJSONObject(i).getString("colorHex")); //Si hay coincidencia añadimos el atributo color
						} 
					}
				}
				
				if (buses.size()==0){ //Si no hay ningún autobús añadimos el string a mostrar
					lines.add(noBuses);
					noBus.setText(noBuses);
				}else{
					//Si no, ordenamos los autobuses por orden de llegada
					Collections.sort(buses, new Comparator<Bus>(){
						@Override
						public int compare(Bus bus1, Bus bus2){							
							return bus1.compareTo(bus2);
						}						
					});
					/*Y los añadimos a los String a mostrar*/
					for(Bus i : buses){
						lines.add("Línea "+i.getIdLinea()+" - "+i.getMin()+" minutos");
					}
					
					/*Mostramos la información por pantalla*/
					AdapterBuses adapter = new AdapterBuses(GetInfo.this, buses);
					list.setAdapter(adapter);
					
				}
				 
				
				if(MainActivity.isAudioActive){
					/* Si está activo el audio, activamos el TextToSpeech*/
					tts = new TextToSpeech(GetInfo.this, GetInfo.this);
				}	
								
				activateGestures();		
				
	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	
		
	}
	
	/* Método que activa los eventos de gestos */
	public void activateGestures(){
		/*Escuchamos eventos de click y doble click tanto en la lista como en el resto de la pantalla*/
		final GestureDetector detector = new GestureDetector(GetInfo.this, new MyGestures());
	
		rl.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	System.out.println("onTouch");
                return detector.onTouchEvent(event);
            }
        });
		
		list.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	System.out.println("onTouch");
                return detector.onTouchEvent(event);
            }
        });		
	}
	
 /* Función que calcula la parada más cercana a nuestra posición */
	public int getCloserStop(double lat, double lng) throws JSONException{
		JSONArray busInfo = MainActivity.busInfo;
		int length = busInfo.length();
		double distance[] = new double[length];
		double utmx;
		double utmy;
		LatLong latlong = null;
		
		/*Recorremos el array JSON obteniendo las posiciones de todas las paradas*/
		for (int i = busInfo.length()-1; i >=0 ; i--) {			
			utmx = Double.parseDouble(busInfo.getJSONObject(i).getString("utmx"));
			utmy = Double.parseDouble(busInfo.getJSONObject(i).getString("utmy"));
			latlong = LatLong.UTMtoLatLong(utmx, utmy); //Convertimos las proyecciones utm en lat y long
			distance[i] = getDistance(latlong.lat, latlong.lng, lat, lng); //Obtenemos la distancia a nuestra posición		
		}
		/*Devolvemos el índice de la parada a menor distancia*/
		return getMinIndex(distance);
	}
	
	/* Método que calcula el índice del mínimo valor dentro de un array */
	public static int getMinIndex(double[] numbers){  
		  double minValue = numbers[0];
		  int minIndex = 0;
		  for(int i=1; i<numbers.length; i++){  
		    if(numbers[i] < minValue){  
		      minValue = numbers[i];
		      minIndex = i;
		    }  
		  }  
		  return minIndex;  
	}
	
	/* Función que calcula la distancia entre dos puntos */
	public double getDistance(double lat1, double lng1, double lat2, double lng2) {
		  double R = 6371000.0; // Radio de la Tierra en metros
		  double lat_rad = Math.toRadians(lat2-lat1);
		  double lng_rad = Math.toRadians(lng2-lng1); 
		  double a = 
		    Math.sin(lat_rad/2) * Math.sin(lat_rad/2) +
		    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
		    Math.sin(lng_rad/2) * Math.sin(lng_rad/2);
		  
		  double c = 2 * Math.asin(Math.sqrt(a)); 
		  double d = R * c; // Distancia en metros
		  return d;
		}
	
	/*onInit se llama al crear un TextToSpeech*/
	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {
			 
			int result = tts.setLanguage(loc); //Establecemos el idioma
			
			/* Comprobamos la velocidad elegida en los ajustes */
			switch(MainActivity.optionSpeed){
				case 0: tts.setSpeechRate((float) 0.5);	
						break;
				case 1: tts.setSpeechRate((float) 1);	
						break;
				case 2: if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
							tts.setSpeechRate((float) 1.5);
						else tts.setSpeechRate((float) 2.5);
						break;
				case 3: if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
							tts.setSpeechRate((float) 2);
						else tts.setSpeechRate((float) 3);
						break;
				default: break;
			}
					
			if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				//Si el dispositivo no soporta el idioma abrimos Google Play para instalarlo
				Intent installIntent = new Intent();
			    installIntent.setAction(
			    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
			    startActivity(installIntent);
                System.out.println("This Language is not supported");
            } else {
            	//Si todo va bien hablamos
                speakOut();

            }
 
 
        } else {
        	System.out.println("Initilization Failed!");
        }
	}
	
	/*Método con el que se dice la información recopilada*/
	public void speakOut(){

		tts.speak(nearStop, TextToSpeech.QUEUE_ADD, null); //Se dice la parada
		if(lines.size() != 0){
			for(String i : lines){
				i = i.replace("-", "a");
				tts.speak(i, TextToSpeech.QUEUE_ADD, null); //Se dicen las líneas de autobús
			}
		}
		if(lines.size()!=0 && lines.get(0)!=noBuses && !nearStop.equals("La información no está disponible"))
			tts.speak("\n Si quiere elegir una línea mantenga la pantalla presionada y dígala, para volver a buscar, click", TextToSpeech.QUEUE_ADD, null);
	}
	
	/*Método con el que abrimos el reconocedor de voz*/
	public void chooseLine(){
		
		/* Se crea un intent con el reconocedor*/
		Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);		
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES"); //Idioma español
        
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
        	/* Si es una versión antigua de Android lo abrimos físicamente */
        	try{
            	startActivityForResult(intent, RESULT_SPEECH);
            }catch(ActivityNotFoundException a){
            	Intent installSTT = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
            	// setting flags to avoid going in application history (Activity call stack)
            	installSTT.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            	startActivity(installSTT);
            }
        }else{
        	/* Si es una versión moderna el reconocedor no tendrá interfaz*/
        	sr = SpeechRecognizer.createSpeechRecognizer(this);       
            sr.setRecognitionListener(new listener()); 
            sr.startListening(intent);
        }
        	
	}
	
	/*Método con el que parar la voz*/
	public void stopTTS(){
		if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
	}
	
	/* Al salir del reconocedor de voz tratamos lo obtenido */
	/* Aquí se llega cuando se cierra el reconocedor con interfaz */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
 
        switch (requestCode) {
        case RESULT_SPEECH: {
            if (resultCode == RESULT_OK && null != data) {
            	//ArrayList en el que se obtiene el mensaje
                ArrayList<String> speech = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); 
                checkMessage(speech); //Llamada al método que trata al mensaje
            }
            else{
            	repeat(); //Si hay algún problema pedimos repetir el mensaje
            }
            break;
        }
 
        }
    }
	
	/* Clase que se utiliza para el reconocedor de voz sin interfaz */
	class listener implements RecognitionListener{
		
	            public void onReadyForSpeech(Bundle params)
	            {
	                     System.out.println("onReadyForSpeech");
	            }
	            public void onBeginningOfSpeech()
	            {
	                     System.out.println("onBeginningOfSpeech");
	            }
	            public void onRmsChanged(float rmsdB)
	            {
	                     System.out.println("onRmsChanged");
	            }
	            public void onBufferReceived(byte[] buffer)
	            {
	                     System.out.println("onBufferReceived");
	            }
	            public void onEndOfSpeech()
	            {
	                     System.out.println("onEndofSpeech");
	            }
	            public void onError(int error)
	            {
	            	switch(error){
	            	/* Ante ciertos errores se pide la repetición del mensaje */
	            		case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
	            			System.out.println("error timeout");
	            			repeat();
	            			break;
	            		case SpeechRecognizer.ERROR_AUDIO:
	            			System.out.println("error audio");
	            			repeat();
	            			break;
	            		case SpeechRecognizer.ERROR_CLIENT:
	            			System.out.println("error client");
	            			break;
	            		case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
	            			System.out.println("error permissions");
	            			break;
	            		case SpeechRecognizer.ERROR_NETWORK:
	            			System.out.println("error network");
	            			break;
	            		case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
	            			System.out.println("error network timeout");
	            			break;
	            		case SpeechRecognizer.ERROR_NO_MATCH:
	            			System.out.println("error no match");
	            			repeat();
	            			break;
	            		case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
	            			System.out.println("error busy");
	            			break;
	            		case SpeechRecognizer.ERROR_SERVER:
	            			System.out.println("error server");
	            			break;
	            	}
	                     
	            }
	            /* Método al que se llama cuando hay resultados*/
	            public void onResults(Bundle results)                   
	            {	
	            	//ArrayList en el que se obtiene el mensaje
	            	ArrayList<String> speech = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
	            	checkMessage(speech);
	            }
	            public void onPartialResults(Bundle partialResults)
	            {
	                     System.out.println("onPartialResults");
	            }
	            public void onEvent(int eventType, Bundle params)
	            {
	                     System.out.println("onEvent " + eventType);
	            }
	   }
	
	public void checkMessage(ArrayList<String> speech){
		
		boolean success = false; //booleano para detectar la primera conicidencia entre el mensaje y las líneas
        String[] arr = speech.get(0).split(" "); //Separamos por palabras

        for(Bus i : buses){
        	for (String s : arr){
            	if(i.getIdLinea().equals(s)){ //Comparamos cada palabra con los idLinea
            		MainActivity.chosenBus = i;
            		success = true;
            		break;
            	}
        	}
        	if(success) break;
        }
        if(!success){ //Si no se ha encontrado una coincidencia se pide repetir el mensaje
        	repeat();
    	}else{
    		/* En caso de obtener coincidencia se abre la siguente Activity*/
			backDestroy = false;
			stopTTS();
    		Intent i = new Intent();
    		i.setClass(GetInfo.this, LineSelected.class);
    		startActivity(i);
    		finish();
    	}
	}
	
	/* Método que pide la repetición del mensaje */
	public void repeat(){
		
		if(MainActivity.isAudioActive){
			/* De forma hablada si el audio está activo*/
    		tts.speak("Pulse y repita, por favor", TextToSpeech.QUEUE_ADD, null);
    	}else{
    		/* Mediante texto en caso contrario */
    		Toast.makeText(getApplicationContext(), "Pulse y repita, por favor", Toast.LENGTH_LONG).show();
    	}
	}
	
	/*Comprobar si tenemos el paquete de idioma instalado*/
	protected boolean isAppInstalled(String packageName) {
        Intent mIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (mIntent != null) {
            return true;
        }
        else {
            return false;
        }
    }
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Añadimos el icono de opciones a la barra de acción (action bar)
		getMenuInflater().inflate(R.menu.options, menu);
		return true;
	}
	
	/* Elementos que deben salir al pinchar en opciones */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        /* Enlace con la activity de ajustes "Settings" */
        case R.id.action_settings:
        	Intent i = new Intent();
			i.setClass(GetInfo.this, Settings.class);
			startActivity(i);
            break;
           
            /* Enlace con la activity de ayuda "Help" */
        case R.id.action_help:
        	Intent i2 = new Intent();
			i2.setClass(GetInfo.this, Help.class);
			startActivity(i2);
            break;  
 
        }
 
        return true;
    }
	
	/*Al destruirse la "pantalla" se para la voz*/
	@Override
	protected void onDestroy(){
		if(backDestroy)
			stopTTS();
		if(sr != null)
			sr.destroy();
		super.onDestroy();
		
	}

	
	/*Clase para detectar el click y el docle lick*/
	class MyGestures extends GestureDetector.SimpleOnGestureListener{
		
		@Override
		public boolean onDown(MotionEvent e){
			System.out.println("Down touch");
	        return true;
		}
		@Override
	    public boolean onDoubleTap(MotionEvent e) {
			System.out.println("DoubleTap");
	        return true;
	    }
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e){
			/* Ante un click se lanza esta activity de nuevo */
			restart();
			System.out.println("Tap");
	        return true;
		}
		
		@Override
		public void onShowPress(MotionEvent e){
			/* Ante un long press se abre la siguiente pantalla */
			System.out.println("Long press");
			if(tts!=null) tts.stop(); //Con un click para mos la voz
			if(lines.size()!=0 && lines.get(0)!=noBuses && !nearStop.equals("La información no está disponible")){
				chooseLine(); //Y activamos el reconocedor
			}
				
		}
		
		public boolean onUp(MotionEvent e){
			System.out.println("Up");
			sr.stopListening();
	        return true;
		}
	}
	
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent evt) {
        return true;
    }
}
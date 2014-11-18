package pfc.quebus.activities;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RelativeLayout;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.example.quebus.R;
import pfc.quebus.elements.MyTextView;
import pfc.quebus.utilities.*;


public class NextStop extends Activity implements OnInitListener{
	
	/* Declaración de variables*/
	String liveInfoURL = "http://datos.gijon.es/doc/transporte/busgijontr.json";
	Bus bus = MainActivity.chosenBus; //Se obtiene la información guardada del autobús elegido
	JSONArray stopsRoutes = MainActivity.stopsRoutes;
	
	static TextToSpeech tts;
	private Locale loc = new Locale("es", "ES"); //Idioma español de España para el tts
	
	MyTextView title;
	MyTextView stop;
	MyTextView minutes;
	RelativeLayout rl;
	
	Thread myThread;
	JSONArray liveInfo;
	
	boolean hoverMove = false;
	int hoverCheck = 0;
	
	boolean userRequest = false;
	boolean firstTime 	= true;
	boolean connOk = true;
	
	CheckConnection conn;
	
	
	/* Handler para modificar la información con los datos obtenidos en el hilo */
	Handler hand = new Handler() {
		@Override
		  public void handleMessage(Message msg) {
			  bus = (Bus)msg.obj; //Se obtiene el objeto bus que envío desde el hilo
			  
			  /* Sacamos por pantalla la información */
			  title.setText("Línea "+bus.getIdLinea());
			  stop.setText(bus.getNextStopName());
			  minutes.setText(bus.getMin()+" min"); //Se cambia el texto de la pantalla 
			  
			  if(MainActivity.isAudioActive){ //Se informa si audio activado
				  tts = new TextToSpeech(NextStop.this, NextStop.this);
			  }
			  MainActivity.chosenBus = bus; //Actualizamos el bus global			  

			  /* Activamos los eventos gestuales*/
			  final GestureDetector detector = new GestureDetector(NextStop.this, new MyGestures());
			  rl.setOnTouchListener(new OnTouchListener() {
					
			      @Override
			      public boolean onTouch(View v, MotionEvent event) {
			          	System.out.println("onTouch");
			            return detector.onTouchEvent(event);
			       }
		      });
					

			  
		  }
	};
	
	/* Método que se ejecuta cuando la activity está creada*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nextstop);
		
		title = (MyTextView)findViewById(R.id.lineaTitulo);
		stop = (MyTextView)findViewById(R.id.parada);
		minutes = (MyTextView)findViewById(R.id.minutes);
		rl = (RelativeLayout)findViewById(R.id.rl);
		
		conn = new CheckConnection(this); //Para comprobar la conexión a internet
		
		start();
		
	}
	
	public void start(){
    	int order = -1;
		try {
			/* Conseguimos la próxima parada */
			for(int i=0; i<stopsRoutes.length(); i++){
				/* Primero se encuentra qué orden tiene nuestra parada dentro del trayecto*/
				if(bus.getIdLinea().equals(stopsRoutes.getJSONObject(i).getString("idlinea"))){
					System.out.println("Coinciden lineas");
					if(bus.getIdJourney().equals(stopsRoutes.getJSONObject(i).getString("idtrayecto"))){
						System.out.println("Coinciden trayectos");
						if(bus.getStop().equals(stopsRoutes.getJSONObject(i).getString("idparada"))){
							System.out.println("Coinciden paradas");
							order = Integer.parseInt(stopsRoutes.getJSONObject(i).getString("orden"));
							break;
						}
					}
				}
			}
			System.out.println("Orden: "+order);
			/* Después se busca la siguiente parada */
			for(int i=0; i<stopsRoutes.length(); i++){				
				if(bus.getIdLinea().equals(stopsRoutes.getJSONObject(i).getString("idlinea"))){
					if(bus.getIdJourney().equals(stopsRoutes.getJSONObject(i).getString("idtrayecto"))){
						if(order != -1  &&  Integer.parseInt(stopsRoutes.getJSONObject(i).getString("orden")) == order+1){
							/* Y guardamos el id y el nombre de la parada */
							bus.setIdStop(stopsRoutes.getJSONObject(i).getString("idparada"));
							bus.setNextStopName(stopsRoutes.getJSONObject(i).getString("descripcion"));
							System.out.println("Parada: "+bus.getNextStopName());
							break;
						}
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		/* Se lanza el hilo para la petición */
		stopThread();
		launchThread();
	}
	
	/* Método que lanza el hilo para realizar la petición */
	public void launchThread(){
		myThread = new Thread(new JSONRequest(0));
		myThread.start();
	}
	
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			 
			tts.setLanguage(loc); //Establecemos el idioma
			
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
		
			if(connOk){
	            //Si todo va bien hablamos
				tts.speak(bus.getNextStopName()+" a "+bus.getMin()+" minutos", TextToSpeech.QUEUE_ADD, null);
			}else{
				tts.speak("Se ha perdido la conexión a internet, inténtelo más tarde", TextToSpeech.QUEUE_ADD, null);
			}

 
        } else {
        	System.out.println("Initilization Failed!");
        }
		
	}
	
	
	/* Clase que implementa el hilo que hará la petición */
	class JSONRequest implements Runnable {
		long sleepingTime;
		
		JSONRequest(long sleepingTime){
			this.sleepingTime = sleepingTime;
		}
        public void run() {           
        	System.out.println("Estoy en el thread");
    		
            try {
				Thread.sleep(sleepingTime);
				System.out.println("Desperté");
				
				if(conn.isConnectedToInternet()) {
					connOk = true;
					JSONObject jObj;
					try {
						/* Se obtiene la información */
						jObj = new JSONObject(GetLiveInfo.getLiveInfo(liveInfoURL).getString("llegadas"));
						liveInfo = new JSONArray(jObj.getString("llegada"));
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				
					/* Si es la primera vez que se entra, si la petición es del usuario o queda más de 1 minuto*/
					if(Integer.parseInt(bus.getMin()) > 1 || firstTime || userRequest){
						System.out.println("Es primera vez o queda más de 1 min");
						for (int i = 0; i < liveInfo.length(); i++) {
							try {
								/* Buscamos el autobús en la parada */
								if(liveInfo.getJSONObject(i).getString("idparada").equals(bus.getStop())){
									if(liveInfo.getJSONObject(i).getString("matricula").equals(bus.getMatricula())){
										System.out.println("Encontré autobús");
										/* Si han pasado 2 min o es la 1ª vez o es petición del usuario*/
										if(Integer.parseInt(bus.getMin()) - Integer.parseInt(liveInfo.getJSONObject(i).getString("minutos")) > 1 || firstTime || userRequest){
											/* Se actualiza la información */
											firstTime = false;
											userRequest = false;
											bus.setMin(liveInfo.getJSONObject(i).getString("minutos"));
											Message msg = new Message();
											msg.obj = bus;
											hand.sendMessage(msg);
										}
										break;
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							} 
						
						}
						
						/* Se hace una espera y se lanza un nuevo hilo */
						Thread.sleep(30000);
						stopThread();
						launchThread();
						
					}else{ /* Si no es la primera vez, no ha pedido el usuario y queda 1 minuto o menos*/
						System.out.println("Queda poco");
						for (int i = 0; i < liveInfo.length(); i++) {
							
							try {
								/* Se comprueba is el autobús aún aparece en la información de la última parada*/
								if(liveInfo.getJSONObject(i).getString("matricula").equals(bus.getMatricula())){
									if(liveInfo.getJSONObject(i).getString("idparada").equals(bus.getStop())){
										/* Si aparece no cambiamos la parada y actualizamos los minutos*/
										bus.setMin(liveInfo.getJSONObject(i).getString("minutos"));
										Message msg = new Message();
										msg.obj = bus;
										hand.sendMessage(msg);
										
										/* Se hace una espera y se lanza el hilo de nuevo*/
										Thread.sleep(30000);
										stopThread();
										launchThread();
										
									}
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						/* Si llegamos aquí es que el autobús no aparece en la última parada y cambiamos a la siguiente */
						stopThread();
						start();					
						
					}
				}else{
					connOk = false;
					Message msg = new Message();
					msg.obj = bus;
					hand.sendMessage(msg);
					Thread.sleep(30000);
					stopThread();
					start();
				}
				
                
            } catch (InterruptedException e1) {
				e1.printStackTrace();
			}
            
        }
        
    }
	
	
	/*Clase para detectar el click y el docle lick*/
	class MyGestures extends GestureDetector.SimpleOnGestureListener{
		
		@Override
		public boolean onDown(MotionEvent e){
	        return true;
		}
		@Override
	    public boolean onDoubleTap(MotionEvent e) {
	        return true;
	    }
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e){
			
			/* Con un click abrimos pantalla GetInfo*/
			stopTTS();			  
			Intent i = new Intent();
			i.setClass(NextStop.this, GetInfo.class);
			startActivity(i);
			finish();
	        return true;
		}
		
		@Override
		public void onShowPress(MotionEvent e){
			/* Con long press se lanza hilo para petición*/
			System.out.println("Long press");
			userRequest = true;
			stopThread();
			myThread = new Thread(new JSONRequest(1));
			myThread.start();
				
		}
	}
	
	
	
	
	/*Método con el que parar la voz*/
	public void stopTTS(){
		if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
	}
	
	
	/* Método para parar y destruir un hilo*/
	public void stopThread(){
		/* Se detiene el hilo */
		 if(myThread != null){
			  myThread.interrupt();
			  myThread = null;
		  }
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Añadimos el icono de opciones a la barra de acción (action bar)
		getMenuInflater().inflate(R.menu.options, menu);
		return true;
	}
	
	/* Se definen los botones que aparecerán en "más opciones" */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.action_settings:
        	Intent i = new Intent();
			i.setClass(NextStop.this, Settings.class);
			startActivity(i);
            break;
            
        case R.id.action_help:
        	Intent i2 = new Intent();
			i2.setClass(NextStop.this, Help.class);
			startActivity(i2);
            break;  
 
        }
 
        return true;
    }
	
	/* Cuando se destruye la activity se paran el hilo y la síntesis de voz*/
	@Override
	protected void onDestroy(){
		stopThread();
		stopTTS();
		super.onDestroy();
		
	}
	
	public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent evt) {
        return true;
    }

}
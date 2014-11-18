package pfc.quebus.activities;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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

import com.example.quebus.R;
import pfc.quebus.elements.MyTextView;
import pfc.quebus.utilities.*;

public class LineSelected extends Activity implements OnInitListener{
	
	/* Declaración de variables */
	String liveInfoURL = "http://datos.gijon.es/doc/transporte/busgijontr.json";
	Bus bus = MainActivity.chosenBus; //Se obtiene la información guardada del autobús elegido
	int stop = GetInfo.stop;
	
	JSONArray liveInfo;
	
	static TextToSpeech tts;
	private Locale loc = new Locale("es", "ES"); //Idioma español de España para el tts
	
	MyTextView title;
	MyTextView minutes;
	RelativeLayout rl;
	
	Thread myThread;
	long time;
	int mins;
	boolean hoverMove = false;
	int hoverCheck = 0;
	
	boolean userRequest = false;
	boolean connOk = true;
	
	CheckConnection conn;
	
	/* Handler para modificar la información con los datos obtenidos en el hilo */
	Handler hand = new Handler() {
		  @Override
		  public void handleMessage(Message msg) {
			  bus = (Bus)msg.obj; //Se obtiene el objeto bus que envío desde el hilo
			  minutes.setText(bus.getMin()+" min"); //Se cambia el texto de la pantalla 
			  MainActivity.chosenBus = bus; //Actualizamos el bus global
			  
			  stopThread();
			  stopTTS();
			  
			  start();
		  }
	};
	
	
	
	/* Método que se ejecuta cuando la activity está creada*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lineselected);
		
		rl = (RelativeLayout)findViewById(R.id.layout);
		title = (MyTextView)findViewById(R.id.lineaTitulo);
		minutes = (MyTextView)findViewById(R.id.minutos);
		
		/* Sacamos por pantalla la información */
		title.setText("Línea "+bus.getIdLinea());
		minutes.setText(bus.getMin()+" min");
		
		conn = new CheckConnection(this); //Para comprobar la conexión a internet
		
		start();
	}
	
	public void start(){
				
		mins = Integer.parseInt(bus.getMin()); //Obtenemos los minutos que quedan para que llegue el autobús
		
		if(MainActivity.isAudioActive){
			/* Se dice la línea que se ha elegido */
			tts = new TextToSpeech(this, this);
		}
		
		/* Se activan los gestos */
		final GestureDetector detector = new GestureDetector(LineSelected.this, new MyGestures());
		rl.setOnTouchListener(new OnTouchListener() {
			
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	System.out.println("onTouch");
                return detector.onTouchEvent(event);
            }
        });
			

		
		if (mins > 1){
			/* Si queda más de un minuto se lanza el hilo para la petición de info*/
			myThread = new Thread(new JSONRequest(30000));
			myThread.start();
		}else{
			/* Si no, se abre la próxima pantalla*/
			stopThread(); //Si hay algún hilo funcionando se destruye
			
			/* Se abre una nueva activity para indicar próximas paradas */
			myThread = new Thread(new launchNextActivity(30000));
			myThread.start();
		}
	}

	/* Método al que se llama al crear un objeto TextToSpeech*/
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
				if(mins > 1){
					tts.speak("Línea "+bus.getIdLinea()+" a "+bus.getMin()+" minutos", TextToSpeech.QUEUE_ADD, null); 
				}else{
					tts.speak("El autobús "+bus.getIdLinea()+" está llegando", TextToSpeech.QUEUE_ADD, null);
				}
			}else{
				tts.speak("Se ha perdido la conexión a internet, inténtelo más tarde", TextToSpeech.QUEUE_ADD, null);
			}
	 
        } else {
        	System.out.println("Initilization Failed!");
        }
		
	}
	

	/* Clase que implementa el hilo que realiza la petición */
	class JSONRequest implements Runnable {
		long sleepingTime;
		
		JSONRequest(long sleepingTime){
			this.sleepingTime = sleepingTime;
		}
		
        public void run() {           
        	System.out.println("Estoy en el thread");
            try {
            	/* Se duerme el hilo durante 30 seg para dejar tiempo entre peticiones*/
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

				
	                for (int i = 0; i < liveInfo.length(); i++) {
						try {
							/* Se busca el autobús que se va a coger en la parada en la que estamos */
							if(liveInfo.getJSONObject(i).getString("idparada").equals(bus.getStop())){
								if(liveInfo.getJSONObject(i).getString("matricula").equals(bus.getMatricula())){
									/* Una vez encontrado, si han pasado más de dos minutos se actualiza la información*/
									if((Integer.parseInt(bus.getMin()) - Integer.parseInt(liveInfo.getJSONObject(i).getString("minutos"))) > 1 || userRequest){
										userRequest = false;
										bus.setMin(liveInfo.getJSONObject(i).getString("minutos"));
										Message msg = new Message();
										msg.obj = bus;
										hand.sendMessage(msg);
										break;
									}else{
										/* Si no se lanza otro hilo y se destruye el actual */
										stopThread();
										System.out.println("Stop thread");
										myThread = new Thread(new JSONRequest(30000));
										myThread.start();						
										
										
									}
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} 
					}
				}else{
					connOk = false;
					Message msg = new Message();
					msg.obj = bus;
					hand.sendMessage(msg);
				}
            } catch (InterruptedException e1) {
				e1.printStackTrace();
			}

        }
        
    }
	
	
	/* Clase que implementa el hilo que lanza la siguiente Activity */
	class launchNextActivity implements Runnable {
		long sleepingTime;
		
		launchNextActivity(long sleepingTime){
			this.sleepingTime = sleepingTime;
		}
        public void run() {
        	
        	/*Se duerme durante 30 segundos y luego se lanza la pantalla*/
        	System.out.println("Estoy en el thread");
            try {
				Thread.sleep(sleepingTime);
				System.out.println("Desperté");
				
				Intent i = new Intent();
        		i.setClass(LineSelected.this, NextStop.class);
        		startActivity(i);
        		stopTTS();
        		finish(); //Se destruye la activity actual para no volver a ella

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
			
			/* Si se produce un click abrimos la activity GetInfo */			
			stopTTS();		
			Intent i = new Intent();
			i.setClass(LineSelected.this, GetInfo.class);
			startActivity(i);
			finish();
		    return true;
		}
		
		@Override
		public void onShowPress(MotionEvent e){
			
			/*Ante un long press se lanza un nuevo hilo de petición */
			System.out.println("Long press");
			userRequest = true;
			stopTTS();
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
	
	/* Elementos que deben salir al pinchar en opciones */
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        
        /* Enlace con la activity de ajustes "Settings" */
        case R.id.action_settings:
        	Intent i = new Intent();
			i.setClass(LineSelected.this, Settings.class);
			startActivity(i);
            break;
            
        /* Enlace con la activity de ayuda "Help" */    
        case R.id.action_help:
        	Intent i2 = new Intent();
			i2.setClass(LineSelected.this, Help.class);
			startActivity(i2);
            break;  
 
        }
 
        return true;
    }
	
	/* Cuando se destruye la activity se para el hilo y el motor de voz*/
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
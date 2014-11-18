package pfc.quebus.activities;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;


import android.app.Activity;
import android.app.AlertDialog;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityEvent;

import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import java.util.Calendar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;

import com.example.quebus.R;
import pfc.quebus.elements.*;
import pfc.quebus.utilities.*;

public class MainActivity extends Activity {
	
	/* Ficheros para guardar en memoria interna */
	static String SETTINGS = "settings.txt";
	static String SAVEDDATA = "saved_data.txt";
	static String SAVEDDAY = "saved_day.txt";
	
	/* Arrays JSON en los que almacenar la información */
	public static JSONArray busInfo;
	public static JSONArray lineInfo;
	public static JSONArray stopsRoutes;
	
	
	/* Declaración de variables */
	static Bus chosenBus;
	
	static boolean isAccessibilityEnabled;
	CheckConnection conn;
	GetLocation gps;
	
	static boolean isAudioActive;	
	static int optionSpeed;
	
	
	AccessibilityManager am;
	Boolean audioCheck;
	MyButton btn;
	ProgressBar pb;
	RelativeLayout rl;
	
	String busInfoURL = "http://datos.gijon.es/doc/transporte/busgijoninfo.json";
	int dayOfYear;	
	int hoverCheck = 0;
	boolean hoverMove = false;	
	static int savedDayOfYear;
	boolean shortModeCheck;
	String speed;
	boolean ttsModeCheck;
	boolean firstTime = true;
	boolean changeGPS = false;
	boolean changeConn = false;
	
	private Context mContext;
    
   
	/* Método que se ejecuta cuando la activity está creada*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* Creación del objeto para comprobar si la accesibilidad está activada*/
        am = (AccessibilityManager)getSystemService("accessibility");
        isAccessibilityEnabled = am.isEnabled();
        
        /* Se obtiene de memoria interna las opciones de los ajustes */
        try {
            getSavedOptions();
        } catch(IOException e1) {
            e1.printStackTrace();
        }
        
        /* Se obtiene el día del año (p.e. 2 de febrero es el día 33) */
        Calendar c = Calendar.getInstance();
        dayOfYear = c.get(Calendar.DAY_OF_YEAR);
        
        /* Se intenta obtener de memoria interna la información, en caso de no existir serán null */
        try {
            JSONObject jObj = getData(SAVEDDATA);
            if(jObj != null) {
            	JSONObject jObj2 = new JSONObject(jObj.getString("paradasTrayectos"));
                stopsRoutes = new JSONArray(jObj2.getString("parada"));
                System.out.println("Vamos bien stopsroutes");
                jObj2 = new JSONObject(jObj.getString("paradas"));
                busInfo = new JSONArray(jObj2.getString("parada"));
                System.out.println("Vamos bien businfo");
                jObj2 = new JSONObject(jObj.getString("lineas"));
                lineInfo = new JSONArray(jObj2.getString("linea"));
                System.out.println("Vamos bien lineinfo");
                
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch(JSONException e) {
            e.printStackTrace();
        }
        
        /* Creación de objetos gps y de conexión */
        gps = new GetLocation(this);
        conn = new CheckConnection(this);
        
        /* Relación entre variables y sus correspondientes id's en el xml */
        pb = (ProgressBar)findViewById(R.id.spinner);
        rl = (RelativeLayout)findViewById(R.id.main_rl);
        
        /* Comprobación de si hay conexión a internet */
        if(!conn.isConnectedToInternet()) {
        	/* Si no hay se abre una alerta para indicarlo y dar la posibilidad de acceder a los ajustes */
        	changeConn = true;
            conn.showConnectionAlert();
        }
        
        /* Comprobación de si hay conexión gps */
        if(!gps.canGetLocation()) {
        	/* Si no hay se abre una alerta para indicarlo y dar la posibilidad de acceder a los ajustes */
        	changeGPS = true;
            gps.showSettingsAlert();
        }
        
        if(!changeConn) 
        	next(); //Si no hay problemas de conexión continuamos
        
    }
    
    /* Método que se ejecuta si hay conexión a internet */
    public void next(){
    	
    	/* Se obtiene el día del año guardado */
    	try {
            getSavedDate();
        } catch(IOException e) {
            e.printStackTrace();
        }
        System.out.println(savedDayOfYear + " dia: " + dayOfYear);
        
        /* Si han pasado 90 días desde la última petición, si se ha cambiado de año
          o si no se dispone de información*/
        if((savedDayOfYear > dayOfYear) || (dayOfYear >= (savedDayOfYear + 90)) || (savedDayOfYear < 1)
            || (busInfo == null) || (lineInfo == null) || (stopsRoutes == null)) {
            
        	/* Se salva el día actual */
	        try {
	            saveDay(dayOfYear);
	            getSavedDate();
	        } catch(IOException e) {
	            e.printStackTrace();
	        }
	        
	        /* Y se realiza la petición de la información */
	        URLReader stopInfo = new URLReader();
	        stopInfo.execute(busInfoURL);
	        

	     }else{
	    	 /* Si no, está todo listo y continuamos */
	    	 ready();
	     }
    }
    
    /* Método que se ejecuta para activar los gestos*/
    public void ready() {
    	/* Se activa el click y si este se produce se avanza a la siguiente activity */
        rl.setOnClickListener(new OnClickListener(){
            
            public void onClick(View v) {
                nextActivity();
                gps.stopUsingGPS();
            }
        });
        /* Se oculta el spinner */
        pb.setVisibility(View.GONE);
    }
    
    /* Método que se ejecuta si después de todo no se dispone de conexión a internet */
    public void error(){
    	/* Se informa del problema */
    	Toast.makeText(getApplicationContext(), "Ahora no dispone de conexión a internet. Inténtelo más tarde.", Toast.LENGTH_LONG).show();


    }
    
    /* Método que abre siguiente activity */
    public void nextActivity() {
        Intent i = new Intent();
        i.setClass(this, GetInfo.class);
        startActivity(i);
    }
    
    
    /**** Métodos para guardar y leer la información en memoria interna ****/
    
    /* Método para obtener las opciones de los ajustes */
    public void getSavedOptions() throws IOException {    	
        File file = new File(getFilesDir(), SETTINGS); //Se abre el fichero
        /* Si el archivo existe */
        if(file.exists()) {
        	/* Se obtienen los dos valores que necesitamos */
            FileInputStream fis = openFileInput(SETTINGS);
            isAudioActive = fis.read() == 1 ? true : false;
            optionSpeed = fis.read();
            fis.close();//Se cierra el archivo
            return;
        }
        /* Si no existe le damos valores por defecto */
        isAudioActive = true;
        optionSpeed = 4;
    }
    
    /* Método para obtener el día del año */
    public void getSavedDate() throws IOException {
        File file = new File(getFilesDir(), SAVEDDAY);//Se abre el fichero
        /* Si el archivo existe */
        if(file.exists()) {
        	/* Se obtienen el valor guardado */
            FileInputStream fis = openFileInput(SAVEDDAY);
            int c;
            String temp = "";
            while( (c = fis.read()) != -1){
            	   temp = temp + Character.toString((char)c);
            	}
            try{
            	savedDayOfYear = Integer.parseInt(temp);
            } catch(NumberFormatException nfe){
            	nfe.printStackTrace();
            }
            fis.close();//Se cierra el archivo
            return;
        }else{
        	/* Si no existe le asignamos un valor alto, creamos el fichero y lo guardamos */
	        savedDayOfYear = 1000;
	        FileOutputStream fos = openFileOutput(SAVEDDAY, Context.MODE_PRIVATE);
	        fos.write(Integer.toString(1000).getBytes());
	        fos.close();//Se cierra el archivo
	        return;
        }
    }
    
    /* Método para obtener la información de las líneas de memoria interna */
    public JSONObject getData(String fileName) throws IOException {
        File file = new File(getFilesDir(), fileName);//Se abre el fichero
        /* Si el archivo existe */
        if(file.exists()) {
        	/* Se va leyendo línea a línea y se crea un StringBuilder*/
            FileInputStream fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
            	sb.append(line);
            }
            
            /* Con ese StringBuilder se crea un objeto JSON */
            try {
                JSONObject jObj = new JSONObject(sb.toString());
                fis.close();
                isr.close();
                bufferedReader.close();
                return jObj;
            } catch(JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
    
    /* Método para guardar el día */
    public void saveDay(int day) throws IOException {
        FileOutputStream os = openFileOutput(SAVEDDAY, Context.MODE_PRIVATE);//Se abre el fichero
        os.write(Integer.toString(day).getBytes());
        os.close();//Se cierra el fichero
    }
    
    /* Método para guardar la información de las líneas */
    public void saveData(JSONObject data, String fileName) throws IOException {
        FileOutputStream os = openFileOutput(fileName, Context.MODE_PRIVATE);
        os.write(data.toString().getBytes());
        os.close();
    }
    
    
    /**** Tarea asíncrona que realiza la petición ****/
    public class URLReader extends AsyncTask<String, Void, JSONObject>{
      String prueba = null;
      
      /* Método por defecto para realizar acciones antes de la ejecución */
      @Override
      protected void onPreExecute(){
      }
      
      /* Método en el que se realiza la petición */
      @Override
      protected JSONObject doInBackground(String... str)
      {
        return GetLiveInfo.getLiveInfo(str[0]);
      }
      
      /* Método que se ejecuta después de la petición ya con la información */
      protected void onPostExecute(JSONObject result){  
    	/* Si se han recibido datos */  
        if (result != null){
	        try{
	          saveData(result, SAVEDDATA); //Guardamos en memoria interna
	          /* Se crean los arrays JSON */
	          JSONObject jObj = new JSONObject(result.getString("paradas"));
	          busInfo = new JSONArray(jObj.getString("parada"));
	          jObj = new JSONObject(result.getString("lineas"));
	          lineInfo = new JSONArray(jObj.getString("linea"));
	          jObj = new JSONObject(result.getString("paradasTrayectos"));
	          stopsRoutes = new JSONArray(jObj.getString("parada"));
	          
	          /* Como se dispone de la información, estamos listos y se continúa*/
	          ready();
	          return;
	          
	        }catch(IOException e){
        	e.printStackTrace();
	        } catch (JSONException e) {
				e.printStackTrace();
			}
        }else{
        	/* Si la petición ha fallado y no se dispone de la información en memoria interna
        	 se saca una alerta informando de la situación */
        	if(busInfo == null || lineInfo == null || stopsRoutes == null){
        		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        	      
                // Nombre de la alerta
                alertDialog.setTitle("Error servidor");
          
                // Mensaje de la alerta
                alertDialog.setMessage("No se ha podido conectar con el servidor. Por favor, cierre la aplicación e inténtelo más tarde.");
          
          
                // Si presionamos Ok se cierra la alerta
                alertDialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    }
                });
          
                // Mostrar la alerta
                alertDialog.show();
        	}
        		
        }
        
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
			i.setClass(MainActivity.this, Settings.class);
			startActivity(i);
            break;
           
        /* Enlace con la activity de ayuda "Help" */
        case R.id.action_help:
        	Intent i2 = new Intent();
			i2.setClass(MainActivity.this, Help.class);
			startActivity(i2);
            break;  
 
        }
 
        return true;
    }
    
	/* Cuando la activity se destruye se libera al gps */
    @Override
    protected void onDestroy() {
        gps.stopUsingGPS();
        super.onDestroy();
    }
    
    /* Se analizará este método para saber si se vuelve de los ajustes de conexión a la red*/
    @Override
    protected void onResume() {
    	super.onResume();
    	/* Se comprueba si se ha pasado por los ajustes */
        if(!firstTime){
        	if(changeConn){
        		/* Si ya disponemos de internet continuamos */
        		if(conn.isConnectedToInternet()){
        			next();
        		}else{ 
        			/* Si no mostramos mensaje de error */
            		error();                	
        		}
        	}
        }
        firstTime = false;
    }
    
    /* Con la accesibilidad activada se oirá el nombre de la aplicación al abrirla */
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent evt) {
        evt.getText().add("QuéBus");
        return true;
    }
}
package pfc.quebus.activities;

import java.io.FileOutputStream;
import java.io.IOException;

import com.example.quebus.R;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;

public class Settings extends Activity{
	
	/* Declaración de variables */
	CheckBox cb;
	RadioGroup rg;
	TextView title;
	TextView install;
	boolean isChecked;
	int index;
	
	/* Método que se ejecuta cuando la activity está creada*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
				
		cb = (CheckBox)findViewById(R.id.checkBox1);
		rg = (RadioGroup)findViewById(R.id.radioGroup1);
		title = (TextView)findViewById(R.id.textView1);
		install = (TextView)findViewById(R.id.textView2);
		
		/* Obtenemos los valores guardados de los ajustes*/
		cb.setChecked(MainActivity.isAudioActive);
		((RadioButton)rg.getChildAt(MainActivity.optionSpeed)).setChecked(true);
		
		/* Si la opción activar audio está desmarcada inhabilitamos la selección de velocidad*/
		if(!MainActivity.isAudioActive){
			for (int i=0; i<5; i++){
				((RadioButton)rg.getChildAt(i)).setClickable(false); //Los volvemos "no clicables"
				((RadioButton)rg.getChildAt(i)).setTextColor(Color.parseColor("#708090")); // Se les cambia el color
			}
			rg.setClickable(false);
			title.setTextColor(Color.parseColor("#708090"));
		}
		
	}
	
	/* Evento click en activar audio */
	public void cbClick(View v){
		
		/* Si no está seleccionado inhabilitamos selección de velocidad*/
		if(!cb.isChecked()){
			for (int i=0; i<5; i++){
				((RadioButton)rg.getChildAt(i)).setClickable(false);
				((RadioButton)rg.getChildAt(i)).setTextColor(Color.parseColor("#708090"));
			}
			rg.setClickable(false);
			title.setTextColor(Color.parseColor("#708090"));
		}else{
			/* Si está seleccionado habilitamos*/
			for (int i=0; i<5; i++){
				((RadioButton)rg.getChildAt(i)).setClickable(true);
				((RadioButton)rg.getChildAt(i)).setTextColor(Color.BLACK);
			}
			rg.setClickable(true);
			title.setTextColor(Color.BLACK);
		}
	}
	
	/* Evento en caso de click en el elemento "Instalar síntesis de voz" */
	public void installTTS(View v){
		
		/* Se abre Play Store en el paquete de instalación*/
		System.out.println("click install");
		Intent installIntent = new Intent();
	    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	    startActivity(installIntent);
	}
	
	@Override
	protected void onDestroy(){
		
		/* Cuando se de para atrás y se salga de los ajustes*/
		/* Modificamos los valores de las variables globales*/
		MainActivity.isAudioActive = cb.isChecked();
		MainActivity.optionSpeed = rg.indexOfChild(findViewById(rg.getCheckedRadioButtonId()));
		/* Y el archivo guardado en memoria del teléfono*/
		try {
			saveOptions(cb.isChecked(), rg.indexOfChild(findViewById(rg.getCheckedRadioButtonId())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		super.onDestroy();
		
	}
	
	/* Método para guardar los ajustes en el archivo de memoria*/
	public void saveOptions(boolean b, int i) throws IOException{
		FileOutputStream os= openFileOutput(MainActivity.SETTINGS, Context.MODE_PRIVATE);
		os.write((b) ? 1 : 0); //Booleano que indica la selección de "activar audio"
		os.write(i); //Entero que indica el elemento seleccionado de la lista de velocidades
		os.close();
	}
}
package pfc.quebus.utilities;
 
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
 
public class CheckConnection {
     
    private Context _context;
     
    public CheckConnection(Context context){
        this._context = context;
    }
    
    /* Método que nos devuelve si tenemos conexión a internet o no*/
    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
          if (connectivity != null)
          {
              NetworkInfo[] info = connectivity.getAllNetworkInfo();
              if (info != null)
                  for (int i = 0; i < info.length; i++)
                      if (info[i].getState() == NetworkInfo.State.CONNECTED)
                      {
                          return true;
                      }
 
          }
          return false;
    }
    
    
    /* Método para mostrar la alerta de falta de conexión */
    public void showConnectionAlert(){
    	
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(_context);
      
        // Nombre de la alerta
        alertDialog.setTitle("Conexión");
  
        // Mensaje de la alerta
        alertDialog.setMessage("No tiene conexión a la red. ¿Quiere ir al menú de ajustes?");
  
        
        // Si presionamos el botón Ajustes se abre la ventana de ajustes de red de android
        alertDialog.setPositiveButton("Ajustes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                _context.startActivity(intent);
            }
        });
  
        // Si presionamos cancelar se cierra la alerta
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Mostrar la alerta
        alertDialog.show();
    }
}
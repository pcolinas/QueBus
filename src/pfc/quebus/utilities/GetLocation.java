package pfc.quebus.utilities;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GetLocation extends Service implements LocationListener{
	
	private final Context mContext;
	 
    //Flag para el estado del GPS
    boolean isGPSEnabled = false;
 
    //Flag para el estado de la red
    boolean isNetworkEnabled = false;
 
    boolean canGetLocation = false;
 
    Location location;
    double latitude;
    double longitude;
 
    //Mínima distancia para cambiar actualizaciones en metros
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
 
    //Tiempo mínimo en milisegundos entre actualizaciones
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
 
    protected LocationManager locationManager;
 
    public GetLocation(Context context) {
        this.mContext = context;
        getLocation();
    }
	
    public Location getLocation(){
    	try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
 
            //Se obtiene el estado del GPS
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
            //Se obtiene el estado de la red
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                //Obtenemos la localización del proveedor de la red
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                //Obtenemos la información del GPS si está activado
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                stopUsingGPS();
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return location;
    }
    
    /* Método que devuelve la latitud */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
         
        return latitude;
    }
    
    /* Método que devuelve la longitud */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        return longitude;
    }
    
    /* Método que libera al gps*/
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GetLocation.this);
        }      
    }
    
    
    /* Método que nos dice si hay acceso */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
    /* Método para la configuración de la alerta a mostrar si el gps desactivado */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
      
        //Nombre de la alerta 
        alertDialog.setTitle("Ajustes del GPS");
  
        //Mensaje de la alerta
        alertDialog.setMessage("El GPS no está activo. ¿Quiere ir al menú de ajustes?");
  
        // Si se presiona Ajustes se abre la ventana de ajustes del gps
        alertDialog.setPositiveButton("Ajustes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
  
        // Si se presiona cancelar se cierra la alerta
        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Se muestra la alerta
        alertDialog.show();
    }
    
    
    /* Métodos necesarios al implementar un locationlistener*/
	@Override
	public void onLocationChanged(Location location) {		
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
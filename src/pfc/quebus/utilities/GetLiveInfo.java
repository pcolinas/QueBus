package pfc.quebus.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class GetLiveInfo{
/*Función que hace la petición y que devuelve un array JSON*/
		public static JSONObject getLiveInfo(String url/*, String query*/){
			String data = new String();
	        BufferedReader in = null;
	        StringBuilder sBuilder = null;
	        HttpURLConnection urlConnection = null;		
	        
	        try {
	        	/*Se configura la conexión*/
	        	URL liveInfoURL = new URL(url);
	        	urlConnection = (HttpURLConnection) liveInfoURL.openConnection();
	        	
	        	urlConnection.setRequestMethod("GET");
	        	urlConnection.setReadTimeout(40000);
	        	urlConnection.setConnectTimeout(42000);
	        	urlConnection.connect(); 
	        		
	        	in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	        	sBuilder= new StringBuilder();
	        	
	             /*Leemos el archivo*/
	        	String inputLine;
	        	while ((inputLine = in.readLine()) != null)
	        		sBuilder.append(inputLine+ "\n");  
	        	in.close();
	        
	        	data = sBuilder.toString(); //Lo convertimos a String
	        
	        	
	        	try { 
	        		/* Se obtiene el objeto JSON con la información */
	        		JSONObject jObject = new JSONObject(data);
	   	 			
	   	 			return jObject; //Se devuelve el objeto hallado
	   	 			
	        	} catch (JSONException e) {
					e.printStackTrace();
					return null;
				}
	   	 			
	   	 		
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	            return null;
	        } catch (ProtocolException e) {
	            e.printStackTrace();
	            return null;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
	         finally {
	          urlConnection.disconnect();
	          in = null;
	          sBuilder = null;
	          urlConnection = null;          
	        }     
		}
}
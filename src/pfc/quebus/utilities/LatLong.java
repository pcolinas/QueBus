package pfc.quebus.utilities;


/* Clase para la conversión de proyecciones utm en coordenadas lat y long*/
public class LatLong {
	
	public double lat;
    public double lng;
    
    public LatLong(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }


/* Función que realiza la conversión */
public static LatLong UTMtoLatLong(double utmx, double utmy) {
        
		//Ajustes para el cambio de ED50 a ETRS89
		double diflat = -0.00189958d;
		double diflon = -0.00139999d;

		int zone = 30; //Huso en el que se encuentra Gijón
		double c_sa = 6378137.000000; //Semieje menor de la elipsoide WGS84
		double c_sb = 6356752.314245; //Semieje mayor de la elipsoide WGS84
		double e2 = Math.pow((Math.pow(c_sa, 2) - Math.pow(c_sb, 2)), 0.5)/ c_sb; //Segunda excentricidad
		double e2cuadrada = Math.pow(e2, 2); //Cuadrado de la 2ª excentricidad
		double c = Math.pow(c_sa, 2) / c_sb; //Radio Polar de Curvatura
		
		double x = utmx - 500000.0; //Se elimina el retranqueo del eje de las x
		double y = utmy; 
		double s = ((zone * 6.0) - 183.0); //meridiano central del huso
		
		//Parámetros necesarios para el cálculo
		double lat = y / (c_sa * 0.9996);
		double v = (c / Math.pow(1 + (e2cuadrada * Math.pow(Math.cos(lat), 2)),	0.5)) * 0.9996;
		double a = x / v;
		double a1 = Math.sin(2 * lat);
		double a2 = a1 * Math.pow((Math.cos(lat)), 2);

		double j2 = lat + (a1 / 2.0);
		double j4 = ((3 * j2) + a2) / 4.0;
		double j6 = ((5 * j4) + Math.pow(a2 * (Math.cos(lat)), 2)) / 3.0;
		double alfa = (3.0 / 4.0) * e2cuadrada;
		double beta = (5.0 / 3.0) * Math.pow(alfa, 2);

		double gama = (35.0 / 27.0) * Math.pow(alfa, 3);
		double bm = 0.9996 * c * (lat - alfa * j2 + beta * j4 - gama * j6);
		double b = (y - bm) / v;
		double epsi = ((e2cuadrada * Math.pow(a, 2)) / 2.0)	* Math.pow((Math.cos(lat)), 2);

		double eps = a * (1 - (epsi / 3.0));
		double nab = (b * (1 - epsi)) + lat;
		double senoheps = (Math.exp(eps) - Math.exp(-eps)) / 2.0;
		double delt = Math.atan(senoheps / (Math.cos(nab)));
		double tao = Math.atan(Math.cos(delt) * Math.tan(nab));

		//Cálculo final de las coordenadas
		double lng = ((delt * (180.0 / Math.PI)) + s) + diflon;
		double lati = ((lat + (1 + e2cuadrada * Math.pow(Math.cos(lat), 2) - (3.0 / 2.0)
				* e2cuadrada * Math.sin(lat) * Math.cos(lat) * (tao - lat))
				* (tao - lat)) * (180.0 / Math.PI))	+ diflat;
	      
        LatLong latlong = new LatLong(lati, lng);
        return latlong;
    }

}


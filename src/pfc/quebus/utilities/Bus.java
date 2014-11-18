package pfc.quebus.utilities;

 /*Objeto bus en el que almacenamos la informaci�n necesaria*/
public class Bus {
	
	public String idlinea;
    public String min;
    public String idparada;
    public String idProxParada;
    public String idtrayecto;
    public String nombreProxParada;
    public String matricula;
    public String color;
    public long id;
    
    /* Constructor */
    public Bus(String idlinea, String min, String idparada, String idtrayecto, String matricula, long id){
    	this.idlinea = idlinea;
    	this.min = min;
    	this.idparada = idparada;
    	this.idtrayecto = idtrayecto;
    	this.matricula = matricula;
    	this.id = id;
    }
    
    /* M�todo para obtener el id de la l�nea*/
    public String getIdLinea(){
    	return idlinea;
    }
    
    /* M�todo para obtener los minutos */
    public String getMin(){
    	return min;
    }
    
    /* M�todo para obtener el id de la parada actual */
    public String getStop(){
    	return idparada;
    }
    
    /* M�todo para obtener el id de la pr�xima parada */
    public String getNextStop(){
    	return idProxParada;
    }
    
    /* M�todo para obtener el id del trayecto */
    public String getIdJourney(){
    	return idtrayecto;
    }
    
    /* M�todo para obtener el nombre de la pr�xima parada */
    public String getNextStopName(){
    	return nombreProxParada;
    }
    
    /* M�todo para obtener la matr�cula */
    public String getMatricula() {		
		return matricula;
	}
    
    /* M�todo para obtener el color en hexadecimal */
    public String getColor(){
    	return color;
    }
    
    /* M�todo para obtener el id para la ordenaci�n posterior */
    public long getId(){
    	return id;
    }
    
    /* M�todo para modificar los minutos */
    public void setMin(String min){
    	this.min = min;
    }
    
    /* M�todo para modificar el id de parada */
    public void setIdStop(String idparada){
    	this.idparada =  idparada;
    }
    
    /* M�todo para modificar el id de la pr�xima parada */
    public void setIdNextStop(String idProxParada){
    	this.idProxParada =  idProxParada;
    }
    
    /* M�todo para modificar el nombre de la pr�xima parada */
    public void setNextStopName(String nombreProxParada){
    	this.nombreProxParada =  nombreProxParada;
    }
    
    /* M�todo para modificar el id del trayecto */
    public void setIdJourney(String idtrayecto){
    	this.idtrayecto =  idtrayecto;
    }
    
    /* M�todo para modificar el color */
    public void setColor(String color){
    	this.color = "#"+color;
    }
    
    /* M�todo para realizar la comparaci�n por el atributo minutos */
    public int compareTo(Bus bus) {
        int minutes = Integer.parseInt(((Bus) bus).getMin());
 
        return (Integer.parseInt(this.min) - minutes);
    }
      
}
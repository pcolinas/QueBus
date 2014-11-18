package pfc.quebus.utilities;

 /*Objeto bus en el que almacenamos la información necesaria*/
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
    
    /* Método para obtener el id de la línea*/
    public String getIdLinea(){
    	return idlinea;
    }
    
    /* Método para obtener los minutos */
    public String getMin(){
    	return min;
    }
    
    /* Método para obtener el id de la parada actual */
    public String getStop(){
    	return idparada;
    }
    
    /* Método para obtener el id de la próxima parada */
    public String getNextStop(){
    	return idProxParada;
    }
    
    /* Método para obtener el id del trayecto */
    public String getIdJourney(){
    	return idtrayecto;
    }
    
    /* Método para obtener el nombre de la próxima parada */
    public String getNextStopName(){
    	return nombreProxParada;
    }
    
    /* Método para obtener la matrícula */
    public String getMatricula() {		
		return matricula;
	}
    
    /* Método para obtener el color en hexadecimal */
    public String getColor(){
    	return color;
    }
    
    /* Método para obtener el id para la ordenación posterior */
    public long getId(){
    	return id;
    }
    
    /* Método para modificar los minutos */
    public void setMin(String min){
    	this.min = min;
    }
    
    /* Método para modificar el id de parada */
    public void setIdStop(String idparada){
    	this.idparada =  idparada;
    }
    
    /* Método para modificar el id de la próxima parada */
    public void setIdNextStop(String idProxParada){
    	this.idProxParada =  idProxParada;
    }
    
    /* Método para modificar el nombre de la próxima parada */
    public void setNextStopName(String nombreProxParada){
    	this.nombreProxParada =  nombreProxParada;
    }
    
    /* Método para modificar el id del trayecto */
    public void setIdJourney(String idtrayecto){
    	this.idtrayecto =  idtrayecto;
    }
    
    /* Método para modificar el color */
    public void setColor(String color){
    	this.color = "#"+color;
    }
    
    /* Método para realizar la comparación por el atributo minutos */
    public int compareTo(Bus bus) {
        int minutes = Integer.parseInt(((Bus) bus).getMin());
 
        return (Integer.parseInt(this.min) - minutes);
    }
      
}
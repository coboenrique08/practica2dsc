package practica2;

import static spark.Spark.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import redis.clients.jedis.Jedis;

public class Practica2 {
	
	static List<Medicion> mediciones = new ArrayList<Medicion>();
	
	static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST","localhost");	
	
	// Metodo para obtener todos los datos almacenados
	public synchronized static ArrayList<Medicion> obtenerDatos() {
		ArrayList<Medicion> lista = new ArrayList<Medicion>();
		try {
			Jedis jedis = new Jedis(REDIS_HOST);
			String fecha = "", dato="";
			
			long ult = jedis.llen("queue#fechas");
			for (long i=0; i<ult; i++) {
				fecha = jedis.lindex("queue#fechas", i);
				dato = jedis.lindex("queue#datos", i);
				
				Medicion med = new Medicion(fecha, dato);
				lista.add(med);
					
			}        	
			jedis.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		return lista;
	}
	
	// Metodo para anyadir un nuevo dato a los datos ya almacenados
	public static void anyadirNuevoDato(Medicion medicion) {
		
		try {
			//jedis.flushAll();	// Borra el contenido previo	
			
			Jedis jedis = new Jedis(REDIS_HOST);
			
			jedis.rpush("queue#fechas", medicion.getCuando());
			jedis.rpush("queue#datos", medicion.getValor() + "");
			
			
			jedis.close();
			
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		
	}
	
	// Metodo para obtener el formato de texto que se imprimira en /listar
	private static String imprimir(ArrayList<Medicion> datosObtenidos) {
		StringBuilder sb = new StringBuilder();
		for(Medicion med : datosObtenidos) {
			sb.append(med.toString());
			sb.append("<br />");
		}
		
		return sb.toString();
	}
	
	// Metodo para obtener el JSON a imprimir en /listajson
	private static Object medicionJSON() {
		JsonObject obj = new JsonObject();
		JsonArray objArray = new JsonArray();
		
		List<Medicion> mediciones = obtenerDatos();
		
		int tam = mediciones.size() - 10;
		if(tam < 0) {
			tam = 0;
		}
		for (int i=tam; i<mediciones.size(); i++) {
			obj.addProperty("time", mediciones.get(i).getCuando());
			obj.addProperty("valor", mediciones.get(i).getValor());
			objArray.add(obj);
			obj = new JsonObject();
		}

	    obj.add("Mediciones", objArray);
		return obj;
	}

	public static void main(String[] args) {
		
		//Conectamos con redis
		try {
			System.out.println("Conectando con " + REDIS_HOST);
			Jedis jedis = new Jedis(REDIS_HOST);
			//jedis.flushAll();	// Borra el contenido previo		
			jedis.close();
		} catch (Exception ex) {
			
		}
		
		System.out.println("Conectado con exito");
		
		
		//Recibe una nueva medición en :dato
        get("/nuevo/:dato", (req, res) -> {
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        	Date date = new Date(System.currentTimeMillis());
        	
        	Medicion medicion = new Medicion(formatter.format(date) , req.params(":dato"));
        	anyadirNuevoDato(medicion);
        	return "En la fecha " + date.toString() + " se ha registrado el nuevo valor " + medicion.getValor();
        });
        
        //Muestra en un listado de texto las mediciones almacenadas
        get("/listar", (req, res) -> {
        	return imprimir(obtenerDatos());
        });
        
        //Muestra una grafica con las ultimas 10 temperaturas
        get("/grafica", (req, res) -> {
        	return GraficaChart.crea_grafica();
        });
        
        //Proporciona un listado en formato JSON con las ultimas 10 mediciones
        get("/listajson", (req, res) -> {
        	return medicionJSON();
        });
    }
}

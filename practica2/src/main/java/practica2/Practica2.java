package practica2;

import static spark.Spark.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import redis.clients.jedis.Jedis;

public class Practica2 {
	
	static List<Medicion> mediciones = new ArrayList<Medicion>();
	
	static final String REDIS_HOST = System.getenv().getOrDefault("REDIS_HOST","localhost");	
	
	public static void escribirDatos() {
		try {
			Jedis jedis = new Jedis(REDIS_HOST);
			jedis.flushAll();
			
			for (int j=0;j<mediciones.size();j++) {
				DateFormat df = new SimpleDateFormat("M dd yyyy HH:mm:ss");
				Medicion act=mediciones.get(j);			
				
				jedis.rpush("queue#fechas", df.format(act.getCuando()));
				jedis.rpush("queue#datos", act.getValor());
				System.out.println("Escribiendo " + act.getValor());
			}
			jedis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
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
	
	private static String imprimir(ArrayList<Medicion> datosObtenidos) {
		StringBuilder sb = new StringBuilder();
		for(Medicion med : datosObtenidos) {
			sb.append(med.toString());
			sb.append("<br />");
		}
		
		return sb.toString();
	}
	
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
		
		try {
			System.out.println("Conectando con " + REDIS_HOST);
			Jedis jedis = new Jedis(REDIS_HOST);
			//jedis.flushAll();	// Borra el contenido previo		
			jedis.close();
		} catch (Exception ex) {
			
		}
		
		System.out.println("Conectado con exito");
		
		
        get("/nuevo/:dato", (req, res) -> {
        	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        	Date date = new Date(System.currentTimeMillis());
        	
        	Medicion medicion = new Medicion(formatter.format(date) , req.params(":dato"));
        	anyadirNuevoDato(medicion);
        	return "En la fecha " + date.toString() + " se ha registrado el nuevo valor " + medicion.getValor();
        });
        
        get("/listar", (req, res) -> {
        	return imprimir(obtenerDatos());
        });
        
        get("/grafica", (req, res) -> {
        	return GraficaChart.crea_grafica();
        });
        
        get("/listajson", (req, res) -> {
        	return medicionJSON();
        });
    }
}

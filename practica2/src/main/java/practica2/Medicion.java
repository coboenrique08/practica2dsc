package practica2;

public class Medicion {
	private String cuando;
	private String valor;
	
	public Medicion(String string, String valor) {
		this.cuando = string;
		this.valor = valor;
	}


	public String getCuando() {
		return cuando;
	}


	public void setCuando(String cuando) {
		this.cuando = cuando;
	}


	public String getValor() {
		return valor;
	}


	public void setValor(String valor) {
		this.valor = valor;
	}
	
	public String toString() {
		String res = "En la fecha " + cuando + " se registro el valor " + valor;
		return res;
	}
	
	
}

package practica2;

import java.io.IOException;

//Codigo de ayuda Java INCOMPLETO para la creacion de pagina web con Chart.js desde Java
//El método inicial a consultar es: String crea_grafica() que devuelve el código de la página
//web con la gráfica incluida.
//
//La clase Medicion ya ha sido suministrada en el ejemplo de Redis

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import be.ceau.chart.Chart;
import be.ceau.chart.LineChart;
import be.ceau.chart.color.Color;
import be.ceau.chart.data.LineData;
import be.ceau.chart.dataset.LineDataset;
import be.ceau.chart.enums.BorderCapStyle;
import be.ceau.chart.enums.BorderJoinStyle;

public class GraficaChart {
	private static int tam;

	private static LineData createLineData() {
		LineData ld=new LineData()				
				.addDataset(createLineDataset());

		// Etiquetas eje horizontal		
		ArrayList<Medicion> datos = Practica2.obtenerDatos();// Aqui debeis obtener vuestros datos o refactorizar para obtenerlos una sola vez
		tam = datos.size() - 10;
		if(tam < 0) {
			tam = 0;
		}
		for (int i=tam;i<datos.size();i++) {			
			ld.addLabel(datos.get(i).getCuando());
		}

		return ld;
	}


	
	private static LineDataset createLineDataset() {
	
		LineDataset lds= new LineDataset()
				.setLabel("Ultimas 10 Mediciones")
				.setLineTension(0.1f)
				.setBackgroundColor(new Color(75, 192, 192, 0.4))
				.setBorderColor(new Color(75,192,192,1))
				.setBorderCapStyle(BorderCapStyle.BUTT)
				.setBorderDashOffset(0.0f)
				.setBorderJoinStyle(BorderJoinStyle.MITER)
				.addPointBorderColor(new Color(75, 192, 192, 1))					
				.addPointBackgroundColor(new Color(255, 255, 255, 1))
				.addPointBorderWidth(1)
				.addPointHoverRadius(5)
				.addPointHoverBackgroundColor(new Color(75,192,192,1))
				.addPointHoverBorderColor(new Color(220,220,220,1))
				.addPointHoverBorderWidth(2)
				.addPointRadius(1)
				.addPointHitRadius(10)
				.setSpanGaps(false);

		ArrayList<Medicion> datos = Practica2.obtenerDatos();// Aqui debeis obtener vuestros datos
		tam = datos.size() - 10;
		if(tam < 0) {
			tam = 0;
		}
		for (int i=tam;i<datos.size();i++) {	
			lds.addData(new Integer(datos.get(i).getValor()).intValue());
		}
		
		return lds;
	}

	public static String inBrowser(Chart chart) throws IOException {

		if (!chart.isDrawable()) {
			return "chart is not drawable";
		}

		return createWebPage(chart.getType(), chart.toJson());
	}

 
	private static String createWebPage(String type, String json) {
		String line = System.getProperty("line.separator");
		return new StringBuilder()
				.append("<!DOCTYPE html>")
				.append(line)
				.append("<html lang='en'>")
				.append(line)
				.append("<head>")
				.append(line)
				.append("<meta charset='UTF-8'>")
				.append(line)
				.append("<title>Chart.java test page - ").append(type).append("</title>")
				.append(line)
				.append("<meta name='author' content='Nombre Estudiante'>")
				.append(line)
				.append("<script src='https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.0/Chart.js'></script>")
				.append(line)
				.append("<script>")
				.append("function r(e,t){return new Chart(document.getElementById(e).getContext('2d'),t)}")
				.append("</script>")
				.append(line)
				.append("</head>")
				.append(line)
				.append("<body>")
				.append(line)
				.append("<canvas id='c' style='border:1px solid #555;'></canvas>")
				.append(line)
				.append("<div><pre>").append(json).append("</pre></div>")
				.append(line)
				.append("<script>")
				.append(line)
				.append("var myLineChart=r('c', ").append(json).append(");")
				.append(line)
				.append("myLineChart.options.animation=false;")
				.append(line)
				.append("setInterval(function() {")
				.append(line)
				.append("var xhttp = new XMLHttpRequest();")
				.append(line)
				.append("xhttp.onreadystatechange = function() {")
				.append(line)
				.append("if (this.readyState == 4 && this.status == 200) {")
				.append(line)
				.append("obj = JSON.parse(xhttp.responseText);")
				.append(line)
				.append("for (i=0;i<obj.Mediciones.length;i++) {")
				.append(line)
				.append("myLineChart.data.datasets[0].data[i]=obj.Mediciones[i].valor;")
				.append(line)
				.append("myLineChart.data.labels[i]=new Date(obj.Mediciones[i].time).toLocaleString();")
				.append(line)
				.append("}}")
				.append(line)
				.append("};")
				.append(line)
				.append("xhttp.open(\"GET\", \"listajson\", true);")
				.append(line)
				.append("xhttp.send();")
				.append(line)
				.append("	    myLineChart.update();")
				.append(line)            	
         	.append("	  }, 2000);")
				.append(line)			
				.append("</script>")
				.append(line)
				.append("</body>")
				.append(line)
				.append("</html>")
				.toString();
	}

	public static String crea_grafica() throws IOException {
		LineChart chart = new LineChart();
		chart.setData(createLineData());
		String resultado=inBrowser(chart);
		return resultado;
	}


}

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Main {

	public static void main(String[] args) {

		generateFromFile();
	}

	private static void generateFromFile() {
		// File xmlFile = new File(FILE_PATH);
//		InputStream is = Main.class.getResourceAsStream("configurationCentral.xml");

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document doc = null;

		try {
			InputStream is = new FileInputStream("./configurationCentral.xml");
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(is);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		if (doc != null) {

			Node nNode = doc.getDocumentElement();

			String server = null;
			String generator_type_string = null;
			String lat_String = null;
			String lng_String = null;
			double lat = 200;
			double lng = 200;
			int maxDist = 0;
			int num_ger = 0;
			int interval_ger = 0;
			int minP = 0;
			int maxP = 0;
			int minPoints = 0;
			int maxPoints = 0;
			int minScreen = 0;
			int maxScreen = 0;
			int minSteps = 0;
			int maxSteps = 0;
			double latitude;
			double longitude;
			LatLng coord = null;

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				server = eElement.getElementsByTagName("server").item(0).getTextContent();

				generator_type_string = eElement.getElementsByTagName("gen_type").item(0).getTextContent();

				GenType generator_type = GenType.valueOf(generator_type_string);

				lat_String = eElement.getElementsByTagName("lat").item(0).getTextContent();
				lng_String = eElement.getElementsByTagName("lng").item(0).getTextContent();

				boolean validCoords = true;
				if (!lat_String.equals("") && !lng_String.equals("")) {
					lat = Double.parseDouble(lat_String);
					lng = Double.parseDouble(lng_String);
					validCoords = validCoords(lat, lng);
					if (validCoords)
						coord = new LatLng(lat, lng);
				}

				if (validCoords) {

					maxDist = Integer.parseInt(eElement.getElementsByTagName("max_dist").item(0).getTextContent());
					num_ger = Integer.parseInt(eElement.getElementsByTagName("num_ger").item(0).getTextContent());
					interval_ger = Integer.parseInt(eElement.getElementsByTagName("int_ger").item(0).getTextContent());
					minP = Integer.parseInt(eElement.getElementsByTagName("min_victims").item(0).getTextContent());
					maxP = Integer.parseInt(eElement.getElementsByTagName("max_victims").item(0).getTextContent());
					minPoints = Integer.parseInt(eElement.getElementsByTagName("min_points").item(0).getTextContent());
					maxPoints = Integer.parseInt(eElement.getElementsByTagName("max_points").item(0).getTextContent());
					minScreen = Integer.parseInt(eElement.getElementsByTagName("min_screen").item(0).getTextContent());
					maxScreen = Integer.parseInt(eElement.getElementsByTagName("max_screen").item(0).getTextContent());
					minSteps = Integer.parseInt(eElement.getElementsByTagName("min_steps").item(0).getTextContent());
					maxSteps = Integer.parseInt(eElement.getElementsByTagName("max_steps").item(0).getTextContent());

					if (validData(minP, maxP, minPoints, maxPoints, minScreen, maxScreen, minSteps, maxSteps)) {

						IGenerator generator = create_generator(server, generator_type, maxDist, num_ger, interval_ger, minP,
								maxP, minPoints, maxPoints, minScreen, maxScreen, minSteps, maxSteps);

						generator.generate(coord);

						System.out.println("Data has been successfully generated!");
					}
				}
			} else {
				System.err.println("It was not possible to read the file. Please try again.");
			}
		} else {
			System.err.println("It was not possible to read the file. Please try again.");
		}
	}
	
	/**
	 * Valida as coordenadas indicadas pelo utilizador
	 * @param lat - a latitude indicada
	 * @param lng - a longitude indicada
	 * @return - true se as coordenadas sao validas, false cc
	 */
	private static boolean validCoords(double lat, double lng) {
		if (lat < -90 || lat < -90) {
			System.err.println("Latitude must be between -85 and 85. Exiting system...");
			return false;
		}
		else {
			if (lng < -180 || lng < -180) {
				System.err.println("Longitude must be between -180 and 180. Exiting system...");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Valida os dados indicados pelo utilizador
	 * @param minP - minimo numero de vitimas
	 * @param maxP - maximo numero de vitimas
	 * @param minPoints - minimo numero de pontos por vitima
	 * @param maxPoints - maximo numero de pontos po vitima
	 * @param minScreen - maximo numero de toques ecra
	 * @param maxScreen - maximo numero de toques ecra
	 * @param minSteps - minimo numero de passos
	 * @param maxSteps - maximo numero de passos
	 * @return - true se os dados sao validos, false cc
	 */
	private static boolean validData(int minP, int maxP, int minPoints, int maxPoints, int minScreen,
			int maxScreen, int minSteps, int maxSteps) {

		boolean valid = true;
		if (maxP < minP) {
			valid = false;
			System.err.println("max_victims must be greater than or equal to min_victims. Exiting system...");
		}
		else {
			if (maxPoints < minPoints) {
				valid = false;
				System.err.println("max_points must be greater than or equal to min_points. Exiting system...");
			}
			else {
				if (maxScreen < minScreen) {
					valid = false;
					System.err.println("max_screen must be greater than or equal to min_screen. Exiting system...");
				}
				else {
					if (maxSteps < minSteps) {
						valid = false;
						System.err.println("max_steps must be greater than or equal to min_steps. Exiting system...");
					}
				}
			}
		}
		return valid;
	}

	private static IGenerator create_generator(String server, GenType generator_type, int maxDist,
			int num_ger, int interval, int minP, int maxP, int minPoints,
			int maxPoints, int minScreen, int maxScreen, int minSteps,
			int maxSteps) {
		IGenerator generator;
		switch (generator_type) {
		case CENTRAL:
			generator = new GeneratorCentral(server, maxDist, num_ger, interval,
					minP, maxP, minPoints, maxPoints, minScreen, maxScreen,
					minSteps, maxSteps);
			break;
//		case FIELD:
//			generator = new GeneratorField(maxDist, num_ger, interval,
//					minP, maxP, minPoints, maxPoints, minScreen, maxScreen,
//					minSteps, maxSteps);
//			break;
		default:
			generator = null;
			break;
		}
		return generator;
	}
}

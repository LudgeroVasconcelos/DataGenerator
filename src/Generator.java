
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Generator implements IGenerator {

	public static final int ONE_BATT_DECREASE = 1800000;
	public static final int METRES_PER_DEGREE_LAT = 111300;

	public final int RADIUS = 10;
	public final float FACT = 0.9f;
	
	private static final String ACCESSIBLE = "http://accessible-serv.lasige.di.fc.ul.pt/~lost/LostMap/index.php/rest/victims/legacy";
	private static final String LOCALHOST = "http://localhost/astarte/index.php/rest/victims/legacy";

	private String server;
	private int maxDist;
	protected int nodeId = 0;
	protected String mac;
	protected int num_ger;
	protected int interval;
	protected int minP;
	protected int maxP;
	protected int minPoints;
	protected int maxPoints;
	protected int minScreen;
	protected int maxScreen;
	protected int minSteps;
	protected int maxSteps;

	public Generator(String server, int maxDist, int num_ger, int interval, int minP, int maxP, int minPoints, int maxPoints,
			int minScreen, int maxScreen, int minSteps, int maxSteps) {

		this.server = server;
		this.interval = interval;
		this.maxDist = maxDist;
		this.maxP = maxP;
		this.maxPoints = maxPoints;
		this.maxScreen = maxScreen;
		this.maxSteps = maxSteps;
		this.minP = minP;
		this.minPoints = minPoints;
		this.minScreen = minScreen;
		this.minSteps = minSteps;
		this.num_ger = num_ger;

		mac = genMac();
	}

	/**
	 * Gera um mac address aleatoriamente
	 * 
	 * @return - a string que representa o mac address
	 */
	private static String genMac() {
		Random rand = new Random();
		byte[] macAddr = new byte[6];
		rand.nextBytes(macAddr);

		macAddr[0] = (byte) (macAddr[0] & (byte) 254);

		StringBuilder sb = new StringBuilder(18);
		for (byte b : macAddr) {

			if (sb.length() > 0)
				sb.append(":");

			sb.append(String.format("%02x", b));
		}

		return sb.toString();
	}

	/**
	 * Gera os dados
	 */
	@Override
	public abstract void generate(LatLng coord);

//	/**
//	 * Cria uma LatLng a partir da localizacao do dispositivo, se coord nao
//	 * inicializada ou se eh nao se trata da primeira iteracao de geracao de
//	 * dados
//	 * 
//	 * @param num_victims
//	 *            - numero de vitimas geradas anteriormente
//	 * @param coord
//	 *            - ultima localizacao
//	 * @param context
//	 *            - contexto da app
//	 * @return uma nova LatLng representando as coordenadas baseadas na
//	 *         localiza��o atual do dispositivo
//	 */
//	protected LatLng inic_coord(int num_victims, LatLng coord, Context context) {
////		if (num_victims > 0 || coord == null) {
////			Location loc = LocationServices.getBestLocation(context, interval);
////			if (loc == null)
////				return null;
////			else
////				coord = new LatLng(loc.getLatitude(), loc.getLongitude());
////		}
//		return coord;
//	}

	/**
	 * Gera informacao sobre vitimas
	 * 
	 * @param context
	 *            - o contexto da app
	 * @param victims
	 *            - a lista de vitimas geradas ate ao momento
	 * @param randInterval
	 *            - intervalo de tempo entre iteracao atual e a anterior, para
	 *            geracao de nova informacao sobre as vitimas
	 * @param coordE
	 *            - coordenada do canto superior direito da area afetada
	 * @param coordS
	 *            - coordenada do canto inferior esquerdo da area afetada
	 * @param lastVictimNodes
	 *            - lista dos ultimos nos gerados, das vitimas geradas ate ao
	 *            momento
	 * @param index_victim
	 *            - indice correspondente ah vitima cuja nova informacao vai ser
	 *            gerada, em lastVictimNodes
	 */
	protected abstract void generateVictim(List<VictimNode> victims, int randInterval, LatLng coordE, LatLng coordS,
			ArrayList<VictimNode> lastVictimNodes, int index_victim);

	/**
	 * Gera pontos para uma vitima
	 * 
	 * @param mac
	 *            - macAddress do dispositivo
	 * @param numPoints
	 *            - numero de pontos a gerar para cada vitima
	 * @param victim
	 *            - lista de pontos criados ate ao momento
	 * @param coord
	 *            - ultima localizacao da vitima
	 * @param interval
	 *            - valor base para o intervalo decorrido entre dois pontos
	 *            sucessivos, em torno do qual e calculado o proximo valor
	 * @param timestamps
	 *            - timestamps necessarias ah geracao de uma nova timestamp
	 * @param batt
	 *            - ultimo valor de bateria conhecido
	 */
	protected abstract void generatePoints(String mac, int numPoints, ArrayList<VictimNode> victim, LatLng coord,
			int interval, int steps, int screen, int batt, long... timestamps);

	/**
	 * Gera um booleano aleatoriamente
	 * 
	 * @return - o booleano gerado
	 */
	protected boolean genSafe() {
		Random rand = new Random();
		return rand.nextBoolean();
	}

	protected void insertPoints(final List<VictimNode> victims) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					HttpClient httpclient = HttpClientBuilder.create().build();
					
					String url = LOCALHOST;
					if(server.equalsIgnoreCase("accessible")){
						url = ACCESSIBLE;
					}
					
					HttpPost httppost = new HttpPost(url);

					// get messages from send queue and create auto-message
					// List<Message> messages =
					// environment.fetchMessagesFromQueue();
					// messages.add(environment.createTextMessage(""));
					JSONArray jsonArray = new JSONArray();

					for (VictimNode v : victims) {
						JSONObject json = victimToJsonObject(v);
						jsonArray.put(json);
					}

					String contents = jsonArray.toString();

					System.out.println("About to send: " + contents);

					// send request to webservice
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
					nameValuePairs.add(new BasicNameValuePair("data", contents));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity entity = response.getEntity();
					String body = EntityUtils.toString(entity, "UTF-8");

					System.out.println("Response: " + response.getStatusLine().getStatusCode());
					System.out.println("Response body: " + body);
					

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private static JSONObject victimToJsonObject(VictimNode v) {
		JSONObject json = new JSONObject();
		try {
			json.put("nodeid", v.node);
			json.put("timestamp", v.time);
			json.put("latitude", v.lat);
			json.put("longitude", v.lon);
			json.put("accuracy", genAccuracy());
			json.put("locationTimestamp", v.time);
			json.put("battery", v.battery);
			json.put("steps", v.steps);
			json.put("screen", v.screen);
			json.put("safe", v.safe);
			json.put("msg", v.message);
			// json.put("status", msg.getStatus());
			// json.put("statusTimestamp", msg.getStatusTime());
			// json.put("origin", msg.getOrigin());
			// json.put("target", msg.getTarget());
			// json.put("targetLatitude", msg.getTargetLatitude());
			// json.put("targetLongitude", msg.getTargetLongitude());
			// json.put("targetRadius", msg.getTargetRadius());

		} catch (JSONException e) {
			return null;
		}
		return json;
	}

	/**
	 * Gera um valor aleatorio entre min e max, inclusive
	 * 
	 * @param min
	 *            - o valor minimo
	 * @param max
	 *            - o valor maximo
	 * @return - um inteiro compreendido entre min e max, inclusive
	 * @requires - min <= max
	 */
	protected int genValue(int min, int max) {
		if (min == max)
			return min;
		else {
			Random rand = new Random();
			return rand.nextInt(max - min) + min;
		}
	}

	/**
	 * Gera um valor aleatorio entre min e max, inclusive
	 * 
	 * @param min
	 *            - o valor minimo
	 * @param max
	 *            - o valor maximo
	 * @return - um float compreendido entre min e max, inclusive
	 * @requires - min <= max
	 */
	private float genValue(float min, float max) {
		Random rand = new Random();
		float fact = rand.nextFloat();
		return fact * max + (1 - fact) * min;
	}

	/**
	 * Gera um numero aleatorio correspondente ao numero de toques no ecra
	 * (neste momento atraves de genValue(min, max))
	 * 
	 * @param min
	 *            - minimo de toques pretendido
	 * @param max
	 *            - maximo de toques pretendido
	 * @return - o numero de toques a gerar
	 */
	protected int genScreen(int min, int max) {
		return genValue(min, max);
	}

	/**
	 * Gera um numero aleatorio correspondente ao numero de passos (neste
	 * momento atraves de genValue(min, max))
	 * 
	 * @param min
	 *            - minimo de passos pretendido
	 * @param max
	 *            - maximo de passos pretendido
	 * @return - o numero de passos a gerar
	 */
	protected int genSteps(int min, int max) {
		return genValue(min, max);
	}

	/**
	 * Gera uma nova timestamp
	 * 
	 * @param interval
	 *            - intervalo de tempo decorrido entre a timestamp a gerar e o
	 *            momento atual
	 * @param now
	 *            - momento atual
	 * @return - a nova timestamp
	 */
	protected abstract long genTimestamp(int interval, long now);

	/**
	 * Gera um novo intervalo de tempo, calculado aleatoriamente a partir do
	 * valor base time_interval
	 * 
	 * @param time_interval
	 *            - valor base indicado para o intervalo de tempo
	 * @param fact_min
	 *            - factor minimo a manter de time_interval
	 * @param fact_max
	 *            - factor maximo a manter de time_interval
	 * @return - um inteiro que corresponde ao intervalo de tempo gerado
	 */
	protected int genInterval(int time_interval, float fact_min, float fact_max) {
		float fact = genValue(fact_min, fact_max);
		int rand_interval = Math.round(fact * time_interval);
		return rand_interval;
	}

	/**
	 * Devolve uma mensagem ou a string vazia, aleatoriamente
	 * 
	 * @return - a string correspondente ah mensagem
	 */
	protected String genMessage() {
		Random rand = new Random();
		boolean message = rand.nextBoolean();
		if (message) {
			return buildMessage();
		}
		return "";
	}

	/**
	 * Gera uma nova mensagem (A implementar)
	 * 
	 * @return - string correspondente a uma mensagem
	 */
	private String buildMessage() {
		return "a string";
	}

	/**
	 * Gera um numero aleatorio que corresponde ao numero de pontos a serem
	 * gerados (neste momento atraves de genValue(min, max))
	 * 
	 * @param min
	 *            - minimo de pontos pretendido
	 * @param max
	 *            - maximo de pontos pretendido
	 * @return - o numero de pontos a gerar
	 */
	protected int getNumPoints(int min, int max) {
		return genValue(min, max);
	}

	/**
	 * Gera um novo valor de bateria
	 * 
	 * @param last
	 *            - o ultimo valor de bateria conhecido
	 * @param last_timestamp
	 *            - o momento em que foi obtido o ultimo valor de bateria
	 * @param current_timestamp
	 *            - o momento atual
	 * @param fact
	 *            - o factor de diminuicao da bateria (quanto menor, mais
	 *            diminui)
	 * @return
	 */
	protected int generateBattery(int last, long last_timestamp, long current_timestamp, float fact) {
		int bat;
		if (last == -1) {
			Random rand = new Random();
			bat = rand.nextInt(101);
		} else {
			int dif = (int) (current_timestamp - last_timestamp);
			bat = last - (dif / ONE_BATT_DECREASE);
			bat = Math.round(bat * fact) > 0 ? Math.round(bat * fact) : 0;
		}
		return bat;
	}

	/**
	 * Gera um numero aleatorio que corresponde ao numero de vitimas a serem
	 * geradas (neste momento atraves de genValue(min, max))
	 * 
	 * @param min
	 *            - minimo de vitimas pretendido
	 * @param max
	 *            - maximo de vitimas pretendido
	 * @return - o numero de vitimas a gerar
	 */
	protected int generateNumVictims(int min, int max) {
		return genValue(min, max);
	}

	/**
	 * Gera aleatoriamente um valor de precisao de coordenadas
	 * 
	 * @return - o inteiro que corresponde ao valor de precisao
	 */
	protected static int genAccuracy() {
		Random rand = new Random();
		return rand.nextInt(500);
	}

	/**
	 * Devolve o factor de diminuicao da bateria (neste momento eh usada uma
	 * constante)
	 * 
	 * @return - o float correspondente ao factor de diminuicao da bateria a
	 *         usar
	 */
	protected float fact() {
		return FACT;
	}

	/**
	 * Determina a coordenada correspondente ao canto superior direito ou ao
	 * canto inferior esquerdo da area afetada, a partir do seu ponto central
	 * 
	 * @param coord
	 *            - a coordenada do ponto central
	 * @param i
	 *            - valor que indica a coordenada a calcular: canto superior
	 *            direito se i > 0, canto inferior esquerdo cc.
	 * @return - a coordenada calculada, de acordo com o valor de i
	 */
	protected LatLng getCoordES(LatLng coord, int i) {
		double catMetres = Math.sqrt(Math.pow(maxDist, 2) / 2);
		double degreesLat = catMetres / METRES_PER_DEGREE_LAT;
		double lat = -100;
		double degreesLng = -100;
		double lng = -200;
		if (i > 0) {
			lat = coord.getLatitude() + degreesLat;
			degreesLng = catMetres / (METRES_PER_DEGREE_LAT * Math.cos(lat));
			lng = coord.getLongitude() + degreesLng;
		} else {
			lat = coord.getLatitude() - degreesLat;
			degreesLng = catMetres / (METRES_PER_DEGREE_LAT * Math.cos(lat));
			lng = coord.getLongitude() - degreesLng;
		}
		return new LatLng(lat, lng);
	}

	/**
	 * Gera uma localizacao dentro de uma area retangular, cujo anto superior
	 * direito corresponde a coordE e cujo canto inferior esquerdo corresponde a
	 * coordS
	 * 
	 * @param coordE
	 *            - posicao do canto superior direito
	 * @param coordS
	 *            - posicao do canto inferior esquerdo
	 * @return - LatLng correspondente ah localizacao gerada
	 */
	protected LatLng generatePosition(LatLng coordE, LatLng coordS) {

		Random rand = new Random();
		double maxY = coordE.getLatitude() + 90;
		double minY = coordS.getLatitude() + 90;
		double maxX = coordE.getLongitude() + 180;
		double minX = coordS.getLongitude() + 180;
		double difY = maxY - minY;
		double lat = rand.nextDouble() * difY + minY;

		double difX = maxX - minX;
		double lng = rand.nextDouble() * difX + minX;

		LatLng coord = new LatLng(lat - 90, lng - 180);
		return coord;
	}

	/**
	 * Gera uma nova localizacao dentro de um circulo de tamanho variavel, cujo
	 * centro se encontra nas coordenadas coord
	 * 
	 * @param coord
	 *            - localizacao do centro do circulo
	 * @return - LatLng correspondente ah nova localizacao
	 */
	protected LatLng new_coord(LatLng coord) {
		double u = Math.random();
		double v = Math.random();
		double r = radius() / 111300;
		double w = r * Math.sqrt(u);
		double t = 2 * Math.PI * v;
		double x = w * Math.cos(t);
		double y = w * Math.sin(t);
		double x_adjust = x / Math.cos(coord.getLongitude());
		double new_lat = x_adjust + coord.getLatitude();
		double new_lng = y + coord.getLongitude();
		return new LatLng(new_lat, new_lng);
	}

	/**
	 * Devolve o raio do circulo em que um ponto deve ser gerado (neste momento
	 * eh usada uma constante)
	 * 
	 * @return - um double correspondente ao raio
	 */
	protected double radius() {
		return RADIUS;
	}

}
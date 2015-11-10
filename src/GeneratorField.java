//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.google.android.gms.maps.model.LatLng;
//
//import android.app.AlertDialog;
//import android.content.Context;
//
///**
// * Representa um gerador de dados semi-aleatorio, a ser utilizado no terreno, sendo os dados gerados a partir dos
// * dados reais do dispositivo (localizacao e mac address), gerados para o passado e com tempo real entre cada iteracao da geracao
// * @author Marta
// *
// */
public class GeneratorField {
//	
//	private static final int LAST_TIMESTAMP = 0;
//	private static final int TIME_NOW = 1;
//
//	public GeneratorField(int maxDist, int num_ger,
//			int interval, int minP, int maxP, int minPoints, int maxPoints,
//			int minScreen, int maxScreen, int minSteps, int maxSteps) {
//		super(maxDist, num_ger, interval, minP, maxP, minPoints,
//				maxPoints, minScreen, maxScreen, minSteps, maxSteps);
//	}
//
//	/**
//	 * Gera os dados, projetados num tempo passado
//	 */
//	@Override
//	public void generate(LatLng coord) {
//
//		int numVictims = generateNumVictims(minP, maxP);
//		List<VictimNode> victims = new ArrayList<VictimNode>(numVictims);
//		
//		for (int i = 0; i < num_ger; i++) {
//			// obter vertice do topo direito e do fundo esquerdo da area afetada
//			// janla afetada muda consoante posicao do dispositivo no local
//			coord = inic_coord(victims.size(), coord, context);
//			if(coord != null) {
//				LatLng coordE = getCoordES(coord, 1);
//				LatLng coordS = getCoordES(coord, 0);
//
//				ArrayList<VictimNode> lastVictimNodes = new ArrayList<VictimNode>();
//
//				//nao eh a primeira iteracao e ja ha informacao sobre as vitimas
//				if (victims.size() > 0) {
//					lastVictimNodes = new ArrayList<VictimNode>();
//					for (VictimNode lastNode : victims) {
//						//so se mantem visiveis as vitimas dentro da area de recepcao do dispositivo e ainda com bateria
//						if (!outOfWindow(lastNode.lat, lastNode.lon, coordE, coordS)
//								&& lastNode.battery > 0) {
//							lastVictimNodes.add(lastNode);
//						}
//					}
//					victims.clear();
//				}
//
//				// determina intervalo ao fim do qual havera nova geracao de dados
//				int rand_interval = genInterval(interval, 0.75f, 1.25f);
//				//gerar vitimas
//				for (int j = 0; j < numVictims; j++) {
//
//					generateVictim(context, victims, rand_interval, coordE, coordS,
//							lastVictimNodes, j);
//				}
//				//aguardar ate nova recepcao de dados
//				try {
//
//					Thread.sleep(rand_interval * 1000);
//
//				} catch (InterruptedException e) {
//					new AlertDialog.Builder(context).setMessage("Generator error")
//					.setTitle("Error").setCancelable(true).show();
//				}
//			}
//			else{
//				new AlertDialog.Builder(context)
//				.setMessage("It was not to possible to obtain a location. Please try again.")
//				.setTitle("Error").setCancelable(true).show();
//			}
//
//		}
//	}
//
//	/**
//	 * Gera informacao sobre vitimas
//	 * @param context - o contexto da app
//	 * @param victims - a lista de vitimas geradas ate ao momento
//	 * @param randInterval - intervalo de tempo entre iteracao atual e a anterior, para geracao de nova informacao sobre as vitimas
//	 * @param coordE - coordenada do canto superior direito da area afetada
//	 * @param coordS - coordenada do canto inferior esquerdo da area afetada
//	 * @param lastVictimNodes - lista dos ultimos nos gerados, das vitimas geradas ate ao momento
//	 * @param index_victim - indice correspondente ah vitima cuja nova informacao vai ser gerada, em lastVictimNodes
//	 */
//	@Override
//	protected void generateVictim(Context context, List<VictimNode> victims,
//			int randInterval, LatLng coordE, LatLng coordS,
//			ArrayList<VictimNode> lastVictimNodes, int index_victim) {
//
//		long time_now = System.currentTimeMillis();
//		//intervalo de tempo entre timestamp a gerar e momento atual
//		int vInterval = genInterval(randInterval, 0f, 1f);
//		long timestamp = genTimestamp(vInterval, time_now);
//
//		int numPoints = getNumPoints(minPoints, maxPoints);
//		String message = genMessage();
//		int steps = genSteps(minSteps, maxSteps);
//		int screen = genScreen(minScreen, maxScreen);
//		int last_battery = -1;
//		String victimMac;
//		// index nao superior a numero de vitimas que se mantem na area e com
//		// bateria
//		if (index_victim < lastVictimNodes.size()) {
//			VictimNode vLastNode = lastVictimNodes.get(index_victim);
//			last_battery = vLastNode.battery;
//			victimMac = vLastNode.node;
//		}
//		// houve vitimas que nao se mantiveram na area; geram-se novas
//		// vitimas ate perfazer total
//		else {
//			StringBuilder macSb = new StringBuilder();
//			macSb.append(mac);
//			macSb.append("_" + nodeId);
//			nodeId++;
//			victimMac = macSb.toString();
//		}
//		int batt = generateBattery(last_battery, 0, 0, 1);
//
//		ArrayList<VictimNode> victim = new ArrayList<VictimNode>();
//
//		LatLng coord = generatePosition(coordE, coordS);
//
//		victim.add(new VictimNode(victimMac, coord.latitude, coord.longitude,
//				timestamp, message, steps, screen, batt, false));
//		numPoints--;
//		//atualiza BD com primeiro ponto desta vitima nesta iteracao
//		//updateDB(context, victim.get(0));
//
//		//gera restantes pontos a partir do primeiro
//		generatePoints(victimMac, numPoints, victim, coord, randInterval,
//				steps, screen, batt, timestamp, time_now);
//
//		/*
//		//atualiza BD
//		for (VictimNode node : victim) {
//			updateDB(context, node);
//		}
//		*/
//		insertPoints(victim);
//		//guarda informacao sobre o ultimo no da vitima
//		victims.add(victim.get(victim.size() - 1));
//	}
//	
//	/**
//	 * Gera pontos para uma vitima
//	 * @param mac - macAddress do dispositivo
//	 * @param numPoints - numero de pontos a gerar para cada vitima
//	 * @param victim - lista de pontos criados ate ao momento
//	 * @param coord - ultima localizacao da vitima
//	 * @param interval - valor base para o intervalo decorrido entre dois pontos sucessivos, em torno do qual eh calculado o proximo valor
//	 * @param timestamps - timestamps necessarias ah geracao de uma nova timestamp; na primeira posicao eh esperado o valor da timestamp do ultimo ponto e
//	 * 						na segunda a timestamp correspondente ao momento atual
//	 * @param batt - ultimo valor de bateria conhecido
//	 */
//	@Override
//	protected void generatePoints(String mac, int numPoints,
//			ArrayList<VictimNode> victim, LatLng coord, int interval,
//			int steps, int screen, int batt, long... timestamps) {
//
//		long last_timestamp =  timestamps[LAST_TIMESTAMP];
//		long time_now = timestamps[TIME_NOW];
//		//gera novos pontos apenas se nao atingiu total e se a ultima timestamp eh ainda inferior ao tempo atual
//		if (numPoints > 0 && last_timestamp < time_now) {
//			LatLng position = new_coord(coord);
//
//			int new_interval = genInterval(interval, 0f, 1f);
//			long timestamp = genTimestamp(new_interval, time_now);
//
//			String message = genMessage();
//			steps = genSteps(0, 5);
//			screen = genScreen(0, 10);
//			float fact = fact();
//			int battery = generateBattery(batt, last_timestamp, timestamp, fact);
//			boolean safe = genSafe();
//			victim.add(new VictimNode(mac, position.latitude,
//					position.longitude, timestamp, message, steps, screen,
//					battery, safe));
//
//			numPoints--;
//			generatePoints(mac, numPoints, victim, position, new_interval,
//					steps, screen, battery, timestamp, time_now);
//		}
//	}
//	
//	/**
//	 * Verifica se uma dada localizacao se encontra fora da area retangular definida por coordE e coordS
//	 * @param lat - a latitude da localizacao
//	 * @param lon - a longitude da localizacao
//	 * @param coordE - o canto superior direito da area
//	 * @param coordS - o canto inferior esquerdo da area
//	 * @return - verdadeiro se a localizacao se encontra fora da area; false cc.
//	 */
//	private boolean outOfWindow(double lat, double lon, LatLng coordE,
//			LatLng coordS) {
//		if (lat > coordE.latitude || lon > coordE.longitude
//				|| lat < coordS.latitude || lon < coordS.longitude)
//			return true;
//		return false;
//	}
//	
//	/**
//	 * Gera uma nova timestamp, que corresponde a um tempo anterior ao tempo atual
//	 * @param interval - intervalo de tempo decorrido entre a timestamp a gerar e o momento atual
//	 * @param now - momento atual
//	 * @return - a nova timestamp
//	 */
//	@Override
//	protected long genTimestamp(int interval, long now) {
//		return now - interval * 1000;
//	}
//
}


import java.util.ArrayList;
import java.util.List;

/**
 * Representa um gerador aleatorio, a ser usado na central, que gera dados para um tempo futuro, populando a BD
 * @author Marta
 *
 */
public class GeneratorCentral extends Generator {

	private static final int LAST_TIMESTAMP = 0;
	
	public GeneratorCentral(String server, int maxDist, int num_ger,
			int interval, int minP, int maxP, int minPoints, int maxPoints,
			int minScreen, int maxScreen, int minSteps, int maxSteps) {
		super(server, maxDist, num_ger, interval, minP, maxP, minPoints,
				maxPoints, minScreen, maxScreen, minSteps, maxSteps);
	}
	
	/**
	 * Gera os dados, projetados num tempo futuro
	 */
	@Override
	public void generate(LatLng coord) {

		int numVictims = generateNumVictims(minP, maxP);
		List<VictimNode> victims = new ArrayList<VictimNode>(numVictims);

		//obter vertice do topo direito e do fundo esquerdo da area afetada
		// janela fixa onde sao geradas sucessivas vitimas e respetivos pontos
//		coord = inic_coord(victims.size(), coord, context);
		if (coord != null) {
			LatLng coordE = getCoordES(coord, 1);
			LatLng coordS = getCoordES(coord, 0);

			for (int i = 0; i < num_ger; i++) {

				ArrayList<VictimNode> lastVictimNodes = new ArrayList<VictimNode>();

				//nao eh a primeira iteracao e ja ha informacao sobre as vitimas
				if (victims.size() > 0) {
					lastVictimNodes = new ArrayList<VictimNode>();
					for (VictimNode lastNode : victims) {
						//so se mantem as vitimas ainda com bateria
						if (lastNode.battery > 0) {
							lastVictimNodes.add(lastNode);
						}
					}
					victims.clear();
				}
				for (int j = 0; j < numVictims; j++) {

					generateVictim(victims, interval, coordE, coordS,
							lastVictimNodes, j);
				}
			}
		}
		else {
			System.err.println("It was not able to obtain a location. Please try again.");
		}
	}

	/**
	 * Gera informacao sobre vitimas
	 * @param context - o contexto da app
	 * @param victims - a lista de vitimas geradas ate ao momento
	 * @param randInterval - intervalo de tempo entre iteracao atual e a anterior, para geracao de nova informacao sobre as vitimas
	 * @param coordE - coordenada do canto superior direito da area afetada
	 * @param coordS - coordenada do canto inferior esquerdo da area afetada
	 * @param lastVictimNodes - lista dos ultimos nos gerados, das vitimas geradas ate ao momento
	 * @param index_victim - indice correspondente ah vitima cuja nova informacao vai ser gerada, em lastVictimNodes
	 */
	@Override
	protected void generateVictim(List<VictimNode> victims,
			int randInterval, LatLng coordE, LatLng coordS,
			ArrayList<VictimNode> lastVictimNodes, int index_victim) {

		//obter tempo atual
		long timestamp = System.currentTimeMillis();
		int numPoints = getNumPoints(minPoints, maxPoints);
		String message = genMessage();
		int steps = genSteps(minSteps, maxSteps);
		int screen = genScreen(minScreen, maxScreen);
		int last_battery = -1;
		String victimMac;
		// index nao superior a numero de vitimas que se mantem na area e com
		// bateria
		if (index_victim < lastVictimNodes.size()) {
			VictimNode vLastNode = lastVictimNodes.get(index_victim);
			last_battery = vLastNode.battery;
			victimMac = vLastNode.node;
			//intervalo de tempo entre ultima timestamp e timestamp a gerar
			int vInterval = genInterval(randInterval, 0.75f, 1.25f);
			timestamp = genTimestamp(vInterval, vLastNode.time);
		}
		// houve vitimas que nao se mantiveram; geram-se novas
		// vitimas ate perfazer total
		else {
			StringBuilder macSb = new StringBuilder();
			macSb.append(mac);
			macSb.append("_" + nodeId);
			nodeId++;
			victimMac = macSb.toString();
		}

		int batt = generateBattery(last_battery, 0, 0, 1);

		ArrayList<VictimNode> victim = new ArrayList<VictimNode>();

		LatLng coord = generatePosition(coordE, coordS);

		victim.add(new VictimNode(victimMac, coord.getLatitude(), coord.getLongitude(),
				timestamp, message, steps, screen, batt, false));
		numPoints--;
		//atualiza BD com primeiro ponto desta vitima nesta iteracao
		//updateDB(context, victim.get(0));
		
		//gera restantes pontos a partir do primeiro
		generatePoints(victimMac, numPoints, victim, coord, interval,
				steps, screen, batt, timestamp);

		//atualiza BD
		/*
		for (VictimNode node : victim) {
			updateDB(context, node);
		}
		*/
		insertPoints(victim);
		//guarda informacao sobre o ultimo no da vitima
		victims.add(victim.get(victim.size() - 1));
	}
	
	/**
	 * Gera pontos para uma vitima
	 * @param mac - macAddress do dispositivo
	 * @param numPoints - numero de pontos a gerar para cada vitima
	 * @param victim - lista de pontos criados ate ao momento
	 * @param coord - ultima localizacao da vitima
	 * @param interval - valor base para o intervalo decorrido entre dois pontos sucessivos, em torno do qual e calculado o proximo valor
	 * @param timestamps - timestamps necessarias ah geracao de uma nova timestamp; na primeira posicao eh esperado o valor timestamp do ultimo ponto
	 * @param batt - ultimo valor de bateria conhecido
	 */
	@Override
	protected void generatePoints(String mac, int numPoints,
			ArrayList<VictimNode> victim, LatLng coord, int interval,
			int steps, int screen, int batt, long... timestamps) {
		
		long last_timestamp = timestamps[LAST_TIMESTAMP];

		//gera novos pontos apenas se nao atingiu total
		if (numPoints > 0) {
			LatLng position = new_coord(coord);

			int new_interval = genInterval(interval, 0.75f, 1.25f);
			long timestamp = genTimestamp(new_interval, last_timestamp);
			String message = genMessage();
			steps = genSteps(0, 5);
			screen = genScreen(0, 10);
			float fact = fact();
			int battery = generateBattery(batt, last_timestamp, timestamp, fact);
			boolean safe = genSafe();
			victim.add(new VictimNode(mac, position.getLatitude(),
					position.getLongitude(), timestamp, message, steps, screen,
					battery, safe));

			numPoints--;
			generatePoints(mac, numPoints, victim, position, new_interval,
					steps, screen, battery, timestamp);
		}
	}

	/**
	 * Gera uma nova timestamp, que corresponde a um tempo posterior ao tempo atual
	 * @param interval - intervalo de tempo decorrido entre a timestamp a gerar e o momento atual
	 * @param now - momento atual
	 * @return - a nova timestamp
	 */
	@Override
	protected long genTimestamp(int interval, long now) {
		return now + interval * 1000;
	}

}

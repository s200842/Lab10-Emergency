package it.polito.tdp.emergency.model;

import it.polito.tdp.emergency.db.FieldHospitalDAO;
import it.polito.tdp.emergency.simulation.Core;
import it.polito.tdp.emergency.simulation.Evento;
import it.polito.tdp.emergency.simulation.Paziente;

public class Model {
	
	private FieldHospitalDAO dao;
	private Core simulatore;
	
	public Model(){
		dao = new FieldHospitalDAO();
		simulatore = new Core();
		//Aggiungo i pazienti alla lista pazienti
		for(Paziente p : dao.getPazienti()){
			simulatore.aggiungiPaziente(p);
		}
		//Aggiungo gli arrivi alla lista di eventi
		for(Evento e : dao.getArrivi()){
			simulatore.aggiungiEvento(e);
		}
	}

}

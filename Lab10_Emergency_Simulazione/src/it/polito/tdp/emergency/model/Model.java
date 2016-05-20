package it.polito.tdp.emergency.model;

import java.util.List;
import java.util.Set;

import it.polito.tdp.emergency.db.FieldHospitalDAO;
import it.polito.tdp.emergency.simulation.Core;
import it.polito.tdp.emergency.simulation.Dottore;
import it.polito.tdp.emergency.simulation.Evento;
import it.polito.tdp.emergency.simulation.Paziente;

public class Model {
	
	private FieldHospitalDAO dao;
	private Core simulatore;
	
	public Model(){
		dao = new FieldHospitalDAO();
		simulatore = new Core();
	}
	
	public void startSimulation(Set<Dottore> dott, List<Evento> ev){
		//OPERAZIONI PRELIMINARI
		//Aggiungo i pazienti alla lista pazienti
		for(Paziente p : dao.getPazienti()){
			simulatore.aggiungiPaziente(p);
		}
		//Aggiungo gli arrivi e gli inizi dei turni alla lista di eventi
		for(Evento e : dao.getArrivi()){
			simulatore.aggiungiEvento(e);
		}
		for(Evento e : ev){
			simulatore.aggiungiEvento(e);
		}
		//Aggiungo i dottori alla mappa di dottori
		for(Dottore d : dott){
			simulatore.aggiungiDottore(d);
		}
		
		simulatore.simula();
	}
	
	public int getSalvi(){
		return simulatore.getPazientiSalvati();
	}
	
	public int getPersi(){
		return simulatore.getPazientiPersi();
	}
	
	public void clear(){
		simulatore.clear();
	}

}

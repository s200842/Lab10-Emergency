//Simulatore vero e proprio, gestisce eventi ed entità relative

package it.polito.tdp.emergency.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import it.polito.tdp.emergency.simulation.Dottore.StatoDottore;
import it.polito.tdp.emergency.simulation.Evento.TipoEvento;
import it.polito.tdp.emergency.simulation.Paziente.StatoPaziente;

public class Core {

	//Variabili quantitative
	int pazientiSalvati = 0;
	int pazientiPersi = 0;
	
	//Lista eventi da processare uno dopo l'altro in ordine temporale
	Queue<Evento> listaEventi = new PriorityQueue<Evento>();
	
	//Mappa codPaziente-paziente, idem per dottori
	Map<Integer, Paziente> pazienti = new HashMap<Integer, Paziente>();
	Map<Integer, Dottore> dottori = new HashMap<Integer, Dottore>();
	
	//Coda per gestire la priorità di trattamento dei pazienti
	Queue<Paziente> pazientiInAttesa = new PriorityQueue<Paziente>();
	Queue<Dottore> mediciDisponibili = new PriorityQueue<Dottore>();

	//Aggiunta eventi, dottori e pazienti alle liste
	public void aggiungiEvento(Evento e) {
		listaEventi.add(e);
	}

	public void aggiungiPaziente(Paziente p) {
		pazienti.put(p.getId(), p);
	}
	
	public void aggiungiDottore(Dottore d){
		dottori.put(d.getIdDottore(), d);
	}
	
	//Getters - setters
	public int getPazientiSalvati() {
		return pazientiSalvati;
	}

	public int getPazientiPersi() {
		return pazientiPersi;
	}


	public int getMediciDisponibili() {
		return mediciDisponibili.size();
	}

	//Entità fondamentale del simulatore -- COSA FA --
	public void passo() {
		//Prendo il primo evento in coda e in base al tipo di evento agisco di conseguenza
		Evento e = listaEventi.remove();
		switch (e.getTipo()) {
		//Se arriva un paziente
		case PAZIENTE_ARRIVA:
			System.out.println("Arrivo paziente:" + e);
			//Aggiungo il paziente a quelli in attesa in base alla sua priorità
			pazientiInAttesa.add(pazienti.get(e.getDato()));
			//Imposto la morte del paziente in base alla gravità della situazione
			switch (pazienti.get(e.getDato()).getStato()) {
			case BIANCO:
				this.aggiungiEvento(new Evento(e.getTempo() + 6 * 60, Evento.TipoEvento.PAZIENTE_GUARISCE, e.getDato()));
				break;
			case GIALLO:
				this.aggiungiEvento(new Evento(e.getTempo() + 6 * 60, Evento.TipoEvento.PAZIENTE_MUORE, e.getDato()));
				break;
			case ROSSO:
				this.aggiungiEvento(new Evento(e.getTempo() + 1 * 60, Evento.TipoEvento.PAZIENTE_MUORE, e.getDato()));
				break;
			case VERDE:
				this.aggiungiEvento(new Evento(e.getTempo() + 12 * 60, Evento.TipoEvento.PAZIENTE_MUORE, e.getDato()));
				break;
			default:
				System.err.println("Panik!");
			}
			break;
		//Se il paziente è stato curato e guarisce
		case PAZIENTE_GUARISCE:
			//Se era bianco guarisce da solo
			if(pazienti.get(e.getDato()).getStato() == StatoPaziente.BIANCO){
				System.out.println("Paziente guarito: "+ e);
				pazientiSalvati ++;
				break;
			}
			//Se non è morto (inserito per evitare stampa di eventi superflui)
			if (pazienti.get(e.getDato()).getStato() != Paziente.StatoPaziente.NERO) {
				System.out.println("Paziente salvato: " + e);
				//Aggiorno lo stato del paziente (per non farlo "morire" erroneamente dopo")
				pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.SALVO);
				//Libero il medico che lo ha curato ed aggiorno il numero di pazienti salvati
				Dottore d = dottori.get(pazienti.get(e.getDato()).getCuratore().getIdDottore());
				if(d.getStato() == StatoDottore.DOCTOR_CURA){
					//Il dottore ha già finito il turno e ha fatto straordinari, non lo devo aggiungere alla coda
					d.setStato(StatoDottore.DOCTOR_OUT);
				}
				else{
					d.setStato(StatoDottore.DOCTOR_AVAILABLE);
					mediciDisponibili.add(dottori.get(pazienti.get(e.getDato()).getCuratore().getIdDottore()));
				}
				++pazientiSalvati;
			}
			break;
		//Se il paziente muore	
		case PAZIENTE_MUORE:
			//Se il paziente è stato curato evito di stamparne l'evento "morto" programmato in precedenza
			if (pazienti.get(e.getDato()).getStato() == Paziente.StatoPaziente.SALVO) {
				System.out.println("Paziente già salvato: " + e);
			} else {
				//Se muore sotto i ferri, libero il medico che lo stava operando
				if (pazienti.get(e.getDato()).getStato() == Paziente.StatoPaziente.IN_CURA) {
					mediciDisponibili.add(pazienti.get(e.getDato()).getCuratore());
				}
				//Se non è stato curato aggiorno i pazienti morti ed imposto il suo stato su nero (morto)
				++pazientiPersi;
				pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.NERO);
				System.out.println("Paziente morto: " + e);
			}
			break;
		case DOCTOR_INIZIO_TURNO:
			//Il dottore diventa disponibile. Aggiorno i medici disponibili ed imposto la fine del turno
			dottori.get(e.getDato()).setStato(StatoDottore.DOCTOR_AVAILABLE);
			mediciDisponibili.add(dottori.get(e.getDato()));
			System.out.println("Inizio turno: "+e.toString());
			this.aggiungiEvento(new Evento((e.getTempo()+(8*60)), TipoEvento.DOCTOR_FINE_TURNO, e.getDato()));
			break;
		case DOCTOR_FINE_TURNO:
			//Tolgo il dottore da quelli disponibili ed imposto l'inizio del prossimo turno SE non sta operando.
			Dottore d = dottori.get(e.getDato());
			if(d.getStato() == StatoDottore.DOCTOR_AVAILABLE){
				d.setStato(StatoDottore.DOCTOR_OUT);
				mediciDisponibili.remove(d);
				this.aggiungiEvento(new Evento((long)(e.getTempo()+16*60), TipoEvento.DOCTOR_INIZIO_TURNO, d.getIdDottore()));
			}
			else{
				mediciDisponibili.remove(d);
				this.aggiungiEvento(new Evento((long)(e.getTempo()+16*60), TipoEvento.DOCTOR_INIZIO_TURNO, d.getIdDottore()));
			}
			System.out.println("Fine turno: "+e.toString());
			break;	
		default:
			System.err.println("Panik!");
		}
		//Finchè ho medici disponibili e pazienti da curare curo tutti i pazienti che posso
		while (cura(e.getTempo()));
	}

	protected boolean cura(long adesso) {
		//Se non ho pazienti in attesa o non ho medici non curo nessuno
		if (pazientiInAttesa.isEmpty())
			return false;
		if (this.getMediciDisponibili() == 0)
			return false;
		//Se ne ho la possibilitò curo il paziente con la priorità più alta se non è ancora morto
		Paziente p = pazientiInAttesa.remove();
		if (p.getStato() != Paziente.StatoPaziente.NERO) {
			//Tolgo un medico da quelli disponibili ed imposto lo stato in cura, con relativo evento
			Dottore d = mediciDisponibili.remove();
			pazienti.get(p.getId()).setStato(Paziente.StatoPaziente.IN_CURA);
			dottori.get(d.getIdDottore()).setStato(StatoDottore.DOCTOR_CURA);
			pazienti.get(p.getId()).setCuratore(d);
			aggiungiEvento(new Evento(adesso + 30, Evento.TipoEvento.PAZIENTE_GUARISCE, p.getId()));
			System.out.println("Inizio a curare: " + p);
		}
		return true;
	}
	
	//Avvia la simulazione
	public void simula() {
		while (!listaEventi.isEmpty()) {
			//Se ho trattato tutti i pazienti smetto
			if((pazientiSalvati + pazientiPersi) < pazienti.size()){
				passo();
			}
			else break;
		}
	}
	
	public void clear(){
		listaEventi.clear();
		dottori.clear();
		pazienti.clear();
		mediciDisponibili.clear();
		pazientiInAttesa.clear();
		pazientiSalvati = 0;
		pazientiPersi = 0;
	}

}

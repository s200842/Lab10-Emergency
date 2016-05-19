//Simulatore vero e proprio, gestisce eventi ed entità relative

package it.polito.tdp.emergency.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class Core {

	//Lista eventi da processare uno dopo l'altro in ordine temporale
	Queue<Evento> listaEventi = new PriorityQueue<Evento>();
	
	//Mappa codPaziente-paziente
	Map<Integer, Paziente> pazienti = new HashMap<Integer, Paziente>();
	
	//Coda per gestire la priorità di trattamento dei pazienti
	Queue<Paziente> pazientiInAttesa = new PriorityQueue<Paziente>();
	
	//Variabili quantitative
	int mediciDisponibili = 0;
	int pazientiSalvati = 0;
	int pazientiPersi = 0;
	
	//Getters - setters
	public int getPazientiSalvati() {
		return pazientiSalvati;
	}

	public int getPazientiPersi() {
		return pazientiPersi;
	}


	public int getMediciDisponibili() {
		return mediciDisponibili;
	}

	public void setMediciDisponibili(int mediciDisponibili) {
		this.mediciDisponibili = mediciDisponibili;
	}

	//Aggiunta eventi e pazienti alle liste
	public void aggiungiEvento(Evento e) {
		listaEventi.add(e);
	}

	public void aggiungiPaziente(Paziente p) {
		pazienti.put(p.getId(), p);
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
			//Se non è morto (inserito per evitare stampa di eventi superflui)
			if (pazienti.get(e.getDato()).getStato() != Paziente.StatoPaziente.NERO) {
				System.out.println("Paziente salvato: " + e);
				//Aggiorno lo stato del paziente (per non farlo "morire" erroneamente dopo")
				pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.SALVO);
				//Libero il medico che lo ha curato ed aggiorno il numero di pazienti salvati
				++mediciDisponibili;
				++pazientiSalvati;
			}
			break;
		//Se il paziente muore	
		case PAZIENTE_MUORE:
			//Se il paziente è stato curato evito di stamparne l'evento "morto" programmato in precedenza
			if (pazienti.get(e.getDato()).getStato() == Paziente.StatoPaziente.SALVO) {
				System.out.println("Paziente già salvato: " + e);
			} else {
				//Se muore mentre era in cura, libero il medico che lo stava operando
				if (pazienti.get(e.getDato()).getStato() == Paziente.StatoPaziente.IN_CURA) {
					++mediciDisponibili;
				}
				//Se non è stato curato aggiorno i pazienti morti ed imposto il suo stato su nero (morto)
				++pazientiPersi;
				pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.NERO);
				System.out.println("Paziente morto: " + e);
				
			}
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
		if (mediciDisponibili == 0)
			return false;
		//Se ne ho la possibilitò curo il paziente con la priorità più alta se non è ancora morto
		Paziente p = pazientiInAttesa.remove();
		if (p.getStato() != Paziente.StatoPaziente.NERO) {
			//Tolgo un medico da quelli disponibili ed imposto lo stato in cura, con relativo evento
			--mediciDisponibili;
			pazienti.get(p.getId()).setStato(Paziente.StatoPaziente.IN_CURA);
			aggiungiEvento(new Evento(adesso + 30, Evento.TipoEvento.PAZIENTE_GUARISCE, p.getId()));
			System.out.println("Inizio a curare: " + p);
		}

		return true;
	}
	
	//Avvia la simulazione
	public void simula() {
		while (!listaEventi.isEmpty()) {
			passo();
		}
	}

}

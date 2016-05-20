//Simulatore vero e proprio, gestisce eventi ed entità relative

package it.polito.tdp.emergency.simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import it.polito.tdp.emergency.simulation.Assistente.StatoAssistente;
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
	Map<Integer, Assistente> assistenti = new HashMap<Integer, Assistente>();
	
	//Coda per gestire la priorità di trattamento dei pazienti
	Queue<Paziente> pazientiInAttesa = new PriorityQueue<Paziente>();
	Queue<Dottore> mediciDisponibili = new PriorityQueue<Dottore>();
	Queue<Assistente> assistentiDisponibili = new PriorityQueue<Assistente>();

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
	
	public void aggiungiAssistente(Assistente a){
		assistenti.put(a.getIdAssistente(), a);
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
	
	public int getAssistentiDisponibili(){
		return assistentiDisponibili.size();
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
				//Aggiorno lo stato del paziente (per non farlo "morire" erroneamente dopo)
				pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.SALVO);
				//Controllo se è stato curato da un assistente
				if(pazienti.get(e.getDato()).getAssistenteCuratore() != null){
					Assistente a = assistenti.get(pazienti.get(e.getDato()).getAssistenteCuratore().getIdAssistente());
					if(a.getStato() == StatoAssistente.ASSISTANT_CURA_OLTRE){
						//L'assistente ha già finito il turno e ha fatto straordinari, non lo devo aggiungere alla coda
						a.setStato(StatoAssistente.ASSISTANT_OUT);
					}
					else{
						a.setStato(StatoAssistente.ASSISTANT_AVAILABLE);
						assistentiDisponibili.add(assistenti.get(pazienti.get(e.getDato()).getAssistenteCuratore().getIdAssistente()));
					}
				}
				else{
					//Libero il medico che lo ha curato ed aggiorno il numero di pazienti salvati
					Dottore d = dottori.get(pazienti.get(e.getDato()).getCuratore().getIdDottore());
					if(d.getStato() == StatoDottore.DOCTOR_CURA_OLTRE){
						//Il dottore ha già finito il turno e ha fatto straordinari, non lo devo aggiungere alla coda
						d.setStato(StatoDottore.DOCTOR_OUT);
					}
					else{
						d.setStato(StatoDottore.DOCTOR_AVAILABLE);
						mediciDisponibili.add(dottori.get(pazienti.get(e.getDato()).getCuratore().getIdDottore()));
					}
				}
				
				++pazientiSalvati;
			}
			break;
		//Se il paziente muore	
		case PAZIENTE_MUORE:
			//Se il paziente è stato curato evito di stamparne l'evento "morto" programmato in precedenza
			if (pazienti.get(e.getDato()).getStato() == Paziente.StatoPaziente.SALVO) {
				System.out.println("Paziente già salvato: " + e);
			} 
			else {
				//Se muore sotto i ferri
				if (pazienti.get(e.getDato()).getStato() == Paziente.StatoPaziente.IN_CURA) {
					//Controllo se operato da un assistente o da un dottore
					if(pazienti.get(e.getDato()).getAssistenteCuratore() != null){
						Assistente a = pazienti.get(e.getDato()).getAssistenteCuratore();
						//Se l'assistente che lo doveva operare ha finito il turno non lo aggiungo a quelli disponibili, se no lo libero subito
						if(a.getStato() == StatoAssistente.ASSISTANT_CURA_OLTRE){
							pazientiPersi ++;
							pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.NERO);
							System.out.println("Paziente morto sotto i ferri con assistente: " + e);
							break;
						}
						else{
							//Se l'assistente non ha finito il turno lo libero
							assistentiDisponibili.add(pazienti.get(e.getDato()).getAssistenteCuratore());
							pazientiPersi ++;
							pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.NERO);
							System.out.println("Paziente morto sotto i ferri con assistente: " + e);
							break;
						}
					}
					else{
						Dottore d = pazienti.get(e.getDato()).getCuratore();
						//Se il medico che lo doveva operare ha finito il turno non lo aggiungo a quelli disponibili, se no lo libero subito
						if(d.getStato() == StatoDottore.DOCTOR_CURA_OLTRE){
							pazientiPersi ++;
							pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.NERO);
							System.out.println("Paziente morto sotto i ferri: " + e);
							break;
						}
						else{
							//Se il medico non ha finito il turno lo libero
							mediciDisponibili.add(pazienti.get(e.getDato()).getCuratore());
							pazientiPersi ++;
							pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.NERO);
							System.out.println("Paziente morto sotto i ferri: " + e);
							break;
						}
					}
				}
				else{
					//Morto prima delle cure aggiorno i pazienti morti ed imposto il suo stato su nero (morto)
					++pazientiPersi;
					pazienti.get(e.getDato()).setStato(Paziente.StatoPaziente.NERO);
					System.out.println("Paziente morto: " + e);
				}
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
				d.setStato(StatoDottore.DOCTOR_CURA_OLTRE);
				mediciDisponibili.remove(d);
				this.aggiungiEvento(new Evento((long)(e.getTempo()+16*60), TipoEvento.DOCTOR_INIZIO_TURNO, d.getIdDottore()));
			}
			System.out.println("Fine turno: "+e.toString());
			break;	
		case ASSISTANT_INIZIO_TURNO:
			//L'assistente diventa disponibile. Aggiorno gli assistenti disponibili ed imposto la fine del turno
			assistenti.get(e.getDato()).setStato(StatoAssistente.ASSISTANT_AVAILABLE);
			assistentiDisponibili.add(assistenti.get(e.getDato()));
			System.out.println("Inizio turno assistente: "+e.toString());
			this.aggiungiEvento(new Evento((e.getTempo()+(8*60)), TipoEvento.ASSISTANT_FINE_TURNO, e.getDato()));
			break;
		case ASSISTANT_FINE_TURNO:
			//Tolgo l'assistente da quelli disponibili ed imposto l'inizio del prossimo turno SE non sta operando.
			Assistente a = assistenti.get(e.getDato());
			if(a.getStato() == StatoAssistente.ASSISTANT_AVAILABLE){
				a.setStato(StatoAssistente.ASSISTANT_OUT);
				assistentiDisponibili.remove(a);
				this.aggiungiEvento(new Evento((long)(e.getTempo()+16*60), TipoEvento.ASSISTANT_INIZIO_TURNO, a.getIdAssistente()));
			}
			else{
				a.setStato(StatoAssistente.ASSISTANT_CURA_OLTRE);
				assistentiDisponibili.remove(a);
				this.aggiungiEvento(new Evento((long)(e.getTempo()+16*60), TipoEvento.ASSISTANT_INIZIO_TURNO, a.getIdAssistente()));
			}
			System.out.println("Fine turno assistente: "+e.toString());
			break;	
		default:
			System.err.println("Panik!");
		}
		//Finchè ho medici/assistenti disponibili e pazienti da curare curo tutti i pazienti che posso
		while (cura(e.getTempo()));
	}

	protected boolean cura(long adesso) {
		//Se non ho pazienti in attesa o non ho medici/assistenti non curo nessuno
		if (pazientiInAttesa.isEmpty())
			return false;
		if (this.getMediciDisponibili() == 0 && this.getAssistentiDisponibili() == 0)
			return false;
		//Se ne ho la possibilitò curo il paziente con la priorità più alta se non è ancora morto
		Paziente p = pazientiInAttesa.remove();
		if (p.getStato() != Paziente.StatoPaziente.NERO) {
			//Controllo prima se ho assistenti disponibili
			if(p.getStato() != StatoPaziente.ROSSO && this.getAssistentiDisponibili() != 0){
				Assistente a = assistentiDisponibili.remove();
				pazienti.get(p.getId()).setStato(Paziente.StatoPaziente.IN_CURA);
				assistenti.get(a.getIdAssistente()).setStato(StatoAssistente.ASSISTANT_CURA);
				pazienti.get(p.getId()).setAssistenteCuratore(a);
				aggiungiEvento(new Evento(adesso + 30, Evento.TipoEvento.PAZIENTE_GUARISCE, p.getId()));
				System.out.println("Assistente inizia a curare: " + p);
			}
			else if(this.getMediciDisponibili() != 0){
				//Tolgo un medico da quelli disponibili ed imposto lo stato in cura, con relativo evento
				Dottore d = mediciDisponibili.remove();
				pazienti.get(p.getId()).setStato(Paziente.StatoPaziente.IN_CURA);
				dottori.get(d.getIdDottore()).setStato(StatoDottore.DOCTOR_CURA);
				pazienti.get(p.getId()).setCuratore(d);
				aggiungiEvento(new Evento(adesso + 30, Evento.TipoEvento.PAZIENTE_GUARISCE, p.getId()));
				System.out.println("Inizio a curare: " + p);
			}
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

}

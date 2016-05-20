//Gestione dei tipi di evento possibili

package it.polito.tdp.emergency.simulation;

public class Evento implements Comparable<Evento> {

	public enum TipoEvento { PAZIENTE_ARRIVA, PAZIENTE_GUARISCE, PAZIENTE_MUORE, DOCTOR_INIZIO_TURNO, DOCTOR_FINE_TURNO	}
	
	protected long tempo;
	protected TipoEvento tipo;
	protected int dato;

	public Evento(long time, TipoEvento type, int dato) {
		super();
		this.tempo = time;
		this.tipo = type;
		this.dato = dato;
	}
	
	public int getDato() {
		return dato;
	}

	public long getTempo() {
		return tempo;
	}

	public TipoEvento getTipo() {
		return tipo;
	}

	@Override
	public String toString() {
		return "Evento [tempo=" + tempo + ", tipo=" + tipo + ", dato=" + dato + "]";
	}

	@Override
	public int compareTo(Evento arg0) {
		return Long.compare(this.tempo, arg0.tempo);
	}

}

package it.polito.tdp.emergency.simulation;

public class Paziente implements Comparable<Paziente> {
	
	//Dati per gestione stato paziente 
	public enum StatoPaziente {	ROSSO, GIALLO, VERDE, BIANCO, IN_CURA, SALVO, NERO };

	private int id;
	private StatoPaziente stato;
	private String nome;
	
	public Paziente(int id, String nome, StatoPaziente stato) {
		super();
		this.id = id;
		this.nome = nome;
		this.stato = stato;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Paziente other = (Paziente) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public StatoPaziente getStato() {
		return stato;
	}

	public void setStato(StatoPaziente stato) {
		this.stato = stato;
	}

	public int getId() {
		return id;
	}
	
	public String getNome(){
		return nome;
	}

	@Override
	public String toString() {
		return "Paziente [id=" + id + ", nome=" + nome + ", stato=" + stato + "]";
	}

	//Confonta gli stati in ordine di gravità
	@Override
	public int compareTo(Paziente arg0) {
		return Integer.compare(this.getStato().ordinal(), arg0.getStato().ordinal());
	}

}

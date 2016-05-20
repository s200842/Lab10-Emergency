package it.polito.tdp.emergency.simulation;

public class Assistente implements Comparable<Assistente>{
	
	public enum StatoAssistente {	ASSISTANT_AVAILABLE, ASSISTANT_CURA, ASSISTANT_OUT, ASSISTANT_CURA_OLTRE };
	
	private int idAssistente;
	private String nomeAssistente;
	private StatoAssistente stato;
	
	public Assistente(int idAssistente, String nomeAssistente, StatoAssistente stato) {
		this.idAssistente = idAssistente;
		this.nomeAssistente = nomeAssistente;
		this.stato = stato;
	}
	
	public StatoAssistente getStato() {
		return stato;
	}

	public void setStato(StatoAssistente stato) {
		this.stato = stato;
	}

	public int getIdAssistente() {
		return idAssistente;
	}

	public String getNomeAssistente() {
		return nomeAssistente;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idAssistente;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Assistente other = (Assistente) obj;
		if (idAssistente != other.idAssistente)
			return false;
		return true;
	}

	@Override
	public int compareTo(Assistente arg0) {
		return Integer.compare(this.getStato().ordinal(), arg0.getStato().ordinal());
	}	
		
		

}

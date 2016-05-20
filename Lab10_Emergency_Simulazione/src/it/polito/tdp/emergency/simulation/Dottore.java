package it.polito.tdp.emergency.simulation;

public class Dottore implements Comparable<Dottore>{
	
	public enum StatoDottore {	DOCTOR_AVAILABLE, DOCTOR_CURA, DOCTOR_OUT, DOCTOR_CURA_OLTRE };
	
	private int idDottore;
	private String nomeDottore;
	private StatoDottore stato;
	
	public Dottore(int idDottore, String nomeDottore, StatoDottore stato) {
		this.idDottore = idDottore;
		this.nomeDottore = nomeDottore;
		this.stato = stato;
	}

	public StatoDottore getStato() {
		return stato;
	}

	public void setStato(StatoDottore stato) {
		this.stato = stato;
	}

	public int getIdDottore() {
		return idDottore;
	}

	public String getNomeDottore() {
		return nomeDottore;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idDottore;
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
		Dottore other = (Dottore) obj;
		if (idDottore != other.idDottore)
			return false;
		return true;
	}

	@Override
	public int compareTo(Dottore arg0) {
		return Integer.compare(this.getStato().ordinal(), arg0.getStato().ordinal());
	}
	
	
	
	

}

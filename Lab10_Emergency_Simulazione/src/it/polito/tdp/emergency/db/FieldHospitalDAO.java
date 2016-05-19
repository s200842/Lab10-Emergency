package it.polito.tdp.emergency.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import it.polito.tdp.emergency.simulation.Evento;
import it.polito.tdp.emergency.simulation.Evento.TipoEvento;
import it.polito.tdp.emergency.simulation.Paziente;
import it.polito.tdp.emergency.simulation.Paziente.StatoPaziente;

public class FieldHospitalDAO {

	public Set<Paziente> getPazienti(){
		Set<Paziente> pazienti = new HashSet<Paziente>();
		Connection c = DBConnect.getConnection();
		try {
			PreparedStatement st = c.prepareStatement("SELECT * FROM patients;");
			ResultSet res = st.executeQuery();
			while(res.next()){
				int id_paziente = res.getInt("id");
				String name = res.getString("name");
				String state = this.getStatoPaziente(id_paziente);
				
				switch(state){
				case "White":
					pazienti.add(new Paziente(id_paziente, name, StatoPaziente.BIANCO));
					break;
				case "Green":
					pazienti.add(new Paziente(id_paziente, name, StatoPaziente.VERDE));
					break;
				case "Yellow":
					pazienti.add(new Paziente(id_paziente, name, StatoPaziente.GIALLO));
					break;
				case "Red":
					pazienti.add(new Paziente(id_paziente, name, StatoPaziente.ROSSO));
					break;
				default:
					throw new RuntimeException("Triage non riconosciuto");
				}
			}
			res.close();
			c.close();
			return pazienti;	
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getStatoPaziente(int id){
		Connection c = DBConnect.getConnection();
		try {
			PreparedStatement st = c.prepareStatement("SELECT triage FROM arrivals WHERE patient = ?;");
			st.setInt(1, id);
			ResultSet res = st.executeQuery();
			if(res.next()){
				String state = res.getString("triage");
				res.close();
				c.close();
				return state;
			}
			else{
				res.close();
				c.close();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Set<Evento> getArrivi(){
		Set<Evento> arrivi = new HashSet<Evento>();
		Connection c = DBConnect.getConnection();
		try {
			PreparedStatement st = c.prepareStatement("SELECT timestamp, patient FROM arrivals ORDER BY timestamp;");
			ResultSet res = st.executeQuery();
			long mezzanotte=0;
			while(res.next()){
				//Accorcia orizzonte temporale da 1970 alla mezzanotte peravere stampe più leggere
				if(mezzanotte==0){
					mezzanotte=res.getTimestamp("timestamp").getTime()-143000;
				}
				long tempo=(res.getTimestamp("timestamp").getTime()-mezzanotte)/(1000*60);
				Evento e = new Evento(tempo, TipoEvento.PAZIENTE_ARRIVA, res.getInt("patient"));
				arrivi.add(e);
			}
			res.close();
			c.close();
			return arrivi;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}

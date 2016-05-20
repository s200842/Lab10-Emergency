package it.polito.tdp.emergency;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import it.polito.tdp.emergency.simulation.Dottore;
import it.polito.tdp.emergency.simulation.Dottore.StatoDottore;
import it.polito.tdp.emergency.simulation.Evento;
import it.polito.tdp.emergency.simulation.Evento.TipoEvento;
import it.polito.tdp.emergency.model.Model;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EmergencyController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField txtNomeDottore;

    @FXML
    private TextField txtOre;

    @FXML
    private Button btnAggiungi;

    @FXML
    private Label txtDottoriInseriti;

    @FXML
    private Button btnAvvia;
    
    @FXML
    private Button btnReset;

    @FXML
    private TextArea txtResult;
    
    private Model model;
    
    private List<Evento> eventiDottori = new LinkedList<Evento>();
    
    private Set<Dottore> dottori = new HashSet<Dottore>(); 
    int id_dott = 1;
    
    public void setModel(Model m){
    	model = m;
    }

    @FXML
    void doAggiungi(ActionEvent event) {
    	txtResult.clear();
    	//Devo avere una stringa nel campo txtNomeDottore e un numero nel campo txtOre
    	if(txtNomeDottore.getText().matches("[a-zA-Z ]+") == false){
    		txtResult.setText("Si prega di inserire il nome di un dottore.");
    		return;
    	}
    	else if(!txtOre.getText().matches("[0-9]+")){
    		txtResult.setText("Si prega di inserire un valore numerico intero nel campo \"Minuti Sfasamento\".");
    	}
    	else{
    		//Tutto corretto, aggiungo il dottore al mio elenco di dottori ed incremento l'id per il dott successivo
    		dottori.add(new Dottore(id_dott, txtNomeDottore.getText(), StatoDottore.DOCTOR_OUT));
    		txtDottoriInseriti.setText("Dottori inseriti: "+id_dott);
    		//Aggiungo l'evento di inizio turno del dottore
    		long timeStart = Long.parseLong(txtOre.getText());
    		eventiDottori.add(new Evento(timeStart, TipoEvento.DOCTOR_INIZIO_TURNO, id_dott));
    		id_dott++;
    		txtNomeDottore.clear();
    		txtOre.clear();
    	}

    }

    @FXML
    void doAvvia(ActionEvent event) {
    	model.startSimulation(dottori, eventiDottori);
    	txtResult.setText(String.format("Pazienti salvati: %d\nPazienti persi: %d", model.getSalvi(), model.getPersi()));
    }
    
    @FXML
    void doReset(ActionEvent event) {
    	id_dott = 1;
    	dottori.clear();
    	eventiDottori.clear();
    	txtDottoriInseriti.setText("Dottori inseriti: 0");
    	txtNomeDottore.clear();
    	txtOre.clear();
    	txtResult.clear();
    	model.clear();
    }

    @FXML
    void initialize() {
        assert txtNomeDottore != null : "fx:id=\"txtNomeDottore\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert txtOre != null : "fx:id=\"txtOre\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert btnAggiungi != null : "fx:id=\"btnAggiungi\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert txtDottoriInseriti != null : "fx:id=\"txtDottoriInseriti\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert btnAvvia != null : "fx:id=\"btnAvvia\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert btnReset != null : "fx:id=\"btnReset\" was not injected: check your FXML file 'Emergency.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Emergency.fxml'.";

    }
}

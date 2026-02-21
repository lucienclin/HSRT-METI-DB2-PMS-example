package de.hsrt.meti.pms.processing;


import java.io.FileWriter;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.*;
import org.junit.jupiter.api.Test;
import de.hsrt.meti.pms.core.*;
import static de.hsrt.meti.pms.processing.TestData.*;


public final class StreamExampleTests implements ProcessingExampleOps
{

  @Test
  public void findPatientsInMusterlingen(){

    // SQL Äquivalent:
    // SELECT COUNT(*) FROM patients WHERE city = 'Musterlingen' 
    
    var city = "Musterlingen";
    
    var count =
      PATIENTS
        .stream()
        .filter(
          patient -> patient.address().city().equals(city)
	)
        .count();

    System.out.println("Patients in " + city + ": " + count);
  }


  @Test
  public void findPatientsWithCovid(){

    // SQL Äquivalent:
    // SELECT patients.id, [other patient columns...]
    //  FROM diagnoses JOIN patients ON diagnoses.patient = patients.id
    //  WHERE diagnoses.code = 'U07.1'; 
    	  
    DIAGNOSES
      .stream()
      .filter( // Filtere nach ICD-10 code von COVID-19
        diag -> diag.coding().code().equals("U07.1")
      )
      .flatMap(            
        diag ->
          PATIENTS.stream()  // Finde Patient, der von Diagnose referenziert wird
            .filter(
	      pat -> pat.id().equals(diag.patient())
	    )
      );

  }


  @Test
  public void getPrescribedMedicationDistribution(){

    // SQL Äquivalent:
    // SELECT medication_code, COUNT(*) AS count
    //  FROM prescriptions
    //  GROUP BY medication_code;
 
    Map<String,Long> medicationCounts =
      PRESCRIPTIONS.stream()
        .map(Prescription::medication)                  // Projeziere Medikations-Codings aus Verschreibung
        .collect(groupingBy(Coding::code,counting()));  // Gruppiere nach Code und zähle Gruppen-Elemente 
	  

    // Ausgabe nach STDOUT
    medicationCounts.forEach(
      (code,count) -> System.out.println(code + ": " + count)
    );
    	
  }


  @Test
  public void getGenderDistributionOfPatientsWithCovid(){

    // SQL Äquivalent:
    // SELECT gender, COUNT(*) AS count
    //  FROM diagnoses JOIN patients ON diagnoses.patient = patients.id
    //  WHERE diagnoses.code = 'U07.1'
    //  GROUP BY gender; 
    	  
    Map<Gender,Long> genderDistribution =
      DIAGNOSES.stream()
       .filter(    // Filtere Diagnosen nach ICD-10-Code von COVID-19
         diag -> diag.coding().code().equals("U07.1")
       )
       .flatMap(   // Finde Patient, der von der Diagnose referenziert wird
         diag ->
           PATIENTS.stream()
             .filter(pat -> pat.id().equals(diag.patient()))
       )
       .collect(   // Gruppiere nach Geschlecht und zähle Gruppen-Elemente
         groupingBy(Patient::gender,counting())
       );

     genderDistribution
       .forEach(
         (gender,count) -> System.out.println(gender + " number of COVID-Patients: " + count)
       );

  }
}

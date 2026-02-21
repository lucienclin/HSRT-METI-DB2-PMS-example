package de.hsrt.meti.pms.processing;


import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import de.hsrt.meti.pms.core.*;
import de.hsrt.meti.pms.gens.Generators;
import static de.hsrt.meti.pms.processing.TestData.*;


public final class ImperativeExampleTests implements ProcessingExampleOps
{

  @Test
  public void findPatientsInMusterlingen(){

    // SQL Äquivalent:
    // SELECT COUNT(*) FROM patients WHERE city = 'Musterlingen' 
    
    var city = "Musterlingen";

    var count = 0;

    for (Patient patient : PATIENTS){
      if (patient.address().city().equals(city)) count++;
    }

    System.out.println("Patients in " + city + ": " + count);
  }


  @Test
  public void findPatientsWithCovid(){

    // SQL Äquivalent:
    // SELECT patients.id, [other patient columns...]
    //  FROM diagnoses JOIN patients ON diagnoses.patient = patients.id
    //  WHERE diagnoses.code = 'U07.1';

    List<Patient> patientsWithCovid = new ArrayList<>();

    for (Diagnosis diagnosis : DIAGNOSES){

      if (diagnosis.coding().code().equals("U07.1")){

	for (Patient patient : PATIENTS){
          if (patient.id().equals(diagnosis.patient())){
	    patientsWithCovid.add(patient);
	  } 
	}

      }
    }

  }


  @Test
  public void getPrescribedMedicationDistribution(){

    // SQL Äquivalent:
    // SELECT medication_code, COUNT(*) AS count
    //  FROM prescriptions
    //  GROUP BY medication_code;

    Map<String,Long> medicationCounts = new HashMap<>();

    for (Prescription prescription : PRESCRIPTIONS){
      var code = prescription.medication().code();

      var count = medicationCounts.getOrDefault(code, 0L);

      medicationCounts.put(code, count+1);
    }

    for (Map.Entry<String,Long> entry : medicationCounts.entrySet()){

      var code = entry.getKey();

      var count = entry.getValue();

      System.out.println(code + ": " + count);
    }
  }


  @Test
  public void getGenderDistributionOfPatientsWithCovid(){

    // SQL Äquivalent:
    // SELECT gender, COUNT(*) AS count
    //  FROM diagnoses JOIN patients ON diagnoses.patient = patients.id
    //  WHERE diagnoses.code = 'U07.1' 
    //  GROUP BY gender;
   
    var numMale = 0; 
    var numFemale = 0; 
    
    for (Diagnosis diagnosis : DIAGNOSES){

      if (diagnosis.coding().code().equals("U07.1")){

        for (Patient patient : PATIENTS){
          if (patient.id().equals(diagnosis.patient())){
            switch (patient.gender()){

              case MALE : 
                numMale++;
                break;
             
              case FEMALE :
                numFemale++;
                break;
             
               default: break; 
            }
          }
        }
      }
    }

    System.out.println(Gender.MALE + " number of COVID-Patients: " + numMale);
    System.out.println(Gender.FEMALE + " number of COVID-Patients: " + numFemale);
  }

}

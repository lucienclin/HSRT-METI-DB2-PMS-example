package de.hsrt.meti.pms.core;


import java.time.Instant;
import java.util.List;
import java.util.Optional;


public class PatientRecordServiceImpl implements PatientRecordService
{

  private final Repository repo;
  

  public PatientRecordServiceImpl(Repository repo){
    this.repo = repo;
  }


  @Override
  public Patient process(Patient.Command cmd) throws Exception
  {
    return switch(cmd){
      case Patient.Create cr  -> create(cr);
      case Patient.Update up  -> update(up);
      case Patient.Delete del -> delete(del);
    };
  }


  private Patient create(Patient.Create cr) throws Exception {

    // Logging, Validate input parameters

    var patient =
      new Patient(
        repo.patientId(),
        cr.gender(),
        cr.givenName(),
        cr.familyName(),
        cr.birthDate(),
        Optional.empty(),
        cr.healthInsurance(),
        cr.address(),
        Instant.now()
      ); 

    repo.save(patient);

    return patient; 
  }


  private Patient update(Patient.Update up) throws Exception {

    // Logging, Validate input parameters

    var patient =
      repo.findPatient(up.id())
        .map(pat -> pat.apply(up))
        .orElseThrow(
          () -> { 
            // Log invalid operation
            throw new IllegalArgumentException("Invalid Patient ID " + up.id());
          }
        );

    repo.save(patient);

/*
    up.dateOfDeath().ifPresent(
      d -> {
        // Execute other operations consecutive to patient death: cancel appointments, etc 
      }
    );
*/    
    return patient; 
  }


  private Patient delete(Patient.Delete del) throws Exception {

    // Logging...

    var patient =
      repo.findPatient(del.id())
        .orElseThrow(
          () -> { 
            // Log invalid operation
            throw new IllegalArgumentException("Invalid Patient ID " + del.id());
          }
        );

    repo.deletePatient(del.id());

    // Delete associated Diagnoses, Prescriptions
    // Execute other operations consecutive to patient deletion: cancel appointments, etc

    return patient;
  }



  @Override
  public Optional<Patient> getPatient(Id<Patient> id){

    // Logging...

    return repo.findPatient(id);
  }



  @Override
  public List<Patient> findPatients(Patient.Filter filter){

    // Logging...

    return repo.findPatients(filter);
  }


}

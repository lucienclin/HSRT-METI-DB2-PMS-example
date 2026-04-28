package de.hsrt.meti.pms.core;


import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Tests
{

  private static PatientRecordService service = null; 


  @BeforeAll
  public static void init(){ 
    service = new PatientRecordServiceImpl(new InMemRepository());
  } 


  @Test
  public void testPatientLifecycle(){
 
    var createPatient =
      new Patient.Create(
        Gender.UNKNOWN,
        "Max",
        "Mustermensch",
        LocalDate.now().minusYears(42),
        "AOK",
        new Address("Musterstr.","42","98765","Musterhausen")
      );

    var createdPatient = assertDoesNotThrow(() -> service.process(createPatient));

    var retrievedPatient = service.getPatient(createdPatient.id());

    // Check that the Patient can be retrieved by ID and the returned instance is identical to the created one
    assertTrue(retrievedPatient.isPresent());
    assertEquals(createdPatient,retrievedPatient.get());

    // Filtering Patients must return exactly 1 entry at this point, given that only one has been created
    assertEquals(service.findPatients(Patient.Filter.NONE).size(),1);


    var deletedPatient = assertDoesNotThrow(() -> service.process(new Patient.Delete(createdPatient.id())));

    // Patient retrieval by ID or by filtering must return no results now, after the one Patient has been deleted
    assertTrue(service.getPatient(createdPatient.id()).isEmpty());
    assertTrue(service.findPatients(Patient.Filter.NONE).isEmpty());

  }

  /*
    NOTE: On the complete PatientRecordService implementation, further tests could be:

    - Check that invalid data (e.g. a Patient with birthdate in the future or outside a meaningful age range,
      e.g. over 150 years old) lead to an error) instead of Patient creation

    - Check that upon patient deletion, all associated diagnoses/prescription have also been deleted
  */
  
}

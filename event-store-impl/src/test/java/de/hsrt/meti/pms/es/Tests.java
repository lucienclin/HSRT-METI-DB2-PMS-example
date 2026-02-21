package de.hsrt.meti.pms.es;


import java.util.Set;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Id;
import de.hsrt.meti.pms.core.Patient;
import de.hsrt.meti.pms.core.EventStore;
import static de.hsrt.meti.pms.gens.Generators.*;


public final class Tests
{

  private static EventStore eventStore = null;


  private static Patient.Create createPatient(){

    // Get a random generated Patient for intital values
    var patient = patient();

    return new Patient.Create(
      patient.gender(),
      patient.givenName(),
      patient.familyName(),
      patient.birthDate(),
      patient.healthInsurance(),
      patient.address()
    );
  }

  private static Patient.Update updateAddress(Patient patient){

    return new Patient.Update(
      patient.id(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.empty(),
      Optional.of(address())
    );

  }
  
  private static Patient.Update updateName(Patient patient){

    return new Patient.Update(
      patient.id(),
      Optional.empty(),
      Optional.empty(),
      Optional.of(oneOf(FAMILY_NAMES)),
      Optional.empty(),
      Optional.empty(),
      Optional.empty()
    );

  }
 
  @BeforeAll
  public static void init(){

    System.setProperty("pms.mongodb.host","localhost");
    System.setProperty("pms.mongodb.name","pms-event-store");

    eventStore = EventStore.getInstance();

/*
    Stream.generate(() -> createPatient())
      .limit(100)
      .forEach(
        cmd -> {
          try {
            eventStore.process(cmd);
          } catch (Exception e){ }
        }
      );
 */
  }

/*
  @AfterAll
  public static void cleanUp(){
    eventStore
      .findPatients(Patient.Filter.NONE)
      .forEach(
        p -> {
          try { 
            eventStore.process(new Patient.Delete(p.id()));
          } catch (Exception e){ }
        }
      );
  }
*/

  @Test
  public void testPatientLifeCycle(){

    try {
      var patient = eventStore.process(createPatient());

      var addressUpdate = updateAddress(patient);

      eventStore.process(addressUpdate);

      var nameUpdate = updateName(patient);

      eventStore.process(nameUpdate);

      var restoredPatient = eventStore.findPatient(patient.id()).get();

      assertTrue(
        restoredPatient.familyName().equals(nameUpdate.familyName().get()) &&
        restoredPatient.address().equals(addressUpdate.address().get())
      );

/*
      eventStore.process(new Patient.Delete(patient.id()));

      assertTrue(
        eventStore.findPatient(patient.id()).isEmpty()
      );
*/
    } catch (Exception e){
      e.printStackTrace();
    }

  }

/*
  @Test
  public void testPatientQuery(){

    var genders = Set.of(Gender.FEMALE);

    var patients = eventStore.findPatients(
      new Patient.Filter(
        Optional.of(genders),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
      )
    );

    assertFalse(patients.isEmpty());

    assertTrue(
      patients.stream()
        .allMatch(p -> genders.contains(p.gender()))
    );

  }
*/

}

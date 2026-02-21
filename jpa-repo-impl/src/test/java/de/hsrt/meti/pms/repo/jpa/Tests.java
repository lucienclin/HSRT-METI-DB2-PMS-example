package de.hsrt.meti.pms.repo.jdbc;


import java.util.Set;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Patient;
import de.hsrt.meti.pms.core.Repository;
import de.hsrt.meti.pms.gens.Generators;



public final class Tests
{

  private static Repository repo = null;

  private static Patient testPatient = null;

  @BeforeAll
  public static void init(){

    repo = Repository.getInstance();
 
    testPatient = Generators.patient();

    Stream.generate(Generators::patient)
      .limit(100)
      .forEach(
        patient -> { 
          try { 
            repo.save(patient);
          } catch (Exception e){ }
        }
      );
  }


  @AfterAll
  public static void cleanUp(){
    try { 
      repo.deletePatients(Patient.Filter.NONE);
    } catch (Exception e){ }
  }



  @Test
  public void testPatientSave(){

    try {
      repo.save(testPatient);
    } catch (Exception e){
      e.printStackTrace();
    }

    assertTrue(
      repo.findPatient(testPatient.id()).isPresent()
    );
  }


  @Test
  public void testPatientDelete(){

    try {
      repo.deletePatient(testPatient.id());
    } catch (Exception e){
      e.printStackTrace();
    }

    assertTrue(
      repo.findPatient(testPatient.id()).isEmpty()
    );
  }


  @Test
  public void testPatientQuery(){

    var genders = Set.of(Gender.FEMALE);

    var patients = repo.findPatients(
      new Patient.Filter(
        Optional.of(genders),
        Optional.empty(),
        Optional.empty(),
        Optional.empty()
      )
    );

    assertTrue(
      patients.stream()
        .allMatch(p -> genders.contains(p.gender()))
    );

  }

}

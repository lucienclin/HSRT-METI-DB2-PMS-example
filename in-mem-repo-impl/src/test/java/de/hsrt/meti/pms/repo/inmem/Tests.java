package de.hsrt.meti.pms.repo.inmem;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import de.hsrt.meti.pms.core.Repository;
import de.hsrt.meti.pms.gens.Generators;



public final class Tests
{

  private final Repository repo = Repository.getInstance();


  @Test
  public void testPatientSave(){

    var patient = Generators.patient();

    try {
      repo.save(patient);
    } catch (Exception e){
      e.printStackTrace();
    }

    assertTrue(
      repo.findPatient(patient.id()).isPresent()
    );
  }


  @Test
  public void testPatientDelete(){

    var patient = Generators.patient();

    try {
      repo.save(patient);
      repo.deletePatient(patient.id());
    } catch (Exception e){
      e.printStackTrace();
    }

    assertTrue(
      repo.findPatient(patient.id()).isEmpty()
    );
  }



}

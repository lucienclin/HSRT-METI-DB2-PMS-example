package de.hsrt.meti.pms.core;


import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ServiceLoader;



public interface Repository
{

  Id<Patient> patientId();

  void save(Patient patient) throws Exception;

  Optional<Patient> findPatient(Id<Patient> id);

  List<Patient> findPatients(Patient.Filter filter);

  Optional<Patient> deletePatient(Id<Patient> id) throws Exception;

  default List<Patient> deletePatients(Patient.Filter filter) throws Exception {
    var patients = findPatients(filter);
    for (Patient patient: patients){
      deletePatient(patient.id());
    }
    return patients;
  }




  /* --------------------------------------------------------------------------
    Service Provider Interface (SPI) pattern for dependency loading

    See e.g. https://docs.oracle.com/javase/tutorial/ext/basics/spi.html
    for usage tutorial
  */
  public static interface Provider
  {
    Repository getInstance();
  }


  public static Repository getInstance(){
    return ServiceLoader.load(Provider.class)
      .iterator()
      .next()
      .getInstance();
  }

}


package de.hsrt.meti.pms.core;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;



public interface EventStore
{

  Patient process(Patient.Command cmd) throws Exception;

  Optional<Patient> findPatient(Id<Patient> id);

  Optional<Patient> stateOfPatientAt(Id<Patient> id, Instant t);

  List<Patient> findPatients(Patient.Filter filter);


  /* --------------------------------------------------------------------------
    Service Provider Interface (SPI) pattern for dependency loading

    See e.g. https://docs.oracle.com/javase/tutorial/ext/basics/spi.html
    for usage tutorial
  */
  public static interface Provider
  {
    EventStore getInstance();
  }


  public static EventStore getInstance(){
    return ServiceLoader.load(Provider.class)
      .iterator()
      .next()
      .getInstance();
  }

}


package de.hsrt.meti.pms.processing;


import java.util.List;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import de.hsrt.meti.pms.core.*;
import de.hsrt.meti.pms.gens.Generators;


public final class TestData
{

  private TestData(){ }


  public static final List<PatientRecord> PATIENT_RECORDS =
    Stream.generate(Generators::patientRecord)
      .limit(420)
      .collect(toList());


  public static final List<Patient> PATIENTS =
    PATIENT_RECORDS
      .stream()
      .map(PatientRecord::patient)
      .collect(toList());

  public static final List<Diagnosis> DIAGNOSES =
    PATIENT_RECORDS
      .stream()
      .flatMap(r -> r.diagnoses().stream())
      .collect(toList());
    

  public static final List<Prescription> PRESCRIPTIONS =
    PATIENT_RECORDS
      .stream()
      .flatMap(r -> r.prescriptions().stream())
      .collect(toList());


}

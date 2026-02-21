package de.hsrt.meti.pms.gens;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import java.util.UUID;
import de.hsrt.meti.pms.core.*;
import static de.hsrt.meti.pms.core.Gender.*;


public final class Generators
{

  private Generators(){}


  private static final Random RND =
    new Random();


  // Helper function: Return random element from List<T>
  public static <T> T oneOf(List<T> ts){
    return ts.get(RND.nextInt(ts.size()));
  }


  public static final List<Gender> GENDERS =
    List.of(MALE, FEMALE);


  public static final Map<Gender,List<String>> GIVEN_NAMES_BY_GENDER =
    Map.of(
      MALE, List.of("Max","Moritz","Tim","Johannes","Christoph","Benjamin","Frank","Christian","Oliver"),
      FEMALE, List.of("Maria","Ute","Lisa","Tina","Christine","Julia","Olivia","Janina","Johanna","Inge")
    );

  public static final List<String> FAMILY_NAMES =
    List.of("Müller","Maier","Meier","Mayer","Schmidt","Schmied","Werner","Weber","Herrmann","Braun","Zimmermann");


  public static final List<String> HEALTH_INSURANCES =
    List.of("AOK","Barmer","TK");


  public static final List<String> STREETS =
    Stream.of("Rhein","Neckar","Weser","Isar","Donau","Elbe","Main","Inn","Alb","Kocher")
      .map(s -> s + "-Straße")
      .collect(toList());

  public static final Map<String,String> CITIES_BY_POSTAL_CODE =
    Map.of(
      "12345", "Musterlingen",
      "67890", "Beispielhausen"
    );

  public static final List<String> POSTAL_CODES =
    CITIES_BY_POSTAL_CODE
      .keySet()
      .stream()
      .collect(toList());


  public static final List<Coding> ICD10CODINGS =
    Stream.of(
      entry(
        "J10.8",
        "Grippe mit sonstigen Manifestationen in den Atemwegen - saisonale Influenzaviren nachgewiesen"
      ),
      entry(
        "A08.1",
        "Akute Gastroenteritis durch Norovirus"
      ),
      entry(
        "A08.4",
        "Virusbedingte Darminfektion - nicht näher bezeichnet"
      ),
      entry(
        "A09.0",
        "Sonstige und nicht näher bezeichnete Gastroenteritis und Kolitis infektiösen Ursprungs"
      ),
      entry(
        "U07.1",
        "COVID-19 - Virus nachgewiesen"
      ),
      entry(
        "R04.0",
        "Nasenbluten"
      ),
      entry(
        "R12",
        "Sodbrennen"
      )
    )
    .map(e -> Coding.of(e.getKey(),e.getValue(),"ICD-10-GM", Optional.of("2022")))
    .collect(toList());


  public static final List<Coding> ATC_CODINGS =
    Stream.of(
      entry(
        "A02AA01",
        "Magnesiumcarbonat"
      ),
      entry(
        "N02BE01",
        "Paracetamol"
      ),
      entry(
        "D01AE12",
        "Salicylsäure"
      ),
      entry(
        "R01BA57",
        "Pseudoephedrin und Ibuprofen"
      )
    )
    .map(e -> Coding.of(e.getKey(),e.getValue(),"ATC", Optional.of("2022")))
    .collect(toList());


  public static final <T> Id<T> id(){
    return new Id<>(UUID.randomUUID().toString());
  }

  public static LocalDate birthDate(){
    return LocalDate.ofEpochDay(RND.nextInt(9000));
  }


  public static Address address(){

    var pc = oneOf(POSTAL_CODES);

    return new Address(
      oneOf(STREETS),
      String.valueOf(RND.nextInt(100)+1),
      pc,
      CITIES_BY_POSTAL_CODE.get(pc)
    );
  }


  public static Patient patient(){

    var gender = oneOf(GENDERS);

    return new Patient(
      id(),
      gender,
      oneOf(GIVEN_NAMES_BY_GENDER.getOrDefault(gender,GIVEN_NAMES_BY_GENDER.get(FEMALE))),
      oneOf(FAMILY_NAMES),
      birthDate(),
      RND.nextDouble() <= 0.95 ? Optional.empty() : Optional.of(LocalDate.now()),
      oneOf(HEALTH_INSURANCES),
      address(),
      Instant.now()
    );
  };


  public static Diagnosis diagnosisFor(Patient patient){
    return
      new Diagnosis(
        id(),
        patient.id(),
        LocalDateTime.now(),
	oneOf(ICD10CODINGS),
        Instant.now()
    );
  }

  public static Diagnosis diagnosis(){
    return diagnosisFor(patient());
  }


  public static Prescription prescriptionFor(Patient patient, Diagnosis diagnosis){
    return
      new Prescription(
        id(),
        patient.id(),
        diagnosis.id(),
        LocalDateTime.now(),
        oneOf(ATC_CODINGS),
        Instant.now()
    );
  }


  public static Prescription prescription(){
    var pat  = patient();
    var diag = diagnosisFor(pat);

    return prescriptionFor(pat,diag);
  }


  public static PatientRecord patientRecordFor(Patient patient){

    var pat = patient();

    var diagnoses =
      Stream.generate(() -> diagnosisFor(pat))
        .limit(RND.nextInt(3)+1)
	.collect(toList());

    var prescriptions =
      diagnoses.stream()
        .map(diag -> prescriptionFor(pat,diag))
	.collect(toList());

    return new PatientRecord(pat,diagnoses,prescriptions);
  }


  public static PatientRecord patientRecord(){
    return patientRecordFor(patient());
  }

}

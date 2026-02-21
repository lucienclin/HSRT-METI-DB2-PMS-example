package de.hsrt.meti.pms.util;


import java.io.FileWriter;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import java.util.function.Function;
import java.util.function.BiFunction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hsrt.meti.pms.core.*;



public final class CSV
{

  public static <T,U,V> Function<T,V> chain(
    Function<T,U> f1,
    Function<U,V> f2
  ){
    return f1.andThen(f2);
  }

  public static <T,U,V,W> Function<T,W> chain(
    Function<T,U> f1,
    Function<U,V> f2,
    Function<V,W> f3
  ){
    return chain(f1,f2).andThen(f3);
  }

  public static <T,U,V,W,X> Function<T,X> chain(
    Function<T,U> f1,
    Function<U,V> f2,
    Function<V,W> f3,
    Function<W,X> f4
  ){
    return chain(f1,f2,f3).andThen(f4);
  }


  public static class Writer<T> implements Function<T,String>
  {  

     public static final String DELIMITER = "|";

     public final String headers;
      
     private final List<Function<T,String>> getters;

     private Writer(
       final String headers,
       final List<Function<T,String>> getters
     ){
       this.headers = headers;
       this.getters = getters;
     }

     @Override
     public String apply(T t){
       return
         getters.stream()
	   .map(f -> f.apply(t))
	   .reduce((acc,v) -> acc + DELIMITER + v)
	   .get();
     }

     @SafeVarargs
     public static <T> Writer<T> of(
       Map.Entry<String,Function<T,String>>... fs
     ){
       return new Writer(
         Stream.of(fs).map(Map.Entry::getKey).reduce((acc,h) -> acc + DELIMITER + h).get(),
	 Stream.of(fs).map(Map.Entry::getValue).collect(toList())
       );
     }

  }


  public static Writer<Coding> codingWriter =
    Writer.of(
      entry("code",    Coding::code),
      entry("display", chain(Coding::display, d -> d.orElse(""))),
      entry("system",  Coding::system),
      entry("version", chain(Coding::version, v -> v.orElse("")))
    );


  public static Writer<Patient> PatientWriter =
    Writer.of(
      entry("id",              chain(Patient::id,Id::value)),
      entry("gender",          chain(Patient::gender, g -> g.toString().toLowerCase())),
      entry("givenName",       Patient::givenName),
      entry("familyName",      Patient::familyName),
      entry("birthDate",       chain(Patient::birthDate, ISO_LOCAL_DATE::format)),
      entry("dateOfDeath",     chain(Patient::dateOfDeath, dod -> dod.map(ISO_LOCAL_DATE::format).orElse(""))),
      entry("healthInsurance", Patient::healthInsurance),
      entry("street",          chain(Patient::address,Address::street)),
      entry("house",           chain(Patient::address,Address::house)),
      entry("postalcode",      chain(Patient::address,Address::postalCode)),
      entry("city",            chain(Patient::address,Address::city))
    );


  public static Writer<Diagnosis> DiagnosisWriter =
    Writer.of(
      entry("id",            chain(Diagnosis::id,Id::value)),
      entry("patient",       chain(Diagnosis::patient,Id::value)),
      entry("datetime",      chain(Diagnosis::recordedOn,ISO_LOCAL_DATE_TIME::format)),
      entry("icd10_code",    chain(Diagnosis::coding, Coding::code)),
      entry("icd10_display", chain(Diagnosis::coding, Coding::display, d -> d.orElse(""))),
      entry("icd10_version", chain(Diagnosis::coding, Coding::version, d -> d.orElse("")))
    );


  public static Writer<Prescription> PrescriptionWriter =
    Writer.of(
      entry("id",                 chain(Prescription::id,Id::value)),
      entry("patient",            chain(Prescription::patient,Id::value)),
      entry("diagnosis",          chain(Prescription::diagnosis,Id::value)),
      entry("datetime",           chain(Prescription::recordedOn, ISO_LOCAL_DATE_TIME::format)),
      entry("medication_code",    chain(Prescription::medication, Coding::code)),
      entry("medication_display", chain(Prescription::medication, Coding::display, d -> d.orElse(""))),
      entry("medication_version", chain(Prescription::medication, Coding::version, d -> d.orElse("")))
    );


  public static Writer<Tuple3<Patient,Diagnosis,Prescription>> TripleWriter =
    new Writer<>(
      PatientWriter.headers + Writer.DELIMITER +
      DiagnosisWriter.headers + Writer.DELIMITER +
      PrescriptionWriter.headers,
      Stream.concat(
        PatientWriter.getters
          .stream()
          .map(g -> g.compose((Tuple3<Patient,Diagnosis,Prescription> tup) -> tup._1())),
        Stream.concat(
          DiagnosisWriter.getters
            .stream()
 	    .map(g -> g.compose((Tuple3<Patient,Diagnosis,Prescription> tup) -> tup._2())),
          PrescriptionWriter.getters
            .stream()
	    .map(g -> g.compose((Tuple3<Patient,Diagnosis,Prescription> tup) -> tup._3()))
	)
      )
      .collect(toList())
    );



  public static <T> void write(
    Collection<T> ts,
    Writer<T> writer,
    String out
  ){

    try (FileWriter w = new FileWriter(out)){

      w.write(writer.headers + "\n");

      ts.stream()
	.map(writer)
	.forEach(
	  csv -> { 
            try {
              w.write(csv + "\n");
            } catch (Exception e){
              throw new RuntimeException(e);
            }
	  }
	);

    } catch (Exception e){
      throw new RuntimeException(e);
    }

  }

}

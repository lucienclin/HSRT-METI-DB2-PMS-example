package de.hsrt.meti.pms.gens;


import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hsrt.meti.pms.core.*;
import de.hsrt.meti.pms.util.CSV;


public final class RandomDataTests
{

  private static final ObjectWriter JSON_WRITER =
    new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .registerModule(new Jdk8Module())
      .writer()
      .withDefaultPrettyPrinter();


  @Test
  public void printJsonPatient() throws Exception {
    System.out.println(
      JSON_WRITER.writeValueAsString(Generators.patient())
    );
    System.out.println("");
  }


/*
  @Test
  public void printJsonPatients() throws Exception {
    Stream.generate(Generators::patient)
      .limit(42)
      .map(
        patient -> {
          try {
            return JSON_WRITER.writeValueAsString(patient);
          } catch (Exception e){
            throw new RuntimeException(e);
          }
        }
      )
      .forEach(System.out::println);
    System.out.println("");
  }
*/
/*
  @Test
  public void printJsonPatientRecord() throws Exception {
    System.out.println(
      JSON_WRITER.writeValueAsString(Generators.patientRecord())
    );
    System.out.println("");
  }


  @Test
  public void printCSVPatients() throws Exception {

    System.out.println(CSV.PatientWriter.headers);
    Stream.generate(Generators::patient)
      .limit(42)
      .map(CSV.PatientWriter)
      .forEach(System.out::println);

    System.out.println("");
  }
*/


}

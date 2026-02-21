package de.hsrt.meti.pms.repo.neo4j;


import static java.time.ZoneOffset.UTC;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;
import java.util.Optional;
import static java.util.UUID.randomUUID;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.TransactionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import static org.neo4j.driver.Values.value;
import static org.neo4j.driver.internal.value.NullValue.NULL;
import de.hsrt.meti.pms.core.Id;
import de.hsrt.meti.pms.core.Patient;
import de.hsrt.meti.pms.core.Period;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Address;
import de.hsrt.meti.pms.core.Repository;



class Neo4jRepository implements Repository
{

  // Service Provider Interface (SPI)  
  public static final class Provider implements Repository.Provider
  {

    @Override
    public Repository getInstance(){
      return instance();
    }

  }

  private final Driver driver;

  private Neo4jRepository(final Driver driver){
    this.driver = driver;
  }


  // Singleton pattern
  private static Neo4jRepository INSTANCE = setup();

  private static Neo4jRepository setup(){

    var user = System.getProperty("pms.neo4j.user");
    var pwd  = System.getProperty("pms.neo4j.password");
    var url  = System.getProperty("pms.neo4j.url");

    return new Neo4jRepository(GraphDatabase.driver(url,AuthTokens.basic(user,pwd)));
  }

  static Neo4jRepository instance(){
    return INSTANCE;
  }
  
  
  
  private static final String SAVE_PATIENT = """
    MERGE (p: Patient { id: $id })
    SET
      p.gender = $gender,
      p.givenName = $givenName,
      p.familyName = $familyName,
      p.birthDate = $birthDate,
      p.dateOfDeath = $dateOfDeath,
      p.healthInsurance = $healthInsurance,
      p.lastUpdate = $lastUpdate
    MERGE (a: Address {
      street: $street,
      house: $house,
      postalCode: $postalCode,
      city: $city
    })
    MERGE
     (p)<-[r:ADDRESS]-(a);
  """;

  private static final String GET_PATIENT =
    "MATCH (p: Patient { id: $id })-[]-(a: Address) RETURN p,a;";

  private static final String DELETE_PATIENT =
    "MATCH (p: Patient { id: $id }) DETACH DELETE p;";

  private static final String GET_PATIENTS =
    "MATCH (p: Patient)-[]-(a: Address) RETURN p,a;";


  private static Map<String,Object> parameters(Patient patient){
    return Map.ofEntries(
      entry("id",              value(patient.id().value())),
      entry("gender",          value(patient.gender().toString())),
      entry("givenName",       value(patient.givenName())),
      entry("familyName",      value(patient.familyName())),
      entry("birthDate",       value(patient.birthDate())),
      entry("dateOfDeath",     patient.dateOfDeath().map(d -> value(d)).orElse(NULL)),
      entry("healthInsurance", value(patient.healthInsurance())),
      entry("street",          value(patient.address().street())),
      entry("house",           value(patient.address().house())),
      entry("postalCode",      value(patient.address().postalCode())),
      entry("city",            value(patient.address().city())),
      entry("lastUpdate",      value(patient.lastUpdate().atOffset(UTC)))
    );

  }


  private static Patient patientFrom(Record record){

    var patient = record.get("p").asNode();
    var address = record.get("a").asNode();

    return new Patient(
      new Id<>(patient.get("id").asString()),
      Gender.valueOf(patient.get("gender").asString()),
      patient.get("givenName").asString(),
      patient.get("familyName").asString(),
      patient.get("birthDate").asLocalDate(),
      Optional.of(patient.get("dateOfDeath"))
        .filter(v -> !v.isNull())
        .map(Value::asLocalDate),
      patient.get("healthInsurance").asString(),
      new Address(
        address.get("street").asString(),
        address.get("house").asString(),
        address.get("postalCode").asString(),
        address.get("city").asString()
      ),
      patient.get("lastUpdate").asOffsetDateTime().toInstant()
    );

  }
 

 
  @Override
  public Id<Patient> patientId(){
    return new Id<>(randomUUID().toString());
  }


  @Override
  public void save(Patient patient) throws Exception {

    try (var session = driver.session()){
      session.executeWriteWithoutResult(
        txn -> txn.run(
          SAVE_PATIENT,
          parameters(patient)
        )
      );
    }

  }


  @Override
  public Optional<Patient> findPatient(Id<Patient> id){

    try (var session = driver.session()){ 
      var result =
        session.run(
          GET_PATIENT,
          Map.of("id",value(id.value())),
          TransactionConfig.empty()
        );

      return
        result.stream()
          .findFirst()
          .map(record -> patientFrom(record));
    }
    
  }


  @Override
  public List<Patient> findPatients(Patient.Filter filter){

    try (var session = driver.session()){ 
      var result =
        session.run(
          GET_PATIENTS,   //TODO: Filter criteria!
          TransactionConfig.empty()
        );

      return
        result.list(record -> patientFrom(record));
    }
    
  }


  @Override
  public Optional<Patient> deletePatient(Id<Patient> id) throws Exception {

    var patient = findPatient(id);

    try (var session = driver.session()){ 
      session.executeWriteWithoutResult(
        txn -> txn.run(
          DELETE_PATIENT,
          Map.of("id",value(id.value()))
        )
      );
    }

    return patient;
  }

}

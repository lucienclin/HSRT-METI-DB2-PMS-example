package de.hsrt.meti.pms.repo.jdbc;


import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;
import static java.util.Map.entry;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;
import static java.util.UUID.randomUUID;
import de.hsrt.meti.pms.core.Id;
import de.hsrt.meti.pms.core.Patient;
import de.hsrt.meti.pms.core.Period;
import de.hsrt.meti.pms.core.Gender;
import de.hsrt.meti.pms.core.Address;
import de.hsrt.meti.pms.core.Repository;



class JDBCRepository implements Repository
{

  // Service Provider Interface (SPI)  
  public static final class Provider implements Repository.Provider
  {

    @Override
     public Repository getInstance(){
       return JDBCRepository.instance();
    }

  }


  private final Connection conn;

  private JDBCRepository(Connection conn){ 
    this.conn = conn;
  }


  // Factory method
  static JDBCRepository instance(){
    try {
      var conn =
        DriverManager.getConnection(
          System.getProperty("pms.repo.jdbc.url"),
          System.getProperty("pms.repo.jdbc.user"),
          System.getProperty("pms.repo.jdbc.password")
        );

      var repo = new JDBCRepository(conn);
      repo.setup();
      return repo;

    } catch (SQLException e){
      throw new RuntimeException(e);
    }
  }
  

  private static final String CREATE_PATIENT_TABLE = """
    CREATE TABLE IF NOT EXISTS patients(
      id VARCHAR(50) PRIMARY KEY,
      gender VARCHAR(10) NOT NULL,
      givenName VARCHAR(100) NOT NULL,
      familyName VARCHAR(100) NOT NULL,
      birthDate DATE NOT NULL,
      dateOfDeath DATE,
      healthInsurance VARCHAR(40) NOT NULL,
      street VARCHAR(50) NOT NULL,
      house VARCHAR(50) NOT NULL,
      postalCode VARCHAR(50) NOT NULL,
      city VARCHAR(50) NOT NULL,
      lastUpdate TIMESTAMP NOT NULL
    );
  """;


  // Set up DB tables etc.
  void setup(){
    try (var stmt = conn.createStatement()){

      stmt.execute(CREATE_PATIENT_TABLE); 

    } catch (SQLException e){
      throw new RuntimeException(e);
    }
  }


  private static String quoted(String s){
    return String.format("'%s'",s);
  }

  private static String sqlValue(Object obj){

    return switch(obj){
      case LocalDate date -> quoted(Date.valueOf(date).toString());
      case Instant t      -> quoted(Timestamp.from(t).toString());
      case Integer n      -> Integer.toString(n);
      case Long n         -> Long.toString(n);
      case Double n       -> Double.toString(n);
      default             -> quoted(obj.toString());
    };

  }


  private static Patient readPatientFromRow(ResultSet rs) throws SQLException {
    return new Patient(
      new Id<>(rs.getString("id")),
      Gender.valueOf(rs.getString("gender")),
      rs.getString("givenName"),
      rs.getString("familyName"),
      rs.getDate("birthDate").toLocalDate(),
      Optional.ofNullable(rs.getDate("dateofdeath")).map(Date::toLocalDate),
      rs.getString("healthInsurance"),
      new Address(
        rs.getString("street"),
        rs.getString("house"),
        rs.getString("postalCode"),
        rs.getString("city")
      ),
      rs.getTimestamp("lastUpdate").toInstant()
    );
  }


  private static String insertSQL(Patient patient){
    return
      "INSERT INTO patients(" + 
        "id,gender,givenName,familyName,birthDate," +
        patient.dateOfDeath().map(d -> "dateOfDeath,").orElse("") +
        "healthInsurance,street,house,postalCode,city,lastUpdate" + 
      ") VALUES (" + 
        sqlValue(patient.id().value()) + "," +
        sqlValue(patient.gender()) + "," +
        sqlValue(patient.givenName()) + "," +
        sqlValue(patient.familyName()) + "," +
        sqlValue(patient.birthDate()) + "," +
        patient.dateOfDeath().map(d -> sqlValue(d) + ",").orElse("") +
        sqlValue(patient.healthInsurance()) + "," +
        sqlValue(patient.address().street()) + "," +
        sqlValue(patient.address().house()) + "," +
        sqlValue(patient.address().postalCode()) + "," +
        sqlValue(patient.address().city()) + "," +
        sqlValue(patient.lastUpdate()) +
      ");";
  }

  private static String updateSQL(Patient patient){
    return
      "UPDATE patients SET " +
        "gender = " + sqlValue(patient.gender()) + "," +
        "givenName = " + sqlValue(patient.givenName()) + "," +
        "familyName = " + sqlValue(patient.familyName()) + "," +
        "birthDate = " + sqlValue(patient.birthDate()) + "," +
        patient.dateOfDeath().map(d -> "dateOfDeath = " + sqlValue(d) + " ,").orElse("") +
        "healthInsurance = " + sqlValue(patient.healthInsurance()) + "," +
        "street = " + sqlValue(patient.address().street()) + "," +
        "house = " + sqlValue(patient.address().house()) + "," +
        "postalCode = " + sqlValue(patient.address().postalCode()) + "," +
        "city = " + sqlValue(patient.address().city()) + "," +
        "lastUpdate = " + sqlValue(patient.lastUpdate()) + " " +
      "WHERE id = " + sqlValue(patient.id().value()) + ";";
  }



  @Override
  public Id<Patient> patientId(){

    var id = new Id<Patient>(randomUUID().toString());

    return findPatient(id).isEmpty() ? id : patientId();
  }


  @Override
  public void save(Patient patient) throws SQLException {

    try (
      var stmt = conn.createStatement()
    ){
      var sql =
        findPatient(patient.id()).isPresent() ?
          updateSQL(patient) :
          insertSQL(patient);

      stmt.executeUpdate(sql);

    } catch (SQLException e){
      throw new RuntimeException(e);
    }

  }


  @Override
  public Optional<Patient> findPatient(Id<Patient> id){
    try (
      var result =
        conn.createStatement()
          .executeQuery("SELECT * FROM patients WHERE id = " + sqlValue(id.value()) + ";")
    ){
      return
        result.next() ?
          Optional.of(readPatientFromRow(result)) :
          Optional.empty();

    } catch (SQLException e){
      throw new RuntimeException(e);
    }
  }



  private static Optional<String> whereClause(Patient.Filter filter){

    return
      Stream.of(
        filter.gender().map(set -> "gender IN (" + set.stream().map(g -> sqlValue(g)).reduce((s,t) -> s + "," + t).orElse(",") + ")").orElse(""),
        filter.familyName().map(n -> "familyName LIKE " + sqlValue(n)).orElse(""),
        filter.birthDatePeriod().map(Period::start).map(d -> "birthDate >= " + sqlValue(d)).orElse(""),
        filter.birthDatePeriod().flatMap(Period::end).map(d -> "birthDate <= " + sqlValue(d)).orElse("")
        //TODO: address criteria
      )
      .filter(s -> !s.isEmpty())
      .reduce((s,t) -> s + " AND " + t)
      .map(c -> "WHERE " + c); 
  }


  @Override
  public List<Patient> findPatients(Patient.Filter filter){

    var sql = "SELECT * FROM patients " + whereClause(filter).orElse("") + ";";

    try (
      var resultSet =
        conn.createStatement().executeQuery(sql)
    ){

      var patients = new ArrayList<Patient>();

      while(resultSet.next()){
        patients.add(readPatientFromRow(resultSet));
      }

      return patients;

    } catch (SQLException e){
      throw new RuntimeException(e);
    }
      
  }


  @Override
  public Optional<Patient> deletePatient(Id<Patient> id) throws SQLException {

    var patient = findPatient(id);

    patient.ifPresent(
      p -> {
        try {
          conn.createStatement()
           .executeUpdate("DELETE FROM patients WHERE id = " + quoted(id.value()) + ";");
        } catch (SQLException e){
          throw new RuntimeException(e);
        }
      }
    );

    return patient;
  }


}

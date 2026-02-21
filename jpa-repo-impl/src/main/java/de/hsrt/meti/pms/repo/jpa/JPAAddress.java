package de.hsrt.meti.pms.repo.jpa;



import jakarta.persistence.Embeddable;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import de.hsrt.meti.pms.core.Address;


@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Embeddable
final class JPAAddress
{

  public String street;
  public String house;
  public String postalCode;
  public String city;


  public static JPAAddress from(Address address){
    return JPAAddress.of(
      address.street(),
      address.house(),
      address.postalCode(),
      address.city()
    ); 
  }

}

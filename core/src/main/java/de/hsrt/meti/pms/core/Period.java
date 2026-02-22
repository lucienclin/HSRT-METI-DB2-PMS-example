package de.hsrt.meti.pms.core;


import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.Comparator;


// Represents an open-end period, i.e. with defined start but optional end
public record Period<T extends Temporal>
(
  T start,
  Optional<T> end
)
{

  // Convenience method to check whether t is within the period, 
  // i.e. (depending on Comparator equal to or after the "start" and before or equal to "end" (if defined) 
  public boolean contains(T t, Comparator<T> comp){
    return comp.compare(start,t) <= 0 && end.map(e -> comp.compare(t,e) <= 0).orElse(true); 
  }

}

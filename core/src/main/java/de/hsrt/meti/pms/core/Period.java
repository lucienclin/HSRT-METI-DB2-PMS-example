package de.hsrt.meti.pms.core;


import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.Comparator;


public record Period<T extends Temporal>
(
  T start,
  Optional<T> end
)
{

  public boolean contains(T t, Comparator<T> comp){
    return comp.compare(start,t) <= 0 && end.map(e -> comp.compare(t,e) <= 0).orElse(true); 
  }

}

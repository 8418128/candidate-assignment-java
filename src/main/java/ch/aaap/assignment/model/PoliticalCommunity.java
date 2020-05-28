package ch.aaap.assignment.model;

import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PoliticalCommunity {

  public String number;

  public String name;

  public String shortName;

  public LocalDate lastUpdate;

  public Set<PostalCommunity> postalCommunities;

}

package ch.aaap.assignment.model;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class District {

  public String number;

  public String name;

  public Set<PoliticalCommunity> politicalCommunities;

}

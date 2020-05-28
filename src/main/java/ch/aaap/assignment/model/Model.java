package ch.aaap.assignment.model;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Model {

  public Set<PoliticalCommunity> politicalCommunities;

  public Set<PostalCommunity> postalCommunities;

  public Set<Canton> cantons;

  public Set<District> districts;
}

package ch.aaap.assignment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostalCommunity {

  public String zipCode;

  public String zipCodeAddition;

  public String name;

}

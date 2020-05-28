package ch.aaap.assignment.model;

import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Canton {

  public String code;

  public String name;

  public Set<District> districts;

}

package ch.aaap.assignment.model;

import ch.aaap.assignment.raw.CSVPoliticalCommunity;
import ch.aaap.assignment.raw.CSVPostalCommunity;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Model {

  public Set<PoliticalCommunity> politicalCommunities;

  public Set<PostalCommunity> postalCommunities;

  public Set<Canton> cantons;

  public Set<District> districts;

  public Set<PostalCommunity> managePostalCommunities(CSVPoliticalCommunity pc,
      Set<CSVPostalCommunity> postalCommunities) {
    Set<CSVPostalCommunity> postalComms = postalCommunities.stream().filter(
        csvPostalCommunity -> csvPostalCommunity.getPoliticalCommunityNumber()
            .equals(pc.getNumber())).collect(Collectors.toSet());
    Set<PostalCommunity> postalCommsModel = new HashSet<>();
    for (CSVPostalCommunity p : postalComms) {
      PostalCommunity postalCommunity = PostalCommunity.builder().name(p.getName())
          .zipCode(p.getZipCode()).zipCodeAddition(p.getZipCodeAddition()).build();
      postalCommsModel.add(postalCommunity);
      getPostalCommunities().add(postalCommunity);
    }
    return postalCommsModel;
  }

  public void manageDistrictsAndCantons(CSVPoliticalCommunity pc,
      PoliticalCommunity politicalCommunity) {
    Optional<District> opDis = getDistricts().stream()
        .filter(district -> district.getNumber().equals(pc.getDistrictNumber())).findFirst();
    if (opDis.isEmpty()) {
      District d = District.builder().politicalCommunities(new HashSet<>())
          .number(pc.getDistrictNumber()).name(pc.getDistrictName()).build();
      d.getPoliticalCommunities().add(politicalCommunity);
      getDistricts().add(d);

      Optional<Canton> opCanton = getCantons().stream()
          .filter(canton -> canton.getCode().equals(pc.getCantonCode())).findFirst();

      if (opCanton.isEmpty()) {
        Canton c = Canton.builder().districts(new HashSet<>()).name(pc.getCantonName())
            .code(pc.getCantonCode()).build();
        c.getDistricts().add(d);
        getCantons().add(c);
      } else {
        opCanton.get().getDistricts().add(d);
      }

    } else {
      opDis.get().getPoliticalCommunities().add(politicalCommunity);
    }
  }
}

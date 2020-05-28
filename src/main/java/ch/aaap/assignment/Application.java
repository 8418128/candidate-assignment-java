package ch.aaap.assignment;

import ch.aaap.assignment.model.Canton;
import ch.aaap.assignment.model.District;
import ch.aaap.assignment.model.Model;
import ch.aaap.assignment.model.PoliticalCommunity;
import ch.aaap.assignment.model.PostalCommunity;
import ch.aaap.assignment.raw.CSVPoliticalCommunity;
import ch.aaap.assignment.raw.CSVPostalCommunity;
import ch.aaap.assignment.raw.CSVUtil;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Application {

  private Model model = null;

  public Application() {
    initModel();
  }

  public static void main(String[] args) {
    new Application();
  }

  /**
   * Reads the CSVs and initializes a in memory model
   */
  private void initModel() {
    Set<CSVPoliticalCommunity> politicalCommunities = CSVUtil.getPoliticalCommunities();
    Set<CSVPostalCommunity> postalCommunities = CSVUtil.getPostalCommunities();
    model = build(politicalCommunities, postalCommunities);
  }

  public Model build(Set<CSVPoliticalCommunity> politicalCommunities,
      Set<CSVPostalCommunity> postalCommunities) {
    Model m = Model.builder().cantons(new HashSet<>()).districts(new HashSet<>())
        .politicalCommunities(new HashSet<>()).postalCommunities(new HashSet<>()).build();

    for (CSVPoliticalCommunity pc : politicalCommunities) {
      Set<CSVPostalCommunity> postalComms = postalCommunities.stream().filter(
          csvPostalCommunity -> csvPostalCommunity.getPoliticalCommunityNumber()
              .equals(pc.getNumber())).collect(Collectors.toSet());
      Set<PostalCommunity> postalCommsModel = new HashSet<>();
      for (CSVPostalCommunity p : postalComms) {
        PostalCommunity postalCommunity = PostalCommunity.builder().name(p.getName())
            .zipCode(p.getZipCode()).zipCodeAddition(p.getZipCodeAddition()).build();
        postalCommsModel.add(postalCommunity);
        m.getPostalCommunities().add(postalCommunity);
      }
      PoliticalCommunity politicalCommunity = PoliticalCommunity.builder().name(pc.getName())
          .lastUpdate(pc.getLastUpdate()).number(pc.getNumber()).postalCommunities(postalCommsModel)
          .build();
      m.getPoliticalCommunities().add(politicalCommunity);

      Optional<District> opDis = m.getDistricts().stream()
          .filter(district -> district.getNumber().equals(pc.getDistrictNumber())).findFirst();
      if (opDis.isEmpty()) {
        District d = District.builder().politicalCommunities(new HashSet<>())
            .number(pc.getDistrictNumber()).name(pc.getDistrictName()).build();
        d.getPoliticalCommunities().add(politicalCommunity);
        m.getDistricts().add(d);

        Optional<Canton> opCanton = m.getCantons().stream()
            .filter(canton -> canton.getCode().equals(pc.getCantonCode())).findFirst();

        if (opCanton.isEmpty()) {
          Canton c = Canton.builder().districts(new HashSet<>()).name(pc.getCantonName())
              .code(pc.getCantonCode()).build();
          c.getDistricts().add(d);
          m.getCantons().add(c);
        } else {
          opCanton.get().getDistricts().add(d);
        }

      } else {
        opDis.get().getPoliticalCommunities().add(politicalCommunity);
      }

    }
    return m;
  }

  /**
   * @return model
   */
  public Model getModel() {
    return model;
  }

  public Canton getCantonByCode(String code) {
    return getModel().getCantons().stream()
        .filter(canton -> canton.getCode().equalsIgnoreCase(code)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException());
  }

  public District getDistrictByNumber(String number) {
    return getModel().getDistricts().stream()
        .filter(district -> district.getNumber().equalsIgnoreCase(number)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException());
  }

  /**
   * @param cantonCode of a canton (e.g. ZH)
   * @return amount of political communities in given canton
   */
  public long getAmountOfPoliticalCommunitiesInCanton(String cantonCode) {
    Canton canton = getCantonByCode(cantonCode);
    long size = canton.getDistricts().stream()
        .collect(Collectors.summingInt(value -> value.getPoliticalCommunities().size()));
    return size;
  }

  /**
   * @param cantonCode of a canton (e.g. ZH)
   * @return amount of districts in given canton
   */
  public long getAmountOfDistrictsInCanton(String cantonCode) {
    return getCantonByCode(cantonCode).getDistricts().size();
  }

  /**
   * @param districtNumber of a district (e.g. 101)
   * @return amount of districts in given canton
   */
  public long getAmountOfPoliticalCommunitiesInDistict(String districtNumber) {
    return getDistrictByNumber(districtNumber).getPoliticalCommunities().size();
  }

  /**
   * @param zipCode 4 digit zip code
   * @return district that belongs to specified zip code
   */
  public Set<String> getDistrictsForZipCode(String zipCode) {
    Set<String> districts = new HashSet<>();
    getModel().getDistricts().stream().filter(
        district -> district.getPoliticalCommunities().stream().anyMatch(
            politicalCommunity -> politicalCommunity.getPostalCommunities().stream()
                .anyMatch(postalCommunity -> postalCommunity.zipCode.equals(zipCode))))
        .forEach(district -> districts.add(district.name));
    return districts;
  }

  /**
   * @param postalCommunityName name
   * @return lastUpdate of the political community by a given postal community name
   */
  public LocalDate getLastUpdateOfPoliticalCommunityByPostalCommunityName(
      String postalCommunityName) {
    PoliticalCommunity pc = getModel().getPoliticalCommunities().stream().filter(
        politicalCommunity -> politicalCommunity.getPostalCommunities().stream().anyMatch(
            postalCommunity -> postalCommunity.getName().equalsIgnoreCase(postalCommunityName)))
        .findFirst().get();
    return pc.getLastUpdate();
  }

  /**
   * https://de.wikipedia.org/wiki/Kanton_(Schweiz)
   *
   * @return amount of canton
   */
  public long getAmountOfCantons() {
    return model.getCantons().size();
  }

  /**
   * https://de.wikipedia.org/wiki/Kommunanz
   *
   * @return amount of political communities without postal communities
   */
  public long getAmountOfPoliticalCommunityWithoutPostalCommunities() {
    return getModel().getPoliticalCommunities().stream()
        .filter(politicalCommunity -> politicalCommunity.getPostalCommunities().size() == 0)
        .count();
  }
}

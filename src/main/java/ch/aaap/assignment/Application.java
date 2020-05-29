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
      Set<PostalCommunity> postalCommsModel = m.managePostalCommunities(pc, postalCommunities);
      PoliticalCommunity politicalCommunity = PoliticalCommunity.builder().name(pc.getName())
          .lastUpdate(pc.getLastUpdate()).number(pc.getNumber()).postalCommunities(postalCommsModel)
          .build();
      m.getPoliticalCommunities().add(politicalCommunity);
      m.manageDistrictsAndCantons(pc, politicalCommunity);
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
        .orElseThrow(IllegalArgumentException::new);
  }

  public District getDistrictByNumber(String number) {
    return getModel().getDistricts().stream()
        .filter(district -> district.getNumber().equalsIgnoreCase(number)).findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }

  /**
   * @param cantonCode of a canton (e.g. ZH)
   * @return amount of political communities in given canton
   */
  public long getAmountOfPoliticalCommunitiesInCanton(String cantonCode) {
    return getCantonByCode(cantonCode).getDistricts().stream()
        .mapToInt(value -> value.getPoliticalCommunities().size()).sum();
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
    return getModel().getDistricts().stream().filter(
        district -> district.getPoliticalCommunities().stream().anyMatch(
            politicalCommunity -> politicalCommunity.getPostalCommunities().stream()
                .anyMatch(postalCommunity -> postalCommunity.zipCode.equals(zipCode))))
        .map(District::getName).collect(Collectors.toSet());
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

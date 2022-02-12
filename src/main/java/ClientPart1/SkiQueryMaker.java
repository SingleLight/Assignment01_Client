package ClientPart1;

public class SkiQueryMaker {

  public static String makeSkiQuery(String baseAddress, int resortID, String seasonID, String dayID,
      int skierID) {
    String s = baseAddress
        + "/skiers/"
        + resortID
        + "/seasons/"
        + seasonID
        + "/days/"
        + dayID
        + "/skiers/"
        + skierID;
    return s;
  }

}

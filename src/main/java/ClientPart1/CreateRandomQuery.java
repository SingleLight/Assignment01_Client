package ClientPart1;

import java.util.Random;

public class CreateRandomQuery {

  private static Random random = new Random();

  public static String createRandomSkiQuery(String base, int skierIDStart, int skierIDEnd) {
    return SkiQueryMaker.makeSkiQuery(base, 1,"spring", "365", random.nextInt(skierIDEnd - skierIDStart) + skierIDStart);
  }

  public static String createRandomLiftRideJson(int numLifts, int timeStart, int timeEnd) {
    LiftRideJson liftRideJson = new LiftRideJson(random.nextInt(timeEnd - timeStart) + timeStart,
        random.nextInt(numLifts), random.nextInt(10));
    return liftRideJson.toJson();
  }
}

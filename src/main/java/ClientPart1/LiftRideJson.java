package ClientPart1;

import com.google.gson.Gson;

public class LiftRideJson {

  public int time;
  public int liftID;
  public int waitTime;

  public LiftRideJson(int time, int liftID, int waitTime) {
    this.time = time;
    this.liftID = liftID;
    this.waitTime = waitTime;
  }

  public String toJson() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}

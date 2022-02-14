package ClientPart2;

public class POSTRecord {

  public long startTime;
  public long latency;
  public String requestType;
  public int responseCode;

  public POSTRecord(long startTime, long latency, String requestType, int responseCode) {
    this.startTime = startTime;
    this.latency = latency;
    this.requestType = requestType;
    this.responseCode = responseCode;
  }
}

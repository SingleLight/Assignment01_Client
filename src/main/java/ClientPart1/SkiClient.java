package ClientPart1;

import io.swagger.client.ApiClient;
import io.swagger.client.api.ResortsApi;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.api.StatisticsApi;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SkiClient {

  private final int numThreads;
  private final int numSkiers;
  private final int numLifts;
  private final int numRuns;
  private final String serverAddress;
  private final ResortsApi resortsApi;
  private final StatisticsApi statisticsApi;
  private final SkiersApi skiersApi;
  private final ApiClient apiClient;
  private final AtomicInteger successCounter;
  private final AtomicInteger failureCounter;

  public SkiClient(int numThreads, int numSkiers, int numLifts, int numRuns, String serverAddress) {
    if (numThreads > 1024 || numThreads < 1 || numSkiers > 100000 || numSkiers < 0 || numLifts < 5
        || numLifts > 60 || numRuns > 20 || numRuns < 0) {
      throw new IllegalArgumentException("Illegal parameters provided");
    }
    this.serverAddress = serverAddress;
    this.numThreads = numThreads;
    this.numSkiers = numSkiers;
    this.numLifts = numLifts;
    this.numRuns = numRuns;
    this.apiClient = new ApiClient();
    this.apiClient.setBasePath(serverAddress);
    this.resortsApi = new ResortsApi(apiClient);
    this.statisticsApi = new StatisticsApi(apiClient);
    this.skiersApi = new SkiersApi(apiClient);
    this.successCounter = new AtomicInteger(0);
    this.failureCounter = new AtomicInteger(0);
  }

  //constructor with default values
  public SkiClient(int numThreads, int numSkiers, String serverAddress) {
    this(numThreads, numSkiers, 40, 10, serverAddress);
  }

  public void phaseOne() throws InterruptedException {
    int phaseOneThreadNumber = numThreads / 4;
    if (phaseOneThreadNumber < 0) {
      phaseOneThreadNumber = 1;
    }
    int onePortionSkierID = numSkiers / phaseOneThreadNumber;
    int phaseOneStartTime = 1;
    int phaseOneEndTime = 90;
    CountDownLatch phaseOneCountDownLatch = new CountDownLatch(
        phaseOneThreadNumber
    );
    for (int i = 0; i < phaseOneThreadNumber; i++) {
      Thread phaseOneThread = new Thread(
          new RequestThread(successCounter, failureCounter, serverAddress, i * onePortionSkierID,
              (i + 1) * onePortionSkierID, phaseOneStartTime, phaseOneEndTime, numLifts, skiersApi,
              new Random(), phaseOneCountDownLatch));
      phaseOneThread.start();
    }
    phaseOneCountDownLatch.await();
    System.out.println(successCounter);
    System.out.println(failureCounter);
  }

  public static void main(String[] args) throws InterruptedException {
    SkiClient skiClient = new SkiClient(32, 1024, 40, 10, "http://ec2-34-210-25-131.us-west-2.compute.amazonaws.com:8080/Assignment01_war%20exploded/ski");
    skiClient.phaseOne();
  }


}

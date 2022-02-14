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
    this.apiClient.setBasePath(this.serverAddress);
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

  public void automate() throws InterruptedException {
    int phaseOneTotalThreadCount = (int) (numThreads * 0.25);
    int phaseOnePartialThreadCount = (int) Math.ceil(phaseOneTotalThreadCount * 0.2);
    int phaseTwoTotalThreadCount = numThreads;
    int phaseTwoPartialThreadCount = (int) Math.ceil(phaseTwoTotalThreadCount * 0.2);
    int phaseThreeTotalThreadCount = (int) (numThreads * 0.1);
    CountDownLatch totalCountDownLatch = new CountDownLatch(
        phaseOneTotalThreadCount + phaseTwoTotalThreadCount + phaseThreeTotalThreadCount);
    CountDownLatch phaseOneCountDownLatch = new CountDownLatch(phaseOnePartialThreadCount);
    CountDownLatch phaseTwoCountDownLatch = new CountDownLatch(phaseTwoPartialThreadCount);
    CountDownLatch phaseThreeCountDownLatch = new CountDownLatch(phaseThreeTotalThreadCount);

    phaseOne(phaseOneTotalThreadCount, phaseOneCountDownLatch, totalCountDownLatch);
    phaseTwo(phaseTwoTotalThreadCount, phaseTwoCountDownLatch, totalCountDownLatch);
    phaseThree(phaseThreeTotalThreadCount, phaseThreeCountDownLatch, totalCountDownLatch);

    totalCountDownLatch.await();
    System.out.println(successCounter);
    System.out.println(failureCounter);
    System.out.println("end");
  }

  public void phaseOne(int phaseOneTotalThreadCount, CountDownLatch phaseOneCountDownLatch,
      CountDownLatch totalCountDownLatch)
      throws InterruptedException {
    int onePortionSkierID = numSkiers / phaseOneTotalThreadCount;
    int phaseOneStartTime = 1;
    int phaseOneEndTime = 90;
    for (int i = 0; i < phaseOneTotalThreadCount; i++) {
      Thread phaseOneThread = new Thread(
          new RequestThread(successCounter, failureCounter, i * onePortionSkierID,
              (i + 1) * onePortionSkierID, phaseOneStartTime, phaseOneEndTime, numLifts, skiersApi,
              new Random(), phaseOneCountDownLatch, totalCountDownLatch,
              (int) Math.ceil(numRuns * 0.2)));
      phaseOneThread.start();
    }
    phaseOneCountDownLatch.await();
  }

  public void phaseTwo(int phaseTwoTotalThreadCount, CountDownLatch phaseTwoCountDownLatch,
      CountDownLatch totalCountDownLatch)
      throws InterruptedException {
    int phaseTwoStartTime = 91;
    int phaseTwoEndTime = 360;
    int onePortionSkierID = numSkiers / phaseTwoTotalThreadCount;
    for (int i = 0; i < phaseTwoTotalThreadCount; i++) {
      Thread phaseTwoThread = new Thread(new RequestThread(
          successCounter, failureCounter, i * onePortionSkierID, (i + 1) * onePortionSkierID,
          phaseTwoStartTime, phaseTwoEndTime, numLifts, skiersApi, new Random(),
          phaseTwoCountDownLatch, totalCountDownLatch,
          (int) Math.ceil(numRuns * 0.6)
      ));
      phaseTwoThread.start();
    }
    phaseTwoCountDownLatch.await();
  }

  public void phaseThree(int phaseThreeTotalThreadCount, CountDownLatch phaseThreeCountDownLatch, CountDownLatch totalCountDownLatch)
      throws InterruptedException {
    int phaseThreeStartTime = 361;
    int phaseThreeEndTime = 420;
    int onePortionSkierID = numSkiers / phaseThreeTotalThreadCount;
    for (int i = 0; i < phaseThreeTotalThreadCount; i++) {
      Thread phaseThreeThread = new Thread(new RequestThread(
          successCounter, failureCounter, i * onePortionSkierID, (i + 1) * onePortionSkierID,
          phaseThreeStartTime, phaseThreeEndTime, numLifts, skiersApi, new Random(),
          phaseThreeCountDownLatch, totalCountDownLatch, (int) Math.ceil(numRuns * 0.1)
      ));
       phaseThreeThread.start();
    }
    phaseThreeCountDownLatch.await();
  }


}

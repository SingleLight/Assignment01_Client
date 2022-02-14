package ClientPart2;

import com.opencsv.CSVWriter;
import io.swagger.client.ApiClient;
import io.swagger.client.api.ResortsApi;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.api.StatisticsApi;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
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
  private final ConcurrentLinkedQueue<POSTRecord> latencyQueue;
  private final List<Long> latencies;

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
    this.latencyQueue = new ConcurrentLinkedQueue<>();
    this.latencies = new ArrayList<>();
  }

  //constructor with default values
  public SkiClient(int numThreads, int numSkiers, String serverAddress) {
    this(numThreads, numSkiers, 40, 10, serverAddress);
  }

  public void automate() throws InterruptedException, IOException {

    int phaseOneTotalThreadCount = (int) Math.ceil(numThreads * 0.25);
    int phaseOnePartialThreadCount = (int) Math.ceil(phaseOneTotalThreadCount * 0.2);
    int phaseTwoTotalThreadCount = numThreads;
    int phaseTwoPartialThreadCount = (int) Math.ceil(phaseTwoTotalThreadCount * 0.2);
    int phaseThreeTotalThreadCount = (int) Math.ceil(numThreads * 0.1);
    CountDownLatch totalCountDownLatch = new CountDownLatch(
        phaseOneTotalThreadCount + phaseTwoTotalThreadCount + phaseThreeTotalThreadCount);
    CountDownLatch phaseOneCountDownLatch = new CountDownLatch(phaseOnePartialThreadCount);
    CountDownLatch phaseTwoCountDownLatch = new CountDownLatch(phaseTwoPartialThreadCount);
    CountDownLatch phaseThreeCountDownLatch = new CountDownLatch(phaseThreeTotalThreadCount);

    long begin = System.currentTimeMillis();

    phaseOne(phaseOneTotalThreadCount, phaseOneCountDownLatch, totalCountDownLatch);
    phaseTwo(phaseTwoTotalThreadCount, phaseTwoCountDownLatch, totalCountDownLatch);
    phaseThree(phaseThreeTotalThreadCount, phaseThreeCountDownLatch, totalCountDownLatch);

    totalCountDownLatch.await();

    double wallTime = (System.currentTimeMillis() - begin) / 1000.0;
    if (wallTime == 0) {
      wallTime = 1;
    }
    totalCountDownLatch.await();
    csvMaker(latencyQueue);
    System.out.println("success posts:" + successCounter);
    System.out.println("failed posts: " + failureCounter);
    System.out.println("wall time: " + wallTime);
    System.out.println(
        "total throughput: " + (successCounter.get() + failureCounter.get()) / wallTime);
    calculate();
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
              (int) Math.ceil(numRuns * 0.2), latencyQueue));
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
          (int) Math.ceil(numRuns * 0.6), latencyQueue
      ));
      phaseTwoThread.start();
    }
    phaseTwoCountDownLatch.await();
  }

  public void phaseThree(int phaseThreeTotalThreadCount, CountDownLatch phaseThreeCountDownLatch,
      CountDownLatch totalCountDownLatch)
      throws InterruptedException {
    int phaseThreeStartTime = 361;
    int phaseThreeEndTime = 420;
    int onePortionSkierID = numSkiers / phaseThreeTotalThreadCount;
    for (int i = 0; i < phaseThreeTotalThreadCount; i++) {
      Thread phaseThreeThread = new Thread(new RequestThread(
          successCounter, failureCounter, i * onePortionSkierID, (i + 1) * onePortionSkierID,
          phaseThreeStartTime, phaseThreeEndTime, numLifts, skiersApi, new Random(),
          phaseThreeCountDownLatch, totalCountDownLatch, (int) Math.ceil(numRuns * 0.1),
          latencyQueue
      ));
      phaseThreeThread.start();
    }
    phaseThreeCountDownLatch.await();
  }

  public void csvMaker(ConcurrentLinkedQueue<POSTRecord> latencyQueue)
      throws IOException {
    CSVWriter csvWriter = new CSVWriter(new FileWriter("docs/output.csv"));
    csvWriter.writeNext(new String[]{"start time", "request type", "latency", "response code"});
    while (!latencyQueue.isEmpty()) {
      POSTRecord record = latencyQueue.poll();
      csvWriter.writeNext(new String[]{String.valueOf(record.startTime), record.requestType,
          String.valueOf(record.latency), String.valueOf(record.responseCode)});
      latencies.add(record.latency);
    }
    csvWriter.close();
  }

  public void calculate() {
    long sum = 0;
    Collections.sort(latencies);
    for (int i = 0; i < latencies.size(); i++) {
      sum += latencies.get(i);
    }
    long meanResponseTime = sum / latencies.size();
    long medianResponseTime = latencies.get(latencies.size() / 2);
    long ninetyNinePercentile = latencies.get(latencies.size() * 99 / 100);
    long minResponseTime = latencies.get(0);
    long maxResponseTime = latencies.get(latencies.size() - 1);
    System.out.println("mean response time:" + meanResponseTime);
    System.out.println("median response time:" + medianResponseTime);
    System.out.println("99 percentile time: " + ninetyNinePercentile);
    System.out.println("min response time: " + minResponseTime);
    System.out.println("max response time: " + maxResponseTime);
  }


}

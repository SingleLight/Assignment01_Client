package ClientPart2;

import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestThread implements Runnable {

  private final AtomicInteger successCounter;
  private final AtomicInteger failureCounter;
  private final int startSkierID;
  private final int endSkierID;
  private final int startTime;
  private final int endTime;
  private final int numLifts;
  private final SkiersApi skiersApi;
  private final Random random;
  private final CountDownLatch countDownLatch;
  private final CountDownLatch totalCountDownLatch;
  private final int numRuns;
  private final ConcurrentLinkedQueue<POSTRecord> latencyQueue;

  public RequestThread(AtomicInteger successCounter,
      AtomicInteger failureCounter, int startSkierID, int endSkierID, int startTime,
      int endTime, int numLifts, SkiersApi skiersApi, Random random, CountDownLatch countDownLatch,
      CountDownLatch totalCountDownLatch, int numRuns, ConcurrentLinkedQueue<POSTRecord> latencyQueue) {
    this.successCounter = successCounter;
    this.failureCounter = failureCounter;
    this.startSkierID = startSkierID;
    this.endSkierID = endSkierID;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numLifts = numLifts;
    this.skiersApi = skiersApi;
    this.random = random;
    this.countDownLatch = countDownLatch;
    this.totalCountDownLatch = totalCountDownLatch;
    this.numRuns = numRuns;
    this.latencyQueue = latencyQueue;
  }

  @Override
  public void run() {
    int runs = numRuns * (endSkierID - startSkierID);
    for (int i = 0; i < runs; i++) {
      for (int j = 0; j < 5; j++) {
        try {
          long initTime = System.currentTimeMillis();
          skiersApi.writeNewLiftRide(new LiftRide().liftID(random.nextInt(numLifts))
                  .time(random.nextInt(endTime - startTime) + startTime).waitTime(random.nextInt(10)),
              1,
              "seasonExample", "dayExample",
              random.nextInt(endSkierID - startSkierID) + startSkierID);
          successCounter.incrementAndGet();
          latencyQueue.offer(new POSTRecord(initTime, System.currentTimeMillis() - initTime, "POST", 200));
          break;
        } catch (ApiException e) {
          e.printStackTrace();
          if (j == 4) {
            failureCounter.incrementAndGet();
            break;
          }
          try {
            Thread.sleep((long) Math.pow(2, j));
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }
      }
    }
    countDownLatch.countDown();
    totalCountDownLatch.countDown();
  }
}

package ClientPart2;

import java.io.IOException;
import java.util.Scanner;

public class UserInterface {
  private int maxThreadNumber;
  private int numSkiers;
  private int numLifts;
  private int numRuns;
  private String address;

  public void connect() throws InterruptedException, IOException {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Please provide the max thread number (max 1024)");
    maxThreadNumber = scanner.nextInt();
    System.out.println("Please provide the number of skiers to generate lift rides for (max 100000)");
    numSkiers = scanner.nextInt();
    System.out.println("Please provide the number of ski lifts (5 - 60)");
    numLifts = scanner.nextInt();
    System.out.println("Please provide the average run number for each skier (max 20)");
    numRuns = scanner.nextInt();
    System.out.println("Please provide the address of the server in string");
    address = scanner.next();

    SkiClient skiClient = new SkiClient(maxThreadNumber, numSkiers, numLifts, numRuns, "http://ec2-34-210-25-131.us-west-2.compute.amazonaws.com:8080/Assignment01_war%20exploded/ski");
    skiClient.automate();

  }

  public static void main(String[] args) throws InterruptedException, IOException {
    UserInterface userInterface = new UserInterface();
    userInterface.connect();
  }
}

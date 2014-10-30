// Deadlands.java

public class Deadlands {

  private static double ln2 = Math.log(2);

  public static long factorial(long N) {
    long ans = 1;
    for (int i=2; i<=N; i++) ans *= i;
    return ans;
  }

  public static long pow(long p, long q) {
    long ans = 1;
    for (int i=0; i<q; i++) ans *= p;
    return ans;
  }

  private static void C_aces(int N, int X, int r, int maxaces, long[] aces) {
    int base = r * X;
    System.out.println(base + ": 0 / 1 (0%)");
    long denom = pow(X, (r + 1) * N);
    for (int k=1; k<X; k++) {
      long sum = 0;
      for (int j=1; j<=N; j++) {
        sum += aces[j] * (pow(k, j) - pow(k - 1, j)) * pow(X, N - j);
      }
      System.out.println((base + k) + ": " + sum + " / " + denom +
        " (" + (100 * ((double) sum / denom)) + "%)");
    }
    for (int a=1; a<=N; a++) {
      long sum = 0;
      for (int j=a; j<=N; j++) {
        long comb = factorial(j) / (factorial(j - a) * factorial(a));
        sum += aces[j] * comb * pow(X - 1, j - a) * pow(X, N - j);
      }
      aces[a] = sum;
    }
    if (r < maxaces) C_aces(N, X, r + 1, maxaces, aces);
  }

  // runs K from 1 to X * maxaces + X - 1
  public static void C(int N, int X, int maxaces) {
    long denom = pow(X, N);

    // compute value 1 (botch)
    long sum = 0;
    for (int i=N/2+1; i<=N; i++) {
      long comb = factorial(N) / (factorial(N - i) * factorial(i));
      sum += comb * pow(X - 1, N - i);
    }
    System.out.println("1: " + sum + " / " + denom +
      " (" + (100 * ((double) sum / denom)) + "%)");

    // compute values from 2 to X
    long kludge = 0;
    for (int k=2; k<X; k++) {
      sum = 0;
      for (int i=N/2+1; i<=N; i++) {
        long comb = factorial(N) / (factorial(N - i) * factorial(i));
        sum += comb * pow(k - 1, N - i);
      }
      sum = pow(k, N) - sum - kludge;
      kludge += sum;
      System.out.println(k + ": " + sum + " / " + denom +
        " (" + (100 * ((double) sum / denom)) + "%)");
    }

    // compute values over X
    long[] aces = new long[N + 1];
    for (int a=1; a<=N; a++) {
      sum = 0;
      for (int i=N/2+1; i<=N-a; i++) {
        long comb = factorial(N) /
          (factorial(N - a - i) * factorial(i) * factorial(a));
        sum += comb * pow(X - 2, N - a - i);
      }
      long comb = factorial(N) / (factorial(N - a) * factorial(a));
      aces[a] = comb * pow(X - 1, N - a) - sum;
    }
    C_aces(N, X, 1, maxaces, aces);
  }

  // compute probability table for rolling NdX in Deadlands
  public static void main(String[] args) {
    // extract command-line arguments
    if (args.length < 2) {
      System.out.println("Usage: java Deadlands N X [maxaces]");
      return;
    }
    int N = Integer.parseInt(args[0]);
    int X = Integer.parseInt(args[1]);
    int maxaces = (int) (64 / (N * Math.log(X) / ln2)) - 1;
    if (args.length > 2) maxaces = Integer.parseInt(args[2]);
    System.out.println("Probability table for " +
      N + "d" + X + " [" + maxaces + "]...");

    C(N, X, maxaces);
  }

}

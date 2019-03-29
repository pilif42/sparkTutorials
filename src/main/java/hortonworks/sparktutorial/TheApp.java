package hortonworks.sparktutorial;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

import static java.lang.String.format;

/**
 * To run inside IntelliJ, add the VM option:
 * -Dprofile=local to run locally
 * or -Dprofile=sandbox to run in the Hortonworks sandbox
 */
public class TheApp {
    private static final String PROFILE = "profile";
    private static final String LOCAL = "local";
    private static final String SANDBOX = "sandbox";
    private static final String UNSUPPORTED_PROFILE_MSG = "Profile %s is not supported.";

    public static void main(String[] args) {
        String profile = System.getProperty(PROFILE);
        if (StringUtils.isBlank(profile)) {
            throw new UnsupportedOperationException("profile has to be defined.");
        }

        // Create a SparkContext to initialize
        SparkConf conf = new SparkConf().setMaster("local").setAppName("Word Count");   // local means using this computer, rather than in distributed mode. To run Spark against multiple machines, we would need to change this value to YARN.

        // Create a Java version of the Spark Context
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Load the text into a Spark RDD, which is a distributed representation of each line of text
        JavaRDD<String> textFile = null;
        if (profile.equals(LOCAL)) {
            textFile = sc.textFile("src/main/resources/shakespeare.txt");
        } else if (profile.equals(SANDBOX)) {
            textFile = sc.textFile("hdfs:///tmp/shakespeare.txt");
        } else {
            throw new UnsupportedOperationException(format(UNSUPPORTED_PROFILE_MSG, profile));
        }

        JavaPairRDD<String, Integer> counts = textFile
                .flatMap(s -> Arrays.asList(s.split("[ ,]")).iterator())
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((a, b) -> a + b);
        counts.foreach(p -> System.out.println(p));
        System.out.println("Total words: " + counts.count());
        if (profile.equals(LOCAL)) {
            counts.saveAsTextFile("/tmp/shakespeareWordCount");
        } else if (profile.equals(SANDBOX)) {
            counts.saveAsTextFile("hdfs:///tmp/shakespeareWordCount");
        } else {
            throw new UnsupportedOperationException(format(UNSUPPORTED_PROFILE_MSG, profile));
        }
    }
}

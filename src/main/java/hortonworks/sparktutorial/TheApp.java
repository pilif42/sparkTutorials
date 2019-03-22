package hortonworks.sparktutorial;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class TheApp {
    public static void main(String[] args) {
        // Create a SparkContext to initialize
        SparkConf conf = new SparkConf().setMaster("local").setAppName("Word Count");   // local means using this computer, rather than in distributed mode. To run Spark against multiple machines, we would need to change this value to YARN.

        // Create a Java version of the Spark Context
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Load the text into a Spark RDD, which is a distributed representation of each line of text
        // To run locally: JavaRDD<String> textFile = sc.textFile("src/main/resources/shakespeare.txt");
        // To run in the Hortonworks sandbox:
        JavaRDD<String> textFile = sc.textFile("hdfs:///tmp/shakespeare.txt");
        JavaPairRDD<String, Integer> counts = textFile
                .flatMap(s -> Arrays.asList(s.split("[ ,]")).iterator())
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((a, b) -> a + b);
        counts.foreach(p -> System.out.println(p));
        System.out.println("Total words: " + counts.count());
        // To run locally: counts.saveAsTextFile("/tmp/shakespeareWordCount");
        // To run in the Hortonworks sandbox:
        counts.saveAsTextFile("hdfs:///tmp/shakespeareWordCount");
    }
}

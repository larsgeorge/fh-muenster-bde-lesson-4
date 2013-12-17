package org.fhmuenster.bde.mr;

import com.cloudera.cdk.morphline.MorphlineDemo;
import org.apache.hadoop.util.ProgramDriver;
import org.fhmuenster.bde.http.HttpServerMain;
import org.fhmuenster.bde.mr.tfidf.TFIDFJobController;

public class Driver {

  public static void main(String argv[]) {
    int exitCode = -1;
    ProgramDriver pgd = new ProgramDriver();
    try {
      pgd.addClass("searchserver", HttpServerMain.class, "Start the search server.");
      pgd.addClass("tfidf", TFIDFJobController.class,
        "MapReduce program to compute TF-IDF of input text files.");
      pgd.addClass("testmorphline", MorphlineDemo.class, "Run a morphline locally.");
      pgd.driver(argv);
      // Success
      exitCode = 0;
    } catch (Throwable e) {
      e.printStackTrace();
    }
    System.exit(exitCode);
  }
}

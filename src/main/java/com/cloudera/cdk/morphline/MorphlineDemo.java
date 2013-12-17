/*
 * Copyright 2013 Cloudera Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* The original CDK is now the Kite Software Development Kit.
 * This project is a trivial backport of the org.kitesdk.morphline.api.MorphlineDemo class
 * from the kite-sdk for use with the older CDK that ships with CDH4.5 and Cloudera Search 1.1.0
 * The original source is here:
 * https://github.com/kite-sdk/kite/blob/master/kite-morphlines/kite-morphlines-core/src/test/java/org/kitesdk/morphline/api/MorphlineDemo.java
 */
package com.cloudera.cdk.morphline;

import java.io.*;

import com.cloudera.cdk.morphline.api.*;
import com.cloudera.cdk.morphline.base.*;
import com.cloudera.cdk.morphline.base.Compiler;

/**
 * Sample program that illustrates how to use the API to embed and execute a morphline
 * in a host system.
 */
public class MorphlineDemo {

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Usage: testmorphline <morphline.conf> <dataFile1> ... <dataFileN>");
      System.exit(1);
    }
    // compile morphline.conf file on the fly
    File morphlineFile = new File(args[0]);
    String morphlineId = null;
    MorphlineContext morphlineContext = new MorphlineContext.Builder().build();
    Command morphline = new Compiler().compile(morphlineFile, morphlineId, morphlineContext, null);

    // process each input data file
    Notifications.notifyBeginTransaction(morphline);
    try {
      for (int i = 1; i < args.length; i++) {
        InputStream in = new BufferedInputStream(new FileInputStream(new File(args[i])));
        Record record = new Record();
        record.put(Fields.ATTACHMENT_BODY, in);
        Notifications.notifyStartSession(morphline);
        boolean success = morphline.process(record);
        if (!success) {
          System.out.println("Morphline failed to process record: " + record);
        }
        in.close();
      }
      Notifications.notifyCommitTransaction(morphline);
    } catch (RuntimeException e) {
      Notifications.notifyRollbackTransaction(morphline);
      morphlineContext.getExceptionHandler().handleException(e, null);
    }
    Notifications.notifyShutdown(morphline);
  }
}

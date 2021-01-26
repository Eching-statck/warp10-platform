//
//   Copyright 2020-2021  SenX S.A.S.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package io.warp10.standalone.datalog;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import io.warp10.continuum.store.DirectoryClient;
import io.warp10.continuum.store.StoreClient;
import io.warp10.continuum.store.thrift.data.DatalogRecord;

public class DatalogWorkers {

  static class DatalogJob {
    final DatalogConsumer consumer;
    final String ref;
    final DatalogRecord record;

    public DatalogJob(DatalogConsumer consumer, String ref, DatalogRecord record) {
      this.consumer = consumer;
      this.ref = ref;
      this.record = record;
    }
  }

  private static final LinkedBlockingQueue<DatalogJob> queues[];
  private static final DatalogWorker[] workers;

  private static StoreClient store = null;
  private static DirectoryClient directory = null;

  private static final int NUM_WORKERS = 8;

  static {
    queues = new LinkedBlockingQueue[NUM_WORKERS];
    workers = new DatalogWorker[NUM_WORKERS];

    for (int i = 0; i < NUM_WORKERS; i++) {
      queues[i] = new LinkedBlockingQueue<DatalogJob>();
      workers[i] = new DatalogWorker(queues[i]);
    }
  }

  public static void init(StoreClient store, DirectoryClient directory) {

    if (null != DatalogWorkers.store || null != DatalogWorkers.directory) {
      throw new RuntimeException("Store/Directory already set.");
    }

    DatalogWorkers.store = store;
    DatalogWorkers.directory = directory;
  }

  public static void offer(DatalogConsumer consumer, String ref, DatalogRecord record) throws IOException {
    //
    // Compute partitioning key
    //

    long classid = record.getMetadata().getClassId();
    long labelsid = record.getMetadata().getLabelsId();

    int partkey = (int) (((classid << 16) & 0xFFFF0000L) | ((labelsid >>> 48) & 0xFFFFL));

    DatalogJob job = new DatalogJob(consumer, ref, record);

    if (!queues[partkey % NUM_WORKERS].offer(job)) {
      throw new IOException("Unable to offer record '" + ref + "'.");
    }
  }

  public static StoreClient getStoreClient() {
    return DatalogWorkers.store;
  }

  public static DirectoryClient getDirectoryClient() {
    return DatalogWorkers.directory;
  }
}

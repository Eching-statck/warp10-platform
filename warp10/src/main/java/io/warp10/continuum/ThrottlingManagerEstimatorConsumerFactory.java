//
//   Copyright 2019  SenX S.A.S.
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

package io.warp10.continuum;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TCompactProtocol;

import io.warp10.continuum.KafkaSynchronizedConsumerPool.ConsumerFactory;
import io.warp10.continuum.sensision.SensisionConstants;
import io.warp10.crypto.CryptoUtils;
import io.warp10.script.HyperLogLogPlus;
import io.warp10.sensision.Sensision;

public class ThrottlingManagerEstimatorConsumerFactory implements ConsumerFactory {
  
  private final byte[] macKey;
  
  public ThrottlingManagerEstimatorConsumerFactory(byte[] macKey) {
    this.macKey = macKey;
  }
  
  @Override
  public Runnable getConsumer(final KafkaSynchronizedConsumerPool pool, final KafkaConsumer<byte[], byte[]> consumer) {
    
    return new Runnable() {          
      @Override
      public void run() {
        // Iterate on the messages
        TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());

        KafkaOffsetCounters counters = pool.getCounters();
        
        try {
          // Kafka 2.x Duration delay = Duration.of(500L, ChronoUnit.MILLIS);
          long delay = 500L;
          
          while (!pool.getAbort().get()) {
            ConsumerRecords<byte[], byte[]> records = pool.poll(consumer,delay);
            
            for (ConsumerRecord<byte[], byte[]> record: records) {
              counters.count(record.partition(), record.offset());
              
              byte[] data = record.value();

              Sensision.update(SensisionConstants.CLASS_WARP_INGRESS_KAFKA_THROTTLING_IN_MESSAGES, Sensision.EMPTY_LABELS, 1);
              Sensision.update(SensisionConstants.CLASS_WARP_INGRESS_KAFKA_THROTTLING_IN_BYTES, Sensision.EMPTY_LABELS, data.length);
              
              if (null != macKey) {
                data = CryptoUtils.removeMAC(macKey, data);
              }
              
              // Skip data whose MAC was not verified successfully
              if (null == data) {
                Sensision.update(SensisionConstants.CLASS_WARP_INGRESS_KAFKA_THROTTLING_IN_INVALIDMACS, Sensision.EMPTY_LABELS, 1);
                continue;
              }

              //
              // Update throttling manager
              //
              
              try {
                ThrottlingManager.fuse(HyperLogLogPlus.fromBytes(data));
                Sensision.update(SensisionConstants.CLASS_WARP_INGRESS_THROTLLING_FUSIONS, Sensision.EMPTY_LABELS, 1);
              } catch (Exception e) {
                Sensision.update(SensisionConstants.CLASS_WARP_INGRESS_THROTLLING_FUSIONS_FAILED, Sensision.EMPTY_LABELS, 1);
              }
            }
          }        
        } catch (Throwable t) {
          t.printStackTrace(System.err);
        } finally {
          // Set abort to true in case we exit the 'run' method
          pool.getAbort().set(true);
        }
      }
    };
  }
}

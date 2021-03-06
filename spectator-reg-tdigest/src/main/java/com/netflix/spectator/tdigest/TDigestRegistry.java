/**
 * Copyright 2015 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.spectator.tdigest;

import com.netflix.spectator.api.*;
import com.netflix.spectator.api.Counter;
import com.netflix.spectator.api.histogram.PercentileDistributionSummary;
import com.netflix.spectator.api.histogram.PercentileTimer;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;


/** Registry that maps spectator types to servo. */
public class TDigestRegistry extends AbstractRegistry {

  private final Registry underlying;
  private final TDigestConfig config;

  /** Create a new instance. */
  @Inject
  public TDigestRegistry(Registry registry, TDigestConfig config) {
    super(registry.clock());
    this.underlying = registry;
    this.config = config;
  }

  @Override protected Counter newCounter(Id id) {
    return underlying.counter(id);
  }

  @Override protected TDigestDistributionSummary newDistributionSummary(Id id) {
    DistributionSummary summary = PercentileDistributionSummary.get(underlying, id);
    return new TDigestDistributionSummary(newDigest(id), summary);
  }

  @Override protected TDigestTimer newTimer(Id id) {
    Timer timer = PercentileTimer.get(underlying, id);
    return new TDigestTimer(newDigest(id), timer);
  }

  private StepDigest newDigest(Id id) {
    final long step = config.getPollingFrequency(TimeUnit.MILLISECONDS);
    return new StepDigest(underlying, id, config.getCompressionFactor(), step);
  }

  @Override public void register(Meter meter) {
    underlying.register(meter);
  }
}

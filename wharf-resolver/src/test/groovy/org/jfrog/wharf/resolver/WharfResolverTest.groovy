/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jfrog.wharf.resolver

import spock.lang.Specification

/**
 * @author Hans Dockter
 */
class WharfResolverTest extends Specification {
  WharfResolver wharfResolver = new WharfResolver()

  def testInit() {
    expect:
    wharfResolver.getSnapshotTimeout().is(WharfResolver.DAILY)
  }

  def timeoutStrategyNever_shouldReturnAlwaysFalse() {
    expect:
    !WharfResolver.NEVER.isCacheTimedOut(0)
    !WharfResolver.NEVER.isCacheTimedOut(System.currentTimeMillis())
  }

  def timeoutStrategyAlways_shouldReturnAlwaysTrue() {
    expect:
    WharfResolver.ALWAYS.isCacheTimedOut(0)
    WharfResolver.ALWAYS.isCacheTimedOut(System.currentTimeMillis())
  }

  def timeoutStrategyDaily() {
    expect:
    !WharfResolver.DAILY.isCacheTimedOut(System.currentTimeMillis())
    WharfResolver.ALWAYS.isCacheTimedOut(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
  }

  def timeoutInterval() {
    def interval = new WharfResolver.Interval(1000)

    expect:
    interval.isCacheTimedOut(System.currentTimeMillis() - 5000)
    !interval.isCacheTimedOut(System.currentTimeMillis())
  }

  def setTimeoutByMilliseconds() {
    when:
    wharfResolver.setSnapshotTimeout(1000)

    then:
    ((WharfResolver.Interval) wharfResolver.getSnapshotTimeout()).interval == 1000
  }

  def setTimeoutByStrategy() {
    when:
    wharfResolver.setSnapshotTimeout(WharfResolver.NEVER)

    then:
    wharfResolver.getSnapshotTimeout().is(WharfResolver.NEVER)
  }
}

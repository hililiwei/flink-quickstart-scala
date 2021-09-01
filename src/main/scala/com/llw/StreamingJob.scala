/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.llw

import org.apache.flink.api.common.state.ValueState
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.evictors.CountEvictor
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import java.sql.Date
import java.text.SimpleDateFormat

/**
 * Skeleton for a Flink Streaming Job.
 *
 * For a tutorial how to write a Flink streaming application, check the
 * tutorials and examples on the <a href="https://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
object StreamingJob {
  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val elements = env.addSource(new ParallelSourceFunction[TestData] {
      override def run(ctx: SourceFunction.SourceContext[TestData]): Unit = {
        while (true) {
          ctx.collect(TestData.apply("name1", 1, "car", 100, System.currentTimeMillis()))
          Thread.sleep(1000L)
        }
      }

      override def cancel(): Unit = {}
    })

    elements.keyBy(new KeySelector[TestData, String] {
      override def getKey(value: TestData): String = {
        value.name
      }
    })
      .window(TumblingProcessingTimeWindows.of(Time.minutes(1)))
      .trigger(ContinuousProcessingTimeTrigger.of[TimeWindow](Time.seconds(10)))
      .evictor(CountEvictor.of[TimeWindow](0, true))
      .process(new ProcessWindowFunction[TestData, TestData, String, TimeWindow] {


        val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        var valueState: ValueState[Double] = _

        override def open(parameters: Configuration): Unit = {

          val desc = new ValueStateDescriptor[Double]("value_state",createTypeInformation[Double])
          valueState = getRuntimeContext.getState(desc)
        }

        override def clear(context: Context): Unit = {
          println(s"Start Clear:${simpleDateFormat.format(new Date(context.currentProcessingTime))},count:${valueState.value()}" )
          super.clear(context)
        }

        override def process(key: String,
                             context: Context,
                             elements: Iterable[TestData],
                             out: Collector[TestData]): Unit = {
          var count = valueState.value()
          elements.foreach(one => {
            count = count + one.price
          })
          valueState.update(count)
          println(s"trigger:${simpleDateFormat.format(new Date(context.currentProcessingTime))},count:${valueState.value()}" )
        }
      })
    env.execute("Flink Streaming Scala API Skeleton")
  }
}

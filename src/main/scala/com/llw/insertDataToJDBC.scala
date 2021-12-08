package com.llw

import org.apache.flink.table.api.{EnvironmentSettings, TableEnvironment}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object insertDataToJDBC {
  
  def main(args: Array[String]) {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)


    // create a DataStream
    val dataStream = env.fromElements("Alice", "Bob", "John")

    val inputTable = tableEnv.fromDataStream(dataStream)

    tableEnv.createTemporaryView("InputTable", inputTable)
    

    // create an input Table
    tableEnv.executeSql(
      """
        |CREATE TABLE MyUserTable (
        |  name STRING
        |) WITH (
        |   'connector' = 'jdbc',
        |   'url' = 'jdbc:mysql://localhost:3306/test',
        |   'table-name' = 'userinfo',
        |   'username' = 'root',
        |   'password' = 'root'
        |)
        |""".stripMargin)
    // register an output Table
    tableEnv.executeSql(
      """
        |
        |INSERT INTO MyUserTable
        |
        |SELECT UPPER(f0) FROM InputTable
        |""".stripMargin)

    env.execute("test")

  }
}

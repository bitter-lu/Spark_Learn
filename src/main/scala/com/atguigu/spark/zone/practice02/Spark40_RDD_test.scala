package com.atguigu.spark.zone.practice02

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark40_RDD_test {
    def main(args: Array[String]): Unit = {

        val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("rdd")

        val sc = new SparkContext(conf)

        //TODO Scala - 转换算子 - aggregateByKey


        val rdd: RDD[(String, Int)] = sc.makeRDD(List(
            ("a", 1), ("a", 2), ("c", 3),
            ("b", 4), ("c", 5), ("c", 6)
        ), 2)

        //TODO 分区内计算和分区间计算规则相同怎么办

        /*val result: RDD[(String, Int)] = rdd.aggregateByKey(0)(
            (x, y) => x + y,
            (x, y) => x + y
        )*/

        val resultRDD: RDD[(String, Int)] = rdd.aggregateByKey(0)(_+_,_+_)

        resultRDD.collect().foreach(println)

        sc.stop()

    }
}

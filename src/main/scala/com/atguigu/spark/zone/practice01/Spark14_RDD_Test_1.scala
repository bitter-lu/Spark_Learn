package com.atguigu.spark.zone.practice01

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Spark14_RDD_Test_1 {
    def main(args: Array[String]): Unit = {

        val conf = new SparkConf().setMaster("local[*]").setAppName("rdd")
        val sc = new SparkContext(conf)

        // TODO : 从服务器日志数据apache.log中获取用户请求URL资源路径
        val logRDD: RDD[String] = sc.textFile("Spark/input/apache.log")

        val urlRDD: RDD[String] = logRDD.map(
            log => {
                val datas: Array[String] = log.split(" ")
                datas(6)
            }
        )
        urlRDD.collect().foreach(println)

        sc.stop()
    }

}

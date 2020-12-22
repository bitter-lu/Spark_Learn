package com.atguigu.spark.zone.project.service

import java.io

import com.atguigu.spark.zone.project.bean.HotCategory
import com.atguigu.spark.zone.project.common.TService
import com.atguigu.spark.zone.project.dao.HotCategoryAnalysisTop10Dao
import com.atguigu.spark.zone.project.helper.HotCategoryAccumulator
import com.atguigu.spark.zone.project.util.ProjectUtil
import org.apache.spark.rdd.RDD

import scala.collection.mutable

class HotCategoryAnalysisTop10Service extends TService{

    private val hotCategoryAnalysisTop10Dao = new HotCategoryAnalysisTop10Dao

    def analysis5() = {
        val dataRDD: RDD[String] = hotCategoryAnalysisTop10Dao.readFile("Spark/input/user_visit_action.txt")

        // TODO 创建累加器
        val acc = new HotCategoryAccumulator
        // TODO 注册累加器
        ProjectUtil.sparkContext().register(acc,"HotCategoryAccumulator")
        // TODO 使用累加器
        dataRDD.foreach(
            data => {
                val datas: Array[String] = data.split("_")
                if (datas(6) != "-1") {
                    acc.add((datas(6),"click"))
                } else if (datas(8) != "null") {
                    val ids: Array[String] = datas(8).split(",")
                    ids.foreach(
                        id => {
                            acc.add(id,"order")
                        }
                    )
                } else if (datas(10) != "null") {
                    val ids: Array[String] = datas(10).split(",")
                    ids.foreach(
                        id => {
                            acc.add(id,"pay")
                        }
                    )
                }
            }
        )
        // TODO 获取累加器的结果
        val accMap: mutable.Map[String, HotCategory] = acc.value
        val categories: mutable.Iterable[HotCategory] = accMap.map(_._2)
        // TODO 排序后取前10名
        categories.toList.sortWith(
            (left,right) => {
                if (left.clickCount > right.clickCount) {
                    true
                } else if (left.clickCount == right.clickCount) {
                    if (left.orderCount > right.orderCount) {
                        true
                    } else if (left.orderCount == right.orderCount) {
                        left.payCount > right.payCount
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        ).take(10)
    }

    def analysis4() = {
        val dataRDD: RDD[String] = hotCategoryAnalysisTop10Dao.readFile("Spark/input/user_visit_action.txt")

        val categoryRDD = dataRDD.flatMap(
            data => {
                val datas: mutable.ArrayOps[String] = data.split("_")
                if (datas(6) != "-1") {
                    List((datas(6), HotCategory(datas(6), 1, 0, 0)))
                } else if (datas(8) != "null") {
                    val ids: Array[String] = datas(8).split(",")
                    ids.map(
                        id => {
                            (id, HotCategory(id, 0, 1, 0))
                        }
                    )
                } else if (datas(10) != "null") {
                    val ids: Array[String] = datas(10).split(",")
                    ids.map(
                        id => {
                            (id, HotCategory(id, 0, 0, 1))
                        }
                    )
                } else {
                    Nil
                }
            }
        )
        val reduceRDD: RDD[HotCategory] = categoryRDD.reduceByKey(
            (c1, c2) => {
                c1.clickCount = c1.clickCount + c2.clickCount
                c1.orderCount = c1.orderCount + c2.orderCount
                c1.payCount = c1.payCount + c2.payCount
                c1
            }
        ).map(_._2)

        reduceRDD.sortBy(
            data => {
                (data.clickCount,data.orderCount,data.payCount)
            },false
        ).take(10)
    }

    def analysis3() = {
        val dataRDD: RDD[String] = hotCategoryAnalysisTop10Dao.readFile("Spark/input/user_visit_action.txt")

        val categoryRDD: RDD[HotCategory] = dataRDD.flatMap(
            data => {
                val datas: mutable.ArrayOps[String] = data.split("_")
                if (datas(6) != "-1") {
                    List(HotCategory(datas(6), 1, 0, 0))
                } else if (datas(8) != "null") {
                    val ids: Array[String] = datas(8).split(",")
                    ids.map(
                        id => {
                            HotCategory(id, 0, 1, 0)
                        }
                    )
                } else if (datas(10) != "null") {
                    val ids: Array[String] = datas(10).split(",")
                    ids.map(
                        id => {
                            HotCategory(id, 0, 0, 1)
                        }
                    )
                } else {
                    Nil
                }
            }
        )
        val groupRDD: RDD[(String, Iterable[HotCategory])] = categoryRDD.groupBy(_.id)

        val categoryMapRDD: RDD[HotCategory] = groupRDD.mapValues(
            iter => {
                iter.reduce(
                    (c1, c2) => {
                        c1.clickCount = c1.clickCount + c2.clickCount
                        c1.orderCount = c1.orderCount + c2.orderCount
                        c1.payCount = c1.payCount + c2.payCount
                        c1
                    }
                )
            }
        ).map(_._2)

        categoryMapRDD.collect().sortWith(
            (left,right) => {
                if (left.clickCount > right.clickCount) {
                    true
                } else if (left.clickCount == right.clickCount) {
                    if (left.orderCount > right.orderCount) {
                        true
                    } else if (left.orderCount == right.orderCount) {
                        left.payCount > right.payCount
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        ).take(10)
    }


    def analysis2() = {
        val dataRDD: RDD[String] = hotCategoryAnalysisTop10Dao.readFile("Spark/input/user_visit_action.txt")

        val categoryRDD: RDD[(String, (Int, Int, Int))] = dataRDD.flatMap(
            data => {
                val datas: mutable.ArrayOps[String] = data.split("_")
                if (datas(6) != "-1") {
                    List((datas(6), (1, 0, 0)))
                } else if (datas(8) != "null") {
                    val ids: Array[String] = datas(8).split(",")
                    ids.map((_, (0, 1, 0)))
                } else if (datas(10) != "null") {
                    val ids: Array[String] = datas(10).split(",")
                    ids.map((_, (0, 0, 1)))
                } else {
                    Nil
                }
            }
        )
        val categorySumRDD: RDD[(String, (Int, Int, Int))] = categoryRDD.reduceByKey(
            (c1, c2) => {
                (c1._1 + c2._1, c1._2 + c2._2, c1._3 + c2._3)
            }
        )
        categorySumRDD.sortBy(_._2,false).take(10)
    }

    // TODO 数据分析1
    override def analysis()= {

        // TODO 获取数据
        val dataRDD: RDD[String] = hotCategoryAnalysisTop10Dao.readFile("Spark/input/user_visit_action.txt")

        val dataCacheRDD: RDD[String] = dataRDD.cache()

        // TODO 品类点击统计
        val categoryClickRDD: RDD[(String, Int)] = dataCacheRDD.map(
            data => {
                val datas: Array[String] = data.split("_")
                (datas(6), 1)
            }
        )
        val categoryClickFilterRDD: RDD[(String, Int)] = categoryClickRDD.filter(_._1 != "null")

        val categoryClickReduceRDD: RDD[(String, Int)] = categoryClickFilterRDD.reduceByKey(_+_)

        // TODO 品类下单统计
        val categoryOrderRDD: RDD[String] = dataCacheRDD.map(
            data => {
                val datas = data.split("_")
                datas(8)
            }
        )
        val categoryFilterRDD = categoryOrderRDD.filter(_ != "null")

        val categoryOrdersRDD: RDD[(String, Int)] = categoryFilterRDD.flatMap(
            data => {
                val ids: mutable.ArrayOps[String] = data.split(",")
                ids.map((_, 1))
            }
        )
        val categoryOrderReduceRDD: RDD[(String, Int)] = categoryOrdersRDD.reduceByKey(_+_)

        // TODO 品类支付统计
        val categoryPayRDD: RDD[String] = dataCacheRDD.map(
            data => {
                val datas = data.split("_")
                datas(10)
            }
        )
        val categoryPayFilterRDD = categoryPayRDD.filter(_ != "null")

        val categoryPaysRDD: RDD[(String, Int)] = categoryPayFilterRDD.flatMap(
            data => {
                val ids: mutable.ArrayOps[String] = data.split(",")
                ids.map((_, 1))
            }
        )
        val categoryPayReduceRDD: RDD[(String, Int)] = categoryPaysRDD.reduceByKey(_+_)

        // TODO 将数据进行格式的转换
        val clickRDD: RDD[(String, (Int, Int, Int))] = categoryClickReduceRDD.map {
            case (category, clickcount) => {
                (category, (clickcount, 0, 0))
            }
        }

        val orderRDD: RDD[(String, (Int, Int, Int))] = categoryOrderReduceRDD.map {
            case (category, ordercount) => {
                (category, (0, ordercount, 0))
            }
        }

        val payRDD: RDD[(String, (Int, Int, Int))] = categoryPayReduceRDD.map {
            case (category, paycount) => {
                (category, (0, 0, paycount))
            }
        }

        val categoryRDD: RDD[(String, (Int, Int, Int))] = clickRDD.union(orderRDD).union(payRDD)
        val categorySumRDD: RDD[(String, (Int, Int, Int))] = categoryRDD.reduceByKey(
            (c1, c2) => {
                (c1._1 + c2._1, c1._2 + c2._2, c1._3 + c2._3)
            }
        )
        val tuples: RDD[(String, (Int, Int, Int))] = categorySumRDD.sortBy(_._2,false)

        tuples
    }
}

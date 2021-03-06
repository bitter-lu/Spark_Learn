package com.atguigu.spark.project.controller

import com.atguigu.spark.project.common.TController
import com.atguigu.spark.project.service.WordCountService
import org.apache.spark.rdd.RDD

class WordCountController extends TController {
    private val wordCountService : WordCountService = new WordCountService

    /**
      * 执行控制器
      */
    def execute(): Unit = {
        // 分析数据
        val result = wordCountService.analysis()

        // 打印结果
        result.collect().foreach(println)
    }
}

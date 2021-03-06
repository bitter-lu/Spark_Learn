package com.atguigu.spark.project.service



import com.atguigu.spark.project.common.TService
import com.atguigu.spark.project.dao.WordCountDao
import org.apache.spark.rdd.RDD

class WordCountService extends TService{
    private val wordCountDao : WordCountDao = new WordCountDao

    /**
      * 数据分析
      */
    override def analysis() = {
        // 操作数据
        val dataRDD = wordCountDao.readFile("Spark/input/1.txt")

        val wordRDD: RDD[String] = dataRDD.flatMap(
            line => {
                line.split(" ")
            }
        )
        val word2OneRDD: RDD[(String, Int)] = wordRDD.map( (_,1) )
        val word2CountRDD: RDD[(String, Int)] = word2OneRDD.reduceByKey(_+_)
        word2CountRDD
    }
}

import java.io.File
import java.nio.charset.Charset

import com.google.common.io.Files
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

object RecoverdNetWordcount {
  def createContext(ip:String,port:Int,outputPath:String,checkpointDirectory:String)
  :StreamingContext={
    println("Creating new context")
    val outputFile=new File(outputPath)
    if(outputFile.exists()) outputFile.delete()
    val sparkConf=new SparkConf().setAppName("wordcounttest").setMaster("local[*]")
    val ssc=new StreamingContext(sparkConf,Seconds(1))
    ssc.checkpoint(checkpointDirectory)

    val lines=ssc.socketTextStream(ip,port)
    val words=lines.flatMap(_.split(" "))
    val wordCounts=words.map((_,1)).reduceByKey(_ + _)
    wordCounts.foreachRDD{
      (rdd:RDD[(String,Int)],time:Time)=>
        val blacklist=WordBlackList.getInstance(rdd.sparkContext)
        val dropWordcounter=DropWordcounter.getInstance(rdd.sparkContext)
        val counts=rdd.filter{ case (word,count)=>
          if(blacklist.value.contains(word)){
            dropWordcounter.add(count)
            false
          }else{
            true
          }
        }.collect().mkString("")
        val output=s"Counts at time $time $counts"
        println(output)
        println(s"Dropped ${dropWordcounter.value} word(s) totally")
        println(s"Appending to ${outputFile.getAbsolutePath}")
        Files.append(output+"\n",outputFile,Charset.defaultCharset())

    }
    ssc
  }

  def main(args: Array[String]): Unit = {
    if(args.length!=3){
      System.err.println(s"Your arguments were")
      System.err.println(""" """.stripMargin)
      System.exit(1)
    }
    val Array(ip,checkpointDirectory,outputpath)=args
    val port:Int=9999
    val ssc=StreamingContext.getOrCreate(checkpointDirectory,
      ()=>createContext(ip,port,outputpath,checkpointDirectory))
    ssc.start()
    ssc.awaitTermination()
  }
}

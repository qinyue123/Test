import org.apache.log4j.{Level, Logger}
import org.apache.spark.{HashPartitioner, SparkConf}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StateWordcount {
  Logger.getLogger("count").setLevel(Level.ERROR)
  def main(args: Array[String]): Unit = {
    if(args.length<2){
      System.err.println("hostname and port must have")
      System.exit(1)
    }
    val updateFunc=(value:Seq[Int],state:Option[Int])=>{
      val currentCount=value.sum
      val previouscount=state.getOrElse(0)
      Some(currentCount+previouscount)
    }
    val newupdateFunc=(iterator:Iterator[(String,Seq[Int],Option[Int])])=>{
      iterator.flatMap(t=>updateFunc(t._2,t._3).map(x=>(t._1,x)))
    }
    val sparkconf=new SparkConf().setAppName("wordcount").setMaster("local[*]")
    val ssc=new StreamingContext(sparkconf,Seconds(2))
    ssc.checkpoint(".")//检查点在当前位置

    val initRdd=ssc.sparkContext.parallelize(List(("hello",1),("world",1)))
    val lines=ssc.socketTextStream(args(0),args(1).toInt,StorageLevel.MEMORY_AND_DISK_SER_2)
    val words=lines.flatMap(_.split(" "))
    val wordstream=words.map((_,1))
    val wordcount=wordstream.updateStateByKey[Int](newupdateFunc,
      new HashPartitioner(ssc.sparkContext.defaultParallelism),true,initRdd)
    val statecpunt=wordstream.updateStateByKey(newupdateFunc,
      new HashPartitioner(ssc.sparkContext.defaultParallelism),true,initRdd)
    wordcount.print()
    ssc.start()
    ssc.awaitTermination()
  }
}

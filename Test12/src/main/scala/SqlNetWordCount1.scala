import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

object SqlNetWordCount1 {
  Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
  def main(args: Array[String]): Unit = {
    if(args.length<2){
      System.err.println("hostname and port must have")
      System.exit(1)
    }
    val sparkConf=new SparkConf().setAppName("wordcount1").setMaster("local[*]")
    val ssc=new StreamingContext(sparkConf,Seconds(2))
    val lines=ssc.socketTextStream(args(0),args(1).toInt,StorageLevel.MEMORY_AND_DISK_SER)
    val words=lines.flatMap(_.split(" "))
    val wordcounts=words.map(x=>(x,1)).reduceByKeyAndWindow((a:Int,b:Int)=>(a+b),Seconds(10),Seconds(20))
    wordcounts.print()

    /*words.foreachRDD((rdd:RDD[String],time:Time)=>{
      val sparksession=SparkSessionSingleton1.getInstance(rdd.sparkContext.getConf)
      import sparksession.implicits._
      val datawordStream=rdd.map(w=>Record(w)).toDF()
      val wordtable=datawordStream.createOrReplaceTempView("wordcount")
      val datacount=sparksession.sql("select word, count(*) as total from wordcount group by word")
      println(s"===========$time==========")
      datacount.show()
    })*/
    ssc.start()
    ssc.awaitTermination()
  }
}
case class Record(word:String)

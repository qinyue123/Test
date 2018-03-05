import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast

object WordBlackList {
    private var instance:Broadcast[Seq[String]]=null
  def getInstance(sc:SparkContext):Broadcast[Seq[String]]={
    if(instance==null){
      synchronized{
        if(instance==null){
          val wordBlackList=Seq("a","b","c")
          instance=sc.broadcast(wordBlackList)
        }
      }
    }
    instance
  }
}

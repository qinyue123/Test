import org.apache.spark.SparkContext
import org.apache.spark.util.LongAccumulator

object DropWordcounter {
  private var instance:LongAccumulator=null
  def getInstance(sc:SparkContext):LongAccumulator={
    if(instance==null) {
      synchronized {
        if (instance == null) {
          instance = sc.longAccumulator("WordsInstacnecounter")
        }
      }
    }
    instance
  }
}

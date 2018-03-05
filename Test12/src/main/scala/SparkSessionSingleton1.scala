import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkSessionSingleton1 {
  private var instance1:SparkSession= _
  def getInstance(sparkconf:SparkConf):SparkSession={
    if(instance1==null){
      instance1=SparkSession.builder().config(sparkconf).getOrCreate()
    }
    instance1
  }
}

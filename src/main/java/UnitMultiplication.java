import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnitMultiplication {
    /*
     * First Mapper Class
     * Input: relation.txt -> format: fromID\ttoID1,toID2,toID3...
     * Ouput: toId=value
     */
    public static class TransitionMapper extends Mapper<Object, Text, Text, Text> {

        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
          String[] fromTo = value.toString().trim().split("\t");
          // Check if the website is a deadend
          if(fromTo.length<2){
            return;
          }
          String tos[] = fromTo[1].split(",");
          String outputKey = fromTo[0];
          for(String toWebpage : tos){
            context.write(new Text(outputKey), new Text(toWebpage + "=" + (double)1/tos.length));
          }
        }
    }

    /*
     * Second Mapper
     * input format: id\t
     */
    public static class PRMapper extends Mapper<Object, Text, Text, Text> {
        @Override
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
          String[] pr0 = value.toString().trim().split("\t");
          context.write(new Text(pr0[0]), new Text(pr0[1]));
        }
    }

    /*
     * Only One Reducer (MapReduce only allows one reducer)
     * Receive results from both mapper1 and mapper2 (no sequence, indentified by "=")
     */
    public static class MultiplicationReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
                // inputkey = fromID
                // inputvalue = <toID1=1/3, toID2=1/3,...>
                // outputkey = toID (Since the result needs to be added on toID)
                // outputvalue = toIDn*pr0
                List<String> transitionCell = new ArrayList<String>();
                double prCell = 0;
                for(Text value in values){
                  if(value.toString().contains("=")){
                    transitionCell.add(value.toString());
                  }else{
                    prCell = Double.purseDouble(value.toString());
                  }
                }

                for(String cell: transitionCell){
                  String toID = cell.split("=")[0];
                  double prob = Double.purseDouble(cell.split("=")[1]);
                  double subPr = prob * prCell;
                  context.write(new Text(toID), new Text(subPr));
                }
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);
        job.setJarByClass(UnitMultiplication.class);

        job.setReducerClass(MultiplicationReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // Because there are two mapper classes, so MultipleInputs needs to be defined
        // 1st parameter: which job; 2nd parameter: path of input; 3rd: format of the input (relation.txt); 4th: which mapper class
        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, TransitionMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, PRMapper.class);

        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        job.waitForCompletion(true);
    }

}

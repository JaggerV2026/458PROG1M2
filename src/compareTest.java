import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class compareTest {

    public void compareFiles(){
        //.data
        try {
            File exampleDataInput = new File("TestFile/EvenOrOdd.data");
            Scanner exampleDataReader = new Scanner(exampleDataInput);

            File producedDataOutput = new File("data.txt");
            Scanner outputDataReader = new Scanner(producedDataOutput);

            while(exampleDataReader.hasNextLine() && outputDataReader.hasNextLine()){
                String exampleLine = exampleDataReader.nextLine();
                String outputLine = outputDataReader.nextLine();
                if(!exampleLine.equals(outputLine)){
                    System.out.println("Lines not equal. outputLine: " + outputLine);
                }
            }
            if(!exampleDataReader.hasNextLine() && outputDataReader.hasNextLine()){
                System.out.println("Output has too many lines");
            }
            else if(exampleDataReader.hasNextLine() && !outputDataReader.hasNextLine()){
                System.out.println("Output has too few lines");
            }
            exampleDataReader.close();
            outputDataReader.close();

            File exampleTextInput = new File("TestFile/EvenOrOdd.text");
            Scanner exampleTextReader = new Scanner(exampleTextInput);

            File producedTextOutput = new File("text.txt");
            Scanner outputTextReader = new Scanner(producedTextOutput);

            while(exampleTextReader.hasNextLine() && outputTextReader.hasNextLine()){
                String exampleLine = exampleTextReader.nextLine();
                String outputLine = outputTextReader.nextLine();
                if(!exampleLine.equals(outputLine)){
                    System.out.println("Lines not equal. outputLine: " + outputLine);
                }
            }
            if(!exampleTextReader.hasNextLine() && outputTextReader.hasNextLine()){
                System.out.println("Output has too many lines");
            }
            else if(exampleTextReader.hasNextLine() && !outputTextReader.hasNextLine()){
                System.out.println("Output has too few lines");
            }

            exampleTextReader.close();
            outputTextReader.close();
        }
        catch(FileNotFoundException e){
            System.out.println("File not found");
        }
    }
}

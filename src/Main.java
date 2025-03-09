import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

   public static void main(String[] args){
       int dataAddressIndex = 268500992; //0x10010000
       int textAddressIndex = 4194304; //0x00400000
       int currentAddressIndex = dataAddressIndex;

       InstructionNode dataHead = new InstructionNode();
       InstructionNode textHead = new InstructionNode();
       InstructionNode currentNode = dataHead;
       try {
           File asmInput = new File(args[0]);
           Scanner asmReader = new Scanner(asmInput);
           while(asmReader.hasNextLine()){
               String instruction = asmReader.nextLine();
               //Getting rid of tabs and extra spaces
               String spacesRemoved = instruction.replaceAll("\\s{2,}", " ");
                //Need to check for comments and empty lines.
               currentNode.setNext(new InstructionNode(spacesRemoved, currentAddressIndex));
               //Need to look into how the data addresses work more

           }
           asmReader.close();
       } catch (FileNotFoundException e){
           System.out.println("File not found");
       }
   }
}

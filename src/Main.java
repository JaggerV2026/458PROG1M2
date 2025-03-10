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

       boolean inDataSection = false;

       String regex = "[\\s]";

       try {
           File asmInput = new File("TestFile/EvenOrOdd.asm");
           //File asmInput = new File(args[0]);
           Scanner asmReader = new Scanner(asmInput);
           //Look for .data section
           while(asmReader.hasNextLine() && !inDataSection){
               String instruction = asmReader.nextLine();
               instruction = instruction.trim();
               inDataSection = (instruction.equals(".data"));
           }
           while(asmReader.hasNextLine()){
               String instruction = asmReader.nextLine();
               instruction = instruction.trim();
               if(!instruction.isEmpty() && instruction.charAt(0) != '#'){
                   if(instruction.equals(".text")){
                       break;
                   }
                   currentNode.setNext(new InstructionNode(instruction, currentAddressIndex));
                   currentNode = currentNode.next();
                   //Should calculate the memory needed
                   //Length of instruction works because the " can be replaced with null
                   int strStart = instruction.indexOf("\"") + 1;
                   int strEnd = instruction.length();
                   currentAddressIndex += (strEnd - strStart);
               }
           }
           currentNode = textHead;
           currentAddressIndex = textAddressIndex;
           while(asmReader.hasNextLine()){
               String instruction = asmReader.nextLine();
               instruction = instruction.trim();
               //Poor check for labels. Just check that the first value
               //of labelCheck doesn't contain a colon, so it probably is an instruction
               String[] labelCheck = instruction.split(regex);
               if(!instruction.isEmpty() && instruction.charAt(0) != '#' && labelCheck[0].contains(":")){
                   //Address of label is next line
                   currentNode.setNext(new InstructionNode(instruction, currentAddressIndex));
                   currentNode = currentNode.next();
               }
               //Somewhat repetitive, but we want to make sure we're skipping comments
               //and empty lines
               else if(!instruction.isEmpty() && instruction.charAt(0) != '#'){
                   currentNode.setNext(new InstructionNode(instruction, currentAddressIndex));
                   currentNode = currentNode.next();
                   currentAddressIndex += 4;
               }
           }
           asmReader.close();
       } catch (FileNotFoundException e){
           System.out.println("File not found");
       }
       //Writing .data file
       currentNode = dataHead.next();
       int strIndex = 0;
       DataStack datastack = new DataStack();
       while(currentNode != null){
           int strStart = currentNode.getInstruction().indexOf("\"") + 1;
           int strEnd = currentNode.getInstruction().length() - 1;
           String data = currentNode.getInstruction().substring(strStart, strEnd);
           for(int i = 0; i < data.length(); i++){
               datastack.push(data.charAt(i));
               strIndex++;
               //Write to file every 4 characters
               if(strIndex == 4){
                   strIndex = 0;

               }
           }
           currentNode = currentNode.next();
       }
       //Write to file if a line isn't full but data has been read
       if(strIndex != 0){

       }

       /*
       //testing
       currentNode = dataHead;
       while(currentNode.next() != null){
           currentNode = currentNode.next();
           System.out.println(currentNode.getInstruction() + ", " + String.format("%08x", currentNode.getAddress()));
       }
       currentNode = textHead;
       while(currentNode.next() != null){
           currentNode = currentNode.next();
           System.out.println(currentNode.getInstruction() + ", " + String.format("%08x", currentNode.getAddress()));
       }
       */
   }
}

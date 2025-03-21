import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Dictionary;
import java.util.Hashtable;

public class Main {

   public static void main(String[] args){
       int dataAddressIndex = 268500992; //0x10010000
       int textAddressIndex = 4194304; //0x00400000
       int currentAddressIndex = dataAddressIndex;

       InstructionNode dataHead = new InstructionNode();
       InstructionNode textHead = new InstructionNode();
       InstructionNode currentNode = dataHead;
       InstructionNode preCurrentNode = textHead;

       Dictionary<String, Integer> labelDict = new Hashtable<>();

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
       //Create .data file
       try{
            File dataFile = new File("data.txt");
            if(!dataFile.createNewFile()){
               System.out.println("data.txt already exists");
            }
       }
       catch (IOException e){
            System.out.println("Error when creating data.txt");
       }
       //Writing .data file
       currentNode = dataHead.next();
       int strIndex = 0;
       DataStack datastack = new DataStack();
       try {
           BufferedWriter dataWriter = new BufferedWriter(new FileWriter("data.txt"));
           while (currentNode != null) {
               //Saving label
               int labelStart = currentNode.getInstruction().indexOf(":");
               labelDict.put(currentNode.getInstruction().substring(0,labelStart),currentNode.getAddress());
               //Data file writing
               int strStart = currentNode.getInstruction().indexOf("\"") + 1;
               int strEnd = currentNode.getInstruction().length() - 1;
               String data = currentNode.getInstruction().substring(strStart, strEnd);
               for (int i = 0; i < data.length(); i++) {
                   datastack.push(data.charAt(i));
                   strIndex++;
                   //Write to file every 4 characters
                   if (strIndex == 4) {
                       strIndex = 0;
                       dataWriter.write(datastack.returnData());
                       dataWriter.newLine();

                   }
               }
               datastack.push(0);//For null character
               strIndex++;
               //Write to file every 4 characters. If null character fills stack
               if (strIndex == 4) {
                   strIndex = 0;
                   dataWriter.write(datastack.returnData());
                   dataWriter.newLine();

               }
               currentNode = currentNode.next();
           }
           //Write to file if a line isn't full but data has been read
           if (strIndex != 0) {
                dataWriter.write(datastack.returnData());
           }
           dataWriter.close();
       }
       catch (IOException e){
           System.out.println("Error while writing to data.txt");
       }

       //Converting pseudo instructions
       currentNode = textHead.next();
       //Amount to increase each address by
       int addressDisplacement = 0;
       String newInstruction = "";
       int newAddress = 0;
       while(currentNode != null){
           currentNode.incrementAddress(addressDisplacement);
           String commasRemoved = currentNode.getInstruction().replaceAll(",", "");
           String spacesRemoved = commasRemoved.replaceAll("\\s{2,}", " ");
           String[] argArray = spacesRemoved.split(regex);
           switch (argArray[0]){
               case "li":
                   //lui
                   String liImmediate = String.format("%04x", (Integer.parseInt(argArray[2]) >> 4));
                   newInstruction = "lui $at, " + liImmediate;
                   newAddress = currentNode.getAddress();
                   preCurrentNode.setNext(new InstructionNode(newInstruction, newAddress));
                   preCurrentNode = preCurrentNode.next();
                   //ori
                   liImmediate = String.format("%04x", (Integer.parseInt(argArray[2]) & 4095));
                   newInstruction = "ori ";
                   newInstruction += argArray[1];
                   newInstruction += ", ";
                   newInstruction += liImmediate;
                   newAddress = newAddress + 4;
                   preCurrentNode.setNext(new InstructionNode(newInstruction, newAddress));
                   preCurrentNode = preCurrentNode.next();
                   preCurrentNode.setNext(currentNode.next());
                   addressDisplacement += 4;
                   break;
               case "la":
                   //lui
                   newInstruction = "lui $at, 4097";
                   newAddress = currentNode.getAddress();
                   preCurrentNode.setNext(new InstructionNode(newInstruction, newAddress));
                   preCurrentNode = preCurrentNode.next();
                   //ori
                   newInstruction = "ori ";
                   newInstruction += argArray[1];
                   newInstruction += ", $at, ";
                   newInstruction += Integer.toString(labelDict.get(argArray[2]) - dataAddressIndex);
                   newAddress = newAddress + 4;
                   preCurrentNode.setNext(new InstructionNode(newInstruction, newAddress));
                   preCurrentNode = preCurrentNode.next();
                   preCurrentNode.setNext(currentNode.next());
                   addressDisplacement += 4;
                   break;
               case "blt":
                   //slt
                   newInstruction = "slt $at, ";
                   newInstruction += argArray[1];
                   newInstruction += ", ";
                   newInstruction += argArray[2];
                   newAddress = currentNode.getAddress();
                   preCurrentNode.setNext(new InstructionNode(newInstruction, newAddress));
                   preCurrentNode = preCurrentNode.next();
                   //bne
                   newInstruction = "bne $at, $zero, ";
                   newInstruction += argArray[3];
                   newAddress = newAddress + 4;
                   preCurrentNode.setNext(new InstructionNode(newInstruction, newAddress));
                   preCurrentNode = preCurrentNode.next();
                   preCurrentNode.setNext(currentNode.next());
                   addressDisplacement += 4;
                   break;
               case "move":
                   //add
                   newInstruction = "add ";
                   newInstruction += argArray[1];
                   newInstruction += ", ";
                   newInstruction += argArray[2];
                   newInstruction += ", $zero";
                   newAddress = currentNode.getAddress();
                   preCurrentNode.setNext(new InstructionNode(newInstruction, newAddress));
                   preCurrentNode = preCurrentNode.next();
                   preCurrentNode.setNext(currentNode.next());
                   break;
               default: //If no change is needed
                   preCurrentNode = currentNode;
           }
           currentNode = currentNode.next();
       }

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

   }
}

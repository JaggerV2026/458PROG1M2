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

       String fileRegex = "[\\\\/]";
       //Used for naming output files
       String fileName = "";
       //For saving files
       String filePath = "";
       String fileSeparator = "/";

       //Amount to increase each address by
       int addressDisplacement = 0;
       String newInstruction = "";
       int newAddress = 0;

       MIPSAssembler assembler = new MIPSAssembler();

       try {
           fileName = args[0];
           String[] fileArray = fileName.split(fileRegex);
           fileName = fileArray[fileArray.length-1];
           fileName = fileName.substring(0, fileName.indexOf("."));

           //Getting file path
           //Using this if statement since I know different systems handle this differently
           //This isn't a perfect solution, but it's fine for now
           if(args[0].contains("/")){
               filePath = args[0].substring(0, args[0].lastIndexOf("/"));
           }
           else {
               filePath = args[0].substring(0, args[0].lastIndexOf("\\"));
               fileSeparator = "\\";
           }

           File asmInput = new File(args[0]);
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
           return;
       }
       //Create .data file
       try{
            File dataFile = new File(filePath + fileSeparator + fileName + ".data");
            if(!dataFile.createNewFile()){
               System.out.println(".data already exists");
            }
       }
       catch (IOException e){
            System.out.println("Error when creating .data");
            return;
       }
       //Writing .data file
       currentNode = dataHead.next();
       int strIndex = 0;
       DataStack datastack = new DataStack();
       try {
           BufferedWriter dataWriter = new BufferedWriter(new FileWriter(filePath + fileSeparator + fileName + ".data"));
           while (currentNode != null) {
               //Saving label
               int labelColon = currentNode.getInstruction().indexOf(":");
               labelDict.put(currentNode.getInstruction().substring(0,labelColon),currentNode.getAddress());
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
           System.out.println("Error while writing to .data");
           return;
       }

       //Converting pseudo instructions
       currentNode = textHead.next();
       while(currentNode != null){
           currentNode.incrementAddress(addressDisplacement);
           String commasRemoved = currentNode.getInstruction().replaceAll(",", "");
           String spacesRemoved = commasRemoved.replaceAll("\\s{2,}", " ");
           String[] argArray = spacesRemoved.split(regex);
           switch (argArray[0]){
               case "li":
                   //Adding 0x to allow the assembler to read it
                   //lui
                   String liImmediate = String.format("%04x", (Integer.parseInt(argArray[2]) >> 4));
                   newInstruction = "lui $at, 0x" + liImmediate;
                   newAddress = currentNode.getAddress();
                   preCurrentNode.setNext(new InstructionNode(newInstruction, newAddress));
                   preCurrentNode = preCurrentNode.next();
                   //ori
                   liImmediate = String.format("%04x", (Integer.parseInt(argArray[2]) & 4095));
                   newInstruction = "ori ";
                   newInstruction += argArray[1];
                   newInstruction += ", $at, 0x";
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

       //collect labels
       currentNode = textHead.next();
       preCurrentNode = textHead;
       while(currentNode != null) {
           //Line contains label
           if (currentNode.getInstruction().contains(":")) {
               //Saving label
               int labelColon = currentNode.getInstruction().indexOf(":");
               labelDict.put(currentNode.getInstruction().substring(0, labelColon), currentNode.getAddress());
               String instructionAfterLabel = currentNode.getInstruction().substring(labelColon+1);
               //Remove node if line is just label
               if(instructionAfterLabel.isEmpty()){
                   preCurrentNode.setNext(currentNode.next());
               }
               //Remove label from instruction for writing to file
               else{
                   currentNode.setInstruction(instructionAfterLabel);
               }
           }
           preCurrentNode = currentNode;
           currentNode = currentNode.next();
       }

       //Apply labels
       currentNode = textHead.next();
       while(currentNode != null){
           String commasRemoved = currentNode.getInstruction().replaceAll(",", "");
           String spacesRemoved = commasRemoved.replaceAll("\\s{2,}", " ");
           String[] argArray = spacesRemoved.split(regex);
           //Both in format instr rs, rt, offset
           if((argArray[0].equals("beq") || argArray[0].equals("bne")) && labelDict.get(argArray[3]) != null){
               //Difference in label address and current address + 4. Divided by 4 for word addressing
               //Not handling illegal branches that are larger than 16 bits
               int branchAddress = (labelDict.get(argArray[3]) - (currentNode.getAddress() + 4)) / 4;
               newInstruction = argArray[0];
               newInstruction += " ";
               newInstruction += argArray[1];
               newInstruction += ", ";
               newInstruction += argArray[2];
               newInstruction += ", ";
               newInstruction += branchAddress;
               currentNode.setInstruction(newInstruction);

           }
           //j format is unique
           else if(argArray[0].equals("j") && labelDict.get(argArray[1]) != null){
               //Divide by 4 to get word addressing. Number is a bit string of length 26 and all 1
               int jumpAddress = (labelDict.get(argArray[1]) / 4) & 67108863;
               newInstruction = argArray[0];
               newInstruction += " ";
               newInstruction += jumpAddress;
               currentNode.setInstruction(newInstruction);
           }
           currentNode = currentNode.next();
       }


       //Create .text file
       try{
           File dataFile = new File(filePath + fileSeparator + fileName + ".text");
           if(!dataFile.createNewFile()){
               System.out.println(".text already exists");
           }
       }
       catch (IOException e){
           System.out.println("Error when creating .text");
           return;
       }
       //write to file
       currentNode = textHead.next();
       try{
           BufferedWriter textWriter = new BufferedWriter(new FileWriter(filePath + fileSeparator + fileName + ".text"));
           while(currentNode != null){
               textWriter.write(assembler.ConvertMIPS(currentNode.getInstruction()));
               textWriter.newLine();
               currentNode = currentNode.next();
           }
           textWriter.close();
       }
       catch(IOException e){
           System.out.println("Error while writing to .text");
           return;
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

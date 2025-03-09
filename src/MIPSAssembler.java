import java.util.Dictionary;
import java.util.Hashtable;

public class MIPSAssembler {

    private Dictionary<String, Integer> registerDict = new Hashtable<>();

    public MIPSAssembler(){
        registerDict.put("$zero", 0);
        registerDict.put("$at", 1);
        registerDict.put("$v0", 2);
        registerDict.put("$v1", 3);
        registerDict.put("$a0", 4);
        registerDict.put("$a1", 5);
        registerDict.put("$a2", 6);
        registerDict.put("$a3", 7);
        registerDict.put("$t0", 8);
        registerDict.put("$t1", 9);
        registerDict.put("$t2", 10);
        registerDict.put("$t3", 11);
        registerDict.put("$t4", 12);
        registerDict.put("$t5", 13);
        registerDict.put("$t6", 14);
        registerDict.put("$t7", 15);
        registerDict.put("$s0", 16);
        registerDict.put("$s1", 17);
        registerDict.put("$s2", 18);
        registerDict.put("$s3", 19);
        registerDict.put("$s4", 20);
        registerDict.put("$s5", 21);
        registerDict.put("$s6", 22);
        registerDict.put("$s7", 23);
        registerDict.put("$t8", 24);
        registerDict.put("$t9", 25);
        registerDict.put("$k0", 26);
        registerDict.put("$k1", 27);
        registerDict.put("$gp", 28);
        registerDict.put("$sp", 29);
        registerDict.put("$fp", 30);
        registerDict.put("$ra", 31);
    }
    public String ConvertMIPS(String[] args) {
        //Commenting this out for testing purposes
        String mipsArguments = args[0];
        //Test argument
        //String mipsArguments = "sw $t6, ($s2)";
        String regex = "[\\s]";
        String commasRemoved = mipsArguments.replaceAll(",", "");
        String spacesRemoved = commasRemoved.replaceAll("\\s{2,}", " ");
        String[] argArray = spacesRemoved.split(regex);
        /*
        Testing
        for(String arg : argArray){
            System.out.println(arg);
        }
        */
        String offsetCleaned;
        String[] offsetArray;

        int bitString = 0; //Bits to convert to hex
        String hexArgument = ""; //Arguments represented with hex
        int convertedHex = 0;
        String hexReturn = ""; //Hex argument to print at end

        //J Format. j
        if(argArray.length == 2){
            bitString = 2; //000010
            hexArgument = argArray[1].substring(2);
            convertedHex = Integer.parseInt(hexArgument, 16);
            bitString = (bitString << 26) | convertedHex;

        }
        //I Format
        else if(argArray[0].equals("addiu")){
            bitString = 9; //001001
            bitString = (bitString << 5) | registerDict.get(argArray[2]);
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            if(argArray[3].contains("0x")){
                hexArgument = argArray[3].substring(2);
                convertedHex = Integer.parseInt(hexArgument, 16);
                bitString = (bitString << 16) | convertedHex;
            }
            else if(Integer.parseInt(argArray[3]) < 0){
                bitString = ((bitString + 1)<< 16) + (Integer.parseInt(argArray[3]));
            }
            else {
                bitString = (bitString << 16) | Integer.parseInt(argArray[3]);
            }
        }
        else if(argArray[0].equals("andi")){
            bitString = 12; //001100
            bitString = (bitString << 5) | registerDict.get(argArray[2]);
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            if(argArray[3].contains("0x")){
                hexArgument = argArray[3].substring(2);
                convertedHex = Integer.parseInt(hexArgument, 16);
                bitString = (bitString << 16) | convertedHex;
            }
            else if(Integer.parseInt(argArray[3]) < 0){
                bitString = ((bitString + 1)<< 16) + (Integer.parseInt(argArray[3]));
            }
            else {
                bitString = (bitString << 16) | Integer.parseInt(argArray[3]);
            }
        }
        else if(argArray[0].equals("beq")){
            bitString = 4; //000100
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            bitString = (bitString << 5) | registerDict.get(argArray[2]);
            if(argArray[3].contains("0x")){
                hexArgument = argArray[3].substring(2);
                convertedHex = Integer.parseInt(hexArgument, 16);
                bitString = (bitString << 16) | convertedHex;
            }
            else if(Integer.parseInt(argArray[3]) < 0){
                bitString = ((bitString + 1)<< 16) + (Integer.parseInt(argArray[3]));
            }
            else {
                bitString = (bitString << 16) | Integer.parseInt(argArray[3]);
            }
        }
        else if(argArray[0].equals("bne")){
            bitString = 5; //000101
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            bitString = (bitString << 5) | registerDict.get(argArray[2]);
            if(argArray[3].contains("0x")){
                hexArgument = argArray[3].substring(2);
                convertedHex = Integer.parseInt(hexArgument, 16);
                bitString = (bitString << 16) | convertedHex;
            }
            else if(Integer.parseInt(argArray[3]) < 0){
                bitString = ((bitString + 1)<< 16) + (Integer.parseInt(argArray[3]));
            }
            else {
                bitString = (bitString << 16) | Integer.parseInt(argArray[3]);
            }
        }
        else if(argArray[0].equals("lui")){
            bitString = 15; //001111
            bitString = (bitString << 5);
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            if(argArray[2].contains("0x")){
                hexArgument = argArray[2].substring(2);
                convertedHex = Integer.parseInt(hexArgument, 16);
                bitString = (bitString << 16) | convertedHex;
            }
            else if(Integer.parseInt(argArray[2]) < 0){
                bitString = ((bitString + 1)<< 16) + (Integer.parseInt(argArray[2]));
            }
            else {
                bitString = (bitString << 16) | Integer.parseInt(argArray[2]);
            }
        }
        else if(argArray[0].equals("lw")){
            bitString = 35; //100011
            offsetCleaned = argArray[2].replace("("," ");
            offsetCleaned = offsetCleaned.replace(")", "");
            offsetArray = offsetCleaned.split(regex);
            bitString = (bitString << 5) | registerDict.get(offsetArray[1]);
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            if(offsetArray[0].isEmpty()){
                bitString = (bitString << 16);
            }
            else if(offsetArray[0].contains("0x")){
                hexArgument = offsetArray[0].substring(2);
                convertedHex = Integer.parseInt(hexArgument, 16);
                bitString = (bitString << 16) | convertedHex;
            }
            else if(Integer.parseInt(offsetArray[0]) < 0){
                bitString = ((bitString + 1)<< 16) + (Integer.parseInt(offsetArray[0]));
            }
            else {
                bitString = (bitString << 16) + (Integer.parseInt(offsetArray[0]));
            }
        }
        else if(argArray[0].equals("ori")){
            bitString = 13; //001101
            bitString = (bitString << 5) | registerDict.get(argArray[2]);
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            if(argArray[3].contains("0x")){
                hexArgument = argArray[3].substring(2);
                convertedHex = Integer.parseInt(hexArgument, 16);
                bitString = (bitString << 16) | convertedHex;
            }
            else if(Integer.parseInt(argArray[3]) < 0){
                bitString = ((bitString + 1)<< 16) + (Integer.parseInt(argArray[3]));
            }
            else {
                bitString = (bitString << 16) | Integer.parseInt(argArray[3]);
            }
        }
        else if(argArray[0].equals("sw")){
            bitString = 43; //101011
            offsetCleaned = argArray[2].replace("("," ");
            offsetCleaned = offsetCleaned.replace(")", "");
            offsetArray = offsetCleaned.split(regex);
            bitString = (bitString << 5) | registerDict.get(offsetArray[1]);
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            if(offsetArray[0].isEmpty()){
                bitString = (bitString << 16);
            }
            else if(offsetArray[0].contains("0x")){
                hexArgument = offsetArray[0].substring(2);
                convertedHex = Integer.parseInt(hexArgument, 16);
                bitString = (bitString << 16) | convertedHex;
            }
            else if(Integer.parseInt(offsetArray[0]) < 0){
                bitString = ((bitString + 1)<< 16) + (Integer.parseInt(offsetArray[0]));
            }
            else {
                bitString = (bitString << 16) + (Integer.parseInt(offsetArray[0]));
            }
        }
        //R Format
        //R Format, but works a bit differently
        else if(argArray[0].equals("syscall")){
            bitString = (bitString << 25) | 12;
        }
        else {
            bitString = 0;
            bitString = (bitString << 5) | registerDict.get(argArray[2]);
            bitString = (bitString << 5) | registerDict.get(argArray[3]);
            bitString = (bitString << 5) | registerDict.get(argArray[1]);
            bitString = (bitString << 5);
            if(argArray[0].equals("add")){
                bitString = (bitString << 6) | 32; //100000
            }
            else if(argArray[0].equals("and")){
                bitString = (bitString << 6) | 36; //100100
            }
            else if(argArray[0].equals("or")){
                bitString = (bitString << 6) | 37; //100101
            }
            else if(argArray[0].equals("slt")){
                bitString = (bitString << 6) | 42; //101010
            }
            else if(argArray[0].equals("sub")){
                bitString = (bitString << 6) | 34; //100010
            }
        }
        //Could be one line, but might be useful later
        hexReturn = String.format("%08x", bitString);
        return hexReturn;
    }
}

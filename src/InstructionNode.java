public class InstructionNode {

    private String instruction;
    private int address;
    private InstructionNode nextNode = null;

    public InstructionNode(String instruction, int address){
        this.instruction = instruction;
        this.address = address;
    }

    //Used for a dummy node for the head
    public InstructionNode(){
        this.instruction = "Dummy";
        this.address = -1;
    }

    public String getInstruction(){
        return instruction;
    }

    public void setInstruction(String newInstruction){
        instruction = newInstruction;
    }

    public int getAddress(){
        return address;
    }
    //Address should increment by a set amount, so this could be changed
    public void setAddress(int newAddress){
        address = newAddress;
    }

    public void incrementAddress(int increment) { address += increment; }

    public InstructionNode next(){
        return nextNode;
    }

    public void setNext(InstructionNode newNext){
        nextNode = newNext;
    }
}

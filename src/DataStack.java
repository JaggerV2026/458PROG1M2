import java.util.Arrays;

//Used to store data to write to .data file
//Data needs to be little endian, so I'm using a stack
public class DataStack {
   //Each line is 32 bits with 4 characters that take 8 bits
    private int[] stack = new int[4];
    private int index = stack.length - 1;

    public DataStack() {
        Arrays.fill(stack, 0);
    }

    //Add elements to stack
    public void push(int data){
        stack[index] = data;
        index--;
    }

    //Convert stack into a hex string to be written to .data file
    public String returnData(){
        int bitString = 0;
        for(int i = 0; i < stack.length; i++){
            bitString = (bitString << 8) | stack[i];
        }
        resetStack();
        return String.format("%08x", bitString);
    }
    //Reset stack to all zeros. Useful to handle unfilled values
    private void resetStack(){
        Arrays.fill(stack, 0);
        index = stack.length - 1;
    }

}

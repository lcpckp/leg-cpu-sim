import java.util.*;
import java.io.*;

public class CPU
{
    public static int PC = 0; // Program Counter
    public static int SP = 0; // Stack Pointer
    public static String[] registers = new String[32]; //X0-X31 registers
    public static String[] instructions = new String[100]; //store instructions from machinecode.txt
    public static String[] memory = new String[10000]; //virtual memory array
    public static int[] flags = new int[8]; //flags (Comparing values), flags set in ALU
    public static String input;
    public static Scanner keyboard = new Scanner(System.in);
    
    public static void main(String args[]) throws FileNotFoundException
    {
	int i = 0;
	
	// Clear registers
	for(i = 0; i < 32; i++) {
	    registers[i] = "00000000000000000000000000000000";
        }
	
	// Clear memory
	for(i = 0; i < 100; i++) {
	    memory[i] = "00000000000000000000000000000000";
	}


	// Set up initial list (unsorted in memory)
	// 4 8 15 16 23 42 <- aiming for this end result, sorted
	memory[0] = Integer.toBinaryString(42);
	memory[1] = Integer.toBinaryString(4);
	memory[2] = Integer.toBinaryString(8);
	memory[3] = Integer.toBinaryString(23);
	memory[4] = Integer.toBinaryString(16);
	memory[5] = Integer.toBinaryString(15);

	// Initialize SP
	registers[28] = Integer.toBinaryString(50);

	// Size of list to be sorted (storing this in register X1 ahead of the program start)
	// this assumes n is a parameter from a higher level program (X1 = n)
	registers[1] = Integer.toBinaryString(6);
	
	// Import machine code from file ** Loads this program into instructions array
       	Scanner machinecodefile = new Scanner(new File("./machinecode.txt"));
	i = 0;
	while(machinecodefile.hasNext()) {
	    instructions[i] = machinecodefile.nextLine();
	    i++;    
	}
	int numberOfInstructions = i; // count number of instructions that were read in

	// Memory is displayed before and after the execution of the machine code
	// This shows the unsorted list in its initial state, and the final sorted state after execution
	// Any time during the program, you can type 'mem' or 'reg' or 'all' to display memory, registers, or both
	System.out.println("Initial State of Memory:");
        outputMem();
	
	// Entire machinecode program is run within the following for-loop.
	// Program counter (PC) increments by 1 at each step.
	// Machine code instructions can alter the PC to produce control structures
	for(PC = 0; PC < numberOfInstructions; PC++) {
	    System.out.println("PC: " + PC);
	    
	    //entire list of instructions is parameter of programCounter, which returns the instruction at the PC address
	    //this instruction is the parameter of programDecoder, which determines the execution path
	    programDecoder(programCounter(instructions));

	}

	// At this point, execution is complete. Display final state of memory and then quit.
	System.out.println("\nFinal State of Memory:");
	outputMem();
    }

    /* Outputs current state of registers */
    public static void outputReg() {

	for(int i = 0; i < 32; i++)
	{
	    System.out.println("X" + i + ": " + (int) Long.parseLong(registers[i], 2));
	}

    }

    /* Outputs current state of memory (relevant area) */
    public static void outputMem() {
	
	for(int i = 0; i < 6; i++)
	{
	    System.out.println(i + ": " + (int) Long.parseLong(memory[i], 2));
	}
	System.out.println(".\n.\n."); //for dramatic effect
	
    }

    /* programCounter: acts as the clock of the CPU. Step through the program with Enter/Return
     * hold Enter/Return to quickly run through the program
     * type a command and then hit enter to display memory or registers
     * commands: mem, reg, all */
    public static String programCounter(String[] instructions)
    {
	
	//get a command or just accept Enter to move to next step
	input = keyboard.nextLine();
	if(input.equals("mem")) {
	    outputMem();
	}
	if(input.equals("reg")) {
	    outputReg();
	}
	if(input.equals("all")) {
	    outputMem();
	    System.out.println("---");
	    outputReg();
	}
	
	// returns the binary string of the instruction designated by PC
	return instructions[PC];
    }

    /* Decodes the instruction string into fields based on opCode, and sends these fields to an execute function */
    public static void programDecoder(String machineCode)
    {
	// declare common fields:
        String rm = "";
	String rn = "";
	String rd = "";
	String rt = "";
	String op = "";
	String shamt = "";
	String branchAddr = "";
	String dtAddr = "";
	String ALUimmediate = "";
	int addrInt = 0;
	boolean immediate = false;

	// First, examine the front of the instruction and try to find an opcode
	// Get a substring, 6 bits long
	String opCode = machineCode.substring(0,6);

	/* If the opCode matches an instruction listed here, then send the necessary information to the function responsible for execution */
	
	//B - 6
	if (opCode.equals("000101")) {
	    System.out.println("Branch");
	    branchAddr = machineCode.substring(6,32);
	    executeB(branchAddr);
	}

	// Get the next possible length for an opCode, 8, in a substring and look for more matches
	opCode = machineCode.substring(0,8);
	
	//B.cond - 8
	if (opCode.equals("01010100")) {
	    System.out.println("B.cond");
	    
	    branchAddr = machineCode.substring(8,27);
	    rt = machineCode.substring(27,32);
	    executeBcond(branchAddr, rt);
	}

	opCode = machineCode.substring(0,10);
	
	//ADDI - 10
	if (opCode.equals("1001000100")) {
	    System.out.println("ADDI");
	    ALUimmediate = machineCode.substring(10,22);
	    rn = machineCode.substring(22, 27);
	    rd = machineCode.substring(27, 32);
	    executeI("ADDI", ALUimmediate, rn, rd);
	}

	//SUBI
	if (opCode.equals("1101000100")) {
	    System.out.println("SUBI");
	    ALUimmediate = machineCode.substring(10,22);
	    rn = machineCode.substring(22, 27);
	    rd = machineCode.substring(27, 32);
	    executeI("SUBI", ALUimmediate, rn, rd);
	}
	
	opCode = machineCode.substring(0,11);

	//BR - 11
	if (opCode.equals("11010110000")) {
	    System.out.println("Branch Return");
	    String RL = machineCode.substring(11, 32);
	    executeBreturn(RL);
	}
	
	//ADD - 11
	if (opCode.equals("10001011000")) {
	    System.out.println("ADD");
	    rm = machineCode.substring(11, 16);
	    shamt = machineCode.substring(16, 22);
	    rn = machineCode.substring(22, 27);
	    rd = machineCode.substring(27,32);
	    executeR("ADD", rm, shamt, rn, rd);
	}

	//SUB - 11
	if (opCode.equals("11001011000")) {
	    System.out.println("SUB");
	    rm = machineCode.substring(11, 16);
	    shamt = machineCode.substring(16, 22);
	    rn = machineCode.substring(22, 27);
	    rd = machineCode.substring(27,32);
	    executeR("SUB", rm, shamt, rn, rd);
	}

	//LSL - 11 - R
	if (opCode.equals("11010011011")) {
	    System.out.println("LSL");
	    rm = machineCode.substring(11, 16); //unused
	    shamt = machineCode.substring(16, 22); //bits to shift it by
	    rn = machineCode.substring(22, 27); //the thing to shift
	    rd = machineCode.substring(27,32); //the result register
	    executeR("LSL", rm, shamt, rn, rd);
	}
	
	//LDUR - 11 - D
	if ((opCode.equals("11111000010")) || (opCode.equals("11111100010"))) {
	    if(opCode.equals("11111100010")) { immediate = true; }
	    System.out.println("LDUR");
	    dtAddr = machineCode.substring(11, 20);
	    op = machineCode.substring(20, 22);
	    rn = machineCode.substring(22, 27);
	    rt = machineCode.substring(27, 32);
	    executeD("LDUR", dtAddr, op, rn, rt, immediate);
	}

	//STUR - 11 - D
	if (opCode.equals("11111000000") || (opCode.equals("11111100000"))) {
	    if(opCode.equals("111111000000")) { immediate = true; }
	    System.out.println("STUR");
	    dtAddr = machineCode.substring(11, 20);
	    op = machineCode.substring(20, 22);
	    rn = machineCode.substring(22, 27);
	    rt = machineCode.substring(27, 32);
	    executeD("STUR", dtAddr, op, rn, rt, immediate);
	}            
    }

    /* Sets the program counter to the address stored in RL */
    public static void executeBreturn(String RL) {
	PC = (int) Long.parseLong(registers[(int) Long.parseLong(RL, 2)], 2);
    }

    /* Executes an I-format instruction, like ADDI or SUBI */
    public static void executeI(String instruction, String ALUimmediate, String rn, String rd)
    {
	int addr1 = 0;
	String value1 = "";
	String value2 = "";
	
	if(instruction.equals("ADDI")) {
	    addr1 =(int) Long.parseLong(rn, 2);
	    value1 = registers[addr1];
	    value2 = ALUimmediate;

	    writeback(rd, ALU(value1, value2, "ADD"));
	}
	
	if(instruction.equals("SUBI")) {
	    
	    addr1 =(int) Long.parseLong(rn, 2);
	    value1 = registers[addr1];
	    value2 = ALUimmediate;

	    writeback(rd, ALU(value1, value2, "SUB"));
	}
    }
    
    /* executes an R-format instruction, like ADD or SUB */
    public static void executeR(String instruction, String rm, String shamt, String rn, String rd)
    {

	int addr1 = 0;
	int addr2 = 0;
	String value1 = "";
	String value2 = "";
	int shamtInt = 0;
	
	if (instruction.equals("ADD")) {
	    
	    addr1 =(int) Long.parseLong(rn, 2);
	    value1 = registers[addr1];
	    addr2 =(int) Long.parseLong(rm, 2);
	    value2 = registers[addr2];
	    
	    writeback(rd, ALU(value1, value2, "ADD"));
	}
	if (instruction.equals("SUB")) {
	    
	    addr1 =(int) Long.parseLong(rn, 2);
	    value1 = registers[addr1];
	    addr2 =(int) Long.parseLong(rm, 2);
	    value2 = registers[addr2];
	    writeback(rd, ALU(value1, value2, "SUB"));
	}

	if (instruction.equals("LSL")) {
	    
	    shamtInt = (int) Long.parseLong(shamt, 2);
	    addr1 =(int) Long.parseLong(rn, 2);
	    value1 = registers[addr1];
	    value2 = value1;
	    for(int i = 0; i < shamtInt; i++) {
		value2 += "0";
	    }
	    if(value2.length() > 32) {
		value2 = value2.substring(shamtInt);
	    }

	    writeback(rd, value2);
	}
    }

    /* Executes a D-format instruction (data transfer) like LDUR and STUR */
    public static void executeD(String instruction, String dtAddr, String op, String rn, String rt, boolean immediate)
    {
	
	if (instruction.equals("LDUR")) {
	    
	    int regAddr = (int) Long.parseLong(rn, 2);
	    int offsetValue = (int) Long.parseLong(dtAddr, 2);
	    int memAddr =(int) Long.parseLong(registers[regAddr], 2);

	    writeback(rt, memory[memAddr + offsetValue]);
	}

	if (instruction.equals("STUR")) {
	    int regAddr = (int) Long.parseLong(rn, 2);
	    int offsetValue = (int) Long.parseLong(dtAddr, 2);
	    String memAddr = Integer.toBinaryString((int) Long.parseLong(registers[regAddr], 2) + offsetValue);
	    
	    writememory(memAddr, registers[Integer.parseInt(rt, 2)]);
	}

	immediate = false;
    }

    /* Executes a branch instruction. Sets PC to branch address */
    public static void executeB(String addr) {
	PC = (int) Long.parseLong(addr, 2) - 2;
    }

    /* Executes a condition branch instruction. Sets PC to branch address if the specified conditional flag is true */
    public static void executeBcond(String addr, String rt) {
	int flag = (int) Long.parseLong(rt, 2);
	registers[30] = Integer.toBinaryString(PC);

        if(flags[flag] == 1) {
	    PC = (int) Long.parseLong(addr, 2) - 2;
	}
    }

    /* ALU: adds or subtracts two binary numbers together, also sets flags for comparing numbers */
    public static String ALU(String value1, String value2, String instr) {
	int value1Int = (int) Long.parseLong(value1, 2);
	int value2Int = (int) Long.parseLong(value2, 2);

	// Set Flags
	// [0] = EQ, [1] = NE, [2] = LT, [3] = GT, [4] = LE, [5] = GE

	if(value1Int == value2Int) {
	    flags[0] = 1; flags[1] = 0; flags[2] = 0; flags[3] = 0; flags[4] = 1; flags[5] = 1;
	}
	
	else {
	    
	    if(value1Int > value2Int) {
		flags[0] = 0; flags[1] = 1; flags[2] = 0; flags[3] = 1; flags[4] = 0; flags[5] = 1;
	    }
	    
	    else {
		flags[0] = 0; flags[1] = 1; flags[2] = 1; flags[3] = 0; flags[4] = 1; flags[5] = 0;
	    }
	}

	if(instr.equals("SUB")) {
	    value2Int = -1 * value2Int;
	}

	int total = value1Int + value2Int;

	return Integer.toBinaryString(total);

    }

    /* writes a binary value to an address which represents a register */
    public static void writeback(String addr, String value) {

	int intAddr = (int) Long.parseLong(addr, 2);
	registers[intAddr] = value;
	
	System.out.println("Write " + ((int) Long.parseLong(value, 2)) + " to Register X" + intAddr);
	
    }

    

    /* Writes a binary value to a memory address */
    public static void writememory(String addr, String value) {
	int intAddr = (int) Long.parseLong(addr, 2);
	memory[intAddr] = value;

	System.out.println("Write " + ((int) Long.parseLong(value, 2)) + " to Memory location: " + intAddr);
    }
	    
}

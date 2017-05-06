LEGv8 Simulator Readme

Important note: The machinecode.txt file must be placed in the same directory as the compiled java class file.
machinecode.txt is a binary program which runs on the virtual CPU. It contains a program which will sort a list in memory.
The length of the list is intially stored in the X1 register.


Compile and run the program with the commands in a terminal:

$ javac CPU.java
$ java CPU

The program will display the initial state of memory, which shows the unsorted list.
Step through each instruction using the Enter key.
The type of instruction will be displayed, as well as an output of anything being written to a register or memory.

Optionally, at each step, you can use one of the following commands:

mem - outputs the relevant area in memory
reg - outputs all of the registers
all - outputs both memory and registers

When the program is finished running, the final state of memory will be displayed and you will see the list is sorted.


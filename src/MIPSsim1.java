/* To run in terminal:
 * 		javac MIPSsim.java
 * 		java MIPSsim -I instructions.txt -R registers.txt -O simulation.txt
 * 
 * On my honor, I have neither given nor received unauthorized aid on this assignment*/

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

public class MIPSsim {
	
	static InstructionMemory im;
	static RegisterFile rf;
	static InstructionBuffer ib;
	static MultiCycleBuffer mb;
	static SingleCycleBuffer sb;
	static PartialResultBuffer pb;
	static ResultBuffer rb;
	
	static int i;
	
	static MIPSsim sim;
	
	public static enum Opcode {ADD, SUB, MUL, DIV};

	public static void main(String[] args) throws IOException {
		
		//TODO: check arguments better!
		if (args.length < 6) {
			System.out.println("Error, usage: ./MIPSsim -I instructions.txt -R register.txt -O simulation.txt");
			System.exit(1);
		}
		
		sim = new MIPSsim();

		FileWriter fstream = new FileWriter(args[5]);
		BufferedWriter out = new BufferedWriter(fstream);
		
		im = sim.new InstructionMemory(args[1]);
		rf = sim.new RegisterFile(args[3]);
		ib = sim.new InstructionBuffer();
		mb = sim.new MultiCycleBuffer();
		sb = sim.new SingleCycleBuffer();
		pb = sim.new PartialResultBuffer();
		rb = sim.new ResultBuffer();
		
		while (!finished()) {
			//print();
			print(out);
			issue();
			read();	
			asu();
			mdu1();
			mdu2();		
			write();
			sync();
			i++;
		}
		
		//print();
		print(out);
		
		out.close();

	}
	
	public static void write() {
		InstructionToken i = rb.getTop();
		
		if (i == null) {
			return;
		}
		
		rf.putToken(sim.new RegisterToken(i.dest, i.result));
		rb.removeTop();
	}
	
	public static void asu() {
		InstructionToken i = sb.getTop();
		
		if (i == null) {
			return;
		}
		
		rb.putToken(i);
		sb.removeTop();
	}
	
	public static void mdu2() {
		InstructionToken i = pb.getTop();
		
		if (i == null) {
			return;
		}
		
		rb.putToken(i);
		pb.removeTop();
	}
	
	public static void mdu1() {
		InstructionToken i = mb.getTop();
		
		if (i == null) {
			return;
		}
		
		pb.putToken(i);
		mb.removeTop();
		
	}
	
	public static void issue() {
		InstructionToken i = ib.getTop();
		
		if (i == null) {
			return;
		}
		
		mb.putToken(i);
		sb.putToken(i);
		
		ib.removeTop();
	}
	
	public static void read() {
				
		InstructionToken i = im.getTop();
		
		if (i == null) {
			return;
		}
		
		RegisterToken source1 = rf.getToken(i.src1);
		RegisterToken source2 = rf.getToken(i.src2);
		
		if (source1 == null || source2 == null) {
			return;
		}
		
		i.src1 = source1.registerValue;
		i.src2 = source2.registerValue;
		
		ib.putToken(i);
		
		im.removeTop();
	}
	
	public static void sync() {
		rf.sync();
		ib.sync();
		mb.sync();
		sb.sync();
		pb.sync();
		rb.sync();
	}
	
	public static void print(BufferedWriter out) throws IOException {
		
		if (i != 0) {
			out.write("\n");
		}
		out.write("STEP[" + i + "]:\n");
		im.printContents(out);		
		ib.printContents(out);
		sb.printContents(out);
		mb.printContents(out);
		pb.printContents(out);
		rb.printContents(out);
		rf.printContents(out);

	}
	
	public static void print() {
		
		System.out.print("STEP[" + i + "]:\n");
		im.printContents();		
		ib.printContents();
		sb.printContents();
		mb.printContents();
		pb.printContents();
		rb.printContents();
		rf.printContents();
		System.out.print("\n");
	}
	
	public static boolean finished() {
		if (im.isEmpty() && ib.isEmpty() && mb.isEmpty() && sb.isEmpty() && pb.isEmpty() && rb.isEmpty()){
			return true;
		}
		else {
			return false;
		}
	}
	
	public class InstructionToken implements Comparable<InstructionToken>{
		
		public Opcode opcode;
		public int dest;
		public int src1;
		public int src2;
		private int priority;
		public int result;
		
		public InstructionToken(Opcode op, int dest, int src1, int src2, int priority) {
			this.opcode = op;
			this.dest = dest;
			this.src1 = src1;
			this.src2 = src2;
			this.priority = priority;
			result = 0;
		}
		
		public void copy(InstructionToken t) {
			this.opcode = t.opcode;
			this.dest = t.dest;
			this.src1 = t.src1;
			this.src2 = t.src2;
		}
		
		public int compareTo(InstructionToken t) {
			return (priority - t.priority);
		}
		
		public String toString() {
			return (new StringBuilder("<").append(opcode.toString()).append(",R")
					.append(dest).append(",R").append(src1).append(",R").append(src2).append(">")).toString();
		}
		
		public String toString2() {
			return (new StringBuilder("<").append(opcode.toString()).append(",R")
					.append(dest).append(",").append(src1).append(",").append(src2).append(">")).toString();
		}
		public String toString3() {
			return (new StringBuilder("<").append("R")
					.append(dest).append(",").append(result).append(">")).toString();
		}
	}

	public class RegisterToken implements Comparable<RegisterToken> {
		public int registerName;
		public int registerValue;
		
		RegisterToken(int name, int value) {
			this.registerName = name;
			this.registerValue = value;
		}
		
		public void copy(RegisterToken t) {
			this.registerName = t.registerName;
			this.registerValue = t.registerValue;
		}
		
		public int compareTo(RegisterToken t) {
			return (registerName - t.registerName);
		}
		
		public String toString() {
			return (new StringBuilder("<R").append(registerName).append(",")
					.append(registerValue).append(">")).toString();
		}

	}

	public class RegisterFile {
		private LinkedList<RegisterToken> temporary;
		private LinkedList<RegisterToken> finalized;
		private final Path fFilePath;
		private final Charset ENCODING = StandardCharsets.UTF_8;
		
		RegisterFile(String fileName) throws IOException {
			
			temporary = new LinkedList<RegisterToken>();
			finalized = new LinkedList<RegisterToken>();

			fFilePath = Paths.get(fileName);
			try (Scanner scanner = new Scanner(fFilePath, ENCODING.name())) {
				while (scanner.hasNextLine()) {
					processLine(scanner.nextLine());
				}
			}
		}
		
		private void processLine(String aLine) {			
			String tempName = null;
			String tempValue = null;
			int name;
			int value;
			
			aLine.trim();
			
			Scanner scanner = new Scanner(aLine.substring(2, aLine.length()-1));
			scanner.useDelimiter(",");
			
			if (scanner.hasNext())	 {
				tempName = scanner.next();
				tempValue = scanner.next();
			}
			
			name = Integer.parseInt(tempName);
			value = Integer.parseInt(tempValue);
			 
//			temporary.add(new RegisterToken(name, value));
			finalized.add(new RegisterToken(name, value));
			
			scanner.close();		
		}
		
		public RegisterToken getToken(int registerName) {
			for (int i = 0; i < finalized.size(); i++) {
				if(finalized.get(i).registerName == registerName) {
					return finalized.get(i);
				}
			}	
			return null;
		}
		
		public void putToken (RegisterToken t) {
			
			for (int i = 0; i < finalized.size(); i++) {
				if (t.registerName == finalized.get(i).registerName) {
					if (t.registerValue == finalized.get(i).registerValue) {
						return;
					}
					else {
						finalized.get(i).registerValue = t.registerValue;
						return;
					}
				}
			}
			temporary.add(t);
			
		}
		
		public void sync() {
			
			while(!temporary.isEmpty()) {
				finalized.add(temporary.removeFirst());
			}
			temporary.clear();
		}
			
		public void printContents(BufferedWriter out) throws IOException {
			
			Collections.sort(finalized);
			
			out.write("RF:");
			
			if (finalized.isEmpty()) {
				out.write("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				out.write(finalized.get(i).toString() + ",");
			}
			out.write(finalized.get(finalized.size()-1).toString() + "\n");
		}
		
		public void printContents() {
			
			Collections.sort(finalized);
			
			System.out.print("RF:");
			
			if (finalized.isEmpty()) {
				System.out.print("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				System.out.print(finalized.get(i).toString() + ",");
			}
			System.out.print(finalized.get(finalized.size()-1).toString() + "\n");
		}

	}

	public class InstructionMemory {
		private int priority = 1;
		public LinkedList<InstructionToken> instructions;
		private final Path fFilePath;
		private final Charset ENCODING = StandardCharsets.UTF_8;
		
		InstructionMemory(String fileName) throws IOException {
			instructions = new LinkedList<InstructionToken>();

			fFilePath = Paths.get(fileName);
			try (Scanner scanner = new Scanner(fFilePath, ENCODING.name())) {
				while (scanner.hasNextLine()) {
					processLine(scanner.nextLine());
				}
			}
		}
		
		public boolean isEmpty() {
			return instructions.isEmpty();
		}
		
		private void processLine(String aLine) {			

			String tempOp = null;
			String tempDest = null;
			String tempSrc1 = null;
			String tempSrc2 = null;
			Opcode op = null;
			int dest;
			int src1;
			int src2;
			
			aLine.trim();
			
			Scanner scanner = new Scanner(aLine.substring(1, aLine.length()-1));
			scanner.useDelimiter(",");
			
			if (scanner.hasNext())	 {
				tempOp = scanner.next();
				tempDest = scanner.next();
				tempSrc1 = scanner.next();
				tempSrc2 = scanner.next();
			}
			
			tempDest = tempDest.substring(1, tempDest.length());
			tempSrc1 = tempSrc1.substring(1, tempSrc1.length());
			tempSrc2 = tempSrc2.substring(1, tempSrc2.length());
			
			switch (tempOp) {
			case "ADD": op = Opcode.ADD; break;
			case "SUB": op = Opcode.SUB; break;
			case "MUL": op = Opcode.MUL; break;
			case "DIV": op = Opcode.DIV; break;
			default: System.out.println("Error: could not identify opcode");
			}
			dest = Integer.parseInt(tempDest);
			src1 = Integer.parseInt(tempSrc1);
			src2 = Integer.parseInt(tempSrc2);
			 
			instructions.add(new InstructionToken(op, dest, src1, src2, priority++));
			
			scanner.close();		
		}
		
		public InstructionToken getTop() {
			if (!instructions.isEmpty()) {
				return instructions.getFirst();
			}
			return null;
		}
		
		public boolean removeTop() {
			if (!instructions.isEmpty()) {
				instructions.removeFirst();
				return true;
			}
			return false;
		}
		
		public void printContents(BufferedWriter out) throws IOException {
			
			Collections.sort(instructions);
			
			if (instructions.isEmpty()) {
				out.write("IM:\n");
				return;
			}
			
			out.write("IM:");
			for (int i = 0; i < instructions.size() - 1; i++) {
				out.write(instructions.get(i).toString() + ",");
			}
			out.write(instructions.get(instructions.size()-1).toString() + "\n");
		}
		
		public void printContents() {
			
			Collections.sort(instructions);
			
			if (instructions.isEmpty()) {
				System.out.print("IM:\n");
				return;
			}
			
			System.out.print("IM:");
			for (int i = 0; i < instructions.size() - 1; i++) {
				System.out.print(instructions.get(i).toString() + ",");
			}
			System.out.print(instructions.get(instructions.size()-1).toString() + "\n");
		}

	}

	public class MultiCycleBuffer {
		public LinkedList<InstructionToken> temporary;
		public LinkedList<InstructionToken> finalized;
		
		public MultiCycleBuffer() {
			temporary = new LinkedList<InstructionToken>();
			finalized = new LinkedList<InstructionToken>();
		}
		
		public boolean isEmpty() {
			return finalized.isEmpty();
		}

		public InstructionToken getTop() {
			if (!finalized.isEmpty()) {
				return finalized.getFirst();
			}
			return null;
		}
		
		public boolean removeTop() {
			if (!finalized.isEmpty()) {
				finalized.removeFirst();
				return true;
			}
			return false;
		}
		
		public void sync() {

			while(!temporary.isEmpty()) {
				finalized.add(temporary.removeFirst());
			}
			temporary.clear();
		}
		
		public void putToken(InstructionToken i) {
			if (i.opcode == Opcode.MUL || i.opcode == Opcode.DIV) {
				temporary.add(i);
			}
		}
		
		public void printContents(BufferedWriter out) throws IOException {
			
			Collections.sort(finalized);
			
			out.write("MB:");
			
			if (finalized.isEmpty()) {
				out.write("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				out.write(finalized.get(i).toString2() + ",");
			}
			out.write(finalized.get(finalized.size()-1).toString2() + "\n");
		}
		
		public void printContents() {
			
			Collections.sort(finalized);
			
			System.out.print("MB:");
			
			if (finalized.isEmpty()) {
				System.out.print("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				System.out.print(finalized.get(i).toString2() + ",");
			}
			System.out.print(finalized.get(finalized.size()-1).toString2() + "\n");
		}
	}

	public class PartialResultBuffer {
		public LinkedList<InstructionToken> temporary;
		public LinkedList<InstructionToken> finalized;
		
		public PartialResultBuffer() {
			temporary = new LinkedList<InstructionToken>();
			finalized = new LinkedList<InstructionToken>();
		}
		
		public boolean isEmpty() {
			return finalized.isEmpty();
		}

		public InstructionToken getTop() {
			if (!finalized.isEmpty()) {
				return finalized.getFirst();
			}
			return null;
		}
		
		public boolean removeTop() {
			if (!finalized.isEmpty()) {
				finalized.removeFirst();
				return true;
			}
			return false;
		}
		
		public void sync() {

			while(!temporary.isEmpty()) {
				finalized.add(temporary.removeFirst());
			}
			temporary.clear();
		}
		
		public void putToken(InstructionToken i) {
			if (i.opcode == Opcode.MUL || i.opcode == Opcode.DIV) {
				
				if(i.opcode == Opcode.MUL) {
					i.result = i.src1 * i.src2;
				}
				if(i.opcode == Opcode.DIV) {
					i.result = i.src1 / i.src2;
				}
				
				temporary.add(i);
			}
		}
		
		public void printContents(BufferedWriter out) throws IOException {
			
			Collections.sort(finalized);
			
			out.write("PB:");
			
			if (finalized.isEmpty()) {
				out.write("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				out.write(finalized.get(i).toString2() + ",");
			}
			out.write(finalized.get(finalized.size()-1).toString2() + "\n");
		}
		
		public void printContents() {
			
			Collections.sort(finalized);
			
			System.out.print("PB:");
			
			if (finalized.isEmpty()) {
				System.out.print("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				System.out.print(finalized.get(i).toString2() + ",");
			}
			System.out.print(finalized.get(finalized.size()-1).toString2() + "\n");
		}
	}

	public class SingleCycleBuffer {
		public LinkedList<InstructionToken> temporary;
		public LinkedList<InstructionToken> finalized;
		
		public SingleCycleBuffer() {
			temporary = new LinkedList<InstructionToken>();
			finalized = new LinkedList<InstructionToken>();
		}
		
		public boolean isEmpty() {
			return finalized.isEmpty();
		}

		public InstructionToken getTop() {
			if (!finalized.isEmpty()) {
				return finalized.getFirst();
			}
			return null;
		}
		
		public boolean removeTop() {
			if (!finalized.isEmpty()) {
				finalized.removeFirst();
				return true;
			}
			return false;
		}
		
		public void sync() {

			while(!temporary.isEmpty()) {
				finalized.add(temporary.removeFirst());
			}
			temporary.clear();
		}
		
		public void putToken(InstructionToken i) {
			if (i.opcode == Opcode.ADD || i.opcode == Opcode.SUB) {
				temporary.add(i);
			}
		}
		
		public void printContents(BufferedWriter out) throws IOException {
			
			Collections.sort(finalized);
			
			out.write("SB:");
			
			if (finalized.isEmpty()) {
				out.write("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				out.write(finalized.get(i).toString2() + ",");
			}
			out.write(finalized.get(finalized.size()-1).toString2() + "\n");
		}
		
		public void printContents() {
			
			Collections.sort(finalized);
			
			System.out.print("SB:");
			
			if (finalized.isEmpty()) {
				System.out.print("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				System.out.print(finalized.get(i).toString2() + ",");
			}
			System.out.print(finalized.get(finalized.size()-1).toString2() + "\n");
		}
	}

	public class ResultBuffer {
		public LinkedList<InstructionToken> temporary;
		public LinkedList<InstructionToken> finalized;
		public LinkedList<RegisterToken> finalized2;
		
		public ResultBuffer() {
			temporary = new LinkedList<InstructionToken>();
			finalized = new LinkedList<InstructionToken>();
			finalized2 = new LinkedList<RegisterToken>();
		}
		
		public boolean isEmpty() {
			return finalized.isEmpty();
		}

		public InstructionToken getTop() {
			
			Collections.sort(finalized);
			
			if (!finalized.isEmpty()) {
				return finalized.getFirst();
			}
			return null;
		}
		
		public boolean removeTop() {
			
			Collections.sort(finalized);
			
			if (!finalized.isEmpty()) {
				finalized.removeFirst();
				
				finalized2.clear();
				
				if (!finalized.isEmpty()) {
					for (InstructionToken t: finalized) {
						finalized2.add(new RegisterToken (t.dest, t.result));
					}
				}
	
				return true;
			}
			
			return false;
		}
		
		public void sync() {
			
			for(InstructionToken t: temporary) {
				finalized.add(t);
			}
			
			
			for (InstructionToken t: temporary) {
				finalized2.add(new RegisterToken (t.dest, t.result));
			}
			
			temporary.clear();
		}
		
		public void putToken(InstructionToken i) {
			if (i.opcode == Opcode.ADD || i.opcode == Opcode.SUB) {
				
				if(i.opcode == Opcode.ADD) {
					i.result = i.src1 + i.src2;
				}
				if(i.opcode == Opcode.SUB) {
					i.result = i.src1 - i.src2;
				}
			}
			temporary.add(i);
		}
		
		public void printContents(BufferedWriter out) throws IOException {
			
			Collections.sort(finalized2);
			
			out.write("RB:");
			
			if (finalized.isEmpty()) {
				out.write("\n");
				return;
			}

			for (int i = 0; i < finalized2.size() - 1; i++) {
				out.write(finalized2.get(i).toString() + ",");
			}
			out.write(finalized2.get(finalized2.size()-1).toString() + "\n");
			
		}
		
		public void printContents() {
			
			Collections.sort(finalized2);
			
			System.out.print("RB:");
			
			if (finalized2.isEmpty()) {
				System.out.print("\n");
				return;
			}
			
			for (int i = 0; i < finalized2.size() - 1; i++) {
				System.out.print(finalized2.get(i).toString() + ",");
			}
			System.out.print(finalized2.get(finalized2.size()-1).toString() + "\n");
			
		}
	}

	public class InstructionBuffer {
		public LinkedList<InstructionToken> temporary;
		public LinkedList<InstructionToken> finalized;
		
		public InstructionBuffer() {
			temporary = new LinkedList<InstructionToken>();
			finalized = new LinkedList<InstructionToken>();
		}
		
		public boolean isEmpty() {
			return finalized.isEmpty();
		}

		public InstructionToken getTop() {
			if (!finalized.isEmpty()) {
				return finalized.getFirst();
			}
			return null;
		}
		
		public boolean removeTop() {
			if (!finalized.isEmpty()) {
				finalized.removeFirst();
				return true;
			}
			return false;
		}
		
		public void sync() {

			while(!temporary.isEmpty()) {
				finalized.add(temporary.removeFirst());
			}
			temporary.clear();
		}
		
		public void putToken(InstructionToken i) {
			temporary.add(i);
		}
		
		public void printContents(BufferedWriter out) throws IOException {
			
			Collections.sort(finalized);
			
			out.write("IB:");
			
			if (finalized.isEmpty()) {
				out.write("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				out.write(finalized.get(i).toString2() + ",");
			}
			out.write(finalized.get(finalized.size()-1).toString2() + "\n");
		}
		
		public void printContents() {
			
			Collections.sort(finalized);
			
			System.out.print("IB:");
			
			if (finalized.isEmpty()) {
				System.out.print("\n");
				return;
			}
			
			for (int i = 0; i < finalized.size() - 1; i++) {
				System.out.print(finalized.get(i).toString2() + ",");
			}
			System.out.print(finalized.get(finalized.size()-1).toString2() + "\n");
		}
		
	}

}






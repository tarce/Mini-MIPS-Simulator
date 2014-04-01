package miniMipSim;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import miniMipSim.pipeline.*;
import miniMipSim.pipeline.parts.*;

public class MiniMIPSim {
	
	static InstructionMemory im;
	static RegisterFile rf;
	static InstructionBuffer ib;
	static MultiCycleBuffer mb;
	static SingleCycleBuffer sb;
	static PartialResultBuffer pb;
	static ResultBuffer rb;
	
	static int i;
	
	static MiniMIPSim sim;
	
	public static enum Opcode {ADD, SUB, MUL, DIV};

	public static void main(String[] args) throws IOException {
		
		//TODO: check arguments better!
		if (args.length < 6) {
			System.out.println("Error, usage: ./MIPSsim -I instructions.txt -R register.txt -O simulation.txt");
			System.exit(1);
		}
		
		sim = new MiniMIPSim();

		FileWriter fstream = new FileWriter(args[5]);
		BufferedWriter out = new BufferedWriter(fstream);
		
		im = new InstructionMemory(args[1]);
		rf = new RegisterFile(args[3]);
		ib = new InstructionBuffer();
		mb = new MultiCycleBuffer();
		sb = new SingleCycleBuffer();
		pb = new PartialResultBuffer();
		rb = new ResultBuffer();
		
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
		
		rf.putToken(new RegisterToken(i.dest, i.result));
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
}
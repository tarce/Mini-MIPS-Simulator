package miniMipSim.pipeline;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;

import miniMipSim.MiniMIPSim.Opcode;
import miniMipSim.pipeline.parts.InstructionToken;

public class InstructionMemory {
	private int priority = 1;
	public LinkedList<InstructionToken> instructions;
	private final Path fFilePath;
	private final Charset ENCODING = StandardCharsets.UTF_8;
	
	public InstructionMemory(String fileName) throws IOException {
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
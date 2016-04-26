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

import miniMipSim.pipeline.parts.RegisterToken;

public class RegisterFile {
	private LinkedList<RegisterToken> temporary;
	private LinkedList<RegisterToken> finalized;
	private final Path fFilePath;
	private final Charset ENCODING = StandardCharsets.UTF_8;
	
	/**
	 * Constructor.
	 * 
	 * @param fileName - the register file
	 * @throws IOException
	 */
	public RegisterFile(String fileName) throws IOException {
		
		temporary = new LinkedList<RegisterToken>();
		finalized = new LinkedList<RegisterToken>();

		fFilePath = Paths.get(fileName);
		try (Scanner scanner = new Scanner(fFilePath, ENCODING.name())) {
			while (scanner.hasNextLine()) {
				processLine(scanner.nextLine());
			}
		}
	}
	
	/**
	 * Takes a line from the register file provided and adds it to the register file object.
	 * 
	 * @param aLine - string from register file
	 */
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
		 
//		temporary.add(new RegisterToken(name, value));
		finalized.add(new RegisterToken(name, value));
		
		scanner.close();		
	}
	
	/**
	 * Given a register, it will return the token of that register.
	 * 
	 * @param registerName - the integer value of the register
	 * @return
	 */
	public RegisterToken getToken(int registerName) {
		for (int i = 0; i < finalized.size(); i++) {
			if(finalized.get(i).registerName == registerName) {
				return finalized.get(i);
			}
		}	
		return null;
	}
	
	/**
	 * Adds a register token to the register file.
	 * 
	 * @param t - the register token to be added
	 */
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
	
	/**
	 * Syncs the two internal buffers.
	 */
	public void sync() {
		
		while(!temporary.isEmpty()) {
			finalized.add(temporary.removeFirst());
		}
		temporary.clear();
	}
	
	/**
	 * Writes the contents of the RegisterFile to a buffer.
	 * 
	 * @param out - the buffer to be written
	 * @throws IOException
	 */
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
	
	/**
	 * Prints the contents of the RegisterFile
	 */
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
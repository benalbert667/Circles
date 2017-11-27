package main_package;

import java.util.Scanner;

public class User {
	
	private static PictureEditor pe = new PictureEditor();

	public static void main(String[] args) {
		System.out.println("\n**Picture Editor**\n");
		
		Scanner scan = new Scanner(System.in);
		System.out.print("Please provide a file name: ");
		String filename = scan.next();
		scan.close();
		
		boolean loaded = prompt(filename);

		if(loaded){
			System.out.println("\nWorking...");
			pe.makeCircles(); //CHANGE THIS TO DO DIFF STUFF TO PICTURE
			System.out.println("Done working");
			
			save(filename, "new_");
		}
	}
	
	private static boolean prompt(String x) {
		if (!x.endsWith(".png")) {
			System.out.println("File must be of type .png");
			return false;
		}
		
		x = System.getProperty("user.home") + "/Desktop/" + x;
		
		System.out.println("\nLoading...");
		boolean b = pe.setImg(x);
		System.out.println("Image " + x + (b ? "" : " could not be") + " loaded");
		
		return b;
	}
	
	private static void save(String x, String prefix) {
		System.out.println("\nSaving...");
		
		x = System.getProperty("user.home") + "/Desktop/" + prefix + x;
		pe.saveImg(x);
		
		System.out.println("Saved as " + x);
	}

}

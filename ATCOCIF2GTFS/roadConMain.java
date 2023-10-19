/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ATCOCIF2GTFS;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author AFROGENIUS
 */
public class roadConMain {
    
    
    public static void main(String[] args) throws IOException {
        
        Scanner input = new Scanner(System.in); 
        
        //Set input path for CIF file
        System.out.println("Enter input path: ");
        String inPath = input.next();
        
        //Set output path for gtfs
        System.out.println("Enter output path: ");
        String outPath = input.next();
        
        roadConMain startP = new roadConMain();
        startP.run(new File(inPath), new File(outPath));
    }
    
    public void run(File inPath, File outPath) throws IOException {
        
        roadConverter converter = new roadConverter();
        
        converter.setInputPath(inPath);
        
        converter.setOutputPath(outPath);
        
        converter.run();
    }
}
// C:\Users\HARFOE\Documents\CMM512\CIF\AberdeenCity-Q4-2020
// C:\Users\HARFOE\Documents\CMM512\GTFS
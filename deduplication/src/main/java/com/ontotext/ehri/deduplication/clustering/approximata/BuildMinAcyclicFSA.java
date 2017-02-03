package com.ontotext.ehri.deduplication.clustering.approximata;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class BuildMinAcyclicFSA {

    public void sortFile(String inputFile, String inputCharSet, String outputFile, String outputCharSet) throws IOException{
        FileInputStream fis = null;
        DataInputStream dis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        int nLines = getNumberOfLines(inputFile, inputCharSet);
        String[] array = new String[nLines];
        try{
            fis = new FileInputStream(inputFile);
            dis = new DataInputStream(fis);
            isr = new InputStreamReader(dis, inputCharSet);
            br = new BufferedReader(isr);

            for(int i = 0; i < nLines; i++){
                array[i] = br.readLine();
            }
        }
        finally{
            if( br != null ){
                try{ br.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( isr != null ){
                try{ isr.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( dis != null ){
                try{ dis.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( fis != null ){
                try{ fis.close(); }catch(Exception e){e.printStackTrace();}
            }
        }
        Arrays.sort(array);
        writeSortedArray(array, outputFile, outputCharSet, nLines);
    }

    private void writeSortedArray(String[] array, String outputFile, String outputCharSet, int nLines) throws IOException {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try{
            fos = new FileOutputStream(outputFile);
            osw = new OutputStreamWriter(fos, outputCharSet);
            if(nLines > 0){
                osw.write(array[0]); osw.write("\n"); osw.flush();
                for(int i = 1; i < nLines; i++){
                    if(!array[i].equals(array[i-1])){
                        osw.write(array[i]); osw.write("\n"); osw.flush();
                    }
                }
            }
        }
        finally{
            if( osw != null ){
                try{ osw.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( fos != null ){
                try{ fos.close(); }catch(Exception e){e.printStackTrace();}
            }
        }
    }

    public void reverseFile(String inputFile, String inputCharSet, String outputFile, String outputCharSet) throws IOException{
        FileInputStream fis = null;
        DataInputStream dis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        int nLines = getNumberOfLines(inputFile, inputCharSet);
        String[] array = new String[nLines];
        try{
            fis = new FileInputStream(inputFile);
            dis = new DataInputStream(fis);
            isr = new InputStreamReader(dis, inputCharSet);
            br = new BufferedReader(isr);
            String line;
            StringBuilder sb;
            int j;
            for(int i = 0; i < nLines; i++){
                line = br.readLine();
                sb = new StringBuilder(line.length());
                for(j = line.length()-1; j >= 0; j--){
                    sb.append(line.charAt(j));
                }
                array[i] = sb.toString();
            }
        }
        finally{
            if( br != null ){
                try{ br.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( isr != null ){
                try{ isr.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( dis != null ){
                try{ dis.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( fis != null ){
                try{ fis.close(); }catch(Exception e){e.printStackTrace();}
            }
        }
        Arrays.sort(array);
        writeSortedArray(array, outputFile, outputCharSet, nLines);
    }

    public void buildMinAcyclicFSA(String inputFile, String inputCharSet, String prefectHash, String outputFile) throws Exception
    {
        FileInputStream fis = null;
        DataInputStream dis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        ArrayList<String> lines = new ArrayList<>();
        try{
            fis = new FileInputStream(inputFile);
            dis = new DataInputStream(fis);
            isr = new InputStreamReader(dis, inputCharSet);
            br = new BufferedReader(isr);
            String line;
            while((line = br.readLine()) != null){
                lines.add(line);
            }
        }
        finally{
            if( br != null ){
                try{ br.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( isr != null ){
                try{ isr.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( dis != null ){
                try{ dis.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( fis != null ){
                try{ fis.close(); }catch(Exception e){e.printStackTrace();}
            }
        }
        String[] data = new String[lines.size()];
        int i = 0;
        for(String line : lines){
            data[i] = line; i++;
        }
        MinAcyclicFSA fsa = new MinAcyclicFSA(data, prefectHash.equalsIgnoreCase("true"));
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try{
            fos = new FileOutputStream(outputFile);
            out = new ObjectOutputStream(fos);
            out.writeObject(fsa);
        }
        finally{
            if( out != null ){
                try{ out.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( fos != null ){
                try{ fos.close(); }catch(Exception e){e.printStackTrace();}
            }
        }
    }

    private int getNumberOfLines(String inputFile, String inputCharSet) throws IOException {
        FileInputStream fis = null;
        DataInputStream dis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        int nLines = 0;
        try{
            fis = new FileInputStream(inputFile);
            dis = new DataInputStream(fis);
            isr = new InputStreamReader(dis, inputCharSet);
            br = new BufferedReader(isr);
            while ( br.readLine() != null ){
                nLines++;
            }
        }
        finally{
            if( br != null ){
                try{ br.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( isr != null ){
                try{ isr.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( dis != null ){
                try{ dis.close(); }catch(Exception e){e.printStackTrace();}
            }
            if( fis != null ){
                try{ fis.close(); }catch(Exception e){e.printStackTrace();}
            }
        }
        return nLines;
    }

}

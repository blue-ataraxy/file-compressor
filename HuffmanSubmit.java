import java.util.HashMap;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class HuffmanSubmit implements Huffman{

    public class Node{

        private Byte b;
        private Integer freq;
        private Node left;
        private Node right;
        
        Node(Byte b, Integer freq, Node left, Node right){
            this.b = b;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        //getters & setters

        public Byte getByte(){
            return this.b;
        }
        public void setByte(Byte b){
            this.b = b;
        }
        public Integer getFreq(){
            return this.freq;
        }
        public void setFreq(Integer freq){
            this.freq = freq;
        }
        public Node getLeft(){
            return this.left;
        }
        public void setLeft(Node left){
            this.left = left;
        }
        public Node getRight(){
            return this.right;
        }
        public void setRight(Node right){
            this.right = right;
        }
    }

    //1. ENCODING

    public void encode(String inputFile, String outputFile, String freqFile) {
       
        BinaryIn in = new BinaryIn(inputFile);
		BinaryOut out = new BinaryOut(outputFile);
		BinaryOut freq = new BinaryOut(freqFile);

        //create a hashmap to store frequencies
        HashMap<Byte, Integer> freqmap = new HashMap<Byte, Integer>();

        while(in.isEmpty()==false){
            byte b = in.readByte();
            if(freqmap.containsKey(b)==false){
                freqmap.put(b, 1);
            }
            else{
                freqmap.replace(b, freqmap.get(b)+1); 
            }
        }

        //write freqFile
        for (HashMap.Entry<Byte, Integer> entry : freqmap.entrySet()) { //extracting set of entries from hashmap, then iterating through them
            
            String binarybyte = String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(entry.getKey()))).replace(' ', '0'); //convert byte into binary string
            freq.write(binarybyte+":"+entry.getValue()+"\n"); //writing the entries into freqfile 
        }

        //queue nodes into priority queue
        PriorityQueue<Node> nodes = new PriorityQueue<Node>(new NodeComparator());
        
        for (HashMap.Entry<Byte, Integer> entry : freqmap.entrySet()) {
            Node newLeaf = new Node(entry.getKey(), entry.getValue(), null, null);
            nodes.add(newLeaf);
        }
    
        while(nodes.size() > 1){

            Node n1 = nodes.poll(); //left child
            Node n2 = nodes.poll(); //right child
            Node parent = new Node(null, n1.getFreq() + n2.getFreq(), n1, n2);
            nodes.add(parent);
        }

        //reading from huffman tree & putting values into new hashmap
        HashMap<Byte, String> huffmanmap = new HashMap<Byte, String>();
        readTree(nodes.poll(), "", huffmanmap);


        //writing into output file using new hashmap
        BinaryIn in2 = new BinaryIn(inputFile);
        // String o = "";
        while(in2.isEmpty()==false){
            Byte b = in2.readByte();
            String x = huffmanmap.get(b);
            for (char i: x.toCharArray()){
                if (i == '0'){
                    out.write(false);
                    System.out.print("0");
                } else if (i == '1'){
                    out.write(true);
                    System.out.print("1");
                }
            }
        }

        freq.flush();
        out.flush();
    }


    //2. DECODING

    public void decode(String inputFile, String outputFile, String freqFile) {
        BinaryIn freq = new BinaryIn(freqFile);
        BinaryIn in = new BinaryIn(inputFile);
        BinaryOut out = new BinaryOut(outputFile);
        
        PriorityQueue<Node> nodes = new PriorityQueue<Node>(new NodeComparator());
        
        try(BufferedReader reader = new BufferedReader(new FileReader(freqFile))){
            
            String line = reader.readLine();
            while(line != null){
                String[] splitline = line.split(":");
                String bytestring = splitline[0];
                String freqstring = splitline[1];
                //convert Strings into Byte object
                byte b = (byte) Integer.parseUnsignedInt(bytestring, 2);
                Integer frequency = Integer.valueOf(freqstring);
                
                //add node into priority queue
                nodes.add(new Node(b, frequency, null, null));
                line = reader.readLine();
            }
                
            }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
            
        //creating huffman tree
        while(nodes.size() > 1){
            
            Node n1 = nodes.poll(); //left child
            Node n2 = nodes.poll(); //right child
            Node parent = new Node(null, n1.getFreq() + n2.getFreq(), n1, n2);
            nodes.add(parent);
        }
    
        Node n1 = nodes.poll();
        Node n = n1;

        while(in.isEmpty()==false){
            boolean bool = in.readBoolean();
            System.out.print(bool);
            if(n.getByte() != null){ //it is a leaf, so retrieve the byte value from the node and write into output file
                out.write(n.getByte());
                n = n1;
            }
            if(bool == true){
                n = n.getRight();
            }
            else if(bool == false){
                n = n.getLeft();
            }
        }

        out.flush();

    }

    //custom comparator for priority queue
    class NodeComparator implements Comparator<Node>{
        public int compare(Node n1, Node n2){
            if(n1.getFreq() < n2.getFreq()){
                return -1; //nodes with lower frequency are at head of the queue (since priority queue sorts in ascending order)
            }
            if(n1.getFreq() > n2.getFreq()){
                return 1;
            }
            else{
                return 0;
            }
        }
    }

    //method for writing a hashmap with huffman codes
    public void readTree(Node node, String value, HashMap<Byte, String> hashmap){
        if(node.getByte() != null){ //it is a leaf, so retrieve the value
            hashmap.put(node.getByte(), value);
        }
        else{ //it is an internal node, so go to left and right children
            readTree(node.getLeft(), value + "0", hashmap);
            readTree(node.getRight(), value + "1", hashmap);
        }
    }


    public static void main(String args[]){
        
        Huffman huffman = new HuffmanSubmit();

        huffman.encode("ur.jpg", "ur.enc", "freq.txt");
        huffman.decode("ur.enc", "ur_dec.jpg", "freq.txt");
    }
    
}

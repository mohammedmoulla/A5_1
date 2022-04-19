package a5_algorithm;

import static a5_algorithm.Utility.*;
import java.util.Arrays;
class ShiftRegister {
    int n;
    int clock;
    boolean A [];
    public ShiftRegister(int n,int clock) {
        A = new boolean[n];
        this.n = n;
        this.clock = clock;
    }
    void rightShift () {
        //remove the last bit at the right
        for (int i=n-1; i>=1; i--)
            A[i] = A[i-1];
    }
    void print () {
        String s = "";
        for (int i=0; i<n; i++)
            s += A[i] ? '1' : '0';
        System.out.println(s);
    }
    String getBinary(){
        String s = "";
        for (int i=0; i<n; i++)
            s += A[i] ? '1' : '0';
        return s;
    }
}

class A5 {
    ShiftRegister S1,S2,S3;
    String [] arr_S1;
    String [] arr_S2;
    String [] arr_S3;
    String sessionKey;
    String frameCounter;
    String keystream;
    String cipher1,cipher2;
    boolean K1 [];
    boolean K2 [];
    boolean K2_original [];
    
    boolean KEY [];
    boolean generated;
    A5 () {
        S1 = new ShiftRegister(19, 8);
        S2 = new ShiftRegister(22, 10);
        S3 = new ShiftRegister(23, 10);
        
        arr_S1 = new String [3];
        arr_S2 = new String [3];
        arr_S3 = new String [3];
        
        generated = false;
    }
    String getSessionKey () {
        return cipher1.substring(0, 64);
    }
    String getFrameKey () {
        return cipher2.substring(0, 22);
    }
    String getKeyStream () {
        return keystream;
    }
    void setGenerated (boolean generated){
        this.generated = generated;
    }
    boolean isGenerated () {
        return generated;
    }
    
    boolean setKeys(String session , String frame) {
        if (session.length()<8 || frame.length()<3)
            return false;
        this.sessionKey = session;
        this.frameCounter = frame;
        
        //set session key --> 64 bit
        cipher1 = "";
        for (int i=0; i<8; i++){
            char c = session.charAt(i);
            int x = c;
            String s = Integer.toBinaryString(x);
            while (s.length()<8)
                s = "0"+s;
            cipher1 += s;
        }
        K1 = new boolean[64];
        for (int i=0; i<64; i++)
            K1[i] = cipher1.charAt(i) == '1';
        
        //set frame key --> 22 bit
        cipher2 = "";
        for (int i=0; i<3; i++){
            char c = frame.charAt(i);
            int x = Character.getNumericValue(c);
            String s = Integer.toBinaryString(x);
            while (s.length()<8)
                s = "0"+s;
            cipher2 += s;
        }

        K2 = new boolean[22];
        for (int i=0; i<22; i++)
            K2[i] = cipher2.charAt(i) == '1';
        return true;
    }
    
    void increaseFrame () {
        for (int i=K2.length-1; i>=0; i--){
            if (K2[i] == false){
                K2[i] = true;
                break;
            }else {
                K2[i] = false;
            }
                
        }
    }
    
    String getRegisterValues (int i) {
        if (i==1){
            return "S1 = "+arr_S1[0]+"\nS2 = "+arr_S2[0]+"\nS3 = "+arr_S3[0];
        }else if (i==2){
            return "S1 = "+arr_S1[1]+"\nS2 = "+arr_S2[1]+"\nS3 = "+arr_S3[1];    
        }else if (i==3){
            return "S1 = "+arr_S1[2]+"\nS2 = "+arr_S2[2]+"\nS3 = "+arr_S3[2];
        } else {
            return "";
        }
            
    }
    void rotate (int number,String key) {
        boolean K [] = null;
        if (key.equals("session"))
            K= K1;
        else if (key.equals("frame"))
            K = K2;
        for (int i=0; i<number; i++){
            boolean t = K[i];
            //***** X *****
            boolean x = xor(S1.A[13],xor(S1.A[16],xor(S1.A[17],S1.A[18])));
            S1.rightShift();
            S1.A[0] = xor(t,x);
            //***** Y *****
            boolean y = xor(S2.A[20],S2.A[21]);
            S2.rightShift();
            S2.A[0] = xor(t,y);
            //***** Z *****
            boolean z = xor(S3.A[7],xor(S3.A[20],xor(S3.A[21],S3.A[22])));
            S3.rightShift();
            S3.A[0] = xor(t,z);
        }
         
    }
    void rotateMajor () {
        boolean x_clock = S1.A[S1.clock];
            boolean y_clock = S2.A[S2.clock];
            boolean z_clock = S3.A[S3.clock];
            boolean major = majority(x_clock,y_clock,z_clock);
            
            //***** X *****
            if (x_clock == major){
                boolean x = xor(S1.A[13],xor(S1.A[16],xor(S1.A[17],S1.A[18])));
                S1.rightShift();
                S1.A[0] = x;
            }
            
            //***** Y *****
            if (y_clock == major){
                boolean y = xor(S2.A[20],S2.A[21]);
                S2.rightShift();
                S2.A[0] = y;
            }
            //***** Z *****
            if (z_clock == major){
                boolean z = xor(S3.A[7],xor(S3.A[20],xor(S3.A[21],S3.A[22])));
                S3.rightShift();
                S3.A[0] = z;
            }
    }

    void generate_keystream () {
        keystream = "";
        KEY = new boolean[228];
         for (int i=0; i<228; i++){
             boolean x_last = S1.A[S1.A.length-1];
             boolean y_last = S2.A[S2.A.length-1];
             boolean z_last = S3.A[S3.A.length-1];
             KEY[i] = xor(x_last,xor(y_last,z_last));
             keystream += KEY[i]== true ? '1' : '0';
             rotateMajor();
         }
    }
    void zero () {
        for (int i=0; i<S1.A.length; i++)
            S1.A[i] = false;
        for (int i=0; i<S2.A.length; i++)
            S2.A[i] = false;
        for (int i=0; i<S3.A.length; i++)
            S3.A[i] = false;
    }
    
     void initialize () {
         //step1
        rotate(64,"session");
        arr_S1[0] = S1.getBinary();
        arr_S2[0] = S2.getBinary();
        arr_S3[0] = S3.getBinary();
        //step2
        rotate(22,"frame");
        arr_S1[1] = S1.getBinary();
        arr_S2[1] = S2.getBinary();
        arr_S3[1] = S3.getBinary();
        //step3
        for (int i=0; i<100; i++)
            rotateMajor();  
        arr_S1[2] = S1.getBinary();
        arr_S2[2] = S2.getBinary();
        arr_S3[2] = S3.getBinary();
        generate_keystream();
     }
     
     void generateKeys(){
        //initialize with 0
        zero(); 
        initialize();
     }
     
     String encrypt (String plain){
         
         if (!generated)
             return "";
         
         String  temp = "";
        //convert plain to binary
        for (int i=0; i<plain.length(); i++){
            char c = plain.charAt(i);
            int x = c;
            String s = Integer.toBinaryString(x);

            while (s.length()<8)
                s = "0"+s;
            temp += s;
        }
        int n = temp.length();
        
        boolean PLAIN [] = new boolean[n];
        for (int i=0; i<n; i++)
            PLAIN[i] = temp.charAt(i) == '1';
        
        boolean CIPHER [] = new boolean[n];
        //encrypt the text with key
       
//        K2_original = Arrays.copyOf(K2, K2.length);
        int index ;
        for (int i=0; i<n; i++){
            index = i % 228;
//            if(index ==0 && i!=0){
//                increaseFrame();
//                initialize();
//            }
            CIPHER[i] = xor(KEY[index],PLAIN[i]);
        }//end of for
        
        //return to original
//        K2 = Arrays.copyOf(K2_original, K2_original.length);
        String cipher = "";
        int I = 0;
        String s = "";
        while (I<n-1){
            while (s.length()<8)
                s +=CIPHER[I++] ==true ? '1' : '0';
            char c = (char)Integer.parseInt(s,2);
            cipher += c;
            s = "";
        }
         return cipher;
     }
     
}
    
    
class Utility {
    public static boolean majority (boolean t1,boolean t2,boolean t3) {
        if (t1 == t2 || t1 ==t3)
            return t1;
        if (t2 == t3)
            return t2;
        return t3;
    }
    public static boolean xor (boolean t1,boolean t2){
        return t1 != t2;
    }   
    public static void print_arr (boolean A []) {
        String s = "";
        for (int i=0; i<A.length; i++)
            s += A[i] ? '1' : '0';
        System.out.println(s);
    }
}

public class A5_Algorithm {

    public static void main(String[] args) {
        A5 a = new A5();
        if (a.setKeys("hello123456","asdf")){
            
            String encrypted = a.encrypt("helloworld");
            
            String plain = a.encrypt(encrypted);
            System.out.println(encrypted);
            System.out.println(plain);
        }else {
            System.out.println("error");
        }
            
    }
    
}

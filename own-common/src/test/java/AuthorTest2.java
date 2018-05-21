import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

public abstract class AuthorTest2 implements Serializable {
 	
	static public void main(String[] arg){
 	 		
 		try {
	 
 			FileInputStream in = new FileInputStream("d:/test.aa");
			
			ObjectInputStream in2 = new ObjectInputStream(in);
			AuthorTest  testaa =(AuthorTest )in2.readObject();
			testaa.run("test");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		 
	}
 
}

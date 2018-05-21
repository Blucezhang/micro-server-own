import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class AuthorTest implements Serializable {
	
	
	static public void main(String[] arg){
		
		final String testZSt="hello world22222222222222";

		AuthorTest test = new AuthorTest(){
			public void run(String value){
				System.out.println(testZSt);
			}
		};
	 

		FileOutputStream out = null;
		try {
			
			out = new FileOutputStream("d:/test.aa");
			ObjectOutputStream out1 = new ObjectOutputStream(out);
			out1.writeObject(test);
			out1.flush();
			out1.close();
			out.close();
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	  
		
		 
	}
	
	public abstract void run(String value);

}

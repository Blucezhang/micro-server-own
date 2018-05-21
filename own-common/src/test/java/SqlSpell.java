import java.util.HashMap;

public class SqlSpell {
	
 	private StringBuffer sqlBuf = new StringBuffer();
 	
 	private HashMap param	=	new HashMap();
 	
 	public String sql(boolean flag,String sqlstr){
		
 		if(flag)
 		sqlBuf.append(sqlstr);
  		return "where "+sqlBuf.toString();
	}

}

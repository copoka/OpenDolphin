/*
 * CharConversion.java
 *
 * Created on 2003/01/31
 *
 * Last updated on 2003/02/07
 *
 */

 package mirrorI.dolphin.util;

 /**
  *
  * @author Aniruddha, Mirror-i Corp.
  *
 */

public class CharConversion{

	public CharConversion() {
	}


	/**
	 *
	 * convert(), converts Japanese '�\'/'�|' inro '�['<br>
	 *
 	*/
	public static String convert(String in){
		if(in != null) {
			in= in.replace(('�|'),('�['));
			in= in.replace(('�\'),('�['));
			 return in;
		}
		else {
			return null;
		}
	}
}
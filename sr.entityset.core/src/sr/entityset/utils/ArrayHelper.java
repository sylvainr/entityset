package sr.entityset.utils;

import java.util.HashSet;

public class ArrayHelper {

	public static boolean isUnique(Object[] objects)
	{
		HashSet<Object> hash = new HashSet<Object>();
		
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			
			if (hash.contains(object))
				return false;
			
			hash.add(object);
		}
		
		return true;
	}
	
}

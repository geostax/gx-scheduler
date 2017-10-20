package com.geostax.scheduler.core.rpc.serialize;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * hessian serialize
 * 
 * @author Phil XIAO 2017-10-16 19:39:12
 */
public class HessianSerializer {

	/**
	 * serialize obj
	 * 
	 * @param obj
	 * @return
	 */
	public static <T> byte[] serialize(T obj) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		HessianOutput ho = new HessianOutput(os);
		try {
			ho.writeObject(obj);
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		return os.toByteArray();
	}

	/**
	 * deserialize byte[] to Class<T>
	 * 
	 * 
	 * @param bytes
	 * @param clazz
	 * @return
	 */
	public static <T> Object deserialize(byte[] bytes, Class<T> clazz) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		HessianInput hi = new HessianInput(is);
		try {
			return hi.readObject();
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

}

package ca.ubc.jquery.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class JQueryObjectInputStream extends ObjectInputStream {
	private ClassLoader context;

	public JQueryObjectInputStream(ClassLoader context, InputStream is) throws IOException {
		super(is);
		this.context = context;
	}

	public Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		try {
			return context.loadClass(desc.getName());
		} catch (ClassNotFoundException ex1) {
			try {
				return super.resolveClass(desc);
			} catch (ClassNotFoundException ex2) {
				return JQueryAPI.resolveClass(desc);
			}
		}
	}
}
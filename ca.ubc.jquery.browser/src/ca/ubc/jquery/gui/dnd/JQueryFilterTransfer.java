package ca.ubc.jquery.gui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryObjectInputStream;

/**
 * Class for serializing gadgets to/from a byte array
 */
public class JQueryFilterTransfer extends ByteArrayTransfer {
	private static JQueryFilterTransfer instance = new JQueryFilterTransfer();

	private static final String TYPE_NAME = "jquery-filter-transfer-format";

	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Returns the singleton QueryNode transfer instance.
	 */
	public static JQueryFilterTransfer getInstance() {
		return instance;
	}

	/**
	 * Avoid explicit instantiation
	 */
	private JQueryFilterTransfer() {
	}

	/*
	 * Method declared on Transfer.
	 */
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}

	/*
	 * Method declared on Transfer.
	 */
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}

	/*
	 * Method declared on Transfer.
	 */
	protected void javaToNative(Object object, TransferData transferData) {
		byte[] bytes = null;

		try {
			bytes = toByteArray((Object[]) object);
			if (bytes != null) {
				super.javaToNative(bytes, transferData);
			}
		} catch (Exception e) {
			JQueryTreeBrowserPlugin.error("Filter drag and drop transfer error: ", e);
		}
	}

	/*
	 * Method declared on Transfer.
	 */
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		try {
			Object o = fromByteArray(bytes);
			return o;
		} catch (Exception e) {
			JQueryTreeBrowserPlugin.error("Filter drag and drop transfer error: ", e);
			return null;
		}
	}

	protected Object[] fromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new JQueryObjectInputStream(getClass().getClassLoader(), new ByteArrayInputStream(bytes));

		/* read number of QueryNodes */
		int n = in.readInt();
		/* read QueryNodes */
		Object[] results = new Object[n];
		for (int i = 0; i < n; i++) {
			results[i] = (Object) in.readObject();
		}
		return results;
	}

	protected byte[] toByteArray(Object[] nodes) throws IOException {
		/**
		 * ResultsTreeNode serialization format is as follows: (int) number of children, (ResultsTreeNode) child 1, child 2, etc.
		 */
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

		byte[] bytes = null;

		ObjectOutputStream oos = new ObjectOutputStream(byteOut);

		oos.writeInt(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			Object x = ((Map.Entry) nodes[i]).getValue();
			oos.writeObject(x);
		}

		oos.close();
		bytes = byteOut.toByteArray();

		return bytes;
	}
}
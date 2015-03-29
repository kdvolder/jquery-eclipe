package ca.ubc.jquery.gui.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryObjectInputStream;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

/**
 * Class for serializing gadgets to/from a byte array
 */
public class JQueryResultsTreeTransfer extends ByteArrayTransfer {
	private static JQueryResultsTreeTransfer instance = new JQueryResultsTreeTransfer();

	private static final String TYPE_NAME = "ResultTreeNode-transfer-format";

	private static final int TYPEID = registerType(TYPE_NAME);

	/**
	 * Returns the singleton QueryNode transfer instance.
	 */
	public static JQueryResultsTreeTransfer getInstance() {
		return instance;
	}

	/**
	 * Avoid explicit instantiation
	 */
	private JQueryResultsTreeTransfer() {
	}

	protected ResultsTreeNode[] fromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new JQueryObjectInputStream(getClass().getClassLoader(), new ByteArrayInputStream(bytes));

		/* read number of QueryNodes */
		int n = in.readInt();
		/* read QueryNodes */
		ResultsTreeNode[] resultNodes = new ResultsTreeNode[n];
		for (int i = 0; i < n; i++) {
			ResultsTreeNode result = readResultsTreeNode(null, in);
			if (result == null) {
				return null;
			}
			resultNodes[i] = result;
		}
		return resultNodes;
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
			bytes = toByteArray((ResultsTreeNode[]) object);
			if (bytes != null) {
				super.javaToNative(bytes, transferData);
			}
		} catch (Exception e) {
			JQueryTreeBrowserPlugin.error("Results tree drag and drop transfer error: ", e);
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
			JQueryTreeBrowserPlugin.error("Results tree drag and drop transfer error: ", e);
			return null;
		}
	}

	/**
	 * Reads and returns a single QueryNode from the given stream.
	 */
	private ResultsTreeNode readResultsTreeNode(ResultsTreeNode parent, ObjectInputStream dataIn) throws IOException, ClassNotFoundException {
		/**
		 * ResultsTreeNode serialization format is as follows: (int) number of children, (ResultsTreeNode) child 1, child 2, etc.
		 */
		int n = dataIn.readInt();

		ResultsTreeNode newParent = (ResultsTreeNode) dataIn.readObject();
		newParent.setParent(parent);

		for (int i = 0; i < n; i++) {
			readResultsTreeNode(newParent, dataIn);
		}
		return newParent;
	}

	protected byte[] toByteArray(ResultsTreeNode[] nodes) throws IOException {
		/**
		 * ResultsTreeNode serialization format is as follows: (int) number of children, (ResultsTreeNode) child 1, child 2, etc.
		 */
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

		byte[] bytes = null;

		ObjectOutputStream oos = new ObjectOutputStream(byteOut);

		oos.writeInt(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			writeResultsTreeNode((ResultsTreeNode) nodes[i], oos);
		}

		oos.close();
		bytes = byteOut.toByteArray();

		return bytes;
	}

	/**
	 * Writes the given QueryNode to the stream.
	 */
	private void writeResultsTreeNode(ResultsTreeNode node, ObjectOutputStream dataOut) throws IOException {
		Collection children = node.getChildren();

		dataOut.writeInt(children.size());
		dataOut.writeObject(node);
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			ResultsTreeNode element = (ResultsTreeNode) iter.next();
			writeResultsTreeNode(element, dataOut);
		}
	}
}
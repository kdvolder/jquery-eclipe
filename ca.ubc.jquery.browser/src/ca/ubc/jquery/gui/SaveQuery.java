//package ca.ubc.jquery.gui;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import ca.ubc.jquery.api.JQueryAPI;
//import ca.ubc.jquery.gui.results.ResultsTreeNode;
//import ca.ubc.jquery.gui.results.ResultsTreeRootNode;
//
///**
// * This class handles the saving and loading of queries to/from files.
// */
//public class SaveQuery {
////	/**
////	 * Method getSavedTrees. Returns a List of WorkingSetNodes representing the root nodes of the trees saved in this file
////	 */
////	public static List getSavedTrees(File file) throws IOException, ClassNotFoundException {
////		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
////		try {
////			int treeCount = ois.readInt();
////			List savedTrees = new ArrayList(treeCount);
////			for (int i = 1; i <= treeCount; i++) {
////				ResultsTreeNode treeRoot = (ResultsTreeNode) ois.readObject();
////				savedTrees.add(treeRoot);
////			}
////			return savedTrees;
////		} finally {
////			ois.close();
////		}
////	}
////
////	public static void saveTrees(List QueryTrees, File file) throws IOException {
////		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
////		try {
////			int treeCount = QueryTrees.size();
////			oos.writeInt(treeCount);
////			for (int i = 0; i < treeCount; i++) {
////				ResultsTreeNode treeRoot = (ResultsTreeNode) QueryTrees.get(i);
////				oos.writeObject(treeRoot);
////			}
////		} finally {
////			oos.close();
////		}
////	}
//
//	public static ResultsTreeNode getSavedTree(File file) throws IOException, ClassNotFoundException {
//		ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
//		ResultsTreeNode result = null;
//
//		try {
//			result = (ResultsTreeNode) ois.readObject();
//			if (result instanceof ResultsTreeRootNode) {
//				((ResultsTreeRootNode) result).setElement(JQueryAPI.getFactBase());
//			}
//		} finally {
//			ois.close();
//		}
//
//		return result;
//	}
//
//	public static void saveTree(ResultsTreeNode q, File file) throws IOException {
//		ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
//		try {
//			oos.writeObject(q);
//		} finally {
//			oos.close();
//		}
//	}
//}

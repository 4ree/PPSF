package algorithms.PPUM;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;

public class AlgoMSU_MAU extends PPUM {

	public AlgoMSU_MAU() {

	}

	int tid = -1;
	String changeItem = null;

	public void hideSI(String dst) {
		Set<String> removed = new HashSet<String>();
		for (String sk : sensitive.keySet()) {
			double diff = sensitive.get(sk) - getMut();
			//System.out.println("\tgetMut()=" + getMut());
			//System.out.println("\tdiff=" + diff);
			double dec = 0;
			//System.out.println(sk + "=" + sensitive.get(sk));
			while (diff > 0) {
				List<String> items = getKeys(sk);

				addressTIDandItem(sk);
				// String tid_changeItem = getTarget(items);
				// String tid_changeItem = getTargetNew(items);

				Map<String, Double> trans = getTransaction(database.get(tid));
				// updatedTrans.add(tid);
				double total = trans.get(changeItem) * getUtility(changeItem);
				//System.out.println("targe value is :" + total);
				//System.out.println("changeItem:" + changeItem);
				if (total < diff) {
					double utilityInTransaction = 0.0;
					for (String i : items) {
						utilityInTransaction += trans.get(i) * getUtility(i);
					}
					// check subset update.
					checkSubsetOfSensitive(trans, sk, changeItem,
							utilityInTransaction, true);
					// update item.
					trans.remove(changeItem);
					// remove index....
					// index.get(item).remove(tid);

					diff = diff - utilityInTransaction;
					//System.out.println("{{{{{{{{{{{" + utilityInTransaction);
				} else {
					dec = Math.ceil(diff / getUtility(changeItem));
					checkSubsetOfSensitive(trans, sk, changeItem, dec
							* getUtility(changeItem), false);
					trans.put(changeItem, trans.get(changeItem) - dec);
					diff = 0;
					//System.out.println(dec * getUtility(changeItem));
				}
				//System.out.println("\t\tdiff=" + diff);
				// //System.out.println("DDDDiff:" + diff);
				// //System.out.println("TID:" + tid);
				//System.out.println("Before:" + database.get(tid));
				database.set(tid, mapToTrans(trans));
				//System.out.println("After:" + database.get(tid));

				updatedTrans.add(tid);
				modifiedTimes++;
			}
			removed.add(sk);

		}
		writeDB2File(database, dst);

	}

	private void addressTIDandItem(String sk) {
		List<String> items = getKeys(sk);

		double maxMSU = 0;


		for (int i = 0; i < database.size(); ++i) {
			Map<String, Double> trans = getTransaction(database.get(i));
			if (trans.keySet().containsAll(items)) {
				double msu = 0;
				for (String item : items) {
					double iq = trans.get(item);
					if (iq < 1) {
						msu = 0;
						break;
					}
					double iu = iq * getUtility(item);
					msu += iu;
				}
				if (msu > maxMSU) {
					maxMSU = msu;
					tid = i;
				}
			}
		}
		double maxItemUtility = Double.MIN_VALUE;

		Map<String, Double> t = getTransaction(database.get(tid));
		for (String item : t.keySet()) {
			double iu = t.get(item) * getUtility(item);
			if (iu > maxItemUtility) {
				maxItemUtility = iu;
				changeItem = item;
			}
		}

	}

	public void runAlgorithm(double min_util, double sen_per, String inputDatabaseFile, String inputUtilityTableFile, String outputFile) {
		// Read utility table
		readUT(inputUtilityTableFile);

		// Read database
		readDatabaseFile(inputDatabaseFile);
		//System.out.println("database is "+database.get(0));
		// Set threshold
		//setMutThreshold(0.05);
		setMutThreshold(min_util);
		setSensitiveThreshold(sen_per);
		
		String miningResult = "temp_file/hui_mine_result.txt";
		mining(inputDatabaseFile, miningResult);
		// Mine
//		String miningResult = "temp_file/hui_mine_result.txt";
		File f=new File(inputDatabaseFile);
		String name=f.getName();
		name=name.substring(0, name.length()-4);
		String path=f.getParent();

//		String miningResult = "temp_file/hui_mine_result.txt";
		int iLen=inputDatabaseFile.length();
		String mut=String.valueOf(min_util*100);
		String hui=path+"/"+name+"_h_"+ mut;
		String spath=hui+"_si_"+sensitiveThreshold+".txt";
	
		String out=path+"/"+mut +"/"+ name + "_MSU_MAU"+"_h_"+ String.valueOf(min_util*100)+"_si_"+sensitiveThreshold+".txt";
		readSensitiveHUI(spath);
		//System.out.println("sensitive size is :"+sensitive.size());
		hideSI(out);

		// readDatabaseFile("src/hhuif_result.txt");
		//printParameters();
	}
	public void printStats() {
		
	}
}

package jp.kanagawa.kawasaki.suicaviewer;

import 	android.util.SparseArray;

public class DeviceCodedata {
	final SparseArray<String> devmap = new SparseArray<String>();
	
	DeviceCodedata(){
		devmap.put(3   , "精算機");
        devmap.put(4   , "携帯型端末");
        devmap.put(5   , "車載端末");
        devmap.put(7   , "券売機");
        devmap.put(8   , "券売機");
        devmap.put(9   , "入金機");
        devmap.put(18  , "券売機");
        devmap.put(20  , "券売機等");
        devmap.put(21  , "券売機等");
        devmap.put(22  , "改札機");
        devmap.put(23  , "簡易改札機");
        devmap.put(24  , "窓口端末");
        devmap.put(25  , "窓口端末");
        devmap.put(26  , "改札端末");
        devmap.put(27  , "携帯電話");
        devmap.put(28  , "乗継精算機");
        devmap.put(29  , "連絡改札機");
        devmap.put(31  , "簡易入金機");
        devmap.put(70  , "アルッテ");
        devmap.put(72  , "アルッテ");
        devmap.put(199 , "物販端末");
        devmap.put(200 , "自販機");
	}
}

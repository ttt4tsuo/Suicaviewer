package jp.kanagawa.kawasaki.suicaviewer;

import 	android.util.SparseArray;

public class ActCodedata {
	final SparseArray<String> actmap = new SparseArray<String>();
	
	ActCodedata(){        
        actmap.put(1  , "運賃支払（改札出場)");
        actmap.put(2  , "チャージ");
        actmap.put(3  , "磁気券購入");
        actmap.put(4  , "精算");
        actmap.put(5  , "入場精算　");
        actmap.put(6  , "窓出 (改札窓口処理)");
        actmap.put(7  , "新規発行");
        actmap.put(8  , "窓口控除");
        actmap.put(13 , "ピタパ系バス");
        actmap.put(15 , "イルカ系バス");
        actmap.put(17 , "再発（再発行処理");
        actmap.put(19 , "支払（新幹線利用");
        actmap.put(20 , "入場時オートチャージ");
        actmap.put(21 , "出場時オートチャージ");
        actmap.put(31 , "バスチャージ");
        actmap.put(35 , "バス路面電車企画券購入");
        actmap.put(70 , "物販");
        actmap.put(72 , "特典チャージ");
        actmap.put(73 , "レジ入金");
        actmap.put(74 , "物販取消");
        actmap.put(75 , "入場物販");
        actmap.put(198 , "現金併用物販");
        actmap.put(203 , "入場現金併用物販");
        actmap.put(132 , "他社精算");
        actmap.put(133 , "他社入場精算");
	}
}

package jp.kanagawa.kawasaki.suicaviewer;

import android.annotation.SuppressLint;
import java.util.HashMap;

@SuppressLint("UseSparseArrays")
public class DeviceActCodedata {
	final HashMap<Integer,String> devmap = new HashMap<Integer,String>();
	final HashMap<Integer,String> actmap = new HashMap<Integer,String>();
	
	DeviceActCodedata(){
		devmap.put(3   , "精算機　　");
        devmap.put(4   , "携帯型端末");
        devmap.put(5   , "車載端末　");
        devmap.put(7   , "券売機　　");
        devmap.put(8   , "券売機　　");
        devmap.put(9   , "入金機　　");
        devmap.put(18  , "券売機　　");
        devmap.put(20  , "券売機等　");
        devmap.put(21  , "券売機等　");
        devmap.put(22  , "改札機　　");
        devmap.put(23  , "簡易改札機");
        devmap.put(24  , "窓口端末　");
        devmap.put(25  , "窓口端末　");
        devmap.put(26  , "改札端末　");
        devmap.put(27  , "携帯電話　");
        devmap.put(28  , "乗継精算機");
        devmap.put(29  , "連絡改札機");
        devmap.put(31  , "簡易入金機");
        devmap.put(70  , "アルッテ　");
        devmap.put(72  , "アルッテ　");
        devmap.put(199 , "物販端末　");
        devmap.put(200 , "自販機　　");
        
        actmap.put(1  , "運賃支払（改札出場）　");
        actmap.put(2  , "チャージ　　　　　　　");
        actmap.put(3  , "磁気券購入　　　　　　");
        actmap.put(4  , "精算　　　　　　　　　");
        actmap.put(5  , "入場精算　　　　　　　");
        actmap.put(6  , "窓出 (改札窓口処理)　");
        actmap.put(7  , "新規発行　　　　　　　");
        actmap.put(8  , "窓口控除　　　　　　　");
        actmap.put(13 , "ピタパ系バス　　　　　");
        actmap.put(15 , "イルカ系バス　　　　　");
        actmap.put(17 , "再発（再発行処理）　　");
        actmap.put(19 , "支払（新幹線利用）　　");
        actmap.put(20 , "入場時オートチャージ　");
        actmap.put(21 , "出場時オートチャージ　");
        actmap.put(31 , "バスチャージ　　　　　");
        actmap.put(35 , "バス路面電車企画券購入");
        actmap.put(70 , "物販　　　　　　　　　");
        actmap.put(72 , "特典チャージ　　　　　");
        actmap.put(73 , "レジ入金　　　　　　　");
        actmap.put(74 , "物販取消　　　　　　　");
        actmap.put(75 , "入場物販　　　　　　　");
        actmap.put(198 , "現金併用物販　　　　　");
        actmap.put(203 , "入場現金併用物販　　　");
        actmap.put(132 , "他社精算　　　　　　　");
        actmap.put(133 , "他社入場精算　　　　　");
	}
}

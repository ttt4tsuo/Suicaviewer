package jp.kanagawa.kawasaki.suicaviewer;

public class StationCodedata {
	
	private String linecode;
	private String stationcode;
	private String companyname;
	private String linename;
	private String stationname;
	
	public StationCodedata(){
    }		
	
	public String getLineCode(){
		return this.linecode;
	}
	public String getStationCode(){
		return this.stationcode;
	}
	public String getCompanyName(){
		return this.companyname;
	}
	public String getLineName(){
		return this.linename;
	}
	public String getStationName(){
		return this.stationname;
	}
}

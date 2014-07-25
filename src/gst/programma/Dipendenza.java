package gst.programma;

class Dipendenza{
	private String nome, nome_dest, url, platform;
	private long size;
	private boolean x86, x64;
	
	public Dipendenza(String nome, String des, String url, String plat, long size, boolean x86, boolean x64){
		this.nome=nome;
		this.nome_dest=des;
		this.url=url;
		this.platform=plat;
		this.size=size;
		this.x86=x86;
		this.x64=x64;
	}
	
	public String getNome() {
		return nome;
	}
	public String getUrl() {
		return url;
	}
	public long getSize() {
		return size;
	}
	public String getNomeDest() {
		return nome_dest;
	}
	public boolean isX86() {
		return x86;
	}
	public boolean isX64() {
		return x64;
	}
	public boolean isWin(){
		if(platform.compareToIgnoreCase("indipendent")==0)
			return true;
		if(platform.compareToIgnoreCase("windows")==0)
			return true;
		return false;
	}
	public boolean isLinux(){
		if(platform.compareToIgnoreCase("indipendent")==0)
			return true;
		if(platform.compareToIgnoreCase("linux")==0)
			return true;
		return false;
	}
	public boolean isMac(){
		if(platform.compareToIgnoreCase("indipendent")==0)
			return true;
		if(platform.compareToIgnoreCase("mac")==0)
			return true;
		return false;
	}
}
package util.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;

public class MySevenZipCallBack implements IArchiveExtractCallback {
        private int index;
        private boolean skipExtraction;
        private ISevenZipInArchive inArchive;
        private String destinazione;
        private String rename;
        
        private FileOutputStream fw;
        
        public MySevenZipCallBack(ISevenZipInArchive inArchive, String destinazione, String rename) {
            this.inArchive = inArchive;
            this.destinazione=destinazione;
            this.rename=rename;
        }

        public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
            this.index = index;
            skipExtraction = (Boolean) inArchive.getProperty(index, PropID.IS_FOLDER);
            if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT) {
                return null;
            }
            return new ISequentialOutStream() {
            	
            	private String nome_file="";
            	private String file_f="";
            	
                public int write(byte[] data) throws SevenZipException {
                	if(nome_file.isEmpty()){
                		nome_file=(String) inArchive.getProperty(getIndex(), PropID.PATH);
                		String cartella="";
                		if(nome_file.contains(File.separator)){
                			cartella=nome_file.substring(0, nome_file.lastIndexOf(File.separator));
                		}
                		
                		cartella=destinazione+(destinazione.endsWith(File.separator)?"":File.separator)+cartella;
                		File dir_dest=new File(cartella);
                		if(!dir_dest.exists())
                			dir_dest.mkdirs();
                		
                		if(!rename.isEmpty()){
                			if(nome_file.contains(".")){
                				String estensione=nome_file.substring(nome_file.lastIndexOf("."));
                				if(nome_file.contains(File.separator)){
                					nome_file=nome_file.substring(0, nome_file.lastIndexOf(File.separator)+1)+rename+estensione;
                				}
                				else {
                					nome_file=rename+estensione;
                				}
                			}
                			System.out.println(nome_file);
                		}
                		file_f=destinazione+(destinazione.endsWith(File.separator)?"":File.separator)+nome_file;
                		File f=new File(file_f);
                		if(f.exists()){
                			f.delete();
                			try {
								f.createNewFile();
							} 
                			catch (IOException e) {
								e.printStackTrace();
							}
                		}
						else
							try {
								f.createNewFile();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
                	}
                	try {
                		fw=new FileOutputStream(file_f, true);
						fw.write(data);
					} 
                	catch (IOException e) {
						e.printStackTrace();
					}
                	finally {
                		if(fw!=null){
                			try {
								fw.flush();
								fw.close();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
                		}
                	}
                    return data.length; // Return amount of proceed data
                }
            };
        }
        public int getIndex(){
        	return index;
        }
        public void toFile(){
        	
        }
        public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {  }

        public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
            if (skipExtraction) {
                return;
            }
            if (extractOperationResult != ExtractOperationResult.OK) {
                System.err.println("Extraction error");
            } 
            else {
                System.out.println(String.format("%s", inArchive.getProperty(index, PropID.PATH)));
                if(fw!=null){
                	try {
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
                	fw=null;
                }
            }
        }

        public void setCompleted(long completeValue) throws SevenZipException { }

        public void setTotal(long total) throws SevenZipException { }
 
}

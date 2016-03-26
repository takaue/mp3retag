import java.io.*;
import java.util.Iterator;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.FieldKey;


public class mp3retag{
  public static void main(String[] args){

	// 引数が0の場合はエラー
    if( args.length < 1 ){
      System.out.println( "error: no args" );
      System.exit(1);
    }


  	// 引数処理を開始
  	try{
  		procArgs( args );
  	}
  	catch( Exception e ){
  		e.printStackTrace();
  		System.exit(1);
  	}
  	// 終了
  }
// 引数処理関数
	static void procArgs( String[] args ){
		for( int i = 0; i < args.length; i++ ){
			File fp = new File(args[i]);
			if( fp.isFile() ){
				try{
					procFile(fp);
				}
				catch( Exception e ){
					e.printStackTrace();
					System.exit(1);
				}
			} else if( fp.isDirectory() ){
				try{
					procDirectory(args[i]);
				}
				catch( Exception e ){
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				System.out.println( "do not found file or directory: " + args[i] );
			}
			// fpインスタンス削除
			fp = null;
		}
	}
	
// ディレクトリ再帰処理用メソッド
// Fileオブジェクトを渡すと再帰処理でFileオブジェクトが大量生産されてメモリに優しくないような気がするので
// パス文字列でやりとりする。
	static void procDirectory( String dirName ){
		File fp = new File(dirName);
		File[] fList = fp.listFiles();
		fp = null;
		for( int i = 0; i < fList.length; i++ ){
//			System.out.println(fList[i]);
			fp = new File( fList[i].toString() );
			if( fp.isFile() ){
				try{
					procFile(fp);
				}
				catch( Exception e ){
					e.printStackTrace();
					System.exit(1);
				}
			} else if( fp.isDirectory() ){
				fp = null;
				try{
					procDirectory(fList[i].toString());
					System.out.println(fList[i]);
				}
				catch( Exception e ){
					e.printStackTrace();
					System.exit(1);
				}
			}
			fp = null;
//			System.out.println( fList[i] + ": End" );
		}
	}
	
// ファイル処理用メソッド
	static void procFile( File fp ){
		//ファイル名
		System.out.println( fp );
		AudioFile af = new AudioFile();
		try{
			af = AudioFileIO.read(fp);
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		Tag tag = af.getTag();
		System.out.println( tag.getFieldCount() );
		System.out.println( tag.getFieldCountIncludingSubValues() );
		System.out.println( tag.getFirst(FieldKey.TITLE) );
	}



}


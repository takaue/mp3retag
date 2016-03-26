import java.io.*;
import java.util.List;
import java.util.ArrayList;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.id3.AbstractID3v1Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.FieldKey;

public class mp3retag{
	
	// 詳細表示モードフラグ
	static boolean detailMode = false;
	// 書き換えモードフラグ
	static boolean convertMode = false;

	
	public static void main(String[] orgArgs){

		// 引数処理を開始
		List<String> args = new ArrayList<String>();

		// 先頭が「-」を抽出して動作モードフラグを設定する
		for( int i = 0; i < orgArgs.length; i++ ){
			if( "-d".equals(orgArgs[i]) ){
				detailMode = true;
			} else if( "--convert".equals(orgArgs[i])  ){
				convertMode = true;
			} else {
				args.add( orgArgs[i] );
			}
		}
		// 残った引数が0の場合はエラー
    	if( args.size() < 1 ){
    		System.out.println( "error: no args" );
    		System.exit(1);
    	}

		try{
			procArgs( args );
		}
		catch( Exception e ){
			e.printStackTrace();
			System.exit(1);
		}
		// 終了
		args = null;
		System.exit(0);
	}

	// 引数処理関数
	static void procArgs( List<String> args ){
		for( int i = 0; i < args.size(); i++ ){
			File fp = new File(args.get(i));
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
					procDirectory(args.get(i));
				}
				catch( Exception e ){
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				System.out.println( "do not found file or directory: " + args.get(i) );
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
//					System.out.println(fList[i]);
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
	
// ファイル処理メソッド
	static void procFile( File fp ){
		//ファイル名
		MP3File af = new MP3File();
		String id3version = "";
		try{
			af = (MP3File)AudioFileIO.read(fp);
		}
		catch( InvalidAudioFrameException e ){
//			System.out.println( "-> pass:  no Tag now" );
			return;
		}
		catch( java.lang.ClassCastException e ){
//			System.out.println( "->pass:  no mp3 Audio File" );
			return;
		}
		catch( CannotReadException e ){
//			System.out.println( "->pass:  no mp3 Audio File" );
			return;
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		Tag tag = af.getTag();
		System.out.println( "File:\"" + fp + "\"" );
		if( af.hasID3v2Tag() ){
			AbstractID3v2Tag v2tag = af.getID3v2Tag();
			id3version = v2tag.getIdentifier();
			System.out.println( "Tag Version:\"" + id3version + "\"" );
			if( detailMode ){
				System.out.println( v2tag.toString() );
			} else {
				System.out.println( "" );
			}
			v2tag = null;
		} else if( af.hasID3v1Tag() ){
			AbstractID3v1Tag v1tag = af.getID3v1Tag();
			id3version = v1tag.getIdentifier();
			System.out.println( "Tag Version:\"" + id3version + "\"" );
			if( detailMode ){
				System.out.println( "Tag content:" );
				System.out.println( "	TRACK:\"" +  tag.getFirst("TRACK")+"\"" );
				System.out.println( "	TITLE:\"" +  tag.getFirst("TITLE")+"\"" );
				System.out.println( "	ALBUM:\"" +  tag.getFirst("ALBUM")+"\"" );
				System.out.println( "	ARTIST:\"" + tag.getFirst("ARTIST")+"\"" );
				System.out.println( "	GENRE:\""  +  tag.getFirst("GENRE")+"\"" );
				System.out.println( "	YEAR:\""  +  tag.getFirst("YEAR")+"\"" );
				System.out.println( "" );
			} else {
				System.out.println( "" );
			}
			v1tag = null;
		} else {
			System.out.println( ": no tag" );
		}
		tag = null;
		af = null;
	}
}


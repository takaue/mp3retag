import java.io.*;
import java.util.List;
import java.util.ArrayList;
import org.jaudiotagger.audio.exceptions.*;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.id3.AbstractID3v1Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.reference.ID3V2Version;

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
		String v2minor = "";
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

		String vtrack = "";
		String vtitle = "";
		String valbum = "";
		String vartist = "";
		String vgenre = "";
		String vyear = "";

		if( af.hasID3v2Tag() ){
			AbstractID3v2Tag v2tag = af.getID3v2Tag();
			id3version = v2tag.getIdentifier();
			v2minor = id3version.substring( 6,7);
			System.out.println( "Tag Version:\"" + id3version + "\"" );
//			System.out.println( v2tag.toString() );
//			System.out.println( v2minor );

			// convert
			if( convertMode && ! "3".equals( v2minor ) ){
				try{
					ID3v23Tag newtag = new ID3v23Tag(v2tag);
					af.setID3v2TagOnly(newtag);
					System.out.println( "* Convert: " + id3version + " -> ID3V2.30" );
					newtag = null;
				}
				catch( Exception e ){
					System.out.println( "failed to Convert." );
				}
				try{
					af.commit();
				}
				catch( Exception e ){
					System.out.println( "failed to commit." );
				}
			}
			
			if( "2".equals(v2minor) ){
				vtrack = v2tag.getFirst("TRACK");
				vtitle = v2tag.getFirst("TITLE");
				valbum = v2tag.getFirst("ALBUM");
				vartist = v2tag.getFirst("ARTIST");
				vgenre = v2tag.getFirst("GENRE");
				vyear = v2tag.getFirst("YEAR");
			} else if( "3".equals(v2minor ) ){
				vtrack = v2tag.getFirst("TRCK");
				vtitle = v2tag.getFirst("TIT2");
				valbum = v2tag.getFirst("TALB");
				vartist = v2tag.getFirst("TPE1");
				vgenre = v2tag.getFirst("TCON");
				vyear = v2tag.getFirst("TYER");
			} else if( "4".equals(v2minor) ){
				vtrack = v2tag.getFirst("TRCK");
				vtitle = v2tag.getFirst("TIT2");
				valbum = v2tag.getFirst("TALB");
				vartist = v2tag.getFirst("TPE1");
				vgenre = v2tag.getFirst("TCON");
				vyear = v2tag.getFirst("TDRC");
			}
			v2tag = null;
		} else if( af.hasID3v1Tag() ){
			AbstractID3v1Tag v1tag = af.getID3v1Tag();
			id3version = v1tag.getIdentifier();
			System.out.println( "Tag Version:\"" + id3version + "\"" );
			
			if( convertMode ){
				try{
					ID3v23Tag newtag = new ID3v23Tag(v1tag);
					System.out.println( newtag.toString() );
					af.delete(v1tag);
					af.createDefaultTag();
					af.setID3v2Tag(newtag);
					System.out.println( "* Convert: " + id3version + " -> ID3V2.30" );
					newtag = null;
				}
				catch( Exception e ){
					System.out.println( "failed to Convert." );
				}
				try{
					af.commit();
				}
				catch( Exception e ){
					System.out.println( "failed to commit." );
				}
			}

			
			vtrack = tag.getFirst("TRACK");
			vtitle = tag.getFirst("TITLE");
			valbum = tag.getFirst("ALBUM");
			vartist = tag.getFirst("ARTIST");
			vgenre = tag.getFirst("GENRE");
			vyear = tag.getFirst("YEAR");
			v1tag = null;
		} else {
			System.out.println( ": no tag" );
			tag = null;
			af = null;
			return;
		}

		if( detailMode ){
			System.out.println( "Tag content:" );
			System.out.println( "	TRACK:\"" +  vtrack + "\"" );
			System.out.println( "	TITLE:\"" +  vtitle + "\"" );
			System.out.println( "	ALBUM:\"" +  valbum + "\"" );
			System.out.println( "	ARTIST:\"" + vartist + "\"" );
			System.out.println( "	GENRE:\""  +  vgenre + "\"" );
			System.out.println( "	YEAR:\""  +  vyear + "\"" );
			System.out.println( "" );
		}
		tag = null;
		af = null;
	}
}


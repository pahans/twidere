package com.extensions.keyboard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/*this class will only include 
 * basic database queries 
 * handle database connection opening and closing
 * Copying of database from assets to the phone memory
 */
public class DataBaseHandler extends SQLiteOpenHelper {
    private Context mycontext;

    private String DB_PATH;
    private static String DB_NAME = "words";//the extension cannot be .sqlite or .db if using in gingerbread
    public SQLiteDatabase myDataBase;
    public DataBaseHandler(Context context) throws IOException {
        super(context,DB_NAME,null,1);
        this.mycontext=context;
        DB_PATH = "/data/data/"+mycontext.getApplicationContext().getPackageName()+"/databases/";
        boolean dbexist = checkdatabase();
        if (dbexist) {
            opendatabase(); 
        } else {
            createdatabase();
        }
    }

    public void createdatabase() throws IOException {
        boolean dbexist = checkdatabase();
        if(dbexist) {
        
        } else {
            this.getReadableDatabase();
            try {
                copydatabase();
                opendatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }   

    public boolean checkdatabase() {
        //SQLiteDatabase checkdb = null;
        boolean checkdb = false;
        try {
            String myPath = DB_PATH + DB_NAME;
            File dbfile = new File(myPath);
            checkdb = dbfile.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkdb;
    }

    public void copydatabase() throws IOException {
        //Open your local db as the input stream
        InputStream myinput = mycontext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outfilename = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        FileOutputStream myoutput = new FileOutputStream(outfilename);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer))>0) {
            myoutput.write(buffer,0,length);
        }

        //Close the streams
        myoutput.flush();
        myoutput.close();
        myinput.close();
    }

    public void opendatabase() throws SQLException {
        //Open the database
        String mypath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public synchronized void close() {
        if(myDataBase != null) {
            myDataBase.close();
        }
        super.close();
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	/*get suggestions for the input String
	 * sorted according to the frequency of the word usage
	 */
	public ArrayList<String> getWordList(String prefix) {
		ArrayList<String> ret = new ArrayList<String>();
		if(!(prefix.contains("_")||prefix.contains("%"))){
			prefix = DatabaseUtils.sqlEscapeString(prefix+"%");
			String selectQuery = "SELECT  * FROM WORDLIST where WORD LIKE "+prefix+" ORDER BY FREQUENCY ASC LIMIT 6";
			Cursor cursor = myDataBase.rawQuery(selectQuery, null);
	    if (cursor.moveToFirst()) {
	        do {
	             ret.add(cursor.getString(1));
	            
	        } while (cursor.moveToNext());
	    }
	 }
		return ret;
	}
	public boolean addWordToDb(String word) {
		String selectQuery = "SELECT  * FROM WORDLIST where WORD = "+DatabaseUtils.sqlEscapeString(word);
		Cursor cursor = myDataBase.rawQuery(selectQuery, null);
		if(!cursor.moveToFirst()){
			String updateSQL = "Insert into WORDLIST ('WORD','FREQUENCY') values("+DatabaseUtils.sqlEscapeString(word)+",'2')";
			myDataBase.execSQL(updateSQL);
			return true;
		}
		return false;	
	}
}	
	


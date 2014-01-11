package com.extensions.keyboard;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
/*this class will include
 * Suggestions retrieval
 * Frequency updating logic
 * Adding new words to the database
 */
public class SuggestionGenerator{
	private Activity host;
	private DataBaseHandler db;
	public SuggestionGenerator(Activity host) {
		this.host = host;
		db = null;
		try {
			db = new DataBaseHandler(host.getBaseContext());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public AsyncTask<CandidateView,Void,ArrayList<String>> getSuggestions(final String in, final CandidateView view){
		AsyncTask<CandidateView,Void,ArrayList<String>> task = new AsyncTask<CandidateView,Void,ArrayList<String>>() {
			CandidateView view;
			@Override
			protected ArrayList<String> doInBackground(CandidateView... params) {
				view = params[0];
				ArrayList<String> list =db.getWordList(in);
				return list;
			}
			protected void onPostExecute(ArrayList<String> result) {
				view.setSuggestions(result, true, true);
		    }
		};
		task.execute(view);
		return task;
	}
	public void setFrequency(){
		
	}
	public void closeConnection(){
		db.close();
	}
}
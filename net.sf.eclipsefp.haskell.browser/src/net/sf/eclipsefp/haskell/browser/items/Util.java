/**
 * (c) 2011, Alejandro Serrano
 * Released under the terms of the EPL.
 */
package net.sf.eclipsefp.haskell.browser.items;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Utilities for using items.
 * 
 * @author Alejandro Serrano
 */
public class Util {

	public static String[] getStringArray(JSONArray array) throws JSONException {
		ArrayList<String> r = new ArrayList<>();
		for (int i = 0; i < array.length(); i++) {
			r.add(array.getString(i));
		}
		return r.toArray(new String[array.length()]);
	}
}

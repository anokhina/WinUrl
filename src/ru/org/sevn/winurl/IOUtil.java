/*******************************************************************************
 * Copyright 2017 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ru.org.sevn.winurl;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class IOUtil {
	public static void close(Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (IOException e) {
			}
		}
	}
    public static String trimStart(String s, char ch) {
    	return trim(s, ch, true, false);
    }
    public static String trimEnd(String s, char ch) {
    	return trim(s, ch, false, true);
    }
    public static String trim(String s, char ch) {
    	return trim(s, ch, true, true);
    }
    public static String trim(String s, char ch, boolean atStart, boolean atEnd) {
    	if (s == null) return null;
        int start = 0, last = s.length() - 1;
        int end = last;
        if (atStart) while ((start <= end) && (s.charAt(start) == ch)) {
            start++;
        }
        if (atEnd) while ((end >= start) && (s.charAt(end) == ch)) {
            end--;
        }
        if (start == 0 && end == last) {
            return s;
        }
        return s.substring(start, end+1);
    }
    
	private static final String[] FORBIDDEN = new String[] {
			"#", "<", "$", "+", "%", ">", "!", "`", "&", "*","“", "|", "{", "?", "”", "=", "}", "/", ":", "\\", "@", "\"", "'"
	};
	
	public static String normalizeFileName(String n) {
		for (String s : FORBIDDEN) {
			n = n.replace(s, "");
		}
		return n;
	}
	
	public static String getFileName(String str) {
		// ("/","|","\\", "?", ":", ";")
		int maxLen = 16;
		String regex = "[|/\\\\\\?:;]";
		String allowedName = str.replaceAll(regex, ""); 
		allowedName = allowedName.substring(0, Math.min(maxLen, allowedName.length())); //TODO
		if (!allowedName.matches("(?i).*\\.url")) {
			allowedName += ".url";
		}
		return allowedName;
	}
	
    public static String readAll(Reader r) throws IOException {
        StringBuilder ret = new StringBuilder();
        char[] cbuf = new char[2048];
        int cnt = r.read(cbuf);
        while (cnt > 0) {
            ret.append(cbuf, 0, cnt);
            cnt = r.read(cbuf);
        }
        
        return ret.toString();
    }
    
	public static String loadFileContent(File file2load, String encoding) throws IOException {
		FileInputStream fis = null;
		InputStreamReader fr = null;
		try {
			fis = new FileInputStream(file2load);
			fr = new InputStreamReader(fis, encoding);
			String fileContent = readAll(fr);
			return fileContent;
		} finally {
			close(fr);
			close(fis);
		}
	}

}

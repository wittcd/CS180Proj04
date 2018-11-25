import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ChatFilter {
    private String filePath;
    ArrayList<String> array = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {
        filePath = badWordsFileName;
    }

    public String filter(String msg) {
        File f = new File(filePath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            boolean what = true;
            while (what) {
                String temp = br.readLine();
                if (temp == null) {
                    what = false;
                } else {
                    array.add(temp);
                }
            }
            for (int i = 0; i < array.size(); i++ ) {
                String temp = "";
                for (int j = 0; j < array.get(i).length(); j++) {
                    temp = temp + "*";
                }
                while (msg.indexOf(array.get(i)) != -1) {
                    int index = msg.indexOf(array.get(i));
                    msg = msg.substring(0 , index) + temp + msg.substring(index + temp.length());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
       
        return msg;
    }
  
}

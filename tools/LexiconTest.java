import app.wordbud.ime.Lexicon;
import java.io.*;
import java.util.*;
public class LexiconTest {
    static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    static Lexicon.Candidate find(Lexicon l,String p,String w){for(Lexicon.Candidate c:l.query(p))if(c.word.equals(w))return c;throw new AssertionError(p+" missing "+w);}
    public static void main(String[] args)throws Exception{
        Lexicon l=Lexicon.load(new FileInputStream(args[0]));
        check(l.size()>50000,"full dictionary");
        check(find(l,"nihao","你好").consumed==5,"exact word consumption");
        check(find(l,"kaifa","开发").gloss.contains("develop"),"English gloss");
        check(find(l,"xuexi","学习").gloss.contains("learn")||find(l,"xuexi","学习").gloss.contains("study"),"learning gloss");
        check(find(l,"woxiangxuexi","我").consumed==2,"sentence first word selection");
        check(find(l,"xiangxuexi","想").consumed==5,"sentence second word selection");
        check(find(l,"lv","绿").consumed==2,"umlaut spelling");
        check(l.query("").isEmpty(),"empty query");
        check(l.query("zzzzzzzzzz").isEmpty(),"unknown query");
        check(find(l,"NIHAO","你好").consumed==5,"case normalization");
        System.out.println("PASS: dictionary, translations, incremental sentence selection, umlaut, empty and invalid input ("+l.size()+" entries)");
    }
}
